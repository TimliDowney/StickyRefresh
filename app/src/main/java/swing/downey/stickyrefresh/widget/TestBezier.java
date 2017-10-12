package swing.downey.stickyrefresh.widget;

import android.content.Context;
import android.graphics.Canvas;
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

public class TestBezier extends View {
    public TestBezier(Context context) {
        super(context);
        init();
    }

    public TestBezier(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestBezier(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TestBezier(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();

    private void init() {

        Paint paint = mPaint;//用局部变量的方式书写，效率更高，并也会反映到mPaint
        //抗锯齿
        paint.setAntiAlias(true);
        //抗抖动
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1);//1个像素点

        //一阶贝塞尔曲线
        Path path = mPath;//用局部变量的方式书写，效率更高，并也会反映到mPath
        path.moveTo(100, 100);
        path.lineTo(400, 400);

        //二阶贝塞尔曲线
        //path.quadTo(500,0,700,300);
        //相对path.lineTo(300, 300)的点实现，与quadTo(500,0,700,300)相同的实现如下
        path.rQuadTo(200, -300, 400, 0);


        path.moveTo(400,800);
        //三阶贝塞尔曲线
//        path.cubicTo(500, 600, 700, 1200,800,800);
        path.rCubicTo(100,-200,300,400,400,0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
        canvas.drawPoint(600, 100, mPaint);
        canvas.drawPoint(500, 600, mPaint);
        canvas.drawPoint(700, 1200, mPaint);
    }
}
