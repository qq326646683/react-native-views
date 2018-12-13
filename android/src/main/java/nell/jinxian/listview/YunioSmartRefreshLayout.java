package nell.jinxian.listview;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

public class YunioSmartRefreshLayout extends SmartRefreshLayout {
  private RecyclerViewBackedScrollView recyclerView;
  private YunioRefreshHeader refreshHeader;
  private ClassicsFooter refreshFooter;

  private String originalRefreshState;

  public YunioSmartRefreshLayout(Context context) {
    super(context);
    setDisableContentWhenRefresh(true);
    setEnableOverScrollDrag(false);
    refreshHeader = new YunioRefreshHeader(context);
    refreshFooter = new ClassicsFooter(context);
    setRefreshHeader(refreshHeader);
    setRefreshFooter(refreshFooter);
    recyclerView = new RecyclerViewBackedScrollView(context);
    setRefreshContent(recyclerView);
    setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh(RefreshLayout refreshlayout) {
        sendRefrshEvent();
      }
    });

    setOnLoadmoreListener(new OnLoadmoreListener() {
      @Override
      public void onLoadmore(RefreshLayout refreshlayout) {
        sendLoadMoreEvent();
      }
    });
  }

  private void sendRefrshEvent() {
    getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(new OnRefreshEvent(getId()));
  }

  private void sendLoadMoreEvent() {
    getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(new OnLoadMoreEvent(getId()));
  }


  public RecyclerViewBackedScrollView getRecyclerView() {
    return recyclerView;
  }


  private ReactContext getReactContext() {
    return (ReactContext) getContext();
  }

  public void setRefreshState(String refreshState) {
    Log.d("RecyclerViewViewManager", "originalRefreshState : " + originalRefreshState + " , refreshState : " + refreshState);
    //    parent.getRecyclerView().setRefreshState(refreshState);
    //    parent.getRecyclerView().getAdapter().notifyItemChanged(parent.getRecyclerView().getAdapter().getItemCount() - 1);
    refreshFooter.setClickable(false);
    if (!TextUtils.isEmpty(originalRefreshState)) {
      if (RefreshState.HeaderRefreshing.equals(originalRefreshState) && !RefreshState.HeaderRefreshing.equals(refreshState)) {
        finishRefresh();
      } else if (RefreshState.FooterRefreshing.equals(originalRefreshState) && !RefreshState.FooterRefreshing.equals(refreshState)) {
        if (RefreshState.Failure.equals(refreshState)) {//加载失败
          addLoadFailureState();
        } else {
          finishLoadmore();
        }
      } else if (RefreshState.Failure.equals(originalRefreshState)) {
        if (!RefreshState.Failure.equals(refreshState)) {
          finishLoadmore();
        } else {
          addLoadFailureState();
        }
      }
    }
    this.originalRefreshState = refreshState;
  }

  private void addLoadFailureState() {
    refreshFooter.getTitleText().setText("加载失败");
    refreshFooter.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendLoadMoreEvent();
      }
    });
  }
}
