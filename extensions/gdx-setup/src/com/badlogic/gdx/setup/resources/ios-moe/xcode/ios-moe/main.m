#include <MOE/MOE.h>
#import <Foundation/Foundation.h>

int main(int argc, char *argv[]) {
    const char* jvmParam[2];
    int jvmargc = 0;
    
    NSBundle *mainB = [NSBundle mainBundle];
    NSString *prfrpath = [mainB privateFrameworksPath];
    
    NSString *lp = [NSString stringWithFormat:@"-Djava.library.path=%@", prfrpath];

    const char* tmp[1024];
    
    while (jvmargc < argc) { tmp[jvmargc] = argv[jvmargc]; jvmargc++; };
    tmp[jvmargc++] = [lp UTF8String];
    
    return moevm(jvmargc, (char * const *) tmp);
}
