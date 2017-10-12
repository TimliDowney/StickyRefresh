package swing.downey.stickyrefresh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import swing.downey.stickyrefresh.widget.TouchPullView;

public class MainActivity extends AppCompatActivity {

    private static final float TOUCH_MOVE_Y_MAX = 600;
    private float mTouchMoveStartY;
    private TouchPullView mTouchPullView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTouchPullView = (TouchPullView) findViewById(R.id.touch_pull);
//        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this,BezierActivity.class);
//                startActivity(intent);
//            }
//        });

        findViewById(R.id.activity_main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //得到意图
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchMoveStartY = event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float y = event.getY();
                        if (y >= mTouchMoveStartY) {
                            float moveSize = y - mTouchMoveStartY;
                            float progress = moveSize >= TOUCH_MOVE_Y_MAX ? 1 : moveSize / TOUCH_MOVE_Y_MAX;
                            Log.d("Downe1y", "progress: " + progress);
                            mTouchPullView.setProgress(progress);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        Toast.makeText(MainActivity.this,"ACTION_UP",Toast.LENGTH_SHORT).show();
                        mTouchPullView.release();
//                        mTouchPullView.setProgress(0);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }


}
