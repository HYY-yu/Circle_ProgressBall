package com.example.circleprogressball.tools;

import android.util.Log;
import android.view.animation.OvershootInterpolator;

import java.util.List;
import java.util.Random;

/**
 * Created by yufeng on 17-10-9.
 */

public class BallThread extends Thread {
    private Circle ball;
    private final Circle main;
    private final List<Circle> balls;

    private int v;
    private float t;
    private int defaultR;
    private OvershootInterpolator interpolator = new OvershootInterpolator(3f);

    public BallThread(Circle ball, Circle circle, List<Circle> balls) {
        //生成随机速率
        v = 50 + new Random(System.currentTimeMillis()).nextInt(50);
        this.ball = ball;
        this.main = circle;
        this.balls = balls;
        t = 0f;
        defaultR = Utils.dp2Px(3);
        interpolator = new OvershootInterpolator(3f);
    }

    @Override
    public void run() {
        while (true) {
            if (t <= 1) {
                float y = interpolator.getInterpolation(t);
                ball.r = defaultR * y;
                t += 0.1f;
                try {
                    sleep(80);
                } catch (InterruptedException e) {
                }
            } else {
                float dis = Utils.getDistance(ball.a, ball.b, main.a, main.b);

                if (dis < main.r - ball.r) {
                    //认为已经回家
                    balls.remove(ball);
                    break;
                }

                if (dis < main.r * 1.35f) {
                    //开始接触大圆 加速
                    v = 30;
                }

                float dx = Math.abs(ball.a - main.a);
                float beta = dx / v;

                if (dx == 0) {
                    //只需要不断 减少 ball.b 和 main.b 的差距
                    ball.b = ball.b > main.b ? ball.b - beta : ball.b + beta;
                } else if (Math.abs(ball.b - main.b) < 0.001) {
                    ball.a = ball.a > main.a ? ball.a - beta : ball.a + beta;
                } else {
                    float k = (main.b - ball.b) / (main.a - ball.a);
                    float oldA = ball.a;
                    ball.a = ball.a > main.a ? ball.a - beta : ball.a + beta;
                    ball.b = k * (ball.a - oldA) + ball.b;
                }

                try {
                    sleep(40);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
