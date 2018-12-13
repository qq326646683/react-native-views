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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

import nell.jinxian.R;

public class YunioRefreshHeader extends FrameLayout implements RefreshHeader {
  protected SpinnerStyle mSpinnerStyle = SpinnerStyle.Translate;
  protected RefreshKernel mRefreshKernel;
  protected int mFinishDuration = 500;


  private View mRootView;
  private ImageView mIvEarth, mIvEarthTemp;
  private Bitmap targetBitmap;
  private int targetBitmapWidth;
  private int targetWidth, targetHeight;
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

  public YunioRefreshHeader(@NonNull Context context) {
    super(context);
    mRootView = LayoutInflater.from(getContext()).inflate(R.layout.refresh_header_view, this, false);
    mIvEarth = (ImageView) mRootView.findViewById(R.id.iv_earth);
    mIvEarthTemp = (ImageView) mRootView.findViewById(R.id.iv_earth_stroke);
    addView(mRootView);
    mIvEarth.post(new Runnable() {
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
  public void onPullingDown(float percent, int offset, int headerHeight, int extendHeight) {
    mIvEarth.setScaleX(percent);
    mIvEarth.setScaleY(percent);
  }

  @Override
  public void onReleasing(float percent, int offset, int headerHeight, int extendHeight) {
    mIvEarth.setScaleX(percent);
    mIvEarth.setScaleY(percent);
  }

  @Override
  public void onRefreshReleased(RefreshLayout layout, int headerHeight, int extendHeight) {
    mHandler.sendEmptyMessage(MSG_WHAT_UPDATE);
  }

  @NonNull
  @Override
  public View getView() {
    return this;
  }

  @NonNull
  @Override
  public SpinnerStyle getSpinnerStyle() {
    return mSpinnerStyle;
  }

  @Override
  public void setPrimaryColors(int... colors) {

  }

  @Override
  public void onInitialized(@NonNull RefreshKernel kernel, int height, int extendHeight) {
    mRefreshKernel = kernel;
  }

  @Override
  public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

  }

  @Override
  public void onStartAnimator(@NonNull RefreshLayout layout, int height, int extendHeight) {

  }

  @Override
  public int onFinish(@NonNull RefreshLayout layout, boolean success) {
    mHandler.removeMessages(MSG_WHAT_UPDATE);
    mIvEarthTemp.setVisibility(View.INVISIBLE);
    mIvEarth.setVisibility(View.VISIBLE);
    return mFinishDuration;
  }

  @Override
  public boolean isSupportHorizontalDrag() {
    return false;
  }

  @Override
  public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
    switch (newState) {
      case None:
      case PullDownToRefresh:
      case ReleaseToRefresh:
        mIvEarthTemp.setVisibility(View.INVISIBLE);
        mIvEarth.setVisibility(View.VISIBLE);
        break;
      case Refreshing:
      case RefreshReleased:
        mIvEarthTemp.setVisibility(View.VISIBLE);
        mIvEarth.setVisibility(View.GONE);
        break;
    }
  }
}
