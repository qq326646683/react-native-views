package nell.jinxian.listview;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class OnLoadMoreEvent extends Event<OnLoadMoreEvent> {
    public static final String EVENT_NAME = "onLoadMore";

    public OnLoadMoreEvent(int viewTag) {
        super(viewTag);
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), EVENT_NAME, Arguments.createMap());
    }
}
