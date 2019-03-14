# 原生ListView

# 1.How to Install


```
  npm install react-native-views --save

  react-native link react-native-views

```

添加资源文件

# 2. How to Use

## Props
| Prop | Type | Note |
|---|---|---|
| data | Array | 数据源
| refreshState | RefreshState | -Idle 闲置状态 <br/>-HeaderRefreshing 下拉刷新中 <br/>-FooterRefreshing 加载更多中 <br/>-NoMoreData 没有更多 |
| renderItem | ListRenderItem | 返回每个item
| reactModuleForCell | string | item的类名，用于优化ios列表性能
| onHeaderRefresh | func | 下拉刷新触发
| onFooterRefresh | func | 上拉加载出发
| ListHeaderComponent | Component | 头布局

## Events
| Event Name | Param | Notes |
|---|---|---|
| scrollToIndex   | number   | 滑动到第几个条目

## Note

```
1. 条目需要添加height属性
2. 需要注册Item：AppRegistry.registerComponent('ItemName', () => ItemName)
3. 如果Item用到TouchableOpacity嵌套TouchableOpacity,需要用 src/pages/RNListView/native-listview/button/button.tsx

```

## Example

```
<NativeListView
  ref={v => this.listview = v}
  data={this.state.data}
  renderItem={this.renderItem.bind(this)}
  refreshState={this.state.refreshState}
  reactModuleForCell="ListViewItem"
  onHeaderRefresh={this.onRefresh.bind(this)}
  onFooterRefresh={this.onLoadMore.bind(this)}
  ListHeaderComponent={<View
      style={{height: 60, backgroundColor: 'pink'}}><Text>我是header</Text></View>}
/>
```

详见:[RNListViewDemo](https://gitlab.x.yunio.com/web-public/react-native-demo/tree/master/src/pages/RNListView/RNListView.tsx)



