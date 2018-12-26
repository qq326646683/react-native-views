package nell.jinxian.listview;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.yunio.smallworld.R;
import com.yunio.videocapture.utils.LogUtils;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Administrator on 2018/12/12.
 */

public class SmartRefreshManager extends ViewGroupManager<SmartRefreshLayout> {
  public static final String REACT_CLASS = "RTCSmartRefreshLayout";
  private YunioRefreshHeader refreshHeader;
  private ClassicsFooter refreshFooter;
  private String originalRefreshState;
  @Override
  public String getName() {
    return REACT_CLASS;
  }
  @Override
  protected void addEventEmitters(final ThemedReactContext reactContext, final SmartRefreshLayout view) {
    super.addEventEmitters(reactContext, view);
    view.setOnLoadmoreListener(new OnLoadmoreListener() {
      @Override
      public void onLoadmore(RefreshLayout refreshlayout) {
        reactContext
          .getNativeModule(UIManagerModule.class)
          .getEventDispatcher()
          .dispatchEvent(new LoadMoreEvent(view.getId()));

      }
    });
    view.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh(RefreshLayout refreshlayout) {
        reactContext
          .getNativeModule(UIManagerModule.class)
          .getEventDispatcher()
          .dispatchEvent(new RefreshViewEvent(view.getId()));

      }
    });
  }

  @Override
  protected SmartRefreshLayout createViewInstance(ThemedReactContext reactContext) {
    SmartRefreshLayout smartRefreshLayout = new SmartRefreshLayout(reactContext);
    smartRefreshLayout.setDisableContentWhenRefresh(true);
    smartRefreshLayout. setEnableOverScrollDrag(false);
    refreshHeader = new YunioRefreshHeader(reactContext);
    refreshFooter = new ClassicsFooter(reactContext);
    smartRefreshLayout. setRefreshHeader(refreshHeader);
    smartRefreshLayout. setRefreshFooter(refreshFooter);
    return smartRefreshLayout;
  }

  @ReactProp(name = "canRefresh", defaultBoolean = true)
  public void setRefreshEnabled(SmartRefreshLayout parent, boolean enabled) {
    parent.setEnableRefresh(enabled);
  }

  @ReactProp(name = "canLoadMore", defaultBoolean = true)
  public void setLoadMoreEnabled(SmartRefreshLayout parent, boolean enabled) {
    parent.setEnableLoadmore(enabled);
  }
  @ReactProp(name = "finishRefresh", defaultBoolean = false)
  public void onfinishRefresh(SmartRefreshLayout parent, boolean finishRefresh) {
    parent.finishRefresh(finishRefresh);
  }

  @ReactProp(name = "finishLoadMore", defaultBoolean = false)
  public void onFinishLoadMore(SmartRefreshLayout parent, boolean loadMore) {
    parent.finishLoadmore(loadMore);
  }
    @ReactProp(name = "refreshState")
    public void setRefreshState(SmartRefreshLayout parent, String refreshState) {
        refreshState(parent,refreshState);
    }

  @Nullable
  @Override
  public Map getExportedCustomBubblingEventTypeConstants() {
    return MapBuilder.builder()
      .put(
        "topRefresh",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onRefresh"))).put(
        "topLoadMore",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onLoadMore")))
      .build();
  }

  public void refreshState(SmartRefreshLayout parent,String refreshState) {
    Log.d("RecyclerViewViewManager", "originalRefreshState : " + originalRefreshState + " , refreshState : " + refreshState);
    //    parent.getRecyclerView().setRefreshState(refreshState);
    //    parent.getRecyclerView().getAdapter().notifyItemChanged(parent.getRecyclerView().getAdapter().getItemCount() - 1);
    refreshFooter.setClickable(false);
    if (!TextUtils.isEmpty(originalRefreshState)) {
      if (RefreshState.HeaderRefreshing.equals(originalRefreshState) && !RefreshState.HeaderRefreshing.equals(refreshState)) {
        parent.finishRefresh();
      } else if (RefreshState.FooterRefreshing.equals(originalRefreshState) && !RefreshState.FooterRefreshing.equals(refreshState)) {
        if (RefreshState.Failure.equals(refreshState)) {//加载失败
          addLoadFailureState(parent);
        } else {
          parent.finishLoadmore();
        }
      } else if (RefreshState.Failure.equals(originalRefreshState)) {
        if (!RefreshState.Failure.equals(refreshState)) {
          parent. finishLoadmore();
        } else {
          addLoadFailureState(parent);
        }
      }
    }
    this.originalRefreshState = refreshState;
  }

  private void addLoadFailureState( final SmartRefreshLayout parent) {
    refreshFooter.getTitleText().setText("加载失败");
  final  ReactContext context = (ReactContext) parent.getContext();
    refreshFooter.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        context
                .getNativeModule(UIManagerModule.class)
                .getEventDispatcher()
                .dispatchEvent(new LoadMoreEvent(parent.getId()));

      }
    });
  }
}
