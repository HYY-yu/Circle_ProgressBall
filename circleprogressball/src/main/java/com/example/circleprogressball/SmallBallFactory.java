package com.example.circleprogressball;

import android.graphics.PointF;
import android.graphics.RectF;

import com.example.circleprogressball.tools.Circle;
import com.example.circleprogressball.tools.Utils;

/**
 * Created by feng on 2016/1/13.
 */
public class SmallBallFactory {


    public static Circle generateSmallBall(Circle circle) {
        Circle smallBall = new Circle();

        PointF pointF = Utils.getRandomPoint(circle);

        smallBall.a = pointF.x;
        smallBall.b = pointF.y;
        smallBall.r = Utils.dp2Px(3);

        return smallBall;
    }

}
