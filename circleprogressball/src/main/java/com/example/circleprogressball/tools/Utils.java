package com.example.circleprogressball.tools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.circleprogressball.CircleProgressBall;

import java.util.Random;

/**
 * Created by feng on 2016/1/12.
 */
public class Utils {

    private static final float DENSITY = Resources.getSystem().getDisplayMetrics().density;
    private static final Canvas sCanvas = new Canvas();

    static int[] colors = new int[]{
            Color.parseColor("#007439")};

    public static int dp2Px(int dp) {
        return Math.round(dp * DENSITY);
    }

    public static void setColors(int[] colors) {
        Utils.colors = colors;
    }

    /**
     * 算法作者： hellsam
     * 跟据Progress的值改变颜色
     *
     * @param progress
     * @return
     */
    public static int findColorByProgress(int progress) {
        float percent = progress / 100f;

        float[][] f = new float[colors.length][3];

        for (int i = 0; i < colors.length; i++) {
            f[i][0] = (colors[i] & 0xff0000) >> 16;
            f[i][1] = (colors[i] & 0x00ff00) >> 8;
            f[i][2] = (colors[i] & 0x0000ff);
        }

        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < f.length; j++) {
                if (f.length == 1 || percent == j / (f.length - 1f)) {
                    result = f[j];
                } else {
                    if (percent > j / (f.length - 1f) && percent < (j + 1f) / (f.length - 1)) {
                        result[i] = f[j][i] - (f[j][i] - f[j + 1][i]) * (percent - j / (f.length - 1f)) * (f.length - 1f);
                    }
                }
            }
        }
        return Color.rgb((int) result[0], (int) result[1], (int) result[2]);
    }

    public static Bitmap createBitmapFromView(View view) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }
        view.clearFocus();
        Bitmap bitmap = createBitmapSafely(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888, 1);
        if (bitmap != null) {
            synchronized (sCanvas) {
                Canvas canvas = sCanvas;
                canvas.setBitmap(bitmap);
                view.draw(canvas);
                canvas.setBitmap(null);
            }
        }
        return bitmap;
    }

    public static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }

    /**
     * 返回一个缩放值为v 缩放位置位于rect中心的新矩形.
     *
     * @param rect
     * @param v
     * @return
     */
    public static RectF scaleRectF(RectF rect, float v) {
        float tempWidth = rect.width() * v;
        float tempHeight = rect.height() * v;

        RectF outRectF = new RectF();

        if (v > 0 && v < 1f) {
            outRectF.left = rect.left + (rect.width() - tempWidth) * 0.5f;
            outRectF.top = rect.top + (rect.height() - tempHeight) * 0.5f;
            outRectF.right = outRectF.left + tempWidth;
            outRectF.bottom = outRectF.top + tempHeight;
        } else if (v > 1f) {
            outRectF.left = rect.left - (tempWidth - rect.width()) * 0.5f;
            outRectF.top = rect.top - (tempHeight - rect.height()) * 0.5f;
            outRectF.right = outRectF.left + tempWidth;
            outRectF.bottom = outRectF.top + tempHeight;
        } else {
            //返回原Rect
            return rect;
        }
        return outRectF;
    }


    /**
     * 求两点间的距离
     */
    public static float getDistance(final float x1, final float y1, final float x2, final float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        float d = x * x + y * y;
        return (float) Math.abs(Math.sqrt(d));
    }

    /**
     * 将极坐标转换成直角坐标。
     */
    public static float[] getVector(float radians, float length) {
        float x = (float) (Math.cos(radians) * length);
        float y = (float) (Math.sin(radians) * length);
        return new float[]{
                x, y
        };
    }

    /**
     * 求点到原点(0,0)的长度
     */

    public static float getLength(float x, float y) {
        return getDistance(x, y, 0, 0);
    }

    /**
     * 圆形区域周围倍数大小矩形区域随机生成一个点 ,但不包括圆形区域
     *
     * @param circle 圆形区域
     * @return
     */
    public static PointF getRandomPoint(final Circle circle) {

        float x = circle.a, y = circle.b;
        RectF rectF = Utils.scaleRectF(circle.getCircleRect(), 2f); // 可以生成点的区域
        if (rectF.width() != 0) {
            do {
                Random random = new Random(System.currentTimeMillis());
                x = rectF.left + random.nextInt((int) rectF.width());
                y = rectF.top + random.nextInt((int) rectF.height());

            } while (circle.contains(x, y, 1.45f));
        }
        return new PointF(x, y);
    }

    public static int getBallCount(int progress, int maxCount) {
        float x = progress / 100f;
        float a = maxCount / -0.25f;

        return Math.round(a * x * (x - 1));

    }

    /**
     * 生成小球
     *
     * @param circle
     * @return
     */
    public static Circle generateSmallBall(Circle circle) {
        Circle smallBall = new Circle();

        PointF pointF = Utils.getRandomPoint(circle);

        smallBall.a = pointF.x;
        smallBall.b = pointF.y;
        smallBall.r = 0;

        return smallBall;
    }
}
