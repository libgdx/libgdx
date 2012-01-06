#include <jni.h>
#import <UIKit/UIKit.h>

#define JNIEXPORT __attribute__ ((visibility("default"))) \
  __attribute__ ((used))

JNIEXPORT void JNICALL
Java_com_badlogic_gdx_backends_ios_Hello_drawText(JNIEnv* e, jclass c, jlong peer, jstring text, int x,
                    int y, jdouble size)
{
  const char* chars = (*e)->GetStringUTFChars(e, text, 0);
  NSString* string = [[NSString alloc] initWithUTF8String: chars];
  (*e)->ReleaseStringUTFChars(e, text, chars);

  CGPoint point = CGPointMake(x, y);

  UIFont* font = [UIFont systemFontOfSize: size];

  [[UIColor whiteColor] set];

  [string drawAtPoint:point withFont:font];
}
