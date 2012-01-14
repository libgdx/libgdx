#import <UIKit/UIKit.h>
#include "vm.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate> {
@private
    UIWindow* m_window;
    gdx::VirtualMachine* m_vm;
}
@end
