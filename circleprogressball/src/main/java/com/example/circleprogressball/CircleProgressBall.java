package com.example.circleprogressball;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.example.circleprogressball.tools.BallThread;
import com.example.circleprogressball.tools.Circle;
import com.example.circleprogressball.tools.ExplosionAnimator;
import com.example.circleprogressball.tools.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by feng on 2016/1/12.
 */
public class CircleProgressBall extends View {

    static final int TEXT_COLOR = Color.WHITE;
    private static final float SCALE_RATE = 0.3f;

    int mRadius; //圆的宽度，默认为100dp

    int mHorizontalDistance;
    int mTopDistance;
    int mBottomDistance;

    Paint mPaint;
    Paint mTextPaint;

    int mProgress;

    MoveAnimation moveAnimation;
    ValueAnimator alphaChangeAnimation;

    Bitmap iconCancelButton;

    private int[] mExpandInset = new int[2];

    ExplosionAnimator explosion = null;

    boolean cancelFlag = false;

    Circle mainCircle;
    Circle buttonCircle;
    private int buttonAlpha = 0;

    public boolean getCancelFlag() {
        return cancelFlag;
    }

    public interface OnCircleEventListener {

        void onCancel(int mProgress);

        void onFinish();

    }

    public void setOnCircleEventListener(OnCircleEventListener onCircleEventListener) {
        this.onCircleEventListener = onCircleEventListener;
    }

    OnCircleEventListener onCircleEventListener;

    final List<Circle> smallBalls = Collections.synchronizedList(new LinkedList<Circle>());
    ThreadPoolExecutor pool;

    private class MoveAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float tran = (mBottomDistance + mainCircle.r) / 2;  // 位移的最终位置

            float dis = tran * interpolatedTime;

            if (mProgress != 100) {
                if (buttonCircle.b < mTopDistance + mainCircle.r + tran) {
                    buttonCircle.b += dis;
                } else {
                    //加载按钮中的取消图片.
                    if (iconCancelButton == null) {
                        iconCancelButton = BitmapFactory.decodeResource(getResources(), R.drawable.ic_button_cancel);
                    }

                    if (alphaChangeAnimation == null) {
                        alphaChangeAnimation = ValueAnimator.ofFloat(0f, 1f).setDuration(2000);
                        alphaChangeAnimation.setRepeatMode(ValueAnimator.REVERSE);
                        alphaChangeAnimation.setRepeatCount(ValueAnimator.INFINITE);

                        alphaChangeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float value = (float) animation.getAnimatedValue();
                                buttonAlpha = (int) (10 + value* (120 - 10));

                            }
                        });
                        alphaChangeAnimation.start();
                    }
                }
