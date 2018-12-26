package nell.jinxian.listview;

import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by Administrator on 2018/12/12.
 */

public class RefreshViewEvent  extends Event<RefreshViewEvent> {
    public RefreshViewEvent(int viewTag) {
        super(viewTag);
    }
  @Override
  public short getCoalescingKey() {
    // All checkbox events for a given view can be coalesced.
    return 0;
  }
    @Override
    public String getEventName() {
        return "topRefresh";
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), null);
    }
}
