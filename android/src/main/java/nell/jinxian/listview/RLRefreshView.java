package nell.jinxian.listview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import nell.jinxian.R;

/**
 * 下拉刷新控件
 */
class RLRefreshView extends StatusView {

    private TextView tv;
    private ImageView iv;
    private ImageView mIvEarthTemp;
    private RelativeLayout.LayoutParams layoutParams;


    RLRefreshView(Context context) {
        this(context, null);
    }

    RLRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    RLRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        tv = (TextView) findViewById(R.id.tv);
        iv = (ImageView) findViewById(R.id.iv);
        mIvEarthTemp = (ImageView) findViewById(R.id.iv_earth_stroke);

        layoutParams = (RelativeLayout.LayoutParams) iv.getLayoutParams();
        initView();

    }


    private final static int MSG_WHAT_UPDATE = 99;
    private int curPosotion = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT_UPDATE) {
                //        Log.e("MyRefreshHeader", "curPosotion  :" + curPosotion);
                //        Log.e("MyRefreshHeader", "curPosotion + targetHeight:" + (curPosotion + targetHeight));
                if (curPosotion - targetWidth < 0) {
                    curPosotion = targetBitmapWidth - targetWidth;
                }
                Bitmap bitmap = Bitmap.createBitmap(targetBitmap, curPosotion, 0, targetWidth, targetHeight);
                bitmap = bimapRound(bitmap, targetHeight / 2);
                mIvEarthTemp.setImageBitmap(bitmap);
                curPosotion--;
                sendEmptyMessageDelayed(MSG_WHAT_UPDATE, 15);
            }
        }
    };

    private Bitmap bimapRound(Bitmap mBitmap, float index) {
        Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_4444);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        //设置矩形大小
        Rect rect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        RectF rectf = new RectF(rect);

        // 相当于清屏
        canvas.drawARGB(0, 0, 0, 0);
        //画圆角
        canvas.drawRoundRect(rectf, index, index, paint);
        // 取两层绘制，显示上层
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 把原生的图片放到这个画布上，使之带有画布的效果
        canvas.drawBitmap(mBitmap, rect, rect, paint);
        return bitmap;

    }

    private Bitmap targetBitmap;
    private int targetBitmapWidth;
    private int targetWidth, targetHeight;

    private void initView() {
        iv.post(new Runnable() {
            @Override
            public void run() {
                Bitmap srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.earth_bg);
                int srcWidth = srcBitmap.getWidth();
                int srcHeight = srcBitmap.getHeight();
                //        Log.e("MyRefreshHeader", "srcBitmap size  :" + srcWidth + " , " + srcHeight);
                //                targetWidth = mIvEarth.getHeight();
                targetWidth = getContext().getResources().getDimensionPixelSize(R.dimen.earth_ball_size);
                targetHeight = targetWidth - 20;
//                Log.e("MyRefreshHeader", "mIvEarth size  :" + targetWidth);
                float scale = ((float) targetHeight) / srcHeight;
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                targetBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth, srcHeight, matrix, true);
                targetBitmapWidth = targetBitmap.getWidth();
                //        Log.e("MyRefreshHeader", "targetBitmap size  :" + targetBitmapWidth + " , " +
                //          targetHeight);
            }
        });
    }

    @Override
    protected int setLayout() {
        // TODO 在这里设置加载更多控件布局id
        return R.layout.layout_refresh;
    }

    @Override
    public void onLoading() {
        super.onLoading();
        mIvEarthTemp.setVisibility(View.VISIBLE);
        iv.setVisibility(View.GONE);
        mHandler.sendEmptyMessage(MSG_WHAT_UPDATE);
        // TODO 在这里设置正在加载效果

        tv.setText("正在刷新");
//        iv.setImageResource(R.drawable.loading);
//        ((AnimationDrawable)iv.getDrawable()).start();
    }

    @Override
    public void onFinish() {
        super.onFinish();
        mHandler.removeMessages(MSG_WHAT_UPDATE);
        mIvEarthTemp.setVisibility(View.INVISIBLE);
        iv.setVisibility(View.VISIBLE);
        curPosotion = 0;

        // TODO 在这里设置加载完成后效果

        tv.setText("刷新完成");
//        Drawable drawable = iv.getDrawable();
//        if (drawable instanceof AnimationDrawable) {
//            ((AnimationDrawable) drawable).stop();
//        }
    }

    @Override
    public void onPulling(int percent) {
        Log.e("RLRefreshView.class", "percent:" + percent);
        super.onPulling(percent);

        // TODO 在这里设置下拉过程效果

        tv.setText("下拉刷新");

//        if (percent < 20) {
//            iv.setImageResource(R.drawable.pull_end_image_frame_01);
//        } else if (percent < 40) {
//            iv.setImageResource(R.drawable.pull_end_image_frame_02);
//        } else if (percent < 60) {
//            iv.setImageResource(R.drawable.pull_end_image_frame_03);
//        } else if (percent < 80) {
//            iv.setImageResource(R.drawable.pull_end_image_frame_04);
//        } else {
//            iv.setImageResource(R.drawable.pull_end_image_frame_05);
//        }

        layoutParams.width = DisplayUtils.dp2px(getContext(), 30 * percent / 100);
        layoutParams.height = DisplayUtils.dp2px(getContext(), 30 * percent / 100);
        iv.setLayoutParams(layoutParams);
    }

    @Override
    public void onLoosen() {
        super.onLoosen();

        // TODO 在这里设置释放加载效果

        tv.setText("释放刷新");
    }
}
