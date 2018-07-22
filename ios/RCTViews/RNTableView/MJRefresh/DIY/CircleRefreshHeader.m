//
//  CircleRefreshHeader.m
//  RCTViews
//
//  Created by Lydia on 2018/7/11.
//  Copyright © 2018年 Atticus White. All rights reserved.
//

#import "CircleRefreshHeader.h"
#import "NSBundle+MJRefresh.h"

@interface CircleRefreshHeader()<CAAnimationDelegate>
@property (weak, nonatomic) UIView *circle;
@property (weak, nonatomic) UIImageView * img1;
@property (weak, nonatomic) UIImageView * img2;
@property (nonatomic, strong) CALayer * image1Layer;
@property (nonatomic, strong) CALayer * image2Layer;

@property (nonatomic) int circleR;
@property (nonatomic) CGFloat biggerRate;
@property (nonatomic) CGFloat imageWidth;

@end

@implementation CircleRefreshHeader

- (void)prepare {
    [super prepare];
    
    self.mj_h = 80;
    _circleR = 14;
    _biggerRate = 2;

    UIImage * image = [UIImage imageWithContentsOfFile:[[NSBundle mj_refreshBundle] pathForResource:@"earth_bg" ofType:@"png"]];
    
    
    
    
    CGFloat realTwoR = _circleR*2*_biggerRate;
    CGFloat scale = _circleR*2/image.size.height;
    self.imageWidth = image.size.width*scale;

    CGFloat colors = 132.0/255.0;
    UIView * circle = [[UIView alloc] init];
    self.circle = circle;
    self.circle.frame = CGRectMake(-image.size.width*scale*0.2, 0, _circleR*2, _circleR*2);
    self.circle.backgroundColor = [UIColor clearColor];
    self.circle.clipsToBounds = YES;
    self.circle.layer.borderWidth = 1;
    self.circle.layer.borderColor = [UIColor colorWithRed: colors green:colors blue:colors alpha:0.5].CGColor;
    self.circle.layer.cornerRadius = self.circle.frame.size.width/2;
    [self addSubview:self.circle];
    
    UIImageView * img1 = [[UIImageView alloc] initWithImage:image];
    self.img1 = img1;
    self.img1.frame = CGRectMake(-self.imageWidth+realTwoR, 0, self.imageWidth, realTwoR);
    self.image1Layer = self.img1.layer;
    [self.circle addSubview:self.img1];
    
    UIImageView * img2 = [[UIImageView alloc] initWithImage:image];
    self.img2 = img2;
    self.img2.frame = CGRectMake(self.img1.frame.origin.x-self.imageWidth, 0, self.imageWidth,realTwoR);
    self.image2Layer = self.img2.layer;
    [self.circle addSubview:self.img2];
}

- (void)placeSubviews {
    [super placeSubviews];
    self.circle.center = CGPointMake(self.mj_w * 0.5, self.mj_h*0.5);
}

- (void)setPullingPercent:(CGFloat)pullingPercent {
    [super setPullingPercent:pullingPercent];
    double percent = pullingPercent > 1 ? 1 : pullingPercent;
    double scale = percent;
    CGPoint c = self.circle.center;
    double d = _circleR * 2;
    double w = d*scale;
    self.circle.frame = CGRectMake(c.x-w/2, c.y-w/2, w, w);
    self.circle.layer.cornerRadius = w/2;
}

- (void)setState:(MJRefreshState)state
{
    MJRefreshCheckState;
    
    switch (state) {
        case MJRefreshStateIdle:
            [self.image1Layer removeAllAnimations];
            [self.image2Layer removeAllAnimations];
            break;
        case MJRefreshStateRefreshing:
        {
            [self image1AddAnimation:true];
            [self image2AddAnimation];
        }
            break;
        default:
            break;
    }
}

- (void)image1AddAnimation:(BOOL)isFirstAdd {
    CABasicAnimation *basicAni1 = [CABasicAnimation animation];
    basicAni1.keyPath = @"position";
    if (isFirstAdd) {
        basicAni1.fromValue = [NSValue valueWithCGPoint:self.img1.center];
        basicAni1.toValue = [NSValue valueWithCGPoint:CGPointMake(self.img1.center.x+self.imageWidth, self.img1.center.y)];
        basicAni1.duration = 15;
    } else {
        basicAni1.fromValue = [NSValue valueWithCGPoint:CGPointMake(self.img1.center.x-self.imageWidth, self.img1.center.y)];
        basicAni1.toValue = [NSValue valueWithCGPoint:CGPointMake(self.img1.center.x+self.imageWidth, self.img1.center.y)];
        basicAni1.duration = 30;
    }
    basicAni1.repeatCount = 1;
    basicAni1.removedOnCompletion = NO;
    basicAni1.fillMode = kCAFillModeForwards;
    basicAni1.delegate = self;
    basicAni1.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionLinear];
    
    [self.image1Layer addAnimation:basicAni1 forKey:@"image1LayerAnimation1"];
}

- (void)image2AddAnimation {
    CABasicAnimation *basicAni2 = [CABasicAnimation animation];
    basicAni2.keyPath = @"position";
    basicAni2.fromValue = [NSValue valueWithCGPoint:self.img2.center];
    basicAni2.toValue = [NSValue valueWithCGPoint:CGPointMake(self.img2.center.x+2*self.imageWidth, self.img2.center.y)];
    basicAni2.duration = 30;
    basicAni2.repeatCount = 1;
    basicAni2.removedOnCompletion = NO;
    basicAni2.fillMode = kCAFillModeForwards;
    basicAni2.delegate = self;
    basicAni2.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionLinear];
    
    [self.image2Layer addAnimation:basicAni2 forKey:@"image2LayerAnimation2"];
}

- (void)animationDidStop:(CAAnimation *)anim finished:(BOOL)flag {
    if (flag) {
        if ([self.image2Layer animationForKey:@"image2LayerAnimation2"] == anim) {
            [self.image2Layer removeAllAnimations];
            [self image2AddAnimation];
        } else {
            [self.image1Layer removeAllAnimations];
            [self image1AddAnimation: false];
        }
    }
}


@end
