package nell.jinxian.listview;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

class OnRefreshEvent extends Event<OnRefreshEvent> {
    public static final String EVENT_NAME = "onRefresh";

    public OnRefreshEvent(int viewTag) {
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
