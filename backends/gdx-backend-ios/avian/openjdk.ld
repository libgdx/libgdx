#
# @(#)mapfile-vers-product	1.19 08/02/12 10:56:37
#

#
# Copyright 2002-2008 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#  
#

# Define public interface.

SUNWprivate_1.1 {
        global:
                # JNI
                JNI_CreateJavaVM;
                JNI_GetCreatedJavaVMs;
                JNI_GetDefaultJavaVMInitArgs;

                # JVM
                JVM_Accept;
                JVM_ActiveProcessorCount;
                JVM_AllocateNewArray;
                JVM_AllocateNewObject;
                JVM_ArrayCopy;
                JVM_AssertionStatusDirectives;
                JVM_Available;
                JVM_Bind;
                JVM_ClassDepth;
                JVM_ClassLoaderDepth;
                JVM_Clone;
                JVM_Close;
                JVM_CX8Field;
                JVM_CompileClass;
                JVM_CompileClasses;
                JVM_CompilerCommand;
                JVM_Connect;
                JVM_ConstantPoolGetClassAt;
                JVM_ConstantPoolGetClassAtIfLoaded;
                JVM_ConstantPoolGetDoubleAt;
                JVM_ConstantPoolGetFieldAt;
                JVM_ConstantPoolGetFieldAtIfLoaded;
                JVM_ConstantPoolGetFloatAt;
                JVM_ConstantPoolGetIntAt;
                JVM_ConstantPoolGetLongAt;
                JVM_ConstantPoolGetMethodAt;
                JVM_ConstantPoolGetMethodAtIfLoaded;
                JVM_ConstantPoolGetMemberRefInfoAt;
                JVM_ConstantPoolGetSize;
                JVM_ConstantPoolGetStringAt;
                JVM_ConstantPoolGetUTF8At;
                JVM_CountStackFrames;
                JVM_CurrentClassLoader;
                JVM_CurrentLoadedClass;
                JVM_CurrentThread;
                JVM_CurrentTimeMillis;
                JVM_DefineClass;
                JVM_DefineClassWithSource;
                JVM_DesiredAssertionStatus;
                JVM_DisableCompiler;
                JVM_DoPrivileged;
                JVM_DTraceGetVersion;
                JVM_DTraceActivate;
                JVM_DTraceIsProbeEnabled;
                JVM_DTraceIsSupported;
                JVM_DTraceDispose;
                JVM_DumpAllStacks;
                JVM_DumpThreads;
                JVM_EnableCompiler;
                JVM_Exit;
                JVM_FillInStackTrace;
                JVM_FindClassFromClass;
                JVM_FindClassFromClassLoader;
                JVM_FindClassFromBootLoader;
                JVM_FindLibraryEntry;
                JVM_FindLoadedClass;
                JVM_FindPrimitiveClass;
                JVM_FindSignal;
                JVM_FreeMemory;
                JVM_GC;
                JVM_GetAllThreads;
                JVM_GetArrayElement;
                JVM_GetArrayLength;
                JVM_GetCPClassNameUTF;
                JVM_GetCPFieldClassNameUTF;
                JVM_GetCPFieldModifiers;
                JVM_GetCPFieldNameUTF;
                JVM_GetCPFieldSignatureUTF;
                JVM_GetCPMethodClassNameUTF;
                JVM_GetCPMethodModifiers;
                JVM_GetCPMethodNameUTF;
                JVM_GetCPMethodSignatureUTF;
                JVM_GetCallerClass;
                JVM_GetClassAccessFlags;
                JVM_GetClassAnnotations;
                JVM_GetClassCPEntriesCount;
                JVM_GetClassCPTypes;
                JVM_GetClassConstantPool;
                JVM_GetClassContext;
                JVM_GetClassDeclaredConstructors;
                JVM_GetClassDeclaredFields;
                JVM_GetClassDeclaredMethods;
                JVM_GetClassFieldsCount;
                JVM_GetClassInterfaces;
                JVM_GetClassLoader;
                JVM_GetClassMethodsCount;
                JVM_GetClassModifiers;
                JVM_GetClassName;
                JVM_GetClassNameUTF;
                JVM_GetClassSignature;
                JVM_GetClassSigners;
                JVM_GetComponentType;
                JVM_GetDeclaredClasses;
                JVM_GetDeclaringClass;
                JVM_GetEnclosingMethodInfo;
                JVM_GetFieldAnnotations;
                JVM_GetFieldIxModifiers;
                JVM_GetHostName;
                JVM_GetInheritedAccessControlContext;
                JVM_GetInterfaceVersion;
                JVM_GetLastErrorString;
                JVM_GetManagement;
                JVM_GetMethodAnnotations;
                JVM_GetMethodDefaultAnnotationValue;
                JVM_GetMethodIxArgsSize;
                JVM_GetMethodIxByteCode;
                JVM_GetMethodIxByteCodeLength;
                JVM_GetMethodIxExceptionIndexes;
                JVM_GetMethodIxExceptionTableEntry;
                JVM_GetMethodIxExceptionTableLength;
                JVM_GetMethodIxExceptionsCount;
                JVM_GetMethodIxLocalsCount;
                JVM_GetMethodIxMaxStack;
                JVM_GetMethodIxModifiers;
                JVM_GetMethodIxNameUTF;
                JVM_GetMethodIxSignatureUTF;
                JVM_GetMethodParameterAnnotations;
                JVM_GetPrimitiveArrayElement;
                JVM_GetProtectionDomain;
                JVM_GetSockName;
                JVM_GetSockOpt;
                JVM_GetStackAccessControlContext;
                JVM_GetStackTraceDepth;
                JVM_GetStackTraceElement;
                JVM_GetSystemPackage;
                JVM_GetSystemPackages;
                JVM_GetThreadStateNames;
                JVM_GetThreadStateValues;
                JVM_GetVersionInfo;
                JVM_Halt;
                JVM_HoldsLock;
                JVM_IHashCode;
                JVM_InitAgentProperties;
                JVM_InitProperties;
                JVM_InitializeCompiler;
                JVM_InitializeSocketLibrary;
                JVM_InternString;
                JVM_Interrupt;
                JVM_InvokeMethod;
                JVM_IsArrayClass;
                JVM_IsConstructorIx;
                JVM_IsInterface;
                JVM_IsInterrupted;
                JVM_IsNaN;
                JVM_IsPrimitiveClass;
                JVM_IsSameClassPackage;
                JVM_IsSilentCompiler;
                JVM_IsSupportedJNIVersion;
                JVM_IsThreadAlive;
                JVM_LatestUserDefinedLoader;
                JVM_Listen;
                JVM_LoadClass0;
                JVM_LoadLibrary;
                JVM_Lseek;
                JVM_MaxObjectInspectionAge;
                JVM_MaxMemory;
                JVM_MonitorNotify;
                JVM_MonitorNotifyAll;
                JVM_MonitorWait;
                JVM_NanoTime;
                JVM_NativePath;
                JVM_NewArray;
                JVM_NewInstanceFromConstructor;
                JVM_NewMultiArray;
                JVM_OnExit;
                JVM_Open;
                JVM_PrintStackTrace;
                JVM_RaiseSignal;
                JVM_RawMonitorCreate;
                JVM_RawMonitorDestroy;
                JVM_RawMonitorEnter;
                JVM_RawMonitorExit;
                JVM_Read;
                JVM_Recv;
                JVM_RecvFrom;
                JVM_RegisterSignal;
                JVM_ReleaseUTF;
                JVM_ResolveClass;
                JVM_ResumeThread;
                JVM_Send;
                JVM_SendTo;
                JVM_SetArrayElement;
                JVM_SetClassSigners;
                JVM_SetLength;
                JVM_SetPrimitiveArrayElement;
                JVM_SetProtectionDomain;
                JVM_SetSockOpt;
                JVM_SetThreadPriority;
                JVM_Sleep;
                JVM_Socket;
                JVM_SocketAvailable;
                JVM_SocketClose;
                JVM_SocketShutdown;
                JVM_StartThread;
                JVM_StopThread;
                JVM_SuspendThread;
                JVM_SupportsCX8;
                JVM_Sync;
                JVM_Timeout;
                JVM_TotalMemory;
                JVM_TraceInstructions;
                JVM_TraceMethodCalls;
                JVM_UnloadLibrary;
                JVM_Write;
                JVM_Yield;
                JVM_handle_linux_signal;

                # Old reflection routines
                # These do not need to be present in the product build in JDK 1.4
                # but their code has not been removed yet because there will not
                # be a substantial code savings until JVM_InvokeMethod and
                # JVM_NewInstanceFromConstructor can also be removed; see
                # reflectionCompat.hpp.
                JVM_GetClassConstructor;
                JVM_GetClassConstructors;
                JVM_GetClassField;
                JVM_GetClassFields;
                JVM_GetClassMethod;
                JVM_GetClassMethods;
                JVM_GetField;
                JVM_GetPrimitiveField;
                JVM_NewInstance;
                JVM_SetField;
                JVM_SetPrimitiveField;

                # Needed for dropping VM into JDK 1.3.x, 1.4
                _JVM_native_threads;
                jdk_sem_init;
                jdk_sem_post;
                jdk_sem_wait;
                jdk_pthread_sigmask;
                jdk_waitpid;

                # miscellaneous functions
                jio_fprintf;
                jio_printf;
                jio_snprintf;
                jio_vfprintf;
                jio_vsnprintf;
                fork1;
                numa_warn;
                numa_error;

                # Needed because there is no JVM interface for this.
                sysThreadAvailableStackWithSlack;

                # This is for Forte Analyzer profiling support.
                AsyncGetCallTrace;
};

