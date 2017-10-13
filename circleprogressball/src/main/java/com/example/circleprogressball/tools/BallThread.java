package com.example.circleprogressball.tools;

import android.view.animation.OvershootInterpolator;

import java.util.List;
import java.util.Random;

/**
 * Created by yufeng on 17-10-9.
 */

public class BallThread extends Thread {
    Circle ball;
    final Circle main;

    List<Circle> balls;

    int v;
    float t = 0f;
    final int defaultR = Utils.dp2Px(3);

    public BallThread(Circle ball, Circle circle, List<Circle> balls) {
        //生成随机速率
        v = 30 + new Random(System.currentTimeMillis()).nextInt(70);
        this.ball = ball;
        this.main = circle;
        this.balls = balls;
    }

    @Override
    public void run() {
        while (true) {
            if (t <= 1) {
                OvershootInterpolator interpolator = new OvershootInterpolator(3f);

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

                if (dis <= main.r * 1.35f) {
                    //开始接触大圆 加速
                    v = 20;
                }

                if (ball.a - main.a == 0) {
                    //只需要不断 减少 ball.b 和 main.b 的差距
                    ball.b = ball.b > main.b ? ball.b - 1 : ball.b + 1;
                } else if (ball.b - main.b == 0) {
                    ball.a = ball.a > main.a ? ball.a - 1 : ball.a + 1;
                } else {
                    float k = (main.b - ball.b) / (main.a - ball.a);
                    float oldA = ball.a;
                    ball.a = ball.a > main.a ? ball.a - 1 : ball.a + 1;
                    ball.b = k * (ball.a - oldA) + ball.b;
                }

                try {
                    sleep(v);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
