package nell.jinxian.listview;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.scroll.ScrollEventType;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * View manager for {@link YunioSmartRefreshLayout}.
 */
public class RecyclerViewBackedScrollViewManager extends
  ViewGroupManager<YunioSmartRefreshLayout> {

  public static final String REACT_CLASS = "AndroidRecyclerViewBackedScrollView";
  public static final int COMMAND_NOTIFY_ITEM_RANGE_INSERTED = 1;
  public static final int COMMAND_NOTIFY_ITEM_RANGE_REMOVED = 2;
  public static final int COMMAND_NOTIFY_DATASET_CHANGED = 3;
  public static final int COMMAND_SCROLL_TO_INDEX = 4;
  public static final int COMMAND_NOTIFY_ITEM_MOVED = 5;
  public static final int COMMAND_COMPLETE_REFRESH = 6;
  public static final int COMMAND_COMPLETE_LOADMORE = 7;
  private static final String TAG = "RecyclerViewManager";

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  protected YunioSmartRefreshLayout createViewInstance(ThemedReactContext reactContext) {
    return new YunioSmartRefreshLayout(reactContext);
  }

  @Override
  public void addView(YunioSmartRefreshLayout parent, View child, int index) {
    Log.d(TAG, "addView : " + index);
    Assertions.assertCondition(child instanceof RecyclerViewItemView, "Views attached to RecyclerViewBackedScrollView must be RecyclerViewItemView views.");
    RecyclerViewItemView item = (RecyclerViewItemView) child;
    parent.getRecyclerView().addViewToAdapter(item, index);
  }

  @Override
  public int getChildCount(YunioSmartRefreshLayout parent) {
    Log.d(TAG, "getChildCount : " + parent.getRecyclerView().getChildCountFromAdapter());
    return parent.getRecyclerView().getChildCountFromAdapter();
  }

  @Override
  public View getChildAt(YunioSmartRefreshLayout parent, int index) {
    Log.d(TAG, "getChildAt : " + index);
    return parent.getRecyclerView().getChildAtFromAdapter(index);
  }

  @Override
  public void removeViewAt(YunioSmartRefreshLayout parent, int index) {
    Log.d(TAG, "removeViewAt : " + index);
    parent.getRecyclerView().removeViewFromAdapter(index);
  }

  @ReactProp(name = "itemCount")
  public void setItemCount(YunioSmartRefreshLayout parent, int itemCount) {
    parent.getRecyclerView().setItemCount(itemCount);
    parent.getRecyclerView().getAdapter().notifyDataSetChanged();
  }

  @ReactProp(name = "itemAnimatorEnabled", defaultBoolean = true)
  public void setItemAnimatorEnabled(YunioSmartRefreshLayout parent, boolean enabled) {
    parent.getRecyclerView().setItemAnimatorEnabled(enabled);
  }

  @ReactProp(name = "canRefresh", defaultBoolean = true)
  public void setRefreshEnabled(YunioSmartRefreshLayout parent, boolean enabled) {
    Log.d(TAG, "setRefreshEnabled : " + enabled);
    parent.setEnableRefresh(enabled);
  }

  @ReactProp(name = "canLoadMore", defaultBoolean = true)
  public void setLoadMoreEnabled(YunioSmartRefreshLayout parent, boolean enabled) {
    Log.d(TAG, "setLoadMoreEnabled : " + enabled);
    parent.setEnableLoadmore(enabled);
  }

  @ReactProp(name = "refreshState")
  public void setRefreshState(YunioSmartRefreshLayout parent, String refreshState) {
    parent.setRefreshState(refreshState);
  }

  @Override
  public Map<String, Integer> getCommandsMap() {
    return MapBuilder.of(
      "notifyItemRangeInserted", COMMAND_NOTIFY_ITEM_RANGE_INSERTED,
      "notifyItemRangeRemoved", COMMAND_NOTIFY_ITEM_RANGE_REMOVED,
      "notifyItemMoved", COMMAND_NOTIFY_ITEM_MOVED,
      "notifyDataSetChanged", COMMAND_NOTIFY_DATASET_CHANGED,
      "scrollToIndex", COMMAND_SCROLL_TO_INDEX,
      "completeRefresh", COMMAND_COMPLETE_REFRESH,
      "completeLoadMore", COMMAND_COMPLETE_LOADMORE
    );
  }

  @Override
  public void receiveCommand(
    final YunioSmartRefreshLayout parent,
    int commandType,
    @Nullable ReadableArray args) {
    Assertions.assertNotNull(parent);
    Assertions.assertNotNull(args);
    switch (commandType) {
      case COMMAND_NOTIFY_ITEM_RANGE_INSERTED: {
        final int position = args.getInt(0);
        final int count = args.getInt(1);
        //Log.d(TAG, String.format("notify item range inserted: position %d, count %d", position, count));
        RecyclerViewBackedScrollView.ReactListAdapter adapter = (RecyclerViewBackedScrollView.ReactListAdapter) parent.getRecyclerView().getAdapter();
        adapter.setItemCount(adapter.getItemCount() + count);
        adapter.notifyItemRangeInserted(position, count);
        return;
      }

      case COMMAND_NOTIFY_ITEM_RANGE_REMOVED: {
        final int position = args.getInt(0);
        final int count = args.getInt(1);
        //Log.d(TAG, String.format("notify item range removed: position %d, count %d", position, count));
        RecyclerViewBackedScrollView.ReactListAdapter adapter = (RecyclerViewBackedScrollView.ReactListAdapter) parent.getRecyclerView().getAdapter();
        adapter.setItemCount(adapter.getItemCount() - count);
        adapter.notifyItemRangeRemoved(position, count);
        return;
      }


      case COMMAND_NOTIFY_ITEM_MOVED: {
        final int currentPosition = args.getInt(0);
        final int nextPosition = args.getInt(1);
        RecyclerViewBackedScrollView.ReactListAdapter adapter = (RecyclerViewBackedScrollView.ReactListAdapter) parent.getRecyclerView().getAdapter();
        adapter.notifyItemMoved(currentPosition, nextPosition);
        return;
      }

      case COMMAND_NOTIFY_DATASET_CHANGED: {
        final int itemCount = args.getInt(0);
        RecyclerViewBackedScrollView.ReactListAdapter adapter = (RecyclerViewBackedScrollView.ReactListAdapter) parent.getRecyclerView().getAdapter();
        adapter.setItemCount(itemCount);
        parent.getRecyclerView().getAdapter().notifyDataSetChanged();
        return;
      }

      case COMMAND_SCROLL_TO_INDEX: {
        boolean animated = args.getBoolean(0);
        int index = args.getInt(1) + 1;
        RecyclerViewBackedScrollView.ScrollOptions options = new RecyclerViewBackedScrollView.ScrollOptions();
        options.millisecondsPerInch = args.isNull(2) ? null : (float) args.getDouble(2);
        options.viewPosition = args.isNull(3) ? null : (float) args.getDouble(3);
        options.viewOffset = args.isNull(4) ? null : (float) args.getDouble(4);
        Log.d("RecyclerViewViewManager", "COMMAND_SCROLL_TO_INDEX : " + index);
        Log.d("RecyclerViewViewManager", "getPaddingBottom  : " + parent.getRecyclerView().getPaddingBottom() +
          "getPaddingLeft  : " + parent.getRecyclerView().getPaddingLeft() +
          "getPaddingRight  : " + parent.getRecyclerView().getPaddingRight() +
          "getPaddingTop  : " + parent.getRecyclerView().getPaddingTop());

        if (animated) {
          parent.getRecyclerView().smoothScrollToPosition(index, options);
        } else {
          parent.getRecyclerView().scrollToPosition(index, options);
        }
        return;
      }
      case COMMAND_COMPLETE_REFRESH: {
        parent.finishRefresh();
        return;
      }
      case COMMAND_COMPLETE_LOADMORE: {
        parent.finishLoadmore();
        return;
      }

      default:
        throw new IllegalArgumentException(String.format(
          "Unsupported command %d received by %s.",
          commandType,
          getClass().getSimpleName()));
    }
  }

  @Override
  public
  @Nullable
  Map getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.builder()
      .put(ScrollEventType.SCROLL.getJSEventName(ScrollEventType.SCROLL), MapBuilder.of("registrationName", "onScroll"))
      .put(ContentSizeChangeEvent.EVENT_NAME, MapBuilder.of("registrationName", "onContentSizeChange"))
      .put(VisibleItemsChangeEvent.EVENT_NAME, MapBuilder.of("registrationName", "onVisibleItemsChange"))
      .put(OnRefreshEvent.EVENT_NAME, MapBuilder.of("registrationName", "onRefresh"))
      .put(OnLoadMoreEvent.EVENT_NAME, MapBuilder.of("registrationName", "onLoadMore"))
      .build();
  }
}
