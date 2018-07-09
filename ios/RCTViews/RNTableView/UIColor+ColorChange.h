//
//  UIColor+ColorChange.h
//  RNTableView
//
//  Created by nell on 2018/5/18.
//  Copyright © 2018年 Pavlo Aksonov. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIColor (ColorChange)

// 颜色转换：iOS中（以#开头）十六进制的颜色转换为UIColor(RGB)
+ (UIColor *) colorWithHexString: (NSString *)color;

@end
