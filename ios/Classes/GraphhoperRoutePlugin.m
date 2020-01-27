#import "GraphhoperRoutePlugin.h"
#if __has_include(<graphhoper_route/graphhoper_route-Swift.h>)
#import <graphhoper_route/graphhoper_route-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "graphhoper_route-Swift.h"
#endif

@implementation GraphhoperRoutePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftGraphhoperRoutePlugin registerWithRegistrar:registrar];
}
@end
