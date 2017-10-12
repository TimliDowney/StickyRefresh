package swing.downey.stickyrefresh.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Paul on 2017/10/10.
 */

public class BezierFour extends View {
    public BezierFour(Context context) {
        super(context);
        init();
    }

    public BezierFour(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BezierFour(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BezierFour(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mBezier = new Path();

    private void init() {
        Paint paint = mPaint;//用局部变量的方式书写，效率更高，并也会反映到mPaint
        //抗锯齿
        paint.setAntiAlias(true);
        //抗抖动
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);//10个像素点

        //初始化贝塞尔曲线4阶++
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                initBezier();
//            }
//        }.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                initBezier();
            }
        }).start();

    }

    private void initBezier() {
        //(0,0)(300,300)(200,700)(500,1200)(700,200)
        float[] xPoints = new float[]{0, 300, 200, 500, 700,500,600,200};
        float[] yPoints = new float[]{0, 300, 700, 1200, 200,1300,600};

        Path path = mBezier;

        int fps = 10000;
        for (int i = 0; i <= fps; i++) {
            //进度
            float progress = i / (float) fps;
            float x = caculateBezier(progress, xPoints);
            float y = caculateBezier(progress, yPoints);
            //使用连接的方式，当x，y变动足够小的情况下，就是平滑的曲线。
            path.lineTo(x, y);
            postInvalidate();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 计算某时刻贝塞尔所处的值（x或者y）
     *
     * @param t      (0~1)
     * @param values 贝塞尔点集合 (x或者y的集合)
     * @return 当前t时刻贝塞尔所处点
     */
    private float caculateBezier(float t, float... values) {
        //采用双重for循环
        int len = values.length;
        for (int i = len - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                values[j] = values[j] + t * (values[j + 1] - values[j]);
            }
        }

        //运算时候结果保存在第一位
        return values[0];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.rgb(0xff,0x88,0x00));
        canvas.drawPath(mBezier, mPaint);
    }
}
