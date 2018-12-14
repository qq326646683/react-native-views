package nell.jinxian.listview;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.lang.reflect.Field;

public class ScrollSpeedLinearLayoutManger extends LinearLayoutManager {
  private float MILLISECONDS_PER_INCH = 50f;
  private double speedRatio = 1;
  private Context contxt;

  public ScrollSpeedLinearLayoutManger(Context context) {
    super(context);
    this.contxt = context;
    setSpeedSlow();
  }

  public ScrollSpeedLinearLayoutManger(Context context, int orientation, boolean reverseLayout) {
    super(context, orientation, reverseLayout);
    this.contxt = context;
    setSpeedSlow();
  }

  @Override
  public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                     int position) {
    LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(
      recyclerView.getContext()) {
      @Override
      public PointF computeScrollVectorForPosition(int targetPosition) {
        return ScrollSpeedLinearLayoutManger.this
          .computeScrollVectorForPosition(targetPosition);
      }

      // This returns the milliseconds it takes to
      // scroll one pixel.
      @Override
      protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        //                Log.d("ScrollSpeedManger", "calculateSpeedPerPixel : " + displayMetrics.density);
        return MILLISECONDS_PER_INCH / displayMetrics.density;
        // 返回滑动一个pixel需要多少毫秒
      }


      @Override
      protected void onTargetFound(View targetView, RecyclerView.State state,
                                   RecyclerView.SmoothScroller.Action action) {
        //                Log.d("ScrollSpeedManger", "onTargetFound : ");
        if (getLayoutManager() == null) {
          return;
        }
        int dx = calculateDxToMakeVisible(targetView, getHorizontalSnapPreference());
        int dy = calculateDyToMakeVisible(targetView, getVerticalSnapPreference());
        if (dx > 0) {
          dx = dx - getLayoutManager().getLeftDecorationWidth(targetView);
        } else {
          dx = dx + getLayoutManager().getRightDecorationWidth(targetView);
        }
        if (dy > 0) {
          dy = dy - getLayoutManager().getTopDecorationHeight(targetView);
        } else {
          dy = dy + getLayoutManager().getBottomDecorationHeight(targetView);
        }
        final int distance = (int) Math.sqrt(dx * dx + dy * dy);
        final int time = calculateTimeForDeceleration(distance);
        if (time > 0) {// new AccelerateInterpolator()
          action.update(-dx, -dy, time, new DecelerateInterpolator());
        }
      }

    };
    linearSmoothScroller.setTargetPosition(position);
    startSmoothScroll(linearSmoothScroller);
  }

  @Override
  public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
    int a = super.scrollVerticallyBy(dy, recycler, state);
    //        Log.d("ScrollSpeedManger", "scrollVerticallyBy a : " + a);
    //        Log.d("ScrollSpeedManger", "scrollVerticallyBy a : " + ((int) (speedRatio * dy)));
    if (a == (int) (speedRatio * dy)) {
      return dy;
    }
    return a;
  }

  public void setSpeedRatio(double speedRatio) {
    this.speedRatio = speedRatio;
  }

  public void setSpeedSlow() {
    // 自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
    // 0.3f是自己估摸的一个值，可以根据不同需求自己修改
    MILLISECONDS_PER_INCH = contxt.getResources().getDisplayMetrics().density * 0.5f;
  }

  public void setSpeedFast() {
    MILLISECONDS_PER_INCH = contxt.getResources().getDisplayMetrics().density * 0.03f;
  }

  public void setMaxFlingVelocity(RecyclerView recyclerView, int velocity) {
    try {
      Field field = recyclerView.getClass().getField("mMaxFlingVelocity");
      field.setAccessible(true);
      field.set(recyclerView, velocity);
    } catch (Exception e) {

    }
  }
}
