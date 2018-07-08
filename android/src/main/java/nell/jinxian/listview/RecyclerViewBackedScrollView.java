package nell.jinxian.listview;

import android.content.Context;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.SystemClock;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.NativeGestureUtil;
import com.facebook.react.views.scroll.OnScrollDispatchHelper;
import com.facebook.react.views.scroll.ScrollEvent;
import com.facebook.react.views.scroll.ScrollEventType;
import com.facebook.react.views.scroll.VelocityHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps {@link RecyclerView} providing interface similar to `ScrollView.js` where each children
 * will be rendered as a separate {@link RecyclerView} row.
 * <p>
 * Currently supports only vertically positioned item. Views will not be automatically recycled but
 * they will be detached from native view hierarchy when scrolled offscreen.
 * <p>
 * It works by storing all child views in an array within adapter and binding appropriate views to
 * rows when requested.
 */
@VisibleForTesting
public class RecyclerViewBackedScrollView extends RecyclerView {
    private final static String TAG = "RecyclerViewBackedScrol";

    private final OnScrollDispatchHelper mOnScrollDispatchHelper = new OnScrollDispatchHelper();
    private final VelocityHelper mVelocityHelper = new VelocityHelper();

    private RLRefreshView layoutRefresh;
    int refreshHeight = 200;//DisplayUtils.dp2px(getReactContext(),100);
    /**
     * 在被判定为滚动之前用户手指可以移动的最大值
     */
    private int touchSlop;
    private TextView tv;
    private RecyclerView.LayoutParams refreshLayoutParams;
    /**
     * 下拉刷新起始位置
     */
    private float refreshY = -1;

    static class ScrollOptions {
        @Nullable
        Float millisecondsPerInch;
        @Nullable
        Float viewPosition;
        @Nullable
        Float viewOffset;
    }

    /**
     * Simple implementation of {@link ViewHolder} as it's an abstract class. The only thing we need
     * to hold in this implementation is the reference to {@link RecyclableWrapperViewGroup} that
     * is already stored by default.
     */
    private static class ConcreteViewHolder extends ViewHolder {
        public ConcreteViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * View that is going to be used as a cell in {@link RecyclerView}. It's going to be reusable and
     * we will remove/attach views for a certain positions based on the {@code mViews} array stored
     * in the adapter class.
     * <p>
     * This method overrides {@link #onMeasure} and delegates measurements to the child view that has
     * been attached to. This is because instances of {@link RecyclableWrapperViewGroup} are created
     * outside of {@link } and their layout is not managed by that manager
     * as opposed to all the other react-native views. Instead we use dimensions of the child view
     * (dimensions has been set in layouting process) so that size of this view match the size of
     * the view it wraps.
     */
    static class RecyclableWrapperViewGroup extends ViewGroup {

        private ReactListAdapter mAdapter;
        private int mLastMeasuredWidth;
        private int mLastMeasuredHeight;

        public RecyclableWrapperViewGroup(Context context, ReactListAdapter adapter) {
            super(context);
            mAdapter = adapter;
            mLastMeasuredHeight = 10;
            mLastMeasuredWidth = 10;
        }

        private OnLayoutChangeListener mChildLayoutChangeListener = new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int oldHeight = (oldBottom - oldTop);
                int newHeight = (bottom - top);

                if (oldHeight != newHeight) {
                    if (getParent() != null) {
                        requestLayout();
                        getParent().requestLayout();
                    }
                }
            }
        };

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            // This view will only have one child that is managed by the `NativeViewHierarchyManager` and
            // its position and dimensions are set separately. We don't need to handle its layouting here
        }

        @Override
        public void onViewAdded(View child) {
            super.onViewAdded(child);
            child.addOnLayoutChangeListener(mChildLayoutChangeListener);
        }

        @Override
        public void onViewRemoved(View child) {
            super.onViewRemoved(child);
            child.removeOnLayoutChangeListener(mChildLayoutChangeListener);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We override measure spec and use dimensions of the children. Children is a view added
            // from the adapter and always have a correct dimensions specified as they are calculated
            // and set with NativeViewHierarchyManager.
            // In case there is no view attached, we use the last measured dimensions.

            if (getChildCount() > 0) {
                View child = getChildAt(0);
                mLastMeasuredWidth = child.getMeasuredWidth();
                mLastMeasuredHeight = child.getMeasuredHeight();
                setMeasuredDimension(mLastMeasuredWidth, mLastMeasuredHeight);
            } else {
                setMeasuredDimension(mLastMeasuredWidth, mLastMeasuredHeight);
            }
        }

