package com.app.feng.circle_progressball;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by feng on 2016/1/12.
 */
public class Circle_ProgressBall extends View {

    static final int TEXT_COLOR = Color.WHITE;
    private static final float SCALE_RATE = 0.3f;

    int mRadius; //圆的宽度，默认为100dp

    int mPaddingHorizontal;
    int mPaddingTop;
    int mPaddingBottom;

    RectF mCircleRect;

    Paint mPaint;
    Paint mTextPaint;

    float mButtonRadius;
    RectF mButtonRect;

    int mProgress;

    MoveAnimation wa;

    Bitmap ic_cancel_button;

    boolean isShouldCancel = false;

    private int[] mExpandInset = new int[2];

    ExplosionAnimator explosion = null;

    boolean stopFlag = false;

    public interface OnCircleEventListener {

        void onStartAnimFinish(MoveAnimation moveAnimation);

        void onCancel(int mProgress);

    }

    public OnCircleEventListener getOnCircleEventListener() {
        return onCircleEventListener;
    }

    public void setOnCircleEventListener(OnCircleEventListener onCircleEventListener) {
        this.onCircleEventListener = onCircleEventListener;
    }

    OnCircleEventListener onCircleEventListener;

    List<SmallBallFactory.SmallBall> smallBalls = new ArrayList<>();
    ThreadPoolExecutor pool;

    private class MoveAnimation extends Animation {

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float tran = mPaddingBottom / 2 + mRadius / 2;

            float x = tran * interpolatedTime;

