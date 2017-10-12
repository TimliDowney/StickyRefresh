package swing.downey.stickyrefresh.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import swing.downey.stickyrefresh.R;

import static android.content.ContentValues.TAG;

/**
 * Created by Paul on 2017/10/9.
 */

public class TouchPullView extends View {

    //圆的画笔
    private Paint mCirclePaint;
    //圆的半径
    private float mCircleRadius = 50;
    private float mCirclePaintX, mCirclePaintY;

    //进度值
    private float mProgress;

    //目标宽度
    private int mTargetWidth = 300;
    //贝塞尔曲线的路径以及画笔
    private Path mPath = new Path();
    private Paint mPathPaint;
    //重心点的最终高度，决定控制点的Y坐标
    private int mTargetGravityHeight = 10;
    //角度变换0~135度
    private int mTangentAngle = 105;
    private Interpolator mProgressInterpolator = new DecelerateInterpolator();
    private Interpolator mTanentAngleInterpolator;


    //可拖动的高度
    private int mDragHeight = 400;

    private Drawable mContent = null;
    private int mContentMargin = 0;

    public TouchPullView(Context context) {
        super(context);
        init(null);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * 初始化方法
     */
    private void init(AttributeSet attrs) {

        //得到设置的参数
        final Context context = getContext();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TouchPullView, 0, 0);
        int color = array.getColor(R.styleable.TouchPullView_pColor, 0x20000000);
        mCircleRadius = array.getDimension(R.styleable.TouchPullView_pRadius, mCircleRadius);
        mDragHeight = array.getDimensionPixelOffset(R.styleable.TouchPullView_pDragHeight, mDragHeight);
        mTangentAngle = array.getInteger(R.styleable.TouchPullView_pTangentAngle, 500);
        mTargetWidth = array.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetWidth, mTargetWidth);
        mTargetGravityHeight = array.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetGravityHeight,
                mTargetGravityHeight);
        mContent = array.getDrawable(R.styleable.TouchPullView_pContentDrawable);
        mContentMargin = array.getDimensionPixelOffset(R.styleable.TouchPullView_pContentDrawableMargin, 0);

        //销毁
        array.recycle();

        //<Paint.ANTI_ALIAS_FLAG  set true－－用于绘制时抗锯齿.
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        //>
        //设置防抖动。
        p.setDither(true);
        //设置为填充方式。
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF000000);//Black
        mCirclePaint = p;

        //初始化路径部分画笔
//        Paint p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        //>
        //设置防抖动。
        p.setDither(true);
        //设置为填充方式。
        p.setStyle(Paint.Style.FILL);
//        p.setColor(0xFF000000);//Black
//        mCirclePaint = p;
        p.setColor(0xFFFF0000);//Red
        mPathPaint = p;

        //切角路径差值器
        mTanentAngleInterpolator = PathInterpolatorCompat.create((mCircleRadius * 2.0f) / mDragHeight, 90.0f / mTangentAngle);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int count = canvas.save();
        float tranX = (getWidth() - getValueByLine(getWidth(), mTargetWidth, mProgress)) / 2;
        canvas.translate(tranX, 0);

