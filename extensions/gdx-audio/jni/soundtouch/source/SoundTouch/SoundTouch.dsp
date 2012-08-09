# Microsoft Developer Studio Project File - Name="SoundTouch" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Static Library" 0x0104

CFG=SoundTouch - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "SoundTouch.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "SoundTouch.mak" CFG="SoundTouch - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "SoundTouch - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "SoundTouch - Win32 Debug" (based on "Win32 (x86) Static Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "SoundTouch - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /YX /FD /c
# ADD CPP /nologo /W3 /GX /Zi /O2 /I "..\..\include" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /YX /FD /c
# ADD BASE RSC /l 0x40b /d "NDEBUG"
# ADD RSC /l 0x40b /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo
# Begin Special Build Tool
SOURCE="$(InputPath)"
PostBuild_Cmds=copy            .\Release\SoundTouch.lib            ..\..\lib\ 
# End Special Build Tool

!ELSEIF  "$(CFG)" == "SoundTouch - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /YX /FD /GZ /c
# ADD CPP /nologo /W3 /Gm /GX /ZI /Od /I "..\..\include" /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /FR /YX /FD /GZ /c
# ADD BASE RSC /l 0x40b /d "_DEBUG"
# ADD RSC /l 0x40b /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo /out:"Debug\SoundTouchD.lib"
# Begin Special Build Tool
SOURCE="$(InputPath)"
PostBuild_Cmds=copy            .\Debug\SoundTouchD.lib            ..\..\lib\ 
# End Special Build Tool

!ENDIF 

# Begin Target

# Name "SoundTouch - Win32 Release"
# Name "SoundTouch - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Group "bpm"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\BPMDetect.cpp
# End Source File
# Begin Source File

SOURCE=.\PeakFinder.cpp
# End Source File
# End Group
# Begin Source File

SOURCE=.\3dnow_win.cpp
# End Source File
# Begin Source File

SOURCE=.\AAFilter.cpp
# End Source File
# Begin Source File

SOURCE=.\cpu_detect_x86_win.cpp
# End Source File
# Begin Source File

SOURCE=.\FIFOSampleBuffer.cpp
# End Source File
# Begin Source File

SOURCE=.\FIRFilter.cpp
# End Source File
# Begin Source File

SOURCE=.\mmx_optimized.cpp
# End Source File
# Begin Source File

SOURCE=.\RateTransposer.cpp
# End Source File
# Begin Source File

SOURCE=.\SoundTouch.cpp
# End Source File
# Begin Source File

SOURCE=.\sse_optimized.cpp
# End Source File
# Begin Source File

SOURCE=.\TDStretch.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\AAFilter.h
# End Source File
# Begin Source File

SOURCE=.\cpu_detect.h
# End Source File
# Begin Source File

SOURCE=..\..\include\FIFOSampleBuffer.h
# End Source File
# Begin Source File

SOURCE=..\..\include\FIFOSamplePipe.h
# End Source File
# Begin Source File

SOURCE=.\FIRFilter.h
# End Source File
# Begin Source File

SOURCE=.\RateTransposer.h
# End Source File
# Begin Source File

SOURCE=..\..\include\SoundTouch.h
# End Source File
# Begin Source File

SOURCE=..\..\include\STTypes.h
# End Source File
# Begin Source File

SOURCE=.\TDStretch.h
# End Source File
# End Group
# End Target
# End Project