        public ReactListAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Similarly to ReactViewGroup, we return true.
            // In this case it is necessary in order to force the RecyclerView to intercept the touch events,
            // in this way we can exactly know when the drag starts because "onInterceptTouchEvent"
            // of the RecyclerView will return true.
            return true;
        }
    }

    /*package*/  class ReactListAdapter extends Adapter<ConcreteViewHolder> {

        private final List<RecyclerViewItemView> mViews = new ArrayList<>();
        private final RecyclerViewBackedScrollView mScrollView;
        private int mItemCount = 0;

        public ReactListAdapter(RecyclerViewBackedScrollView scrollView) {
            mScrollView = scrollView;
            //setHasStableIds(true);
        }

        public void addView(RecyclerViewItemView child, int index) {
            mViews.add(index, child);

            final int itemIndex = child.getItemIndex();

            notifyItemChanged(itemIndex);
        }

        public void removeViewAt(int index) {
            RecyclerViewItemView child = mViews.get(index);
            if (child != null) {
                mViews.remove(index);
            }
        }

        public int getViewCount() {
            return mViews.size();
        }

        int RefreshHeader = 0;
        int NormalItem = 1;

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return RefreshHeader;
            } else {
                return NormalItem;
            }
        }

        @Override
        public ConcreteViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            if (viewType == RefreshHeader) {
                layoutRefresh = new RLRefreshView(parent.getContext()); //(LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_refresh, null);
                refreshLayoutParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, refreshHeight);
                refreshLayoutParams.topMargin = -refreshHeight + 1;
                layoutRefresh.setLayoutParams(refreshLayoutParams);

                return new ConcreteViewHolder(layoutRefresh);
            } else {
                return new ConcreteViewHolder(new RecyclableWrapperViewGroup(parent.getContext(), this));
            }
        }

        @Override
        public void onBindViewHolder(ConcreteViewHolder holder, int position) {
            if (position != 0) {
                RecyclableWrapperViewGroup vg = (RecyclableWrapperViewGroup) holder.itemView;
                View row = getViewByItemIndex(position);
                if (row != null && row.getParent() != vg) {
                    if (row.getParent() != null) {
                        ((ViewGroup) row.getParent()).removeView(row);
                    }
                    vg.addView(row, 0);
                }
            }
        }

        @Override
        public void onViewRecycled(ConcreteViewHolder holder) {
            super.onViewRecycled(holder);
            if (holder.itemView instanceof RecyclableWrapperViewGroup)
                ((RecyclableWrapperViewGroup) holder.itemView).removeAllViews();
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

        public void setItemCount(int itemCount) {
            this.mItemCount = itemCount;
        }

        public View getView(int index) {
            return mViews.get(index);
        }

        public RecyclerViewItemView getViewByItemIndex(int position) {
            for (int i = 0; i < mViews.size(); i++) {
                if (mViews.get(i).getItemIndex() == position) {
                    return mViews.get(i);
                }
            }

            return null;
        }
    }

    private boolean mDragging;
    private int mFirstVisibleIndex, mLastVisibleIndex;

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mOnScrollDispatchHelper.onScrollChanged(l, t)) {
            getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(ScrollEvent.obtain(
                            getId(),
                            ScrollEventType.SCROLL,
                            0, /* offsetX = 0, horizontal scrolling only */
                            computeVerticalScrollOffset(),
                            mOnScrollDispatchHelper.getXFlingVelocity(),
                            mOnScrollDispatchHelper.getYFlingVelocity(),
                            getWidth(),
                            computeVerticalScrollRange(),
                            getWidth(),
                            getHeight()));
        }

        final int firstIndex = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition() - 1;
        final int lastIndex = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition() - 1;

        if (firstIndex != mFirstVisibleIndex || lastIndex != mLastVisibleIndex) {
            getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(new VisibleItemsChangeEvent(
                            getId(),
                            SystemClock.nanoTime(),
                            firstIndex,
                            lastIndex));

            mFirstVisibleIndex = firstIndex;
            mLastVisibleIndex = lastIndex;
        }
    }

    private ReactContext getReactContext() {
        return (ReactContext) ((ContextThemeWrapper) getContext()).getBaseContext();
    }

    public RecyclerViewBackedScrollView(Context context) {
        super(new ContextThemeWrapper(context, R.style.ScrollbarRecyclerView));
        // 获取判断为滚动之前的最大值
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setHasFixedSize(true);
        ((DefaultItemAnimator) getItemAnimator()).setSupportsChangeAnimations(false);
        setLayoutManager(new LinearLayoutManager(context));
        setAdapter(new ReactListAdapter(this));
    }

    /*package*/ void addViewToAdapter(RecyclerViewItemView child, int index) {
        ((ReactListAdapter) getAdapter()).addView(child, index);
    }

    /*package*/ void removeViewFromAdapter(int index) {
        ((ReactListAdapter) getAdapter()).removeViewAt(index);
    }

    /*package*/ View getChildAtFromAdapter(int index) {
        return ((ReactListAdapter) getAdapter()).getView(index);
    }

    /*package*/ int getChildCountFromAdapter() {
        return ((ReactListAdapter) getAdapter()).getViewCount();
    }

    /*package*/ void setItemCount(int itemCount) {
        ((ReactListAdapter) getAdapter()).setItemCount(itemCount);
    }

    /*package*/ int getItemCount() {
        return getAdapter().getItemCount();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (super.onInterceptTouchEvent(ev)) {
            NativeGestureUtil.notifyNativeGestureStarted(this, ev);
            mDragging = true;
            getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(ScrollEvent.obtain(
                            getId(),
                            ScrollEventType.BEGIN_DRAG,
                            0, /* offsetX = 0, horizontal scrolling only */
                            computeVerticalScrollOffset(),
                            0, // xVelocity
                            0, // yVelocity
                            getWidth(),
                            computeVerticalScrollRange(),
                            getWidth(),
                            getHeight()));
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_UP && mDragging) {
            mDragging = false;
            mVelocityHelper.calculateVelocity(ev);
            getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(ScrollEvent.obtain(
                            getId(),
                            ScrollEventType.END_DRAG,
                            0, /* offsetX = 0, horizontal scrolling only */
                            computeVerticalScrollOffset(),
                            mVelocityHelper.getXVelocity(),
                            mVelocityHelper.getYVelocity(),
                            getWidth(),
                            computeVerticalScrollRange(),
                            getWidth(),
                            getHeight()));
        }

        int firstIndex = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition() - 1;
        Log.e(TAG, "firstIndex:" + firstIndex);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (firstIndex == -1) {
                    if (refreshY == -1) {
                        refreshY = ev.getRawY();
                    }
                    float yMove = ev.getRawY();
                    int distance = (int) (yMove - refreshY);
                    if (distance <= 0 && refreshLayoutParams.topMargin <= refreshHeight) {
                        return super.onTouchEvent(ev);
                    }
                    if (distance < touchSlop) { // 移动距离小于最小响应距离，不做操作
                        return false;
                    }

                    Log.e(TAG, "oldTopMargin:" + refreshLayoutParams.topMargin);
                    Log.e(TAG, "topMargin_distance:" + distance + "  topMargin:" + (distance - refreshHeight));
                    if(layoutRefresh.getStatus() != StatusView.STATUS_LOADING) {
                        if(refreshLayoutParams.topMargin >0) {
                            layoutRefresh.onLoosen();
                        } else {
                            layoutRefresh.onPulling(getPercent(layoutRefresh));
                        }
                        refreshLayoutParams.topMargin = distance - refreshHeight;

                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(layoutRefresh.getStatus() != StatusView.STATUS_LOADING) {
                    if(layoutRefresh.getStatus() == StatusView.STATUS_LOOSEN) {
                        new HideRefreshTask(0, 60).execute();
                        onRefresh();
                    }else if(layoutRefresh.getStatus() == StatusView.STATUS_PULLING) {
                        // 归位
                        new HideRefreshTask(-refreshHeight, 20).execute();
                    }
                }

                refreshY = -1;
                break;
        }
        return super.onTouchEvent(ev);
    }

    private int getPercent(StatusView layoutRefresh) {
        int height = layoutRefresh.getHeight();
        return 100 * (height + refreshLayoutParams.topMargin) / height;
    }

    private void onRefresh() {
        layoutRefresh.onLoading();
        getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                .dispatchEvent(new OnRefreshEvent(getId()));

    }

    class HideRefreshTask extends AsyncTask<Void, Integer, Integer> {
        private int targetTopMargin;
        private int perHeight;
        public HideRefreshTask(int targetTopMargin, int perHeight) {
            this.targetTopMargin = targetTopMargin;
            this.perHeight = perHeight;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int topMargin = refreshLayoutParams.topMargin;
            while (true) {
                topMargin = topMargin - perHeight;
                if (topMargin < targetTopMargin) {
                    topMargin = targetTopMargin;
                    break;
                }
                publishProgress(topMargin);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return topMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... topMargin) {
            Log.e(TAG, "==onProgressUpdate:" + topMargin[0]);
            refreshLayoutParams.topMargin = topMargin[0];
            layoutRefresh.setLayoutParams(refreshLayoutParams);

        }

        @Override
        protected void onPostExecute(Integer topMargin) {
            Log.e(TAG, "==onPostExecute:" + topMargin);

            refreshLayoutParams.topMargin = topMargin + 1;
            layoutRefresh.setLayoutParams(refreshLayoutParams);
        }
    }

    public void onComplete() {
        if (StatusView.STATUS_LOADING == layoutRefresh.getStatus()) {
            layoutRefresh.onFinish();
            new HideRefreshTask(-refreshHeight, 20).execute();
        }
    }

    private boolean mRequestedLayout = false;

    @Override
    public void requestLayout() {
        super.requestLayout();

        if (!mRequestedLayout) {
            mRequestedLayout = true;
            this.post(new Runnable() {
                @Override
                public void run() {
                    mRequestedLayout = false;
                    layout(getLeft(), getTop(), getRight(), getBottom());
                    onLayout(false, getLeft(), getTop(), getRight(), getBottom());
                }
            });
        }
    }

    @Override
    public void scrollToPosition(int position) {
        this.scrollToPosition(position, new ScrollOptions());
    }

    public void scrollToPosition(final int position, final ScrollOptions options) {
        if (options.viewPosition != null) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
            final ReactListAdapter adapter = (ReactListAdapter) getAdapter();
            final View view = adapter.getViewByItemIndex(position);
            if (view != null) {
                final int viewHeight = view.getHeight();

                // In order to calculate the correct offset, we need the height of the target view.
                // If the height of the view is not available it means RN has not calculated it yet.
                // So let's listen to the layout change and we will retry scrolling.
                if (viewHeight == 0) {
                    view.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            view.removeOnLayoutChangeListener(this);
                            scrollToPosition(position, options);
                        }
                    });
                    return;
                }

                final int boxStart = layoutManager.getPaddingTop();
                final int boxEnd = layoutManager.getHeight() - layoutManager.getPaddingBottom();
                final int boxHeight = boxEnd - boxStart;
                float viewOffset = options.viewOffset != null ? PixelUtil.toPixelFromDIP(options.viewOffset) : 0;
                int offset = (int) ((boxHeight - viewHeight) * options.viewPosition + viewOffset);
                layoutManager.scrollToPositionWithOffset(position, offset);
                return;
            }
        }

        super.scrollToPosition(position);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        this.smoothScrollToPosition(position, new ScrollOptions());
    }

    public void smoothScrollToPosition(int position, final ScrollOptions options) {
        final RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this.getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return ((LinearLayoutManager) this.getLayoutManager()).computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                if (options.millisecondsPerInch != null) {
                    return options.millisecondsPerInch / displayMetrics.densityDpi;
                } else {
                    return super.calculateSpeedPerPixel(displayMetrics);
                }
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                int calc = super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, snapPreference);
                if (options.viewPosition != null) {
                    int viewHeight = viewEnd - viewStart;
                    int boxHeight = boxEnd - boxStart;
                    float viewOffset = options.viewOffset != null ? PixelUtil.toPixelFromDIP(options.viewOffset) : 0;
                    float target = boxStart + (boxHeight - viewHeight) * options.viewPosition + viewOffset;
                    return (int) (target - viewStart);
                } else {
                    return super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, snapPreference);
                }
            }
        };

        smoothScroller.setTargetPosition(position);
        this.getLayoutManager().startSmoothScroll(smoothScroller);
    }

    public void setItemAnimatorEnabled(boolean enabled) {
        if (enabled) {
            DefaultItemAnimator animator = new DefaultItemAnimator();
            animator.setSupportsChangeAnimations(false);
            setItemAnimator(animator);
        } else {
            setItemAnimator(null);
        }
    }
}