//                Log.i("feng", " time" + interpolatedTime);
            } else {
                if (buttonCircle.b > mainCircle.b) {
                    buttonCircle.b -= dis;
                } else {
                    if (alphaChangeAnimation != null) {
                        alphaChangeAnimation.cancel();
                        alphaChangeAnimation = null;
                    }
                    return;
                }
            }
            invalidate();
        }
    }

    public CircleProgressBall(Context context) {
        this(context, null);
    }

    public CircleProgressBall(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBall, defStyleAttr, 0);
        int num = array.getIndexCount();
        for (int i = 0; i < num; i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.CircleProgressBall_circle_radius) {
                mRadius = array.getDimensionPixelOffset(attr, 100);
            } else if (attr == R.styleable.CircleProgressBall_circle_progress) {
                mProgress = array.getInt(attr, 0);
            }
        }

        array.recycle();

        init();
    }

    private void init() {
        //构造线程池
        pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        mainCircle = new Circle();
        buttonCircle = new Circle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mRadius / 1.3f);

        Arrays.fill(mExpandInset, Utils.dp2Px(32));

        cancelFlag = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = resolveSizeAndState(Utils.dp2Px(mRadius + 30), widthMeasureSpec, 0);
        int h = resolveSizeAndState(Utils.dp2Px(mRadius + 60), heightMeasureSpec, 0);

        mHorizontalDistance = (w - mRadius) / 2;

        mTopDistance = (h - mRadius) / 3;
        mBottomDistance = (h - mRadius) * 2 / 3;

        mainCircle.a = mHorizontalDistance + mRadius * 0.5f;
        mainCircle.b = mTopDistance + mRadius * 0.5f;
        mainCircle.r = mRadius;

        buttonCircle.a = mainCircle.a;
        buttonCircle.b = mainCircle.b;
        buttonCircle.r = mRadius * 0.5f;

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!cancelFlag) {
            int findColor = Utils.findColorByProgress(mProgress);
            mPaint.setColor(Utils.findColorByProgress(mProgress));
            Paint paint = new Paint(mPaint);

            if (mProgress != 100) {
                paint.setShader(new RadialGradient(buttonCircle.a, buttonCircle.b, buttonCircle.r,
                        findColor,
                        Color.argb(buttonAlpha,
                                Color.red(findColor),
                                Color.green(findColor),
                                Color.blue(findColor)), Shader.TileMode.CLAMP));
            }

            canvas.drawCircle(mainCircle.a, mainCircle.b, mainCircle.r, mPaint);

            canvas.drawCircle(buttonCircle.a, buttonCircle.b, buttonCircle.r, paint);

            if (Utils.getDistance(buttonCircle.a, buttonCircle.b, mainCircle.a, mainCircle.b)
                    > mainCircle.r - buttonCircle.r) {
                metaball(canvas, buttonCircle, mainCircle,
                        0.735f, 8f,
                        mainCircle.r * 2.5f, true);
            }
            if (iconCancelButton != null &&
                    Utils.getDistance(buttonCircle.a, buttonCircle.b, mainCircle.a, mainCircle.b)
                            > mainCircle.r - buttonCircle.r) {
                //绘制按钮中的图片
                canvas.drawBitmap(iconCancelButton, null, Utils.scaleRectF(buttonCircle.getCircleRect(), 0.6f), mPaint);
            }

            //开始画小球
            if (!smallBalls.isEmpty() && mProgress != 100) {
                synchronized (smallBalls) {
                    for (Circle oneBall : smallBalls) {
                        canvas.drawCircle(oneBall.a, oneBall.b, oneBall.r, mPaint);

                        if (Utils.getDistance(oneBall.a, oneBall.b, mainCircle.a, mainCircle.b)
                                > mainCircle.r - oneBall.r) {
                            metaball(canvas, oneBall, mainCircle,
                                    0.735f, 8f, mainCircle.r * 1.25f, false);
                        }
                    }
                }
            }

            //画出圆内的字
            String txt = String.valueOf(mProgress);
            Paint.FontMetrics fm = mTextPaint.getFontMetrics();
            float mTxtHeight = (int) Math.abs(fm.ascent) - fm.descent;
            float mTxtWidth = mTextPaint.measureText(txt, 0, txt.length());

            canvas.drawText(txt,
                    mainCircle.r - (mTxtWidth / 2) + mainCircle.getCircleRect().left,
                    mainCircle.r + (mTxtHeight / 2) + mainCircle.getCircleRect().top,
                    mTextPaint);

        } else {
            explosion.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (iconCancelButton != null) {
                //说明加载出了icon
                float x = event.getX();
                float y = event.getY();

                if (buttonCircle.contains(x, y) && mProgress != 100) {
                    //粒子破碎，任务取消
                    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(200);

                    //先震动自己
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        Random random = new Random();

                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            setTranslationX((random.nextFloat() - 0.5f) * getWidth() * 0.05f);
                            setTranslationY((random.nextFloat() - 0.5f) * getHeight() * 0.05f);
                        }
                    });

                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //爆炸
                            Rect r = getViewRect();
                            explode(Utils.createBitmapFromView(CircleProgressBall.this), r,
                                    150, ExplosionAnimator.DEFAULT_DURATION);
                            //取消
                            cancelFlag = true;
                            pool.shutdownNow();
                            if (onCircleEventListener != null) {
                                onCircleEventListener.onCancel(mProgress);
                            }
                        }
                    });
                    animator.start();
                }
            }
            return true;
        }
        return false;
    }

    @NonNull
    private Rect getViewRect() {
        Rect r = new Rect();
        getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);
        return r;
    }

    public void explode(Bitmap bitmap, Rect bound, long startDelay, long duration) {
        explosion = new ExplosionAnimator(this, bitmap, bound);
        explosion.setStartDelay(startDelay);
        explosion.setDuration(duration);
        explosion.start();
    }

    private void metaball(Canvas canvas, Circle ball1, Circle ball2, float v, float handle_len_rate, float maxDistance, boolean shouldLarge) {

        //拿到两圆的距离
        float distanceForTwoCircle = Utils.getDistance(ball1.a, ball1.b, ball2.a, ball2.b);

        float radius1 = ball1.r;
        float radius2 = ball2.r;

        float pi_2 = (float) (Math.PI / 2);
        float u1 = 0, u2 = 0;

        if (distanceForTwoCircle <= maxDistance && shouldLarge) {
            float scale2 = 1 + SCALE_RATE * (1 - distanceForTwoCircle / maxDistance);
            radius2 *= scale2;
            //大圆因为包含了小圆 ，半径随圆心距改变
            canvas.drawCircle(ball2.a, ball2.b, radius2, mPaint);
        }

        //Log.d("Metaball_radius", "radius1:" + radius1 + ",radius2:" + radius2);

        if (radius1 == 0 || radius2 == 0) {
            //redius1 等于0？  小圆的半径不可能等于0.....除非设置的时候设置了radius为0,那后面的计算就错了，所以这个判断很有必要。
            return;
        }

        //如果两圆距离大于可以绘制曲线的程度或者小圆完全被大圆包裹，就不用绘制曲线了。
        if (distanceForTwoCircle > maxDistance || distanceForTwoCircle <= Math.abs(radius1 - radius2)) {
            return;
        }

        if (distanceForTwoCircle < radius1 + radius2) {
            //此时两圆有重合部分。  u1，u2好像是和两圆之间的某个角度相关。
            u1 = (float) Math.acos((radius1 * radius1 + distanceForTwoCircle * distanceForTwoCircle - radius2 * radius2) /
                    (2 * radius1 * distanceForTwoCircle));
            u2 = (float) Math.acos((radius2 * radius2 + distanceForTwoCircle * distanceForTwoCircle - radius1 * radius1) /
                    (2 * radius2 * distanceForTwoCircle));
        }

//        Log.d("Metaball", "center2:" + Arrays.toString(center2) + ",center1:" + Arrays.toString(center1));

        float[] centermin = new float[]{ball2.a - ball1.a, ball2.b - ball1.b};

        float angle1 = (float) Math.atan2(centermin[1], centermin[0]);
        float angle2 = (float) Math.acos((radius1 - radius2) / distanceForTwoCircle);
        float angle1a = angle1 + u1 + (angle2 - u1) * v;
        float angle1b = angle1 - u1 - (angle2 - u1) * v;
        float angle2a = (float) (angle1 + Math.PI - u2 - (Math.PI - u2 - angle2) * v);
        float angle2b = (float) (angle1 - Math.PI + u2 + (Math.PI - u2 - angle2) * v);

//        Log.d("Metaball", "angle1:" + angle1 + ",angle2:" + angle2 + ",angle1a:" + angle1a + ",angle1b:" + angle1b + ",angle2a:" + angle2a + ",angle2b:" + angle2b);

        //这里就比较复杂了，不知道作者的设计图怎么画的，怎么做的辅助线。
        float[] p1a1 = Utils.getVector(angle1a, radius1);
        float[] p1b1 = Utils.getVector(angle1b, radius1);
        float[] p2a1 = Utils.getVector(angle2a, radius2);
        float[] p2b1 = Utils.getVector(angle2b, radius2);

        //总之p1 - p2 这四个点就是内凹矩形的顶点坐标了
        float[] p1a = new float[]{p1a1[0] + ball1.a, p1a1[1] + ball1.b};
        float[] p1b = new float[]{p1b1[0] + ball1.a, p1b1[1] + ball1.b};
        float[] p2a = new float[]{p2a1[0] + ball2.a, p2a1[1] + ball2.b};
        float[] p2b = new float[]{p2b1[0] + ball2.a, p2b1[1] + ball2.b};

//        Log.d("Metaball", "p1a:" + Arrays.toString(p1a) + ",p1b:" + Arrays.toString(p1b) + ",p2a:" + Arrays.toString(p2a) + ",p2b:" + Arrays.toString(p2b));

        float[] p1_p2 = new float[]{p1a[0] - p2a[0], p1a[1] - p2a[1]};

        float totalRadius = (radius1 + radius2);
        float d2 = Math.min(v * handle_len_rate, Utils.getLength(p1_p2[0], p1_p2[1]) / totalRadius);
        d2 *= Math.min(1, distanceForTwoCircle * 2 / (radius1 + radius2));

//        Log.d("Metaball", "d2:" + d2);

        radius1 *= d2;
        radius2 *= d2;

        //这四个点就是控制贝塞尔曲线的弯曲程度的点了。
        float[] sp1 = Utils.getVector(angle1a - pi_2, radius1);
        float[] sp2 = Utils.getVector(angle2a + pi_2, radius2);
        float[] sp3 = Utils.getVector(angle2b - pi_2, radius2);
        float[] sp4 = Utils.getVector(angle1b + pi_2, radius1);

//        Log.d("Metaball", "sp1:" + Arrays.toString(sp1) + ",sp2:" + Arrays.toString(sp2) + ",sp3:" + Arrays.toString(sp3) + ",sp4:" + Arrays.toString(sp4));

        Path path1 = new Path();
        path1.moveTo(p1a[0], p1a[1]);
        path1.cubicTo(p1a[0] + sp1[0], p1a[1] + sp1[1], p2a[0] + sp2[0], p2a[1] + sp2[1], p2a[0], p2a[1]);
        path1.lineTo(p2b[0], p2b[1]);
        path1.cubicTo(p2b[0] + sp3[0], p2b[1] + sp3[1], p1b[0] + sp4[0], p1b[1] + sp4[1], p1b[0], p1b[1]);
        path1.lineTo(p1a[0], p1a[1]);
        path1.close();

        //计算出了要绘制的路径，是一个内凹进去的长方形。
        canvas.drawPath(path1, mPaint);
    }

    public void setProgress(int progress) {
        mProgress = progress;

        if (mProgress >= 100) {
            startAnimation();
            iconCancelButton = BitmapFactory.decodeResource(getResources(), R.drawable.ic_button_ok);

            if (onCircleEventListener != null) {
                onCircleEventListener.onFinish();
            }

            pool.shutdownNow();
        }

        //奇数就生成ball
        if (progress > 8 && (progress % 2 != 0 || !cancelFlag)) {
            makeSmallBall(mainCircle);
        }
    }

    private void makeSmallBall(Circle circleRect) {
        Circle smallBall = SmallBallFactory.generateSmallBall(circleRect);
        if (!pool.isShutdown() && smallBalls.size() < 6) {
            smallBalls.add(smallBall);
            pool.execute(new BallThread(smallBall, circleRect, smallBalls));
        }
    }

    private void startAnimation() {
        moveAnimation = new MoveAnimation();
        moveAnimation.setRepeatMode(Animation.REVERSE);
        moveAnimation.setRepeatCount(Animation.INFINITE);
        moveAnimation.setDuration(10000);

        moveAnimation.setInterpolator(new DecelerateInterpolator());
        moveAnimation.setFillAfter(true);
        startAnimation(moveAnimation);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }
}
