package com.example.circleprogressball.tools;

import android.graphics.RectF;

/**
 * Created by yufeng on 17-10-9.
 */

public class Circle {

    public float a;
    public float b;
    public float r;

    private RectF circleRect;

    public Circle() {
    }

    public Circle(float a, float b, float r) {
        this.a = a;
        this.b = b;
        this.r = r;
    }

    public boolean contains(float x, float y) {
        float temp1 = (x - a) * (x - a);
        float temp2 = (y - b) * (y - b);
        return temp1 + temp2 < r * r;
    }

    public RectF getCircleRect() {
        if (circleRect == null) {
            circleRect = new RectF();
        }
        circleRect.set(a - r, b - r, a + r, b + r);
        return circleRect;
    }
}