//        //画贝塞尔曲线
        canvas.drawPath(mPath, mPathPaint);
        //画圆
        canvas.drawCircle(mCirclePaintX,
                mCirclePaintY,
                mCircleRadius,
                mCirclePaint);

        Drawable drawable = mContent;
        if (drawable != null) {
            canvas.save();
            //剪切矩形区域
            canvas.clipRect(drawable.getBounds());
            //绘制Drawable
            drawable.draw(canvas);
            canvas.restore();

        }

        canvas.restoreToCount(count);
    }

    /**
     * 当进行测量时候触发
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //宽度的意图、类型（eg:android.view.View.MeasureSpec#UNSPECIFIED,AT_MOST,EXACTLY）
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //高度的意图、类型
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int iWidth = (int) (2 * mCircleRadius + getPaddingLeft() + getPaddingRight());
        int iHeight = (int) ((mDragHeight * mProgress + 0.5f) + getPaddingTop() + getPaddingBottom());

        int measureWidth, measureHeight;

        if (widthMode == MeasureSpec.EXACTLY) {
            //确切的
            measureWidth = width;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //最多
            measureWidth = Math.min(iWidth, width);
        } else {
            measureWidth = iWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            //确切的
            measureHeight = height;
            Toast.makeText(getContext(), "EXACTLY", Toast.LENGTH_SHORT).show();
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //最多
            measureHeight = Math.min(iHeight, height);
//            Toast.makeText(getContext(), "AT_MOST", Toast.LENGTH_SHORT).show();
        } else {
            measureHeight = iHeight;
            Toast.makeText(getContext(), "Others", Toast.LENGTH_SHORT).show();
        }
        //设置测量的宽度高度
        setMeasuredDimension(measureWidth, measureHeight);
    }

    /**
     * 当大小变化时候触发
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        mCirclePaintX = getWidth() >> 1;//width的一半
//        mCirclePaintY = getHeight() >> 1;//height的一半
        Log.d(TAG, "Enter onSizeChanged");
        //当高度变化时进行路径更新
        updatePathLayout();
    }

    /**
     * 设置进度
     *
     * @param progress 进度
     */
    public void setProgress(float progress) {
        Log.d(TAG, "P: " + progress);
        mProgress = progress;
        //请求重新进行测量
        requestLayout();
    }

    /**
     * 更新路径的相关操作
     */
    private void updatePathLayout() {
        //获取进度
        final float progress = mProgressInterpolator.getInterpolation(mProgress);
        Log.d(TAG, "progress: " + progress);
        //获取可绘制区域高度宽度
        final float w = getValueByLine(getWidth(), mTargetWidth, mProgress);
        Log.d(TAG, "w: " + w);
        final float h = getValueByLine(0, mDragHeight, mProgress);
        Log.d(TAG, "h: " + h);
        //圆的圆心X坐标
        final float cPointX = w / 2.0f;
        //园的半径
        final float cRadius = mCircleRadius;
        //圆的圆心Y坐标
        final float cPointY = h - cRadius;
        //控制点结束Y的值
        final float endControlY = mTargetGravityHeight;

        //更新圆的坐标
        mCirclePaintX = cPointX;
        Log.d(TAG, "mCirclePaintX: " + mCirclePaintX);
        mCirclePaintY = cPointY;
        Log.d(TAG, "mCirclePaintY: " + mCirclePaintY);
        //路径
        final Path path = mPath;
        //复位操作
        path.reset();
        path.moveTo(0, 0);

        //左边部分的结束点和控制点
        float lEndPointX, lEndPointY;
        float lControlPointX, lControlPointY;

        //获取当前切线的弧度
        float angle = mTangentAngle * mTanentAngleInterpolator.getInterpolation(progress);
        double radian = Math.toRadians(angle);
        float x = (float) (Math.sin(radian) * cRadius);
        float y = (float) (Math.cos(radian) * cRadius);

        lEndPointX = cPointX - x;
        lEndPointY = cPointY + y;

        //控制点的Y轴变化
        lControlPointY = getValueByLine(0, endControlY, progress);
        //控制点与结束点之间的高度差
        float tHeight = lEndPointY - lControlPointY;
        float tWidth = (float) (tHeight / Math.tan(radian));
        lControlPointX = lEndPointX - tWidth;

        //左边贝塞尔曲线
        path.quadTo(lControlPointX, lControlPointY, lEndPointX, lEndPointY);
        //连接到右边
        path.lineTo(cPointX + (cPointX - lEndPointX), lEndPointY);
        //右边贝塞尔曲线
        path.quadTo((cPointX + (cPointX - lControlPointX)), lControlPointY, w, 0);

        //更新内容部分
        UpdateContentLayout(cPointX, cPointY, cRadius);
    }

    /**
     * 对内容部分进行测量并且进行设置
     *
     * @param cx     圆心X
     * @param cy     圆心Y
     * @param radius 半径
     */
    private void UpdateContentLayout(float cx, float cy, float radius) {
        Drawable drawable = mContent;
        if (drawable != null) {
            int margin = mContentMargin;
            int left = (int) (cx - radius + margin);
            int right = (int) (cx + radius - margin);
            int top = (int) (cy - radius + margin);
            int bottom = (int) (cy + radius - margin);
            drawable.setBounds(left, right, top, bottom);
        }
    }
//    private int getWeightOrHeight(int value){}

    /**
     * 获取当前值
     *
     * @param start    起始值
     * @param end      结束值
     * @param progress 进度
     * @return 当前进度的值
     */
    private float getValueByLine(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    //释放动画
    private ValueAnimator valueAnimator;

    public void release() {
        if (valueAnimator == null) {
            final ValueAnimator animator = ValueAnimator.ofFloat(mProgress, 0f);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(400);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object val = animation.getAnimatedValue();
                    if (val instanceof Float) {
                        setProgress((Float) val);
                    }
                }
            });
            valueAnimator = animator;
        } else {
            valueAnimator.cancel();
            valueAnimator.setFloatValues(mProgress, 0f);
        }
        valueAnimator.start();
    }
}
