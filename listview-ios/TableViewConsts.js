import { NativeModules, Platform } from 'react-native'

const RNTableViewConsts = Platform.OS == 'ios' ? NativeModules.RNTableViewManager.Constants : null

export default RNTableViewConsts
