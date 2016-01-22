package com.app.feng.circle_progressball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;

/**
 * Created by feng on 2016/1/12.
 */
public class Utils {
    private Utils() {
    }

    private static final float DENSITY = Resources.getSystem().getDisplayMetrics().density;
    private static final Canvas sCanvas = new Canvas();

    public static int dp2Px(int dp) {
        return Math.round(dp * DENSITY);
    }

    //跟据Progress的值改变颜色
    public static int findColorByProgress(int progress) {

        if (progress > 90 && progress <= 100) {
            return Color.parseColor("#4DE14D");
        } else if (progress > 80 && progress <= 90) {
            return Color.parseColor("#CAF253");
        } else if (progress > 60 && progress <= 80) {
            return Color.parseColor("#E5E600");
        } else if (progress > 40 && progress <= 60) {
            return Color.parseColor("#FFDD57");
        } else if (progress > 20 && progress <= 40) {
            return Color.parseColor("#FF9957");
        } else if (progress > 0 && progress <= 20) {
            return Color.parseColor("#FE5758");
        } else {
            return Color.parseColor("#E54FA8");
        }
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
        float tempwidth = rect.width() * v;
        float tempheight = rect.height() * v;

        RectF rectF = new RectF();

        if (v > 0 && v < 1f) {
            rectF.left = rect.left + (rect.width() - tempwidth) / 2;
            rectF.top = rect.top + (rect.height() - tempheight) / 2;
            rectF.right = rectF.left + tempwidth;
            rectF.bottom = rectF.top + tempheight;
        } else if (v > 1f) {
            rectF.left = rect.left - (rect.width() - tempwidth) / 2;
            rectF.top = rect.top - (rect.height() - tempheight) / 2;
            rectF.right = rectF.left + tempwidth;
            rectF.bottom = rectF.top + tempheight;
        } else {

        }

        return rectF;
    }


    /**
     * 求两点间的距离
     *
     * @param b1
     * @param b2
     * @return
     */
    public static float getDistance(float[] b1, float[] b2) {
        float x = b1[0] - b2[0];
        float y = b1[1] - b2[1];
        float d = x * x + y * y;
        return (float) Math.sqrt(d);
    }

    /**
     * )
     * 将极坐标转换成直角坐标。
     *
     * @param radians
     * @param length
     * @return
     */
    public static float[] getVector(float radians, float length) {
        float x = (float) (Math.cos(radians) * length);
        float y = (float) (Math.sin(radians) * length);
        return new float[]{
                x, y
        };
    }

    /**
     * 求两点间的长度
     *
     * @param b
     * @return
     */

    public static float getLength(float[] b) {
        return (float) Math.sqrt(b[0] * b[0] + b[1] * b[1]);
    }

    /**
     * 在一个rectF区域(不包括circle区域)随机生成一个点
     *
     * @param rectF
     * @return
     */
    public static synchronized PointF getRandromPointFromRectF(RectF rectF, final RectF circle) {
        float x, y;
        do {
            Random random = new Random(System.currentTimeMillis());

            x = rectF.left + random.nextFloat() * rectF.right;
            y = rectF.top + random.nextFloat() * rectF.bottom;

        } while (circle.contains(x, y));

        return new PointF(x, y);

    }

}
