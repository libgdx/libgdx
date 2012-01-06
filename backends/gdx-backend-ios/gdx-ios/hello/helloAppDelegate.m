#include <stdint.h>
#import "helloAppDelegate.h"
#include "OpenGLES/ES2/gl.h"

static JNIEnv*
getEnv(JavaVM* vm)
{
  void* env;
  if ((*vm)->GetEnv(vm, &env, JNI_VERSION_1_2) == JNI_OK) {
    return (JNIEnv*) env;
  } else {
    return 0;
  }
}

@interface HelloView : UIView

@property JavaVM* vm;
@property jobject peer;
@property jmethodID draw;

@end

@implementation HelloView

@synthesize vm = _vm;
@synthesize peer = _peer;
@synthesize draw = _draw;

- (id) initWithFrame: (CGRect) frame
               andVM: (JavaVM*) vm
             andPeer: (jobject) peer
             andDraw: (jmethodID) draw
{
  self = [super initWithFrame: frame];
  if (self) {
    self.vm = vm;
    self.peer = peer;
    self.draw = draw;
  }
  return self;
}

- (void) drawRect: (CGRect) rect
{
  JNIEnv* e = getEnv(self.vm);
  if (e) {
    int x = (int) floor(rect.origin.x);
    int y = (int) floor(rect.origin.y);
    (*e)->CallVoidMethod
      (e, self.peer, self.draw,
       x,
       y,
       ((int) ceil(rect.origin.x + rect.size.width)) - x,
       ((int) ceil(rect.origin.y + rect.size.height)) - y);
  }
}

@end

@implementation helloAppDelegate

@synthesize window = _window;
@synthesize vm = _vm;
@synthesize peer = _peer;
@synthesize dispose = _dispose;

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  JavaVMInitArgs vmArgs;
  vmArgs.version = JNI_VERSION_1_2;
  vmArgs.nOptions = 2;
  vmArgs.ignoreUnrecognized = JNI_TRUE;

  JavaVMOption options[vmArgs.nOptions];
  vmArgs.options = options;

  options[0].optionString = (char*) "-Davian.bootimage=bootimageBin";
  options[1].optionString = (char*) "-Davian.codeimage=codeimageBin";

  JavaVM* vm;
  void* env;
  JNI_CreateJavaVM(&vm, &env, &vmArgs);
  JNIEnv* e = (JNIEnv*) env;

  jclass hello = (*e)->FindClass(e, "com/badlogic/gdx/backends/ios/Hello");
  if (! (*e)->ExceptionCheck(e)) {
    jmethodID constructor = (*e)->GetMethodID(e, hello, "<init>", "(J)V");
    if (! (*e)->ExceptionCheck(e)) {
      jobject peer = (*e)->NewObject
        (e, hello, constructor, (jlong) (uintptr_t) application);
      if (! (*e)->ExceptionCheck(e)) {
        jmethodID draw = (*e)->GetMethodID(e, hello, "draw", "(IIII)V");
        if (! (*e)->ExceptionCheck(e)) {
          self.dispose = (*e)->GetMethodID(e, hello, "dispose", "()V");
          if (! (*e)->ExceptionCheck(e)) {
            HelloView *view =
              [[HelloView alloc]
                initWithFrame: [self.window frame]
                        andVM: vm
                      andPeer: (*e)->NewGlobalRef(e, peer)
                      andDraw: draw];
          
            [self.window addSubview: view];
          
            [self.window makeKeyAndVisible];

            [view release];
          }
        }
      }
    }
  }

  self.vm = vm;

  return (*e)->ExceptionCheck(e) ? NO : YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
     */
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    /*
     Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
     */
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
}

- (void)applicationWillTerminate:(UIApplication *)application
{
  JNIEnv* e = getEnv(self.vm);
  if (e) {
    (*e)->CallVoidMethod(e, self.peer, self.dispose);
  }
}

- (void)dealloc
{
  JNIEnv* e = getEnv(self.vm);
  if (e) {
    (*e)->DeleteGlobalRef(e, self.peer);
  }
  (*self.vm)->DestroyJavaVM(self.vm);
  [_window release];
  [super dealloc];
}

@end