            if (mProgress != 100) {
                if (mButtonRect.centerY() < mPaddingTop + mRadius / 2 + tran) {
                    mButtonRect.top += x;
                    mButtonRect.bottom += x;
                } else {
                    if (onCircleEventListener != null)
                        onCircleEventListener.onStartAnimFinish(wa);
                    //加载按钮中的取消图片.
                    if (ic_cancel_button == null)
                        ic_cancel_button = BitmapFactory.decodeResource(getResources(), R.drawable.ic_button_cancel);
                    invalidate();
                    return;
                }
                Log.i("feng", " time" + interpolatedTime);
                invalidate();
            } else {
                if (mButtonRect.centerY() > mCircleRect.centerY()) {
                    mButtonRect.top -= x;
                    mButtonRect.bottom -= x;
                } else {

                    return;
                }
                invalidate();
            }
        }
    }

    public Circle_ProgressBall(Context context) {
        this(context, null);
        init();
    }

    public Circle_ProgressBall(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();

        //构造线程池
        pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    public Circle_ProgressBall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Circle_ProgressBall, defStyleAttr, 0);
        int num = array.getIndexCount();
        for (int i = 0; i < num; i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.Circle_ProgressBall_radius)
                mRadius = array.getDimensionPixelOffset(attr, 100);
            else if (attr == R.styleable.Circle_ProgressBall_progress)
                mProgress = array.getInt(attr, 0);

        }
        init();
    }

    private void init() {

        mCircleRect = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mRadius / 1.3f);

        mButtonRadius = mRadius * 0.5f;

        Arrays.fill(mExpandInset, Utils.dp2Px(32));

        isShouldCancel = false;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = resolveSizeAndState(Utils.dp2Px(mRadius + 30), widthMeasureSpec, 0);
        int h = resolveSizeAndState(Utils.dp2Px(mRadius + 60), heightMeasureSpec, 0);


        mPaddingHorizontal = (w - mRadius) / 2;

        mPaddingTop = (h - mRadius) / 3;
        mPaddingBottom = (h - mRadius) * 2 / 3;

        mCircleRect.left = mPaddingHorizontal;
        mCircleRect.right = mPaddingHorizontal + mRadius;
        mCircleRect.top = mPaddingTop;
        mCircleRect.bottom = mCircleRect.top + mRadius;

        mButtonRect = Utils.scaleRectF(mCircleRect, 0.5f);
        setMeasuredDimension(w, h);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isShouldCancel) {
            mPaint.setColor(Utils.findColorByProgress(mProgress));
            canvas.drawCircle(mCircleRect.centerX(), mCircleRect.centerY(), mRadius, mPaint);
            canvas.drawCircle(mButtonRect.centerX(), mButtonRect.centerY(), mButtonRadius, mPaint);

            if (Utils.getDistance(new float[]{mButtonRect.centerX(), mButtonRect.centerY()},
                    new float[]{mCircleRect.centerX(), mCircleRect.centerY()}) > mRadius - mButtonRadius
                    ) {

                metaball(canvas, mButtonRect, mCircleRect, 0.735f, 8f, mRadius * 2.5f);

            }
            if (ic_cancel_button != null && Utils.getDistance(new float[]{mButtonRect.centerX(), mButtonRect.centerY()},
                    new float[]{mCircleRect.centerX(), mCircleRect.centerY()}) > mRadius - mButtonRadius) {
                //绘制按钮中的图片
                canvas.drawBitmap(ic_cancel_button, null, mButtonRect, new Paint());
            }


            //开始画小球
            if (!smallBalls.isEmpty() && mProgress != 100) {
                for (SmallBallFactory.SmallBall temp :
                        smallBalls) {
                    canvas.drawCircle(temp.getCx(), temp.getCy(), temp.getRadius(), mPaint);

                    if (Utils.getDistance(new float[]{temp.getCx(), temp.getCy()},
                            new float[]{mCircleRect.centerX(), mCircleRect.centerY()}) >
                            mRadius - temp.getRadius()) {

                        metaball(canvas, temp.getBallRect(), mCircleRect, 0.735f, 8f, mRadius * 1.35f);
                    }
                }
            }


            //画出圆内的字
            String txt = String.valueOf(mProgress);
            Paint.FontMetrics fm = mTextPaint.getFontMetrics();
            float mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);
            float mTxtWidth = mTextPaint.measureText(txt, 0, txt.length());

            canvas.drawText(txt, mCircleRect.width() / 2 - (mTxtWidth / 2) +
                    mCircleRect.left, mCircleRect.height() / 2 +
                    (mTxtHeight / 4) +
                    mCircleRect.top, mTextPaint);


        } else {
            explosion.draw(canvas);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (ic_cancel_button != null) {
                //说明加载出了icon
                float x = event.getX();
                float y = event.getY();

                boolean con = mButtonRect.contains(x, y);
                if (con && mProgress != 100) {
                    //粒子破碎，任务取消

                    stopFlag = true;
                    pool.shutdown();

                    if (onCircleEventListener != null)
                        onCircleEventListener.onCancel(mProgress);

                    //先震动自己
                    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(200);
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //隐藏
                            Rect r = new Rect();
                            getGlobalVisibleRect(r);
                            int[] location = new int[2];
                            getLocationOnScreen(location);
                            r.offset(-location[0], -location[1]);
                            r.inset(-mExpandInset[0], -mExpandInset[1]);
                            //animate().setDuration(150).setStartDelay(100).scaleX(0f).scaleY(0f).alpha(0f).start();
                            //爆炸
                            Log.i("feng", "r.left " + r.left + "r.top " + r.top + "r.right " + r.right + "r.bottom " + r.bottom);


                            explode(Utils.createBitmapFromView(Circle_ProgressBall.this), r, 150, ExplosionAnimator.DEFAULT_DURATION);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        Random random = new Random();

                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            setTranslationX((random.nextFloat() - 0.5f) * getWidth() * 0.05f);
                            setTranslationY((random.nextFloat() - 0.5f) * getHeight() * 0.05f);

                        }


                    });
                    animator.start();

                }
            }
        }


        return true;
    }

    public void explode(Bitmap bitmap, Rect bound, long startDelay, long duration) {
        explosion = new ExplosionAnimator(this, bitmap, bound);
        isShouldCancel = true;

        explosion.setStartDelay(startDelay);
        explosion.setDuration(duration);

        explosion.start();
    }

    private void metaball(Canvas canvas, RectF ball1, RectF ball2, float v, float handle_len_rate, float maxDistance) {
        //要使用这个方法，唯一和外界有联系的就是这个circlePaths了。可以改造成传入两个Circle型参数。


        float[] center1 = new float[]{
                ball1.centerX(),
                ball1.centerY()
        };
        float[] center2 = new float[]{
                ball2.centerX(),
                ball2.centerY()
        };
        //拿到两圆的距离
        float d = Utils.getDistance(center1, center2);

        float radius1 = ball1.width() / 2;
        float radius2 = ball2.width() / 2;
        float pi2 = (float) (Math.PI / 2);
        float u1, u2;


        if (d > maxDistance) {
//            canvas.drawCircle(ball1.centerX(), ball1.centerY(), circle1.radius, paint);
            //原本的半径
            canvas.drawCircle(ball2.centerX(), ball2.centerY(), mRadius, mPaint);
        } else {
            float scale2 = 1 + SCALE_RATE * (1 - d / maxDistance);
            radius2 *= scale2;
            //大圆因为包含了小圆 ，半径逐渐变大
//            radius1 *= scale1;
//            canvas.drawCircle(ball1.centerX(), ball1.centerY(), radius1, paint);
            canvas.drawCircle(ball2.centerX(), ball2.centerY(), radius2 * 2, mPaint);

        }

        //Log.d("Metaball_radius", "radius1:" + radius1 + ",radius2:" + radius2);
        if (radius1 == 0 || radius2 == 0) {
            //redius1等于0？  小圆的半径不可能等于0.....除非设置的时候设置了radius为0,那后面的计算就错了，所以这个判断很有必要。
            return;
        }
        //如果两圆距离不够到可以绘制曲线的程度或者小圆完全被大圆包裹，就不用绘制曲线了。
        if (d > maxDistance || d <= Math.abs(radius1 - radius2)) {
            return;
        } else if (d < radius1 + radius2) {
            //正在进入。 记录一些计算用到的变量，u1，u2好像是和两圆之间的某个角度相关。
            u1 = (float) Math.acos((radius1 * radius1 + d * d - radius2 * radius2) /
                    (2 * radius1 * d));
            u2 = (float) Math.acos((radius2 * radius2 + d * d - radius1 * radius1) /
                    (2 * radius2 * d));
        } else {
            u1 = 0;
            u2 = 0;
        }
//        Log.d("Metaball", "center2:" + Arrays.toString(center2) + ",center1:" + Arrays.toString(center1));
        float[] centermin = new float[]{center2[0] - center1[0], center2[1] - center1[1]};

        float angle1 = (float) Math.atan2(centermin[1], centermin[0]);
        float angle2 = (float) Math.acos((radius1 - radius2) / d);
        float angle1a = angle1 + u1 + (angle2 - u1) * v;
        float angle1b = angle1 - u1 - (angle2 - u1) * v;
        float angle2a = (float) (angle1 + Math.PI - u2 - (Math.PI - u2 - angle2) * v);
        float angle2b = (float) (angle1 - Math.PI + u2 + (Math.PI - u2 - angle2) * v);

//        Log.d("Metaball", "angle1:" + angle1 + ",angle2:" + angle2 + ",angle1a:" + angle1a + ",angle1b:" + angle1b + ",angle2a:" + angle2a + ",angle2b:" + angle2b);

        //这里就比较复杂了，不知道作者的平面几何怎么画的，怎么做的辅助线。
        float[] p1a1 = Utils.getVector(angle1a, radius1);
        float[] p1b1 = Utils.getVector(angle1b, radius1);
        float[] p2a1 = Utils.getVector(angle2a, radius2);
        float[] p2b1 = Utils.getVector(angle2b, radius2);

        //总之p1 - p2 这四个点就是内凹矩形的顶点坐标了
        float[] p1a = new float[]{p1a1[0] + center1[0], p1a1[1] + center1[1]};
        float[] p1b = new float[]{p1b1[0] + center1[0], p1b1[1] + center1[1]};
        float[] p2a = new float[]{p2a1[0] + center2[0], p2a1[1] + center2[1]};
        float[] p2b = new float[]{p2b1[0] + center2[0], p2b1[1] + center2[1]};


//        Log.d("Metaball", "p1a:" + Arrays.toString(p1a) + ",p1b:" + Arrays.toString(p1b) + ",p2a:" + Arrays.toString(p2a) + ",p2b:" + Arrays.toString(p2b));

        float[] p1_p2 = new float[]{p1a[0] - p2a[0], p1a[1] - p2a[1]};

        float totalRadius = (radius1 + radius2);
        float d2 = Math.min(v * handle_len_rate, Utils.getLength(p1_p2) / totalRadius);
        d2 *= Math.min(1, d * 2 / (radius1 + radius2));
//        Log.d("Metaball", "d2:" + d2);
        radius1 *= d2;
        radius2 *= d2;

        //这四个点就是控制贝塞尔曲线的弯曲程度的点了。
        float[] sp1 = Utils.getVector(angle1a - pi2, radius1);
        float[] sp2 = Utils.getVector(angle2a + pi2, radius2);
        float[] sp3 = Utils.getVector(angle2b - pi2, radius2);
        float[] sp4 = Utils.getVector(angle1b + pi2, radius1);
//        Log.d("Metaball", "sp1:" + Arrays.toString(sp1) + ",sp2:" + Arrays.toString(sp2) + ",sp3:" + Arrays.toString(sp3) + ",sp4:" + Arrays.toString(sp4));


        Path path1 = new Path();
        path1.moveTo(p1a[0], p1a[1]);
        path1.cubicTo(p1a[0] + sp1[0], p1a[1] + sp1[1], p2a[0] + sp2[0], p2a[1] + sp2[1], p2a[0], p2a[1]);
        path1.lineTo(p2b[0], p2b[1]);
        path1.cubicTo(p2b[0] + sp3[0], p2b[1] + sp3[1], p1b[0] + sp4[0], p1b[1] + sp4[1], p1b[0], p1b[1]);
        path1.lineTo(p1a[0], p1a[1]);
        path1.close();
        //总算计算出了要绘制的路径，是一个内凹进去的长方形。
        canvas.drawPath(path1, mPaint);


    }

    public void setProgress(int progress) {
        //TODO:每增加1个progress 就生成一个小圆,冲向大圆.

        Rect r = new Rect();
        getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);
        RectF rf = new RectF(r);

        //奇数就生成ball
        if (progress % 2 != 0 || !stopFlag)
            MakeSmallBall(rf, mCircleRect);


        mProgress = progress;
        //invalidate();
        if (mProgress >= 100) {
            startAnimation();
            ic_cancel_button = BitmapFactory.decodeResource(getResources(), R.drawable.ic_button_ok);

            pool.shutdown();
        }
    }

    private void MakeSmallBall(RectF rectF, RectF circleRect) {
        SmallBallFactory.SmallBall smallBall = SmallBallFactory.generateSmallBall(rectF, circleRect);
        smallBalls.add(smallBall);
        if (!pool.isShutdown())
            pool.execute(new BallThread(smallBall, smallBalls.size(), mCircleRect));

        Log.i("small ball", smallBall.toString());

    }

    class BallThread extends Thread {

        int id;
        SmallBallFactory.SmallBall ball;
        RectF circle;

        public BallThread(SmallBallFactory.SmallBall ball, int id, RectF circle) {
            this.ball = ball;
            this.id = id;

            this.circle = circle;
        }

        @Override
        public void run() {
            Log.i("ball thread ", "我要改变ball" + id);

            while (true) {

                if (stopFlag || mProgress == 100) {
                    return;
                }

                float cutX = ball.getCx() - circle.centerX() / 100;
                float cutY = ball.getCy() - circle.centerY() / 100;

                ball.setCx(ball.getCx() + cutX);
                ball.setCy(ball.getCy() + cutY);

                Log.i("ball thread", "change");

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (circle.contains(ball.getCx(), ball.getCy())) {
                    return;
                }

            }
        }
    }

    private void startAnimation() {
        wa = new MoveAnimation();
        wa.setDuration(10000);

        wa.setInterpolator(new DecelerateInterpolator());
        wa.setFillAfter(true);
        startAnimation(wa);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    public boolean isStartAnimationFinish() {
        return wa.hasEnded();
    }

}
