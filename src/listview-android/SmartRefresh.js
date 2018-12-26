import React, {Component} from 'react';
import {StyleSheet, requireNativeComponent, NativeModules, View, Image, ToastAndroid} from 'react-native';

const RTCSmartRefreshLayout = requireNativeComponent('RTCSmartRefreshLayout', SmartRefreshLayout)
export default class SmartRefreshLayout extends Component {
    render() {
        return (<RTCSmartRefreshLayout
            {...this.props}
        >
        </RTCSmartRefreshLayout>)
    }
}

