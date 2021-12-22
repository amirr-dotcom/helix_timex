#import "HelixTimexPlugin.h"
#if __has_include(<helix_timex/helix_timex-Swift.h>)
#import <helix_timex/helix_timex-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "helix_timex-Swift.h"
#endif

@implementation HelixTimexPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftHelixTimexPlugin registerWithRegistrar:registrar];
}
@end
