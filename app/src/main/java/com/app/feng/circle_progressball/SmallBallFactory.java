package com.app.feng.circle_progressball;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by feng on 2016/1/13.
 */
public class SmallBallFactory {


    public static SmallBall generateSmallBall(RectF f, RectF circle) {
        SmallBall smallBall = new SmallBall();

        PointF pointF = Utils.getRandromPointFromRectF(f, circle);

        smallBall.setCx(pointF.x);
        smallBall.setCy(pointF.y);
        smallBall.setRadius(5);

        return smallBall;
    }


    static class SmallBall {
        private float cx, cy;
        private float radius;

        private RectF ballRect;

        public RectF getBallRect() {
            ballRect = new RectF(cx - radius, cy - radius
                    , cx + radius, cy + radius);

            return ballRect;
        }

        public float getCx() {
            return cx;
        }

        public void setCx(float cx) {
            this.cx = cx;
        }

        public float getCy() {
            return cy;
        }

        public void setCy(float cy) {
            this.cy = cy;
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        @Override
        public String toString() {
            return "SmallBall info:" +
                    "x : " + cx +
                    "y : " + cy +
                    "radius : " + radius;
        }
    }
}
