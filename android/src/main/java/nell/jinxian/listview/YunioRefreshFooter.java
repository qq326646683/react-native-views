package nell.jinxian.listview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;

import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

public class YunioRefreshFooter extends FrameLayout implements RefreshFooter {
  protected SpinnerStyle mSpinnerStyle = SpinnerStyle.Translate;
  private RecyclerViewItemView LoadMore;

  public YunioRefreshFooter(@NonNull Context context) {
    super(context);
  }

  @Override
  public void onPullingUp(float percent, int offset, int footerHeight, int extendHeight) {

  }

  @Override
  public void onPullReleasing(float percent, int offset, int footerHeight, int extendHeight) {

  }

  @Override
  public void onLoadmoreReleased(RefreshLayout layout, int footerHeight, int extendHeight) {

  }

  @Override
  public boolean setLoadmoreFinished(boolean finished) {
    return false;
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

  }

  @Override
  public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

  }

  @Override
  public void onStartAnimator(@NonNull RefreshLayout layout, int height, int extendHeight) {

  }

  @Override
  public int onFinish(@NonNull RefreshLayout layout, boolean success) {
    return 0;
  }

  @Override
  public boolean isSupportHorizontalDrag() {
    return false;
  }

  @Override
  public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {

  }

  public void addFootView(RecyclerViewItemView footerView) {
    this.LoadMore = footerView;
    removeAllViews();
    addView(footerView, 0);
  }

  public RecyclerViewItemView getLoadMore() {
    return LoadMore;
  }
}
