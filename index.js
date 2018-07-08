import React from 'react-native';

const RCTViews = React.NativeModules.RCTViews;

export default {
  init: () => {
    return RCTViews.init();
  },
};
