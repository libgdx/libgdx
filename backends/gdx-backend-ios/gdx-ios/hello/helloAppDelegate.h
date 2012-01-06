#import <UIKit/UIKit.h>
#include <jni.h>

@interface helloAppDelegate : NSObject <UIApplicationDelegate>

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property JavaVM* vm;
@property jobject peer;
@property jmethodID dispose;

@end
