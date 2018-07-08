// import React from 'react-native';
//
// const RCTViews = React.NativeModules.RCTViews;
//
// export default {
//   init: () => {
//     return RCTViews.init();
//   },
// };
import {RecyclerViewList, DataSource} from './src/listview';

var Views = {
    RecyclerViewList,
    DataSource,
};

module.exports = Views;