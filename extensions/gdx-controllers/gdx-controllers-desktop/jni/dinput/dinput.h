/****************************************************************************
 *
 *  Copyright (C) 1996-2000 Microsoft Corporation.  All Rights Reserved.
 *
 *  File:       dinput.h
 *  Content:    DirectInput include file
 *
 ****************************************************************************/

#ifndef __DINPUT_INCLUDED__
#define __DINPUT_INCLUDED__

#ifndef DIJ_RINGZERO

#ifdef _WIN32
#define COM_NO_WINDOWS_H
#include <objbase.h>
#endif

#endif /* DIJ_RINGZERO */

#ifdef __cplusplus
extern "C" {
#endif





/*
 *  To build applications for older versions of DirectInput
 *
 *  #define DIRECTINPUT_VERSION [ 0x0300 | 0x0500 | 0x0700 ]
 *
 *  before #include <dinput.h>.  By default, #include <dinput.h>
 *  will produce a DirectX 8-compatible header file.
 *
 */

#define DIRECTINPUT_HEADER_VERSION  0x0800
#ifndef DIRECTINPUT_VERSION
#define DIRECTINPUT_VERSION         DIRECTINPUT_HEADER_VERSION
#pragma message(__FILE__ ": DIRECTINPUT_VERSION undefined. Defaulting to version 0x0800")
#endif

#ifndef DIJ_RINGZERO

/****************************************************************************
 *
 *      Class IDs
 *
 ****************************************************************************/

DEFINE_GUID(CLSID_DirectInput,       0x25E609E0,0xB259,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(CLSID_DirectInputDevice, 0x25E609E1,0xB259,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);

DEFINE_GUID(CLSID_DirectInput8,      0x25E609E4,0xB259,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(CLSID_DirectInputDevice8,0x25E609E5,0xB259,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);

/****************************************************************************
 *
 *      Interfaces
 *
 ****************************************************************************/

DEFINE_GUID(IID_IDirectInputA,     0x89521360,0xAA8A,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(IID_IDirectInputW,     0x89521361,0xAA8A,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(IID_IDirectInput2A,    0x5944E662,0xAA8A,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(IID_IDirectInput2W,    0x5944E663,0xAA8A,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(IID_IDirectInput7A,    0x9A4CB684,0x236D,0x11D3,0x8E,0x9D,0x00,0xC0,0x4F,0x68,0x44,0xAE);
DEFINE_GUID(IID_IDirectInput7W,    0x9A4CB685,0x236D,0x11D3,0x8E,0x9D,0x00,0xC0,0x4F,0x68,0x44,0xAE);
DEFINE_GUID(IID_IDirectInput8A,    0xBF798030,0x483A,0x4DA2,0xAA,0x99,0x5D,0x64,0xED,0x36,0x97,0x00);
DEFINE_GUID(IID_IDirectInput8W,    0xBF798031,0x483A,0x4DA2,0xAA,0x99,0x5D,0x64,0xED,0x36,0x97,0x00);
DEFINE_GUID(IID_IDirectInputDeviceA, 0x5944E680,0xC92E,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(IID_IDirectInputDeviceW, 0x5944E681,0xC92E,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(IID_IDirectInputDevice2A,0x5944E682,0xC92E,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(IID_IDirectInputDevice2W,0x5944E683,0xC92E,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(IID_IDirectInputDevice7A,0x57D7C6BC,0x2356,0x11D3,0x8E,0x9D,0x00,0xC0,0x4F,0x68,0x44,0xAE);
DEFINE_GUID(IID_IDirectInputDevice7W,0x57D7C6BD,0x2356,0x11D3,0x8E,0x9D,0x00,0xC0,0x4F,0x68,0x44,0xAE);
DEFINE_GUID(IID_IDirectInputDevice8A,0x54D41080,0xDC15,0x4833,0xA4,0x1B,0x74,0x8F,0x73,0xA3,0x81,0x79);
DEFINE_GUID(IID_IDirectInputDevice8W,0x54D41081,0xDC15,0x4833,0xA4,0x1B,0x74,0x8F,0x73,0xA3,0x81,0x79);
DEFINE_GUID(IID_IDirectInputEffect,  0xE7E1F7C0,0x88D2,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);

/****************************************************************************
 *
 *      Predefined object types
 *
 ****************************************************************************/

DEFINE_GUID(GUID_XAxis,   0xA36D02E0,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_YAxis,   0xA36D02E1,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_ZAxis,   0xA36D02E2,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_RxAxis,  0xA36D02F4,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_RyAxis,  0xA36D02F5,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_RzAxis,  0xA36D02E3,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_Slider,  0xA36D02E4,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);

DEFINE_GUID(GUID_Button,  0xA36D02F0,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_Key,     0x55728220,0xD33C,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);

DEFINE_GUID(GUID_POV,     0xA36D02F2,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);

DEFINE_GUID(GUID_Unknown, 0xA36D02F3,0xC9F3,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);

/****************************************************************************
 *
 *      Predefined product GUIDs
 *
 ****************************************************************************/

DEFINE_GUID(GUID_SysMouse,   0x6F1D2B60,0xD5A0,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_SysKeyboard,0x6F1D2B61,0xD5A0,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_Joystick   ,0x6F1D2B70,0xD5A0,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_SysMouseEm, 0x6F1D2B80,0xD5A0,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_SysMouseEm2,0x6F1D2B81,0xD5A0,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_SysKeyboardEm, 0x6F1D2B82,0xD5A0,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);
DEFINE_GUID(GUID_SysKeyboardEm2,0x6F1D2B83,0xD5A0,0x11CF,0xBF,0xC7,0x44,0x45,0x53,0x54,0x00,0x00);

/****************************************************************************
 *
 *      Predefined force feedback effects
 *
 ****************************************************************************/

DEFINE_GUID(GUID_ConstantForce, 0x13541C20,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_RampForce,     0x13541C21,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_Square,        0x13541C22,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_Sine,          0x13541C23,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_Triangle,      0x13541C24,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_SawtoothUp,    0x13541C25,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_SawtoothDown,  0x13541C26,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_Spring,        0x13541C27,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_Damper,        0x13541C28,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_Inertia,       0x13541C29,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_Friction,      0x13541C2A,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);
DEFINE_GUID(GUID_CustomForce,   0x13541C2B,0x8E33,0x11D0,0x9A,0xD0,0x00,0xA0,0xC9,0xA0,0x6E,0x35);

#endif /* DIJ_RINGZERO */

/****************************************************************************
 *
 *      Interfaces and Structures...
 *
 ****************************************************************************/

#if(DIRECTINPUT_VERSION >= 0x0500)

/****************************************************************************
 *
 *      IDirectInputEffect
 *
 ****************************************************************************/

#define DIEFT_ALL                   0x00000000

#define DIEFT_CONSTANTFORCE         0x00000001
#define DIEFT_RAMPFORCE             0x00000002
#define DIEFT_PERIODIC              0x00000003
#define DIEFT_CONDITION             0x00000004
#define DIEFT_CUSTOMFORCE           0x00000005
#define DIEFT_HARDWARE              0x000000FF
#define DIEFT_FFATTACK              0x00000200
#define DIEFT_FFFADE                0x00000400
#define DIEFT_SATURATION            0x00000800
#define DIEFT_POSNEGCOEFFICIENTS    0x00001000
#define DIEFT_POSNEGSATURATION      0x00002000
#define DIEFT_DEADBAND              0x00004000
#define DIEFT_STARTDELAY            0x00008000
#define DIEFT_GETTYPE(n)            LOBYTE(n)

#define DI_DEGREES                  100
#define DI_FFNOMINALMAX             10000
#define DI_SECONDS                  1000000

typedef struct DICONSTANTFORCE {
    LONG  lMagnitude;
} DICONSTANTFORCE, *LPDICONSTANTFORCE;
typedef const DICONSTANTFORCE *LPCDICONSTANTFORCE;

typedef struct DIRAMPFORCE {
    LONG  lStart;
    LONG  lEnd;
} DIRAMPFORCE, *LPDIRAMPFORCE;
typedef const DIRAMPFORCE *LPCDIRAMPFORCE;

typedef struct DIPERIODIC {
    DWORD dwMagnitude;
    LONG  lOffset;
    DWORD dwPhase;
    DWORD dwPeriod;
} DIPERIODIC, *LPDIPERIODIC;
typedef const DIPERIODIC *LPCDIPERIODIC;

typedef struct DICONDITION {
    LONG  lOffset;
    LONG  lPositiveCoefficient;
    LONG  lNegativeCoefficient;
    DWORD dwPositiveSaturation;
    DWORD dwNegativeSaturation;
    LONG  lDeadBand;
} DICONDITION, *LPDICONDITION;
typedef const DICONDITION *LPCDICONDITION;

typedef struct DICUSTOMFORCE {
    DWORD cChannels;
    DWORD dwSamplePeriod;
    DWORD cSamples;
    LPLONG rglForceData;
} DICUSTOMFORCE, *LPDICUSTOMFORCE;
typedef const DICUSTOMFORCE *LPCDICUSTOMFORCE;


typedef struct DIENVELOPE {
    DWORD dwSize;                   /* sizeof(DIENVELOPE)   */
    DWORD dwAttackLevel;
    DWORD dwAttackTime;             /* Microseconds         */
    DWORD dwFadeLevel;
    DWORD dwFadeTime;               /* Microseconds         */
} DIENVELOPE, *LPDIENVELOPE;
typedef const DIENVELOPE *LPCDIENVELOPE;


/* This structure is defined for DirectX 5.0 compatibility */
typedef struct DIEFFECT_DX5 {
    DWORD dwSize;                   /* sizeof(DIEFFECT_DX5) */
    DWORD dwFlags;                  /* DIEFF_*              */
    DWORD dwDuration;               /* Microseconds         */
    DWORD dwSamplePeriod;           /* Microseconds         */
    DWORD dwGain;
    DWORD dwTriggerButton;          /* or DIEB_NOTRIGGER    */
    DWORD dwTriggerRepeatInterval;  /* Microseconds         */
    DWORD cAxes;                    /* Number of axes       */
    LPDWORD rgdwAxes;               /* Array of axes        */
    LPLONG rglDirection;            /* Array of directions  */
    LPDIENVELOPE lpEnvelope;        /* Optional             */
    DWORD cbTypeSpecificParams;     /* Size of params       */
    LPVOID lpvTypeSpecificParams;   /* Pointer to params    */
} DIEFFECT_DX5, *LPDIEFFECT_DX5;
typedef const DIEFFECT_DX5 *LPCDIEFFECT_DX5;

typedef struct DIEFFECT {
    DWORD dwSize;                   /* sizeof(DIEFFECT)     */
    DWORD dwFlags;                  /* DIEFF_*              */
    DWORD dwDuration;               /* Microseconds         */
    DWORD dwSamplePeriod;           /* Microseconds         */
    DWORD dwGain;
    DWORD dwTriggerButton;          /* or DIEB_NOTRIGGER    */
    DWORD dwTriggerRepeatInterval;  /* Microseconds         */
    DWORD cAxes;                    /* Number of axes       */
    LPDWORD rgdwAxes;               /* Array of axes        */
    LPLONG rglDirection;            /* Array of directions  */
    LPDIENVELOPE lpEnvelope;        /* Optional             */
    DWORD cbTypeSpecificParams;     /* Size of params       */
    LPVOID lpvTypeSpecificParams;   /* Pointer to params    */
#if(DIRECTINPUT_VERSION >= 0x0600)
    DWORD  dwStartDelay;            /* Microseconds         */
#endif /* DIRECTINPUT_VERSION >= 0x0600 */
} DIEFFECT, *LPDIEFFECT;
typedef DIEFFECT DIEFFECT_DX6;
typedef LPDIEFFECT LPDIEFFECT_DX6;
typedef const DIEFFECT *LPCDIEFFECT;


#if(DIRECTINPUT_VERSION >= 0x0700)
#ifndef DIJ_RINGZERO
typedef struct DIFILEEFFECT{
    DWORD       dwSize;
    GUID        GuidEffect;
    LPCDIEFFECT lpDiEffect;
    CHAR        szFriendlyName[MAX_PATH];
}DIFILEEFFECT, *LPDIFILEEFFECT;
typedef const DIFILEEFFECT *LPCDIFILEEFFECT;
typedef BOOL (FAR PASCAL * LPDIENUMEFFECTSINFILECALLBACK)(LPCDIFILEEFFECT , LPVOID);
#endif /* DIJ_RINGZERO */
#endif /* DIRECTINPUT_VERSION >= 0x0700 */

#define DIEFF_OBJECTIDS             0x00000001
#define DIEFF_OBJECTOFFSETS         0x00000002
#define DIEFF_CARTESIAN             0x00000010
#define DIEFF_POLAR                 0x00000020
#define DIEFF_SPHERICAL             0x00000040

#define DIEP_DURATION               0x00000001
#define DIEP_SAMPLEPERIOD           0x00000002
#define DIEP_GAIN                   0x00000004
#define DIEP_TRIGGERBUTTON          0x00000008
#define DIEP_TRIGGERREPEATINTERVAL  0x00000010
#define DIEP_AXES                   0x00000020
#define DIEP_DIRECTION              0x00000040
#define DIEP_ENVELOPE               0x00000080
#define DIEP_TYPESPECIFICPARAMS     0x00000100
#if(DIRECTINPUT_VERSION >= 0x0600)
#define DIEP_STARTDELAY             0x00000200
#define DIEP_ALLPARAMS_DX5          0x000001FF
#define DIEP_ALLPARAMS              0x000003FF
#else /* DIRECTINPUT_VERSION < 0x0600 */
#define DIEP_ALLPARAMS              0x000001FF
#endif /* DIRECTINPUT_VERSION < 0x0600 */
#define DIEP_START                  0x20000000
#define DIEP_NORESTART              0x40000000
#define DIEP_NODOWNLOAD             0x80000000
#define DIEB_NOTRIGGER              0xFFFFFFFF

#define DIES_SOLO                   0x00000001
#define DIES_NODOWNLOAD             0x80000000

#define DIEGES_PLAYING              0x00000001
#define DIEGES_EMULATED             0x00000002

typedef struct DIEFFESCAPE {
    DWORD   dwSize;
    DWORD   dwCommand;
    LPVOID  lpvInBuffer;
    DWORD   cbInBuffer;
    LPVOID  lpvOutBuffer;
    DWORD   cbOutBuffer;
} DIEFFESCAPE, *LPDIEFFESCAPE;

#ifndef DIJ_RINGZERO

#undef INTERFACE
#define INTERFACE IDirectInputEffect

DECLARE_INTERFACE_(IDirectInputEffect, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputEffect methods ***/
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;
    STDMETHOD(GetEffectGuid)(THIS_ LPGUID) PURE;
    STDMETHOD(GetParameters)(THIS_ LPDIEFFECT,DWORD) PURE;
    STDMETHOD(SetParameters)(THIS_ LPCDIEFFECT,DWORD) PURE;
    STDMETHOD(Start)(THIS_ DWORD,DWORD) PURE;
    STDMETHOD(Stop)(THIS) PURE;
    STDMETHOD(GetEffectStatus)(THIS_ LPDWORD) PURE;
    STDMETHOD(Download)(THIS) PURE;
    STDMETHOD(Unload)(THIS) PURE;
    STDMETHOD(Escape)(THIS_ LPDIEFFESCAPE) PURE;
};

typedef struct IDirectInputEffect *LPDIRECTINPUTEFFECT;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInputEffect_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInputEffect_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInputEffect_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInputEffect_Initialize(p,a,b,c) (p)->lpVtbl->Initialize(p,a,b,c)
#define IDirectInputEffect_GetEffectGuid(p,a) (p)->lpVtbl->GetEffectGuid(p,a)
#define IDirectInputEffect_GetParameters(p,a,b) (p)->lpVtbl->GetParameters(p,a,b)
#define IDirectInputEffect_SetParameters(p,a,b) (p)->lpVtbl->SetParameters(p,a,b)
#define IDirectInputEffect_Start(p,a,b) (p)->lpVtbl->Start(p,a,b)
#define IDirectInputEffect_Stop(p) (p)->lpVtbl->Stop(p)
#define IDirectInputEffect_GetEffectStatus(p,a) (p)->lpVtbl->GetEffectStatus(p,a)
#define IDirectInputEffect_Download(p) (p)->lpVtbl->Download(p)
#define IDirectInputEffect_Unload(p) (p)->lpVtbl->Unload(p)
#define IDirectInputEffect_Escape(p,a) (p)->lpVtbl->Escape(p,a)
#else
#define IDirectInputEffect_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInputEffect_AddRef(p) (p)->AddRef()
#define IDirectInputEffect_Release(p) (p)->Release()
#define IDirectInputEffect_Initialize(p,a,b,c) (p)->Initialize(a,b,c)
#define IDirectInputEffect_GetEffectGuid(p,a) (p)->GetEffectGuid(a)
#define IDirectInputEffect_GetParameters(p,a,b) (p)->GetParameters(a,b)
#define IDirectInputEffect_SetParameters(p,a,b) (p)->SetParameters(a,b)
#define IDirectInputEffect_Start(p,a,b) (p)->Start(a,b)
#define IDirectInputEffect_Stop(p) (p)->Stop()
#define IDirectInputEffect_GetEffectStatus(p,a) (p)->GetEffectStatus(a)
#define IDirectInputEffect_Download(p) (p)->Download()
#define IDirectInputEffect_Unload(p) (p)->Unload()
#define IDirectInputEffect_Escape(p,a) (p)->Escape(a)
#endif

#endif /* DIJ_RINGZERO */

#endif /* DIRECTINPUT_VERSION >= 0x0500 */

/****************************************************************************
 *
 *      IDirectInputDevice
 *
 ****************************************************************************/

#if DIRECTINPUT_VERSION <= 0x700
#define DIDEVTYPE_DEVICE        1
#define DIDEVTYPE_MOUSE         2
#define DIDEVTYPE_KEYBOARD      3
#define DIDEVTYPE_JOYSTICK      4

#else
#define DI8DEVCLASS_ALL             0
#define DI8DEVCLASS_DEVICE          1
#define DI8DEVCLASS_POINTER         2
#define DI8DEVCLASS_KEYBOARD        3
#define DI8DEVCLASS_GAMECTRL        4

#define DI8DEVTYPE_DEVICE           0x11
#define DI8DEVTYPE_MOUSE            0x12
#define DI8DEVTYPE_KEYBOARD         0x13
#define DI8DEVTYPE_JOYSTICK         0x14
#define DI8DEVTYPE_GAMEPAD          0x15
#define DI8DEVTYPE_DRIVING          0x16
#define DI8DEVTYPE_FLIGHT           0x17
#define DI8DEVTYPE_1STPERSON        0x18
#define DI8DEVTYPE_DEVICECTRL       0x19
#define DI8DEVTYPE_SCREENPOINTER    0x1A
#define DI8DEVTYPE_REMOTE           0x1B
#define DI8DEVTYPE_SUPPLEMENTAL     0x1C
#endif /* DIRECTINPUT_VERSION <= 0x700 */

#define DIDEVTYPE_HID           0x00010000

#if DIRECTINPUT_VERSION <= 0x700
#define DIDEVTYPEMOUSE_UNKNOWN          1
#define DIDEVTYPEMOUSE_TRADITIONAL      2
#define DIDEVTYPEMOUSE_FINGERSTICK      3
#define DIDEVTYPEMOUSE_TOUCHPAD         4
#define DIDEVTYPEMOUSE_TRACKBALL        5

#define DIDEVTYPEKEYBOARD_UNKNOWN       0
#define DIDEVTYPEKEYBOARD_PCXT          1
#define DIDEVTYPEKEYBOARD_OLIVETTI      2
#define DIDEVTYPEKEYBOARD_PCAT          3
#define DIDEVTYPEKEYBOARD_PCENH         4
#define DIDEVTYPEKEYBOARD_NOKIA1050     5
#define DIDEVTYPEKEYBOARD_NOKIA9140     6
#define DIDEVTYPEKEYBOARD_NEC98         7
#define DIDEVTYPEKEYBOARD_NEC98LAPTOP   8
#define DIDEVTYPEKEYBOARD_NEC98106      9
#define DIDEVTYPEKEYBOARD_JAPAN106     10
#define DIDEVTYPEKEYBOARD_JAPANAX      11
#define DIDEVTYPEKEYBOARD_J3100        12

#define DIDEVTYPEJOYSTICK_UNKNOWN       1
#define DIDEVTYPEJOYSTICK_TRADITIONAL   2
#define DIDEVTYPEJOYSTICK_FLIGHTSTICK   3
#define DIDEVTYPEJOYSTICK_GAMEPAD       4
#define DIDEVTYPEJOYSTICK_RUDDER        5
#define DIDEVTYPEJOYSTICK_WHEEL         6
#define DIDEVTYPEJOYSTICK_HEADTRACKER   7

#else
#define DI8DEVTYPEMOUSE_UNKNOWN                     1
#define DI8DEVTYPEMOUSE_TRADITIONAL                 2
#define DI8DEVTYPEMOUSE_FINGERSTICK                 3
#define DI8DEVTYPEMOUSE_TOUCHPAD                    4
#define DI8DEVTYPEMOUSE_TRACKBALL                   5
#define DI8DEVTYPEMOUSE_ABSOLUTE                    6

#define DI8DEVTYPEKEYBOARD_UNKNOWN                  0
#define DI8DEVTYPEKEYBOARD_PCXT                     1
#define DI8DEVTYPEKEYBOARD_OLIVETTI                 2
#define DI8DEVTYPEKEYBOARD_PCAT                     3
#define DI8DEVTYPEKEYBOARD_PCENH                    4
#define DI8DEVTYPEKEYBOARD_NOKIA1050                5
#define DI8DEVTYPEKEYBOARD_NOKIA9140                6
#define DI8DEVTYPEKEYBOARD_NEC98                    7
#define DI8DEVTYPEKEYBOARD_NEC98LAPTOP              8
#define DI8DEVTYPEKEYBOARD_NEC98106                 9
#define DI8DEVTYPEKEYBOARD_JAPAN106                10
#define DI8DEVTYPEKEYBOARD_JAPANAX                 11
#define DI8DEVTYPEKEYBOARD_J3100                   12

#define DI8DEVTYPE_LIMITEDGAMESUBTYPE               1

#define DI8DEVTYPEJOYSTICK_LIMITED                  DI8DEVTYPE_LIMITEDGAMESUBTYPE
#define DI8DEVTYPEJOYSTICK_STANDARD                 2

#define DI8DEVTYPEGAMEPAD_LIMITED                   DI8DEVTYPE_LIMITEDGAMESUBTYPE
#define DI8DEVTYPEGAMEPAD_STANDARD                  2
#define DI8DEVTYPEGAMEPAD_TILT                      3

#define DI8DEVTYPEDRIVING_LIMITED                   DI8DEVTYPE_LIMITEDGAMESUBTYPE
#define DI8DEVTYPEDRIVING_COMBINEDPEDALS            2
#define DI8DEVTYPEDRIVING_DUALPEDALS                3
#define DI8DEVTYPEDRIVING_THREEPEDALS               4
#define DI8DEVTYPEDRIVING_HANDHELD                  5

#define DI8DEVTYPEFLIGHT_LIMITED                    DI8DEVTYPE_LIMITEDGAMESUBTYPE
#define DI8DEVTYPEFLIGHT_STICK                      2
#define DI8DEVTYPEFLIGHT_YOKE                       3
#define DI8DEVTYPEFLIGHT_RC                         4

#define DI8DEVTYPE1STPERSON_LIMITED                 DI8DEVTYPE_LIMITEDGAMESUBTYPE
#define DI8DEVTYPE1STPERSON_UNKNOWN                 2
#define DI8DEVTYPE1STPERSON_SIXDOF                  3
#define DI8DEVTYPE1STPERSON_SHOOTER                 4

#define DI8DEVTYPESCREENPTR_UNKNOWN                 2
#define DI8DEVTYPESCREENPTR_LIGHTGUN                3
#define DI8DEVTYPESCREENPTR_LIGHTPEN                4
#define DI8DEVTYPESCREENPTR_TOUCH                   5

#define DI8DEVTYPEREMOTE_UNKNOWN                    2

#define DI8DEVTYPEDEVICECTRL_UNKNOWN                2
#define DI8DEVTYPEDEVICECTRL_COMMSSELECTION         3
#define DI8DEVTYPEDEVICECTRL_COMMSSELECTION_HARDWIRED 4

#define DI8DEVTYPESUPPLEMENTAL_UNKNOWN              2
#define DI8DEVTYPESUPPLEMENTAL_2NDHANDCONTROLLER    3
#define DI8DEVTYPESUPPLEMENTAL_HEADTRACKER          4
#define DI8DEVTYPESUPPLEMENTAL_HANDTRACKER          5
#define DI8DEVTYPESUPPLEMENTAL_SHIFTSTICKGATE       6
#define DI8DEVTYPESUPPLEMENTAL_SHIFTER              7
#define DI8DEVTYPESUPPLEMENTAL_THROTTLE             8
#define DI8DEVTYPESUPPLEMENTAL_SPLITTHROTTLE        9
#define DI8DEVTYPESUPPLEMENTAL_COMBINEDPEDALS      10
#define DI8DEVTYPESUPPLEMENTAL_DUALPEDALS          11
#define DI8DEVTYPESUPPLEMENTAL_THREEPEDALS         12
#define DI8DEVTYPESUPPLEMENTAL_RUDDERPEDALS        13
#endif /* DIRECTINPUT_VERSION <= 0x700 */

#define GET_DIDEVICE_TYPE(dwDevType)    LOBYTE(dwDevType)
#define GET_DIDEVICE_SUBTYPE(dwDevType) HIBYTE(dwDevType)

#if(DIRECTINPUT_VERSION >= 0x0500)
/* This structure is defined for DirectX 3.0 compatibility */
typedef struct DIDEVCAPS_DX3 {
    DWORD   dwSize;
    DWORD   dwFlags;
    DWORD   dwDevType;
    DWORD   dwAxes;
    DWORD   dwButtons;
    DWORD   dwPOVs;
} DIDEVCAPS_DX3, *LPDIDEVCAPS_DX3;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */

typedef struct DIDEVCAPS {
    DWORD   dwSize;
    DWORD   dwFlags;
    DWORD   dwDevType;
    DWORD   dwAxes;
    DWORD   dwButtons;
    DWORD   dwPOVs;
#if(DIRECTINPUT_VERSION >= 0x0500)
    DWORD   dwFFSamplePeriod;
    DWORD   dwFFMinTimeResolution;
    DWORD   dwFirmwareRevision;
    DWORD   dwHardwareRevision;
    DWORD   dwFFDriverVersion;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */
} DIDEVCAPS, *LPDIDEVCAPS;

#define DIDC_ATTACHED           0x00000001
#define DIDC_POLLEDDEVICE       0x00000002
#define DIDC_EMULATED           0x00000004
#define DIDC_POLLEDDATAFORMAT   0x00000008
#if(DIRECTINPUT_VERSION >= 0x0500)
#define DIDC_FORCEFEEDBACK      0x00000100
#define DIDC_FFATTACK           0x00000200
#define DIDC_FFFADE             0x00000400
#define DIDC_SATURATION         0x00000800
#define DIDC_POSNEGCOEFFICIENTS 0x00001000
#define DIDC_POSNEGSATURATION   0x00002000
#define DIDC_DEADBAND           0x00004000
#endif /* DIRECTINPUT_VERSION >= 0x0500 */
#define DIDC_STARTDELAY         0x00008000
#if(DIRECTINPUT_VERSION >= 0x050a)
#define DIDC_ALIAS              0x00010000
#define DIDC_PHANTOM            0x00020000
#endif /* DIRECTINPUT_VERSION >= 0x050a */
#if(DIRECTINPUT_VERSION >= 0x0800)
#define DIDC_HIDDEN             0x00040000
#endif /* DIRECTINPUT_VERSION >= 0x0800 */

#define DIDFT_ALL           0x00000000

#define DIDFT_RELAXIS       0x00000001
#define DIDFT_ABSAXIS       0x00000002
#define DIDFT_AXIS          0x00000003

#define DIDFT_PSHBUTTON     0x00000004
#define DIDFT_TGLBUTTON     0x00000008
#define DIDFT_BUTTON        0x0000000C

#define DIDFT_POV           0x00000010
#define DIDFT_COLLECTION    0x00000040
#define DIDFT_NODATA        0x00000080

#define DIDFT_ANYINSTANCE   0x00FFFF00
#define DIDFT_INSTANCEMASK  DIDFT_ANYINSTANCE
#define DIDFT_MAKEINSTANCE(n) ((WORD)(n) << 8)
#define DIDFT_GETTYPE(n)     LOBYTE(n)
#define DIDFT_GETINSTANCE(n) LOWORD((n) >> 8)
#define DIDFT_FFACTUATOR        0x01000000
#define DIDFT_FFEFFECTTRIGGER   0x02000000
#if(DIRECTINPUT_VERSION >= 0x050a)
#define DIDFT_OUTPUT            0x10000000
#define DIDFT_VENDORDEFINED     0x04000000
#define DIDFT_ALIAS             0x08000000
#endif /* DIRECTINPUT_VERSION >= 0x050a */
#ifndef DIDFT_OPTIONAL
#define DIDFT_OPTIONAL          0x80000000
#endif

#define DIDFT_ENUMCOLLECTION(n) ((WORD)(n) << 8)
#define DIDFT_NOCOLLECTION      0x00FFFF00

#ifndef DIJ_RINGZERO

typedef struct _DIOBJECTDATAFORMAT {
    const GUID *pguid;
    DWORD   dwOfs;
    DWORD   dwType;
    DWORD   dwFlags;
} DIOBJECTDATAFORMAT, *LPDIOBJECTDATAFORMAT;
typedef const DIOBJECTDATAFORMAT *LPCDIOBJECTDATAFORMAT;

typedef struct _DIDATAFORMAT {
    DWORD   dwSize;
    DWORD   dwObjSize;
    DWORD   dwFlags;
    DWORD   dwDataSize;
    DWORD   dwNumObjs;
    LPDIOBJECTDATAFORMAT rgodf;
} DIDATAFORMAT, *LPDIDATAFORMAT;
typedef const DIDATAFORMAT *LPCDIDATAFORMAT;

#define DIDF_ABSAXIS            0x00000001
#define DIDF_RELAXIS            0x00000002

#ifdef __cplusplus
extern "C" {
#endif
extern const DIDATAFORMAT c_dfDIMouse;

#if(DIRECTINPUT_VERSION >= 0x0700)
extern const DIDATAFORMAT c_dfDIMouse2;
#endif /* DIRECTINPUT_VERSION >= 0x0700 */

extern const DIDATAFORMAT c_dfDIKeyboard;

#if(DIRECTINPUT_VERSION >= 0x0500)
extern const DIDATAFORMAT c_dfDIJoystick;
extern const DIDATAFORMAT c_dfDIJoystick2;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */

#ifdef __cplusplus
};
#endif


#if DIRECTINPUT_VERSION > 0x0700

typedef struct _DIACTIONA {
                UINT_PTR    uAppData;
                DWORD       dwSemantic;
    OPTIONAL    DWORD       dwFlags;
    OPTIONAL    union {
                    LPCSTR      lptszActionName;
                    UINT        uResIdString;
                };
    OPTIONAL    GUID        guidInstance;
    OPTIONAL    DWORD       dwObjID;
    OPTIONAL    DWORD       dwHow;
} DIACTIONA, *LPDIACTIONA ;
typedef struct _DIACTIONW {
                UINT_PTR    uAppData;
                DWORD       dwSemantic;
    OPTIONAL    DWORD       dwFlags;
    OPTIONAL    union {
                    LPCWSTR     lptszActionName;
                    UINT        uResIdString;
                };
    OPTIONAL    GUID        guidInstance;
    OPTIONAL    DWORD       dwObjID;
    OPTIONAL    DWORD       dwHow;
} DIACTIONW, *LPDIACTIONW ;
#ifdef UNICODE
typedef DIACTIONW DIACTION;
typedef LPDIACTIONW LPDIACTION;
#else
typedef DIACTIONA DIACTION;
typedef LPDIACTIONA LPDIACTION;
#endif // UNICODE

typedef const DIACTIONA *LPCDIACTIONA;
typedef const DIACTIONW *LPCDIACTIONW;
#ifdef UNICODE
typedef DIACTIONW DIACTION;
typedef LPCDIACTIONW LPCDIACTION;
#else
typedef DIACTIONA DIACTION;
typedef LPCDIACTIONA LPCDIACTION;
#endif // UNICODE
typedef const DIACTION *LPCDIACTION;


#define DIA_FORCEFEEDBACK       0x00000001
#define DIA_APPMAPPED           0x00000002
#define DIA_APPNOMAP            0x00000004
#define DIA_NORANGE             0x00000008
#define DIA_APPFIXED            0x00000010

#define DIAH_UNMAPPED           0x00000000
#define DIAH_USERCONFIG         0x00000001
#define DIAH_APPREQUESTED       0x00000002
#define DIAH_HWAPP              0x00000004
#define DIAH_HWDEFAULT          0x00000008
#define DIAH_DEFAULT            0x00000020
#define DIAH_ERROR              0x80000000

typedef struct _DIACTIONFORMATA {
                DWORD       dwSize;
                DWORD       dwActionSize;
                DWORD       dwDataSize;
                DWORD       dwNumActions;
                LPDIACTIONA rgoAction;
                GUID        guidActionMap;
                DWORD       dwGenre;
                DWORD       dwBufferSize;
    OPTIONAL    LONG        lAxisMin;
    OPTIONAL    LONG        lAxisMax;
    OPTIONAL    HINSTANCE   hInstString;
                FILETIME    ftTimeStamp;
                DWORD       dwCRC;
                CHAR        tszActionMap[MAX_PATH];
} DIACTIONFORMATA, *LPDIACTIONFORMATA;
typedef struct _DIACTIONFORMATW {
                DWORD       dwSize;
                DWORD       dwActionSize;
                DWORD       dwDataSize;
                DWORD       dwNumActions;
                LPDIACTIONW rgoAction;
                GUID        guidActionMap;
                DWORD       dwGenre;
                DWORD       dwBufferSize;
    OPTIONAL    LONG        lAxisMin;
    OPTIONAL    LONG        lAxisMax;
    OPTIONAL    HINSTANCE   hInstString;
                FILETIME    ftTimeStamp;
                DWORD       dwCRC;
                WCHAR       tszActionMap[MAX_PATH];
} DIACTIONFORMATW, *LPDIACTIONFORMATW;
#ifdef UNICODE
typedef DIACTIONFORMATW DIACTIONFORMAT;
typedef LPDIACTIONFORMATW LPDIACTIONFORMAT;
#else
typedef DIACTIONFORMATA DIACTIONFORMAT;
typedef LPDIACTIONFORMATA LPDIACTIONFORMAT;
#endif // UNICODE
typedef const DIACTIONFORMATA *LPCDIACTIONFORMATA;
typedef const DIACTIONFORMATW *LPCDIACTIONFORMATW;
#ifdef UNICODE
typedef DIACTIONFORMATW DIACTIONFORMAT;
typedef LPCDIACTIONFORMATW LPCDIACTIONFORMAT;
#else
typedef DIACTIONFORMATA DIACTIONFORMAT;
typedef LPCDIACTIONFORMATA LPCDIACTIONFORMAT;
#endif // UNICODE
typedef const DIACTIONFORMAT *LPCDIACTIONFORMAT;

#define DIAFTS_NEWDEVICELOW     0xFFFFFFFF
#define DIAFTS_NEWDEVICEHIGH    0xFFFFFFFF
#define DIAFTS_UNUSEDDEVICELOW  0x00000000
#define DIAFTS_UNUSEDDEVICEHIGH 0x00000000

#define DIDBAM_DEFAULT          0x00000000
#define DIDBAM_PRESERVE         0x00000001
#define DIDBAM_INITIALIZE       0x00000002
#define DIDBAM_HWDEFAULTS       0x00000004

#define DIDSAM_DEFAULT          0x00000000
#define DIDSAM_NOUSER           0x00000001
#define DIDSAM_FORCESAVE        0x00000002

#define DICD_DEFAULT            0x00000000
#define DICD_EDIT               0x00000001

/*
 * The following definition is normally defined in d3dtypes.h
 */
#ifndef D3DCOLOR_DEFINED
typedef DWORD D3DCOLOR;
#define D3DCOLOR_DEFINED
#endif

typedef struct _DICOLORSET{
    DWORD dwSize;
    D3DCOLOR cTextFore;
    D3DCOLOR cTextHighlight;
    D3DCOLOR cCalloutLine;
    D3DCOLOR cCalloutHighlight;
    D3DCOLOR cBorder;
    D3DCOLOR cControlFill;
    D3DCOLOR cHighlightFill;
    D3DCOLOR cAreaFill;
} DICOLORSET, *LPDICOLORSET;
typedef const DICOLORSET *LPCDICOLORSET;


typedef struct _DICONFIGUREDEVICESPARAMSA{
     DWORD             dwSize;
     DWORD             dwcUsers;
     LPSTR             lptszUserNames;
     DWORD             dwcFormats;
     LPDIACTIONFORMATA lprgFormats;
     HWND              hwnd;
     DICOLORSET        dics;
     IUnknown FAR *    lpUnkDDSTarget;
} DICONFIGUREDEVICESPARAMSA, *LPDICONFIGUREDEVICESPARAMSA;
typedef struct _DICONFIGUREDEVICESPARAMSW{
     DWORD             dwSize;
     DWORD             dwcUsers;
     LPWSTR            lptszUserNames;
     DWORD             dwcFormats;
     LPDIACTIONFORMATW lprgFormats;
     HWND              hwnd;
     DICOLORSET        dics;
     IUnknown FAR *    lpUnkDDSTarget;
} DICONFIGUREDEVICESPARAMSW, *LPDICONFIGUREDEVICESPARAMSW;
#ifdef UNICODE
typedef DICONFIGUREDEVICESPARAMSW DICONFIGUREDEVICESPARAMS;
typedef LPDICONFIGUREDEVICESPARAMSW LPDICONFIGUREDEVICESPARAMS;
#else
typedef DICONFIGUREDEVICESPARAMSA DICONFIGUREDEVICESPARAMS;
typedef LPDICONFIGUREDEVICESPARAMSA LPDICONFIGUREDEVICESPARAMS;
#endif // UNICODE
typedef const DICONFIGUREDEVICESPARAMSA *LPCDICONFIGUREDEVICESPARAMSA;
typedef const DICONFIGUREDEVICESPARAMSW *LPCDICONFIGUREDEVICESPARAMSW;
#ifdef UNICODE
typedef DICONFIGUREDEVICESPARAMSW DICONFIGUREDEVICESPARAMS;
typedef LPCDICONFIGUREDEVICESPARAMSW LPCDICONFIGUREDEVICESPARAMS;
#else
typedef DICONFIGUREDEVICESPARAMSA DICONFIGUREDEVICESPARAMS;
typedef LPCDICONFIGUREDEVICESPARAMSA LPCDICONFIGUREDEVICESPARAMS;
#endif // UNICODE
typedef const DICONFIGUREDEVICESPARAMS *LPCDICONFIGUREDEVICESPARAMS;


#define DIDIFT_CONFIGURATION    0x00000001
#define DIDIFT_OVERLAY          0x00000002

#define DIDAL_CENTERED      0x00000000
#define DIDAL_LEFTALIGNED   0x00000001
#define DIDAL_RIGHTALIGNED  0x00000002
#define DIDAL_MIDDLE        0x00000000
#define DIDAL_TOPALIGNED    0x00000004
#define DIDAL_BOTTOMALIGNED 0x00000008

typedef struct _DIDEVICEIMAGEINFOA {
    CHAR        tszImagePath[MAX_PATH];
    DWORD       dwFlags; 
    // These are valid if DIDIFT_OVERLAY is present in dwFlags.
    DWORD       dwViewID;      
    RECT        rcOverlay;             
    DWORD       dwObjID;
    DWORD       dwcValidPts;
    POINT       rgptCalloutLine[5];  
    RECT        rcCalloutRect;  
    DWORD       dwTextAlign;     
} DIDEVICEIMAGEINFOA, *LPDIDEVICEIMAGEINFOA;
typedef struct _DIDEVICEIMAGEINFOW {
    WCHAR       tszImagePath[MAX_PATH];
    DWORD       dwFlags; 
    // These are valid if DIDIFT_OVERLAY is present in dwFlags.
    DWORD       dwViewID;      
    RECT        rcOverlay;             
    DWORD       dwObjID;
    DWORD       dwcValidPts;
    POINT       rgptCalloutLine[5];  
    RECT        rcCalloutRect;  
    DWORD       dwTextAlign;     
} DIDEVICEIMAGEINFOW, *LPDIDEVICEIMAGEINFOW;
#ifdef UNICODE
typedef DIDEVICEIMAGEINFOW DIDEVICEIMAGEINFO;
typedef LPDIDEVICEIMAGEINFOW LPDIDEVICEIMAGEINFO;
#else
typedef DIDEVICEIMAGEINFOA DIDEVICEIMAGEINFO;
typedef LPDIDEVICEIMAGEINFOA LPDIDEVICEIMAGEINFO;
#endif // UNICODE
typedef const DIDEVICEIMAGEINFOA *LPCDIDEVICEIMAGEINFOA;
typedef const DIDEVICEIMAGEINFOW *LPCDIDEVICEIMAGEINFOW;
#ifdef UNICODE
typedef DIDEVICEIMAGEINFOW DIDEVICEIMAGEINFO;
typedef LPCDIDEVICEIMAGEINFOW LPCDIDEVICEIMAGEINFO;
#else
typedef DIDEVICEIMAGEINFOA DIDEVICEIMAGEINFO;
typedef LPCDIDEVICEIMAGEINFOA LPCDIDEVICEIMAGEINFO;
#endif // UNICODE
typedef const DIDEVICEIMAGEINFO *LPCDIDEVICEIMAGEINFO;

typedef struct _DIDEVICEIMAGEINFOHEADERA {
    DWORD       dwSize;
    DWORD       dwSizeImageInfo;
    DWORD       dwcViews;
    DWORD       dwcButtons;
    DWORD       dwcAxes;
    DWORD       dwcPOVs;
    DWORD       dwBufferSize;
    DWORD       dwBufferUsed;
    LPDIDEVICEIMAGEINFOA lprgImageInfoArray;
} DIDEVICEIMAGEINFOHEADERA, *LPDIDEVICEIMAGEINFOHEADERA;
typedef struct _DIDEVICEIMAGEINFOHEADERW {
    DWORD       dwSize;
    DWORD       dwSizeImageInfo;
    DWORD       dwcViews;
    DWORD       dwcButtons;
    DWORD       dwcAxes;
    DWORD       dwcPOVs;
    DWORD       dwBufferSize;
    DWORD       dwBufferUsed;
    LPDIDEVICEIMAGEINFOW lprgImageInfoArray;
} DIDEVICEIMAGEINFOHEADERW, *LPDIDEVICEIMAGEINFOHEADERW;
#ifdef UNICODE
typedef DIDEVICEIMAGEINFOHEADERW DIDEVICEIMAGEINFOHEADER;
typedef LPDIDEVICEIMAGEINFOHEADERW LPDIDEVICEIMAGEINFOHEADER;
#else
typedef DIDEVICEIMAGEINFOHEADERA DIDEVICEIMAGEINFOHEADER;
typedef LPDIDEVICEIMAGEINFOHEADERA LPDIDEVICEIMAGEINFOHEADER;
#endif // UNICODE
typedef const DIDEVICEIMAGEINFOHEADERA *LPCDIDEVICEIMAGEINFOHEADERA;
typedef const DIDEVICEIMAGEINFOHEADERW *LPCDIDEVICEIMAGEINFOHEADERW;
#ifdef UNICODE
typedef DIDEVICEIMAGEINFOHEADERW DIDEVICEIMAGEINFOHEADER;
typedef LPCDIDEVICEIMAGEINFOHEADERW LPCDIDEVICEIMAGEINFOHEADER;
#else
typedef DIDEVICEIMAGEINFOHEADERA DIDEVICEIMAGEINFOHEADER;
typedef LPCDIDEVICEIMAGEINFOHEADERA LPCDIDEVICEIMAGEINFOHEADER;
#endif // UNICODE
typedef const DIDEVICEIMAGEINFOHEADER *LPCDIDEVICEIMAGEINFOHEADER;

#endif /* DIRECTINPUT_VERSION > 0x0700 */

#if(DIRECTINPUT_VERSION >= 0x0500)
/* These structures are defined for DirectX 3.0 compatibility */

typedef struct DIDEVICEOBJECTINSTANCE_DX3A {
    DWORD   dwSize;
    GUID    guidType;
    DWORD   dwOfs;
    DWORD   dwType;
    DWORD   dwFlags;
    CHAR    tszName[MAX_PATH];
} DIDEVICEOBJECTINSTANCE_DX3A, *LPDIDEVICEOBJECTINSTANCE_DX3A;
typedef struct DIDEVICEOBJECTINSTANCE_DX3W {
    DWORD   dwSize;
    GUID    guidType;
    DWORD   dwOfs;
    DWORD   dwType;
    DWORD   dwFlags;
    WCHAR   tszName[MAX_PATH];
} DIDEVICEOBJECTINSTANCE_DX3W, *LPDIDEVICEOBJECTINSTANCE_DX3W;
#ifdef UNICODE
typedef DIDEVICEOBJECTINSTANCE_DX3W DIDEVICEOBJECTINSTANCE_DX3;
typedef LPDIDEVICEOBJECTINSTANCE_DX3W LPDIDEVICEOBJECTINSTANCE_DX3;
#else
typedef DIDEVICEOBJECTINSTANCE_DX3A DIDEVICEOBJECTINSTANCE_DX3;
typedef LPDIDEVICEOBJECTINSTANCE_DX3A LPDIDEVICEOBJECTINSTANCE_DX3;
#endif // UNICODE
typedef const DIDEVICEOBJECTINSTANCE_DX3A *LPCDIDEVICEOBJECTINSTANCE_DX3A;
typedef const DIDEVICEOBJECTINSTANCE_DX3W *LPCDIDEVICEOBJECTINSTANCE_DX3W;
typedef const DIDEVICEOBJECTINSTANCE_DX3  *LPCDIDEVICEOBJECTINSTANCE_DX3;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */

typedef struct DIDEVICEOBJECTINSTANCEA {
    DWORD   dwSize;
    GUID    guidType;
    DWORD   dwOfs;
    DWORD   dwType;
    DWORD   dwFlags;
    CHAR    tszName[MAX_PATH];
#if(DIRECTINPUT_VERSION >= 0x0500)
    DWORD   dwFFMaxForce;
    DWORD   dwFFForceResolution;
    WORD    wCollectionNumber;
    WORD    wDesignatorIndex;
    WORD    wUsagePage;
    WORD    wUsage;
    DWORD   dwDimension;
    WORD    wExponent;
    WORD    wReportId;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */
} DIDEVICEOBJECTINSTANCEA, *LPDIDEVICEOBJECTINSTANCEA;
typedef struct DIDEVICEOBJECTINSTANCEW {
    DWORD   dwSize;
    GUID    guidType;
    DWORD   dwOfs;
    DWORD   dwType;
    DWORD   dwFlags;
    WCHAR   tszName[MAX_PATH];
#if(DIRECTINPUT_VERSION >= 0x0500)
    DWORD   dwFFMaxForce;
    DWORD   dwFFForceResolution;
    WORD    wCollectionNumber;
    WORD    wDesignatorIndex;
    WORD    wUsagePage;
    WORD    wUsage;
    DWORD   dwDimension;
    WORD    wExponent;
    WORD    wReportId;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */
} DIDEVICEOBJECTINSTANCEW, *LPDIDEVICEOBJECTINSTANCEW;
#ifdef UNICODE
typedef DIDEVICEOBJECTINSTANCEW DIDEVICEOBJECTINSTANCE;
typedef LPDIDEVICEOBJECTINSTANCEW LPDIDEVICEOBJECTINSTANCE;
#else
typedef DIDEVICEOBJECTINSTANCEA DIDEVICEOBJECTINSTANCE;
typedef LPDIDEVICEOBJECTINSTANCEA LPDIDEVICEOBJECTINSTANCE;
#endif // UNICODE
typedef const DIDEVICEOBJECTINSTANCEA *LPCDIDEVICEOBJECTINSTANCEA;
typedef const DIDEVICEOBJECTINSTANCEW *LPCDIDEVICEOBJECTINSTANCEW;
typedef const DIDEVICEOBJECTINSTANCE  *LPCDIDEVICEOBJECTINSTANCE;

typedef BOOL (FAR PASCAL * LPDIENUMDEVICEOBJECTSCALLBACKA)(LPCDIDEVICEOBJECTINSTANCEA, LPVOID);
typedef BOOL (FAR PASCAL * LPDIENUMDEVICEOBJECTSCALLBACKW)(LPCDIDEVICEOBJECTINSTANCEW, LPVOID);
#ifdef UNICODE
#define LPDIENUMDEVICEOBJECTSCALLBACK  LPDIENUMDEVICEOBJECTSCALLBACKW
#else
#define LPDIENUMDEVICEOBJECTSCALLBACK  LPDIENUMDEVICEOBJECTSCALLBACKA
#endif // !UNICODE

#if(DIRECTINPUT_VERSION >= 0x0500)
#define DIDOI_FFACTUATOR        0x00000001
#define DIDOI_FFEFFECTTRIGGER   0x00000002
#define DIDOI_POLLED            0x00008000
#define DIDOI_ASPECTPOSITION    0x00000100
#define DIDOI_ASPECTVELOCITY    0x00000200
#define DIDOI_ASPECTACCEL       0x00000300
#define DIDOI_ASPECTFORCE       0x00000400
#define DIDOI_ASPECTMASK        0x00000F00
#endif /* DIRECTINPUT_VERSION >= 0x0500 */
#if(DIRECTINPUT_VERSION >= 0x050a)
#define DIDOI_GUIDISUSAGE       0x00010000
#endif /* DIRECTINPUT_VERSION >= 0x050a */

typedef struct DIPROPHEADER {
    DWORD   dwSize;
    DWORD   dwHeaderSize;
    DWORD   dwObj;
    DWORD   dwHow;
} DIPROPHEADER, *LPDIPROPHEADER;
typedef const DIPROPHEADER *LPCDIPROPHEADER;

#define DIPH_DEVICE             0
#define DIPH_BYOFFSET           1
#define DIPH_BYID               2
#if(DIRECTINPUT_VERSION >= 0x050a)
#define DIPH_BYUSAGE            3
#endif /* DIRECTINPUT_VERSION >= 0x050a */

#if(DIRECTINPUT_VERSION >= 0x050a)
#define DIMAKEUSAGEDWORD(UsagePage, Usage) \
                                (DWORD)MAKELONG(Usage, UsagePage)
#endif /* DIRECTINPUT_VERSION >= 0x050a */

typedef struct DIPROPDWORD {
    DIPROPHEADER diph;
    DWORD   dwData;
} DIPROPDWORD, *LPDIPROPDWORD;
typedef const DIPROPDWORD *LPCDIPROPDWORD;

#if(DIRECTINPUT_VERSION >= 0x0800)
typedef struct DIPROPPOINTER {
    DIPROPHEADER diph;
    UINT_PTR uData;
} DIPROPPOINTER, *LPDIPROPPOINTER;
typedef const DIPROPPOINTER *LPCDIPROPPOINTER;
#endif /* DIRECTINPUT_VERSION >= 0x0800 */

typedef struct DIPROPRANGE {
    DIPROPHEADER diph;
    LONG    lMin;
    LONG    lMax;
} DIPROPRANGE, *LPDIPROPRANGE;
typedef const DIPROPRANGE *LPCDIPROPRANGE;

#define DIPROPRANGE_NOMIN       ((LONG)0x80000000)
#define DIPROPRANGE_NOMAX       ((LONG)0x7FFFFFFF)

#if(DIRECTINPUT_VERSION >= 0x050a)
typedef struct DIPROPCAL {
    DIPROPHEADER diph;
    LONG    lMin;
    LONG    lCenter;
    LONG    lMax;
} DIPROPCAL, *LPDIPROPCAL;
typedef const DIPROPCAL *LPCDIPROPCAL;

typedef struct DIPROPCALPOV {
    DIPROPHEADER diph;
    LONG   lMin[5];
    LONG   lMax[5];
} DIPROPCALPOV, *LPDIPROPCALPOV;
typedef const DIPROPCALPOV *LPCDIPROPCALPOV;

typedef struct DIPROPGUIDANDPATH {
    DIPROPHEADER diph;
    GUID    guidClass;
    WCHAR   wszPath[MAX_PATH];
} DIPROPGUIDANDPATH, *LPDIPROPGUIDANDPATH;
typedef const DIPROPGUIDANDPATH *LPCDIPROPGUIDANDPATH;

typedef struct DIPROPSTRING {
    DIPROPHEADER diph;
    WCHAR   wsz[MAX_PATH];
} DIPROPSTRING, *LPDIPROPSTRING;
typedef const DIPROPSTRING *LPCDIPROPSTRING;

#endif /* DIRECTINPUT_VERSION >= 0x050a */

#if(DIRECTINPUT_VERSION >= 0x0800)
#define MAXCPOINTSNUM          8

typedef struct _CPOINT
{
    LONG  lP;     // raw value
    DWORD dwLog;  // logical_value / max_logical_value * 10000
} CPOINT, *PCPOINT;

typedef struct DIPROPCPOINTS {
    DIPROPHEADER diph;
    DWORD  dwCPointsNum;
    CPOINT cp[MAXCPOINTSNUM];
} DIPROPCPOINTS, *LPDIPROPCPOINTS;
typedef const DIPROPCPOINTS *LPCDIPROPCPOINTS;
#endif /* DIRECTINPUT_VERSION >= 0x0800 */


#ifdef __cplusplus
#define MAKEDIPROP(prop)    (*(const GUID *)(prop))
#else
#define MAKEDIPROP(prop)    ((REFGUID)(prop))
#endif

#define DIPROP_BUFFERSIZE       MAKEDIPROP(1)

#define DIPROP_AXISMODE         MAKEDIPROP(2)

#define DIPROPAXISMODE_ABS      0
#define DIPROPAXISMODE_REL      1

#define DIPROP_GRANULARITY      MAKEDIPROP(3)

#define DIPROP_RANGE            MAKEDIPROP(4)

#define DIPROP_DEADZONE         MAKEDIPROP(5)

#define DIPROP_SATURATION       MAKEDIPROP(6)

#define DIPROP_FFGAIN           MAKEDIPROP(7)

#define DIPROP_FFLOAD           MAKEDIPROP(8)

#define DIPROP_AUTOCENTER       MAKEDIPROP(9)

#define DIPROPAUTOCENTER_OFF    0
#define DIPROPAUTOCENTER_ON     1

#define DIPROP_CALIBRATIONMODE  MAKEDIPROP(10)

#define DIPROPCALIBRATIONMODE_COOKED    0
#define DIPROPCALIBRATIONMODE_RAW       1

#if(DIRECTINPUT_VERSION >= 0x050a)
#define DIPROP_CALIBRATION      MAKEDIPROP(11)

#define DIPROP_GUIDANDPATH      MAKEDIPROP(12)

#define DIPROP_INSTANCENAME     MAKEDIPROP(13)

#define DIPROP_PRODUCTNAME      MAKEDIPROP(14)
#endif /* DIRECTINPUT_VERSION >= 0x050a */

#if(DIRECTINPUT_VERSION >= 0x05b2)
#define DIPROP_JOYSTICKID       MAKEDIPROP(15)

#define DIPROP_GETPORTDISPLAYNAME       MAKEDIPROP(16)

#endif /* DIRECTINPUT_VERSION >= 0x05b2 */

#if(DIRECTINPUT_VERSION >= 0x0700)
#define DIPROP_PHYSICALRANGE            MAKEDIPROP(18)

#define DIPROP_LOGICALRANGE             MAKEDIPROP(19)
#endif /* DIRECTINPUT_VERSION >= 0x0700 */

#if(DIRECTINPUT_VERSION >= 0x0800)
#define DIPROP_KEYNAME                     MAKEDIPROP(20)

#define DIPROP_CPOINTS                 MAKEDIPROP(21)

#define DIPROP_APPDATA       MAKEDIPROP(22)

#define DIPROP_SCANCODE      MAKEDIPROP(23)

#define DIPROP_VIDPID           MAKEDIPROP(24)

#define DIPROP_USERNAME         MAKEDIPROP(25)

#define DIPROP_TYPENAME         MAKEDIPROP(26)
#endif /* DIRECTINPUT_VERSION >= 0x0800 */


typedef struct DIDEVICEOBJECTDATA_DX3 {
    DWORD       dwOfs;
    DWORD       dwData;
    DWORD       dwTimeStamp;
    DWORD       dwSequence;
} DIDEVICEOBJECTDATA_DX3, *LPDIDEVICEOBJECTDATA_DX3;
typedef const DIDEVICEOBJECTDATA_DX3 *LPCDIDEVICEOBJECTDATA_DX;

typedef struct DIDEVICEOBJECTDATA {
    DWORD       dwOfs;
    DWORD       dwData;
    DWORD       dwTimeStamp;
    DWORD       dwSequence;
#if(DIRECTINPUT_VERSION >= 0x0800)
    UINT_PTR    uAppData;
#endif /* DIRECTINPUT_VERSION >= 0x0800 */
} DIDEVICEOBJECTDATA, *LPDIDEVICEOBJECTDATA;
typedef const DIDEVICEOBJECTDATA *LPCDIDEVICEOBJECTDATA;

#define DIGDD_PEEK          0x00000001

#define DISEQUENCE_COMPARE(dwSequence1, cmp, dwSequence2) \
                        ((int)((dwSequence1) - (dwSequence2)) cmp 0)
#define DISCL_EXCLUSIVE     0x00000001
#define DISCL_NONEXCLUSIVE  0x00000002
#define DISCL_FOREGROUND    0x00000004
#define DISCL_BACKGROUND    0x00000008
#define DISCL_NOWINKEY      0x00000010

#if(DIRECTINPUT_VERSION >= 0x0500)
/* These structures are defined for DirectX 3.0 compatibility */

typedef struct DIDEVICEINSTANCE_DX3A {
    DWORD   dwSize;
    GUID    guidInstance;
    GUID    guidProduct;
    DWORD   dwDevType;
    CHAR    tszInstanceName[MAX_PATH];
    CHAR    tszProductName[MAX_PATH];
} DIDEVICEINSTANCE_DX3A, *LPDIDEVICEINSTANCE_DX3A;
typedef struct DIDEVICEINSTANCE_DX3W {
    DWORD   dwSize;
    GUID    guidInstance;
    GUID    guidProduct;
    DWORD   dwDevType;
    WCHAR   tszInstanceName[MAX_PATH];
    WCHAR   tszProductName[MAX_PATH];
} DIDEVICEINSTANCE_DX3W, *LPDIDEVICEINSTANCE_DX3W;
#ifdef UNICODE
typedef DIDEVICEINSTANCE_DX3W DIDEVICEINSTANCE_DX3;
typedef LPDIDEVICEINSTANCE_DX3W LPDIDEVICEINSTANCE_DX3;
#else
typedef DIDEVICEINSTANCE_DX3A DIDEVICEINSTANCE_DX3;
typedef LPDIDEVICEINSTANCE_DX3A LPDIDEVICEINSTANCE_DX3;
#endif // UNICODE
typedef const DIDEVICEINSTANCE_DX3A *LPCDIDEVICEINSTANCE_DX3A;
typedef const DIDEVICEINSTANCE_DX3W *LPCDIDEVICEINSTANCE_DX3W;
typedef const DIDEVICEINSTANCE_DX3  *LPCDIDEVICEINSTANCE_DX3;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */

typedef struct DIDEVICEINSTANCEA {
    DWORD   dwSize;
    GUID    guidInstance;
    GUID    guidProduct;
    DWORD   dwDevType;
    CHAR    tszInstanceName[MAX_PATH];
    CHAR    tszProductName[MAX_PATH];
#if(DIRECTINPUT_VERSION >= 0x0500)
    GUID    guidFFDriver;
    WORD    wUsagePage;
    WORD    wUsage;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */
} DIDEVICEINSTANCEA, *LPDIDEVICEINSTANCEA;
typedef struct DIDEVICEINSTANCEW {
    DWORD   dwSize;
    GUID    guidInstance;
    GUID    guidProduct;
    DWORD   dwDevType;
    WCHAR   tszInstanceName[MAX_PATH];
    WCHAR   tszProductName[MAX_PATH];
#if(DIRECTINPUT_VERSION >= 0x0500)
    GUID    guidFFDriver;
    WORD    wUsagePage;
    WORD    wUsage;
#endif /* DIRECTINPUT_VERSION >= 0x0500 */
} DIDEVICEINSTANCEW, *LPDIDEVICEINSTANCEW;
#ifdef UNICODE
typedef DIDEVICEINSTANCEW DIDEVICEINSTANCE;
typedef LPDIDEVICEINSTANCEW LPDIDEVICEINSTANCE;
#else
typedef DIDEVICEINSTANCEA DIDEVICEINSTANCE;
typedef LPDIDEVICEINSTANCEA LPDIDEVICEINSTANCE;
#endif // UNICODE

typedef const DIDEVICEINSTANCEA *LPCDIDEVICEINSTANCEA;
typedef const DIDEVICEINSTANCEW *LPCDIDEVICEINSTANCEW;
#ifdef UNICODE
typedef DIDEVICEINSTANCEW DIDEVICEINSTANCE;
typedef LPCDIDEVICEINSTANCEW LPCDIDEVICEINSTANCE;
#else
typedef DIDEVICEINSTANCEA DIDEVICEINSTANCE;
typedef LPCDIDEVICEINSTANCEA LPCDIDEVICEINSTANCE;
#endif // UNICODE
typedef const DIDEVICEINSTANCE  *LPCDIDEVICEINSTANCE;

#undef INTERFACE
#define INTERFACE IDirectInputDeviceW

DECLARE_INTERFACE_(IDirectInputDeviceW, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputDeviceW methods ***/
    STDMETHOD(GetCapabilities)(THIS_ LPDIDEVCAPS) PURE;
    STDMETHOD(EnumObjects)(THIS_ LPDIENUMDEVICEOBJECTSCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetProperty)(THIS_ REFGUID,LPDIPROPHEADER) PURE;
    STDMETHOD(SetProperty)(THIS_ REFGUID,LPCDIPROPHEADER) PURE;
    STDMETHOD(Acquire)(THIS) PURE;
    STDMETHOD(Unacquire)(THIS) PURE;
    STDMETHOD(GetDeviceState)(THIS_ DWORD,LPVOID) PURE;
    STDMETHOD(GetDeviceData)(THIS_ DWORD,LPDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(SetDataFormat)(THIS_ LPCDIDATAFORMAT) PURE;
    STDMETHOD(SetEventNotification)(THIS_ HANDLE) PURE;
    STDMETHOD(SetCooperativeLevel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(GetObjectInfo)(THIS_ LPDIDEVICEOBJECTINSTANCEW,DWORD,DWORD) PURE;
    STDMETHOD(GetDeviceInfo)(THIS_ LPDIDEVICEINSTANCEW) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;
};

typedef struct IDirectInputDeviceW *LPDIRECTINPUTDEVICEW;

#undef INTERFACE
#define INTERFACE IDirectInputDeviceA

DECLARE_INTERFACE_(IDirectInputDeviceA, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputDeviceA methods ***/
    STDMETHOD(GetCapabilities)(THIS_ LPDIDEVCAPS) PURE;
    STDMETHOD(EnumObjects)(THIS_ LPDIENUMDEVICEOBJECTSCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetProperty)(THIS_ REFGUID,LPDIPROPHEADER) PURE;
    STDMETHOD(SetProperty)(THIS_ REFGUID,LPCDIPROPHEADER) PURE;
    STDMETHOD(Acquire)(THIS) PURE;
    STDMETHOD(Unacquire)(THIS) PURE;
    STDMETHOD(GetDeviceState)(THIS_ DWORD,LPVOID) PURE;
    STDMETHOD(GetDeviceData)(THIS_ DWORD,LPDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(SetDataFormat)(THIS_ LPCDIDATAFORMAT) PURE;
    STDMETHOD(SetEventNotification)(THIS_ HANDLE) PURE;
    STDMETHOD(SetCooperativeLevel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(GetObjectInfo)(THIS_ LPDIDEVICEOBJECTINSTANCEA,DWORD,DWORD) PURE;
    STDMETHOD(GetDeviceInfo)(THIS_ LPDIDEVICEINSTANCEA) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;
};

typedef struct IDirectInputDeviceA *LPDIRECTINPUTDEVICEA;

#ifdef UNICODE
#define IID_IDirectInputDevice IID_IDirectInputDeviceW
#define IDirectInputDevice IDirectInputDeviceW
#define IDirectInputDeviceVtbl IDirectInputDeviceWVtbl
#else
#define IID_IDirectInputDevice IID_IDirectInputDeviceA
#define IDirectInputDevice IDirectInputDeviceA
#define IDirectInputDeviceVtbl IDirectInputDeviceAVtbl
#endif
typedef struct IDirectInputDevice *LPDIRECTINPUTDEVICE;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInputDevice_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInputDevice_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInputDevice_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInputDevice_GetCapabilities(p,a) (p)->lpVtbl->GetCapabilities(p,a)
#define IDirectInputDevice_EnumObjects(p,a,b,c) (p)->lpVtbl->EnumObjects(p,a,b,c)
#define IDirectInputDevice_GetProperty(p,a,b) (p)->lpVtbl->GetProperty(p,a,b)
#define IDirectInputDevice_SetProperty(p,a,b) (p)->lpVtbl->SetProperty(p,a,b)
#define IDirectInputDevice_Acquire(p) (p)->lpVtbl->Acquire(p)
#define IDirectInputDevice_Unacquire(p) (p)->lpVtbl->Unacquire(p)
#define IDirectInputDevice_GetDeviceState(p,a,b) (p)->lpVtbl->GetDeviceState(p,a,b)
#define IDirectInputDevice_GetDeviceData(p,a,b,c,d) (p)->lpVtbl->GetDeviceData(p,a,b,c,d)
#define IDirectInputDevice_SetDataFormat(p,a) (p)->lpVtbl->SetDataFormat(p,a)
#define IDirectInputDevice_SetEventNotification(p,a) (p)->lpVtbl->SetEventNotification(p,a)
#define IDirectInputDevice_SetCooperativeLevel(p,a,b) (p)->lpVtbl->SetCooperativeLevel(p,a,b)
#define IDirectInputDevice_GetObjectInfo(p,a,b,c) (p)->lpVtbl->GetObjectInfo(p,a,b,c)
#define IDirectInputDevice_GetDeviceInfo(p,a) (p)->lpVtbl->GetDeviceInfo(p,a)
#define IDirectInputDevice_RunControlPanel(p,a,b) (p)->lpVtbl->RunControlPanel(p,a,b)
#define IDirectInputDevice_Initialize(p,a,b,c) (p)->lpVtbl->Initialize(p,a,b,c)
#else
#define IDirectInputDevice_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInputDevice_AddRef(p) (p)->AddRef()
#define IDirectInputDevice_Release(p) (p)->Release()
#define IDirectInputDevice_GetCapabilities(p,a) (p)->GetCapabilities(a)
#define IDirectInputDevice_EnumObjects(p,a,b,c) (p)->EnumObjects(a,b,c)
#define IDirectInputDevice_GetProperty(p,a,b) (p)->GetProperty(a,b)
#define IDirectInputDevice_SetProperty(p,a,b) (p)->SetProperty(a,b)
#define IDirectInputDevice_Acquire(p) (p)->Acquire()
#define IDirectInputDevice_Unacquire(p) (p)->Unacquire()
#define IDirectInputDevice_GetDeviceState(p,a,b) (p)->GetDeviceState(a,b)
#define IDirectInputDevice_GetDeviceData(p,a,b,c,d) (p)->GetDeviceData(a,b,c,d)
#define IDirectInputDevice_SetDataFormat(p,a) (p)->SetDataFormat(a)
#define IDirectInputDevice_SetEventNotification(p,a) (p)->SetEventNotification(a)
#define IDirectInputDevice_SetCooperativeLevel(p,a,b) (p)->SetCooperativeLevel(a,b)
#define IDirectInputDevice_GetObjectInfo(p,a,b,c) (p)->GetObjectInfo(a,b,c)
#define IDirectInputDevice_GetDeviceInfo(p,a) (p)->GetDeviceInfo(a)
#define IDirectInputDevice_RunControlPanel(p,a,b) (p)->RunControlPanel(a,b)
#define IDirectInputDevice_Initialize(p,a,b,c) (p)->Initialize(a,b,c)
#endif

#endif /* DIJ_RINGZERO */


#if(DIRECTINPUT_VERSION >= 0x0500)

#define DISFFC_RESET            0x00000001
#define DISFFC_STOPALL          0x00000002
#define DISFFC_PAUSE            0x00000004
#define DISFFC_CONTINUE         0x00000008
#define DISFFC_SETACTUATORSON   0x00000010
#define DISFFC_SETACTUATORSOFF  0x00000020

#define DIGFFS_EMPTY            0x00000001
#define DIGFFS_STOPPED          0x00000002
#define DIGFFS_PAUSED           0x00000004
#define DIGFFS_ACTUATORSON      0x00000010
#define DIGFFS_ACTUATORSOFF     0x00000020
#define DIGFFS_POWERON          0x00000040
#define DIGFFS_POWEROFF         0x00000080
#define DIGFFS_SAFETYSWITCHON   0x00000100
#define DIGFFS_SAFETYSWITCHOFF  0x00000200
#define DIGFFS_USERFFSWITCHON   0x00000400
#define DIGFFS_USERFFSWITCHOFF  0x00000800
#define DIGFFS_DEVICELOST       0x80000000

#ifndef DIJ_RINGZERO

typedef struct DIEFFECTINFOA {
    DWORD   dwSize;
    GUID    guid;
    DWORD   dwEffType;
    DWORD   dwStaticParams;
    DWORD   dwDynamicParams;
    CHAR    tszName[MAX_PATH];
} DIEFFECTINFOA, *LPDIEFFECTINFOA;
typedef struct DIEFFECTINFOW {
    DWORD   dwSize;
    GUID    guid;
    DWORD   dwEffType;
    DWORD   dwStaticParams;
    DWORD   dwDynamicParams;
    WCHAR   tszName[MAX_PATH];
} DIEFFECTINFOW, *LPDIEFFECTINFOW;
#ifdef UNICODE
typedef DIEFFECTINFOW DIEFFECTINFO;
typedef LPDIEFFECTINFOW LPDIEFFECTINFO;
#else
typedef DIEFFECTINFOA DIEFFECTINFO;
typedef LPDIEFFECTINFOA LPDIEFFECTINFO;
#endif // UNICODE
typedef const DIEFFECTINFOA *LPCDIEFFECTINFOA;
typedef const DIEFFECTINFOW *LPCDIEFFECTINFOW;
typedef const DIEFFECTINFO  *LPCDIEFFECTINFO;

#define DISDD_CONTINUE          0x00000001

typedef BOOL (FAR PASCAL * LPDIENUMEFFECTSCALLBACKA)(LPCDIEFFECTINFOA, LPVOID);
typedef BOOL (FAR PASCAL * LPDIENUMEFFECTSCALLBACKW)(LPCDIEFFECTINFOW, LPVOID);
#ifdef UNICODE
#define LPDIENUMEFFECTSCALLBACK  LPDIENUMEFFECTSCALLBACKW
#else
#define LPDIENUMEFFECTSCALLBACK  LPDIENUMEFFECTSCALLBACKA
#endif // !UNICODE
typedef BOOL (FAR PASCAL * LPDIENUMCREATEDEFFECTOBJECTSCALLBACK)(LPDIRECTINPUTEFFECT, LPVOID);

#undef INTERFACE
#define INTERFACE IDirectInputDevice2W

DECLARE_INTERFACE_(IDirectInputDevice2W, IDirectInputDeviceW)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputDeviceW methods ***/
    STDMETHOD(GetCapabilities)(THIS_ LPDIDEVCAPS) PURE;
    STDMETHOD(EnumObjects)(THIS_ LPDIENUMDEVICEOBJECTSCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetProperty)(THIS_ REFGUID,LPDIPROPHEADER) PURE;
    STDMETHOD(SetProperty)(THIS_ REFGUID,LPCDIPROPHEADER) PURE;
    STDMETHOD(Acquire)(THIS) PURE;
    STDMETHOD(Unacquire)(THIS) PURE;
    STDMETHOD(GetDeviceState)(THIS_ DWORD,LPVOID) PURE;
    STDMETHOD(GetDeviceData)(THIS_ DWORD,LPDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(SetDataFormat)(THIS_ LPCDIDATAFORMAT) PURE;
    STDMETHOD(SetEventNotification)(THIS_ HANDLE) PURE;
    STDMETHOD(SetCooperativeLevel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(GetObjectInfo)(THIS_ LPDIDEVICEOBJECTINSTANCEW,DWORD,DWORD) PURE;
    STDMETHOD(GetDeviceInfo)(THIS_ LPDIDEVICEINSTANCEW) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;

    /*** IDirectInputDevice2W methods ***/
    STDMETHOD(CreateEffect)(THIS_ REFGUID,LPCDIEFFECT,LPDIRECTINPUTEFFECT *,LPUNKNOWN) PURE;
    STDMETHOD(EnumEffects)(THIS_ LPDIENUMEFFECTSCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetEffectInfo)(THIS_ LPDIEFFECTINFOW,REFGUID) PURE;
    STDMETHOD(GetForceFeedbackState)(THIS_ LPDWORD) PURE;
    STDMETHOD(SendForceFeedbackCommand)(THIS_ DWORD) PURE;
    STDMETHOD(EnumCreatedEffectObjects)(THIS_ LPDIENUMCREATEDEFFECTOBJECTSCALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(Escape)(THIS_ LPDIEFFESCAPE) PURE;
    STDMETHOD(Poll)(THIS) PURE;
    STDMETHOD(SendDeviceData)(THIS_ DWORD,LPCDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
};

typedef struct IDirectInputDevice2W *LPDIRECTINPUTDEVICE2W;

#undef INTERFACE
#define INTERFACE IDirectInputDevice2A

DECLARE_INTERFACE_(IDirectInputDevice2A, IDirectInputDeviceA)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputDeviceA methods ***/
    STDMETHOD(GetCapabilities)(THIS_ LPDIDEVCAPS) PURE;
    STDMETHOD(EnumObjects)(THIS_ LPDIENUMDEVICEOBJECTSCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetProperty)(THIS_ REFGUID,LPDIPROPHEADER) PURE;
    STDMETHOD(SetProperty)(THIS_ REFGUID,LPCDIPROPHEADER) PURE;
    STDMETHOD(Acquire)(THIS) PURE;
    STDMETHOD(Unacquire)(THIS) PURE;
    STDMETHOD(GetDeviceState)(THIS_ DWORD,LPVOID) PURE;
    STDMETHOD(GetDeviceData)(THIS_ DWORD,LPDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(SetDataFormat)(THIS_ LPCDIDATAFORMAT) PURE;
    STDMETHOD(SetEventNotification)(THIS_ HANDLE) PURE;
    STDMETHOD(SetCooperativeLevel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(GetObjectInfo)(THIS_ LPDIDEVICEOBJECTINSTANCEA,DWORD,DWORD) PURE;
    STDMETHOD(GetDeviceInfo)(THIS_ LPDIDEVICEINSTANCEA) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;

    /*** IDirectInputDevice2A methods ***/
    STDMETHOD(CreateEffect)(THIS_ REFGUID,LPCDIEFFECT,LPDIRECTINPUTEFFECT *,LPUNKNOWN) PURE;
    STDMETHOD(EnumEffects)(THIS_ LPDIENUMEFFECTSCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetEffectInfo)(THIS_ LPDIEFFECTINFOA,REFGUID) PURE;
    STDMETHOD(GetForceFeedbackState)(THIS_ LPDWORD) PURE;
    STDMETHOD(SendForceFeedbackCommand)(THIS_ DWORD) PURE;
    STDMETHOD(EnumCreatedEffectObjects)(THIS_ LPDIENUMCREATEDEFFECTOBJECTSCALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(Escape)(THIS_ LPDIEFFESCAPE) PURE;
    STDMETHOD(Poll)(THIS) PURE;
    STDMETHOD(SendDeviceData)(THIS_ DWORD,LPCDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
};

typedef struct IDirectInputDevice2A *LPDIRECTINPUTDEVICE2A;

#ifdef UNICODE
#define IID_IDirectInputDevice2 IID_IDirectInputDevice2W
#define IDirectInputDevice2 IDirectInputDevice2W
#define IDirectInputDevice2Vtbl IDirectInputDevice2WVtbl
#else
#define IID_IDirectInputDevice2 IID_IDirectInputDevice2A
#define IDirectInputDevice2 IDirectInputDevice2A
#define IDirectInputDevice2Vtbl IDirectInputDevice2AVtbl
#endif
typedef struct IDirectInputDevice2 *LPDIRECTINPUTDEVICE2;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInputDevice2_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInputDevice2_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInputDevice2_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInputDevice2_GetCapabilities(p,a) (p)->lpVtbl->GetCapabilities(p,a)
#define IDirectInputDevice2_EnumObjects(p,a,b,c) (p)->lpVtbl->EnumObjects(p,a,b,c)
#define IDirectInputDevice2_GetProperty(p,a,b) (p)->lpVtbl->GetProperty(p,a,b)
#define IDirectInputDevice2_SetProperty(p,a,b) (p)->lpVtbl->SetProperty(p,a,b)
#define IDirectInputDevice2_Acquire(p) (p)->lpVtbl->Acquire(p)
#define IDirectInputDevice2_Unacquire(p) (p)->lpVtbl->Unacquire(p)
#define IDirectInputDevice2_GetDeviceState(p,a,b) (p)->lpVtbl->GetDeviceState(p,a,b)
#define IDirectInputDevice2_GetDeviceData(p,a,b,c,d) (p)->lpVtbl->GetDeviceData(p,a,b,c,d)
#define IDirectInputDevice2_SetDataFormat(p,a) (p)->lpVtbl->SetDataFormat(p,a)
#define IDirectInputDevice2_SetEventNotification(p,a) (p)->lpVtbl->SetEventNotification(p,a)
#define IDirectInputDevice2_SetCooperativeLevel(p,a,b) (p)->lpVtbl->SetCooperativeLevel(p,a,b)
#define IDirectInputDevice2_GetObjectInfo(p,a,b,c) (p)->lpVtbl->GetObjectInfo(p,a,b,c)
#define IDirectInputDevice2_GetDeviceInfo(p,a) (p)->lpVtbl->GetDeviceInfo(p,a)
#define IDirectInputDevice2_RunControlPanel(p,a,b) (p)->lpVtbl->RunControlPanel(p,a,b)
#define IDirectInputDevice2_Initialize(p,a,b,c) (p)->lpVtbl->Initialize(p,a,b,c)
#define IDirectInputDevice2_CreateEffect(p,a,b,c,d) (p)->lpVtbl->CreateEffect(p,a,b,c,d)
#define IDirectInputDevice2_EnumEffects(p,a,b,c) (p)->lpVtbl->EnumEffects(p,a,b,c)
#define IDirectInputDevice2_GetEffectInfo(p,a,b) (p)->lpVtbl->GetEffectInfo(p,a,b)
#define IDirectInputDevice2_GetForceFeedbackState(p,a) (p)->lpVtbl->GetForceFeedbackState(p,a)
#define IDirectInputDevice2_SendForceFeedbackCommand(p,a) (p)->lpVtbl->SendForceFeedbackCommand(p,a)
#define IDirectInputDevice2_EnumCreatedEffectObjects(p,a,b,c) (p)->lpVtbl->EnumCreatedEffectObjects(p,a,b,c)
#define IDirectInputDevice2_Escape(p,a) (p)->lpVtbl->Escape(p,a)
#define IDirectInputDevice2_Poll(p) (p)->lpVtbl->Poll(p)
#define IDirectInputDevice2_SendDeviceData(p,a,b,c,d) (p)->lpVtbl->SendDeviceData(p,a,b,c,d)
#else
#define IDirectInputDevice2_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInputDevice2_AddRef(p) (p)->AddRef()
#define IDirectInputDevice2_Release(p) (p)->Release()
#define IDirectInputDevice2_GetCapabilities(p,a) (p)->GetCapabilities(a)
#define IDirectInputDevice2_EnumObjects(p,a,b,c) (p)->EnumObjects(a,b,c)
#define IDirectInputDevice2_GetProperty(p,a,b) (p)->GetProperty(a,b)
#define IDirectInputDevice2_SetProperty(p,a,b) (p)->SetProperty(a,b)
#define IDirectInputDevice2_Acquire(p) (p)->Acquire()
#define IDirectInputDevice2_Unacquire(p) (p)->Unacquire()
#define IDirectInputDevice2_GetDeviceState(p,a,b) (p)->GetDeviceState(a,b)
#define IDirectInputDevice2_GetDeviceData(p,a,b,c,d) (p)->GetDeviceData(a,b,c,d)
#define IDirectInputDevice2_SetDataFormat(p,a) (p)->SetDataFormat(a)
#define IDirectInputDevice2_SetEventNotification(p,a) (p)->SetEventNotification(a)
#define IDirectInputDevice2_SetCooperativeLevel(p,a,b) (p)->SetCooperativeLevel(a,b)
#define IDirectInputDevice2_GetObjectInfo(p,a,b,c) (p)->GetObjectInfo(a,b,c)
#define IDirectInputDevice2_GetDeviceInfo(p,a) (p)->GetDeviceInfo(a)
#define IDirectInputDevice2_RunControlPanel(p,a,b) (p)->RunControlPanel(a,b)
#define IDirectInputDevice2_Initialize(p,a,b,c) (p)->Initialize(a,b,c)
#define IDirectInputDevice2_CreateEffect(p,a,b,c,d) (p)->CreateEffect(a,b,c,d)
#define IDirectInputDevice2_EnumEffects(p,a,b,c) (p)->EnumEffects(a,b,c)
#define IDirectInputDevice2_GetEffectInfo(p,a,b) (p)->GetEffectInfo(a,b)
#define IDirectInputDevice2_GetForceFeedbackState(p,a) (p)->GetForceFeedbackState(a)
#define IDirectInputDevice2_SendForceFeedbackCommand(p,a) (p)->SendForceFeedbackCommand(a)
#define IDirectInputDevice2_EnumCreatedEffectObjects(p,a,b,c) (p)->EnumCreatedEffectObjects(a,b,c)
#define IDirectInputDevice2_Escape(p,a) (p)->Escape(a)
#define IDirectInputDevice2_Poll(p) (p)->Poll()
#define IDirectInputDevice2_SendDeviceData(p,a,b,c,d) (p)->SendDeviceData(a,b,c,d)
#endif

#endif /* DIJ_RINGZERO */

#endif /* DIRECTINPUT_VERSION >= 0x0500 */

#if(DIRECTINPUT_VERSION >= 0x0700)
#define DIFEF_DEFAULT               0x00000000
#define DIFEF_INCLUDENONSTANDARD    0x00000001
#define DIFEF_MODIFYIFNEEDED            0x00000010

#ifndef DIJ_RINGZERO

#undef INTERFACE
#define INTERFACE IDirectInputDevice7W

DECLARE_INTERFACE_(IDirectInputDevice7W, IDirectInputDevice2W)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputDevice2W methods ***/
    STDMETHOD(GetCapabilities)(THIS_ LPDIDEVCAPS) PURE;
    STDMETHOD(EnumObjects)(THIS_ LPDIENUMDEVICEOBJECTSCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetProperty)(THIS_ REFGUID,LPDIPROPHEADER) PURE;
    STDMETHOD(SetProperty)(THIS_ REFGUID,LPCDIPROPHEADER) PURE;
    STDMETHOD(Acquire)(THIS) PURE;
    STDMETHOD(Unacquire)(THIS) PURE;
    STDMETHOD(GetDeviceState)(THIS_ DWORD,LPVOID) PURE;
    STDMETHOD(GetDeviceData)(THIS_ DWORD,LPDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(SetDataFormat)(THIS_ LPCDIDATAFORMAT) PURE;
    STDMETHOD(SetEventNotification)(THIS_ HANDLE) PURE;
    STDMETHOD(SetCooperativeLevel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(GetObjectInfo)(THIS_ LPDIDEVICEOBJECTINSTANCEW,DWORD,DWORD) PURE;
    STDMETHOD(GetDeviceInfo)(THIS_ LPDIDEVICEINSTANCEW) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;
    STDMETHOD(CreateEffect)(THIS_ REFGUID,LPCDIEFFECT,LPDIRECTINPUTEFFECT *,LPUNKNOWN) PURE;
    STDMETHOD(EnumEffects)(THIS_ LPDIENUMEFFECTSCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetEffectInfo)(THIS_ LPDIEFFECTINFOW,REFGUID) PURE;
    STDMETHOD(GetForceFeedbackState)(THIS_ LPDWORD) PURE;
    STDMETHOD(SendForceFeedbackCommand)(THIS_ DWORD) PURE;
    STDMETHOD(EnumCreatedEffectObjects)(THIS_ LPDIENUMCREATEDEFFECTOBJECTSCALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(Escape)(THIS_ LPDIEFFESCAPE) PURE;
    STDMETHOD(Poll)(THIS) PURE;
    STDMETHOD(SendDeviceData)(THIS_ DWORD,LPCDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;

    /*** IDirectInputDevice7W methods ***/
    STDMETHOD(EnumEffectsInFile)(THIS_ LPCWSTR,LPDIENUMEFFECTSINFILECALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(WriteEffectToFile)(THIS_ LPCWSTR,DWORD,LPDIFILEEFFECT,DWORD) PURE;
};

typedef struct IDirectInputDevice7W *LPDIRECTINPUTDEVICE7W;

#undef INTERFACE
#define INTERFACE IDirectInputDevice7A

DECLARE_INTERFACE_(IDirectInputDevice7A, IDirectInputDevice2A)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputDevice2A methods ***/
    STDMETHOD(GetCapabilities)(THIS_ LPDIDEVCAPS) PURE;
    STDMETHOD(EnumObjects)(THIS_ LPDIENUMDEVICEOBJECTSCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetProperty)(THIS_ REFGUID,LPDIPROPHEADER) PURE;
    STDMETHOD(SetProperty)(THIS_ REFGUID,LPCDIPROPHEADER) PURE;
    STDMETHOD(Acquire)(THIS) PURE;
    STDMETHOD(Unacquire)(THIS) PURE;
    STDMETHOD(GetDeviceState)(THIS_ DWORD,LPVOID) PURE;
    STDMETHOD(GetDeviceData)(THIS_ DWORD,LPDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(SetDataFormat)(THIS_ LPCDIDATAFORMAT) PURE;
    STDMETHOD(SetEventNotification)(THIS_ HANDLE) PURE;
    STDMETHOD(SetCooperativeLevel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(GetObjectInfo)(THIS_ LPDIDEVICEOBJECTINSTANCEA,DWORD,DWORD) PURE;
    STDMETHOD(GetDeviceInfo)(THIS_ LPDIDEVICEINSTANCEA) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;
    STDMETHOD(CreateEffect)(THIS_ REFGUID,LPCDIEFFECT,LPDIRECTINPUTEFFECT *,LPUNKNOWN) PURE;
    STDMETHOD(EnumEffects)(THIS_ LPDIENUMEFFECTSCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetEffectInfo)(THIS_ LPDIEFFECTINFOA,REFGUID) PURE;
    STDMETHOD(GetForceFeedbackState)(THIS_ LPDWORD) PURE;
    STDMETHOD(SendForceFeedbackCommand)(THIS_ DWORD) PURE;
    STDMETHOD(EnumCreatedEffectObjects)(THIS_ LPDIENUMCREATEDEFFECTOBJECTSCALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(Escape)(THIS_ LPDIEFFESCAPE) PURE;
    STDMETHOD(Poll)(THIS) PURE;
    STDMETHOD(SendDeviceData)(THIS_ DWORD,LPCDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;

    /*** IDirectInputDevice7A methods ***/
    STDMETHOD(EnumEffectsInFile)(THIS_ LPCSTR,LPDIENUMEFFECTSINFILECALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(WriteEffectToFile)(THIS_ LPCSTR,DWORD,LPDIFILEEFFECT,DWORD) PURE;
};

typedef struct IDirectInputDevice7A *LPDIRECTINPUTDEVICE7A;

#ifdef UNICODE
#define IID_IDirectInputDevice7 IID_IDirectInputDevice7W
#define IDirectInputDevice7 IDirectInputDevice7W
#define IDirectInputDevice7Vtbl IDirectInputDevice7WVtbl
#else
#define IID_IDirectInputDevice7 IID_IDirectInputDevice7A
#define IDirectInputDevice7 IDirectInputDevice7A
#define IDirectInputDevice7Vtbl IDirectInputDevice7AVtbl
#endif
typedef struct IDirectInputDevice7 *LPDIRECTINPUTDEVICE7;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInputDevice7_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInputDevice7_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInputDevice7_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInputDevice7_GetCapabilities(p,a) (p)->lpVtbl->GetCapabilities(p,a)
#define IDirectInputDevice7_EnumObjects(p,a,b,c) (p)->lpVtbl->EnumObjects(p,a,b,c)
#define IDirectInputDevice7_GetProperty(p,a,b) (p)->lpVtbl->GetProperty(p,a,b)
#define IDirectInputDevice7_SetProperty(p,a,b) (p)->lpVtbl->SetProperty(p,a,b)
#define IDirectInputDevice7_Acquire(p) (p)->lpVtbl->Acquire(p)
#define IDirectInputDevice7_Unacquire(p) (p)->lpVtbl->Unacquire(p)
#define IDirectInputDevice7_GetDeviceState(p,a,b) (p)->lpVtbl->GetDeviceState(p,a,b)
#define IDirectInputDevice7_GetDeviceData(p,a,b,c,d) (p)->lpVtbl->GetDeviceData(p,a,b,c,d)
#define IDirectInputDevice7_SetDataFormat(p,a) (p)->lpVtbl->SetDataFormat(p,a)
#define IDirectInputDevice7_SetEventNotification(p,a) (p)->lpVtbl->SetEventNotification(p,a)
#define IDirectInputDevice7_SetCooperativeLevel(p,a,b) (p)->lpVtbl->SetCooperativeLevel(p,a,b)
#define IDirectInputDevice7_GetObjectInfo(p,a,b,c) (p)->lpVtbl->GetObjectInfo(p,a,b,c)
#define IDirectInputDevice7_GetDeviceInfo(p,a) (p)->lpVtbl->GetDeviceInfo(p,a)
#define IDirectInputDevice7_RunControlPanel(p,a,b) (p)->lpVtbl->RunControlPanel(p,a,b)
#define IDirectInputDevice7_Initialize(p,a,b,c) (p)->lpVtbl->Initialize(p,a,b,c)
#define IDirectInputDevice7_CreateEffect(p,a,b,c,d) (p)->lpVtbl->CreateEffect(p,a,b,c,d)
#define IDirectInputDevice7_EnumEffects(p,a,b,c) (p)->lpVtbl->EnumEffects(p,a,b,c)
#define IDirectInputDevice7_GetEffectInfo(p,a,b) (p)->lpVtbl->GetEffectInfo(p,a,b)
#define IDirectInputDevice7_GetForceFeedbackState(p,a) (p)->lpVtbl->GetForceFeedbackState(p,a)
#define IDirectInputDevice7_SendForceFeedbackCommand(p,a) (p)->lpVtbl->SendForceFeedbackCommand(p,a)
#define IDirectInputDevice7_EnumCreatedEffectObjects(p,a,b,c) (p)->lpVtbl->EnumCreatedEffectObjects(p,a,b,c)
#define IDirectInputDevice7_Escape(p,a) (p)->lpVtbl->Escape(p,a)
#define IDirectInputDevice7_Poll(p) (p)->lpVtbl->Poll(p)
#define IDirectInputDevice7_SendDeviceData(p,a,b,c,d) (p)->lpVtbl->SendDeviceData(p,a,b,c,d)
#define IDirectInputDevice7_EnumEffectsInFile(p,a,b,c,d) (p)->lpVtbl->EnumEffectsInFile(p,a,b,c,d)
#define IDirectInputDevice7_WriteEffectToFile(p,a,b,c,d) (p)->lpVtbl->WriteEffectToFile(p,a,b,c,d)
#else
#define IDirectInputDevice7_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInputDevice7_AddRef(p) (p)->AddRef()
#define IDirectInputDevice7_Release(p) (p)->Release()
#define IDirectInputDevice7_GetCapabilities(p,a) (p)->GetCapabilities(a)
#define IDirectInputDevice7_EnumObjects(p,a,b,c) (p)->EnumObjects(a,b,c)
#define IDirectInputDevice7_GetProperty(p,a,b) (p)->GetProperty(a,b)
#define IDirectInputDevice7_SetProperty(p,a,b) (p)->SetProperty(a,b)
#define IDirectInputDevice7_Acquire(p) (p)->Acquire()
#define IDirectInputDevice7_Unacquire(p) (p)->Unacquire()
#define IDirectInputDevice7_GetDeviceState(p,a,b) (p)->GetDeviceState(a,b)
#define IDirectInputDevice7_GetDeviceData(p,a,b,c,d) (p)->GetDeviceData(a,b,c,d)
#define IDirectInputDevice7_SetDataFormat(p,a) (p)->SetDataFormat(a)
#define IDirectInputDevice7_SetEventNotification(p,a) (p)->SetEventNotification(a)
#define IDirectInputDevice7_SetCooperativeLevel(p,a,b) (p)->SetCooperativeLevel(a,b)
#define IDirectInputDevice7_GetObjectInfo(p,a,b,c) (p)->GetObjectInfo(a,b,c)
#define IDirectInputDevice7_GetDeviceInfo(p,a) (p)->GetDeviceInfo(a)
#define IDirectInputDevice7_RunControlPanel(p,a,b) (p)->RunControlPanel(a,b)
#define IDirectInputDevice7_Initialize(p,a,b,c) (p)->Initialize(a,b,c)
#define IDirectInputDevice7_CreateEffect(p,a,b,c,d) (p)->CreateEffect(a,b,c,d)
#define IDirectInputDevice7_EnumEffects(p,a,b,c) (p)->EnumEffects(a,b,c)
#define IDirectInputDevice7_GetEffectInfo(p,a,b) (p)->GetEffectInfo(a,b)
#define IDirectInputDevice7_GetForceFeedbackState(p,a) (p)->GetForceFeedbackState(a)
#define IDirectInputDevice7_SendForceFeedbackCommand(p,a) (p)->SendForceFeedbackCommand(a)
#define IDirectInputDevice7_EnumCreatedEffectObjects(p,a,b,c) (p)->EnumCreatedEffectObjects(a,b,c)
#define IDirectInputDevice7_Escape(p,a) (p)->Escape(a)
#define IDirectInputDevice7_Poll(p) (p)->Poll()
#define IDirectInputDevice7_SendDeviceData(p,a,b,c,d) (p)->SendDeviceData(a,b,c,d)
#define IDirectInputDevice7_EnumEffectsInFile(p,a,b,c,d) (p)->EnumEffectsInFile(a,b,c,d)
#define IDirectInputDevice7_WriteEffectToFile(p,a,b,c,d) (p)->WriteEffectToFile(a,b,c,d)
#endif

#endif /* DIJ_RINGZERO */

#endif /* DIRECTINPUT_VERSION >= 0x0700 */

#if(DIRECTINPUT_VERSION >= 0x0800)

#ifndef DIJ_RINGZERO

#undef INTERFACE
#define INTERFACE IDirectInputDevice8W

DECLARE_INTERFACE_(IDirectInputDevice8W, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputDevice8W methods ***/
    STDMETHOD(GetCapabilities)(THIS_ LPDIDEVCAPS) PURE;
    STDMETHOD(EnumObjects)(THIS_ LPDIENUMDEVICEOBJECTSCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetProperty)(THIS_ REFGUID,LPDIPROPHEADER) PURE;
    STDMETHOD(SetProperty)(THIS_ REFGUID,LPCDIPROPHEADER) PURE;
    STDMETHOD(Acquire)(THIS) PURE;
    STDMETHOD(Unacquire)(THIS) PURE;
    STDMETHOD(GetDeviceState)(THIS_ DWORD,LPVOID) PURE;
    STDMETHOD(GetDeviceData)(THIS_ DWORD,LPDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(SetDataFormat)(THIS_ LPCDIDATAFORMAT) PURE;
    STDMETHOD(SetEventNotification)(THIS_ HANDLE) PURE;
    STDMETHOD(SetCooperativeLevel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(GetObjectInfo)(THIS_ LPDIDEVICEOBJECTINSTANCEW,DWORD,DWORD) PURE;
    STDMETHOD(GetDeviceInfo)(THIS_ LPDIDEVICEINSTANCEW) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;
    STDMETHOD(CreateEffect)(THIS_ REFGUID,LPCDIEFFECT,LPDIRECTINPUTEFFECT *,LPUNKNOWN) PURE;
    STDMETHOD(EnumEffects)(THIS_ LPDIENUMEFFECTSCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetEffectInfo)(THIS_ LPDIEFFECTINFOW,REFGUID) PURE;
    STDMETHOD(GetForceFeedbackState)(THIS_ LPDWORD) PURE;
    STDMETHOD(SendForceFeedbackCommand)(THIS_ DWORD) PURE;
    STDMETHOD(EnumCreatedEffectObjects)(THIS_ LPDIENUMCREATEDEFFECTOBJECTSCALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(Escape)(THIS_ LPDIEFFESCAPE) PURE;
    STDMETHOD(Poll)(THIS) PURE;
    STDMETHOD(SendDeviceData)(THIS_ DWORD,LPCDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(EnumEffectsInFile)(THIS_ LPCWSTR,LPDIENUMEFFECTSINFILECALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(WriteEffectToFile)(THIS_ LPCWSTR,DWORD,LPDIFILEEFFECT,DWORD) PURE;
    STDMETHOD(BuildActionMap)(THIS_ LPDIACTIONFORMATW,LPCWSTR,DWORD) PURE;
    STDMETHOD(SetActionMap)(THIS_ LPDIACTIONFORMATW,LPCWSTR,DWORD) PURE;
    STDMETHOD(GetImageInfo)(THIS_ LPDIDEVICEIMAGEINFOHEADERW) PURE;
};

typedef struct IDirectInputDevice8W *LPDIRECTINPUTDEVICE8W;

#undef INTERFACE
#define INTERFACE IDirectInputDevice8A

DECLARE_INTERFACE_(IDirectInputDevice8A, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputDevice8A methods ***/
    STDMETHOD(GetCapabilities)(THIS_ LPDIDEVCAPS) PURE;
    STDMETHOD(EnumObjects)(THIS_ LPDIENUMDEVICEOBJECTSCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetProperty)(THIS_ REFGUID,LPDIPROPHEADER) PURE;
    STDMETHOD(SetProperty)(THIS_ REFGUID,LPCDIPROPHEADER) PURE;
    STDMETHOD(Acquire)(THIS) PURE;
    STDMETHOD(Unacquire)(THIS) PURE;
    STDMETHOD(GetDeviceState)(THIS_ DWORD,LPVOID) PURE;
    STDMETHOD(GetDeviceData)(THIS_ DWORD,LPDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(SetDataFormat)(THIS_ LPCDIDATAFORMAT) PURE;
    STDMETHOD(SetEventNotification)(THIS_ HANDLE) PURE;
    STDMETHOD(SetCooperativeLevel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(GetObjectInfo)(THIS_ LPDIDEVICEOBJECTINSTANCEA,DWORD,DWORD) PURE;
    STDMETHOD(GetDeviceInfo)(THIS_ LPDIDEVICEINSTANCEA) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD,REFGUID) PURE;
    STDMETHOD(CreateEffect)(THIS_ REFGUID,LPCDIEFFECT,LPDIRECTINPUTEFFECT *,LPUNKNOWN) PURE;
    STDMETHOD(EnumEffects)(THIS_ LPDIENUMEFFECTSCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetEffectInfo)(THIS_ LPDIEFFECTINFOA,REFGUID) PURE;
    STDMETHOD(GetForceFeedbackState)(THIS_ LPDWORD) PURE;
    STDMETHOD(SendForceFeedbackCommand)(THIS_ DWORD) PURE;
    STDMETHOD(EnumCreatedEffectObjects)(THIS_ LPDIENUMCREATEDEFFECTOBJECTSCALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(Escape)(THIS_ LPDIEFFESCAPE) PURE;
    STDMETHOD(Poll)(THIS) PURE;
    STDMETHOD(SendDeviceData)(THIS_ DWORD,LPCDIDEVICEOBJECTDATA,LPDWORD,DWORD) PURE;
    STDMETHOD(EnumEffectsInFile)(THIS_ LPCSTR,LPDIENUMEFFECTSINFILECALLBACK,LPVOID,DWORD) PURE;
    STDMETHOD(WriteEffectToFile)(THIS_ LPCSTR,DWORD,LPDIFILEEFFECT,DWORD) PURE;
    STDMETHOD(BuildActionMap)(THIS_ LPDIACTIONFORMATA,LPCSTR,DWORD) PURE;
    STDMETHOD(SetActionMap)(THIS_ LPDIACTIONFORMATA,LPCSTR,DWORD) PURE;
    STDMETHOD(GetImageInfo)(THIS_ LPDIDEVICEIMAGEINFOHEADERA) PURE;
};

typedef struct IDirectInputDevice8A *LPDIRECTINPUTDEVICE8A;

#ifdef UNICODE
#define IID_IDirectInputDevice8 IID_IDirectInputDevice8W
#define IDirectInputDevice8 IDirectInputDevice8W
#define IDirectInputDevice8Vtbl IDirectInputDevice8WVtbl
#else
#define IID_IDirectInputDevice8 IID_IDirectInputDevice8A
#define IDirectInputDevice8 IDirectInputDevice8A
#define IDirectInputDevice8Vtbl IDirectInputDevice8AVtbl
#endif
typedef struct IDirectInputDevice8 *LPDIRECTINPUTDEVICE8;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInputDevice8_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInputDevice8_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInputDevice8_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInputDevice8_GetCapabilities(p,a) (p)->lpVtbl->GetCapabilities(p,a)
#define IDirectInputDevice8_EnumObjects(p,a,b,c) (p)->lpVtbl->EnumObjects(p,a,b,c)
#define IDirectInputDevice8_GetProperty(p,a,b) (p)->lpVtbl->GetProperty(p,a,b)
#define IDirectInputDevice8_SetProperty(p,a,b) (p)->lpVtbl->SetProperty(p,a,b)
#define IDirectInputDevice8_Acquire(p) (p)->lpVtbl->Acquire(p)
#define IDirectInputDevice8_Unacquire(p) (p)->lpVtbl->Unacquire(p)
#define IDirectInputDevice8_GetDeviceState(p,a,b) (p)->lpVtbl->GetDeviceState(p,a,b)
#define IDirectInputDevice8_GetDeviceData(p,a,b,c,d) (p)->lpVtbl->GetDeviceData(p,a,b,c,d)
#define IDirectInputDevice8_SetDataFormat(p,a) (p)->lpVtbl->SetDataFormat(p,a)
#define IDirectInputDevice8_SetEventNotification(p,a) (p)->lpVtbl->SetEventNotification(p,a)
#define IDirectInputDevice8_SetCooperativeLevel(p,a,b) (p)->lpVtbl->SetCooperativeLevel(p,a,b)
#define IDirectInputDevice8_GetObjectInfo(p,a,b,c) (p)->lpVtbl->GetObjectInfo(p,a,b,c)
#define IDirectInputDevice8_GetDeviceInfo(p,a) (p)->lpVtbl->GetDeviceInfo(p,a)
#define IDirectInputDevice8_RunControlPanel(p,a,b) (p)->lpVtbl->RunControlPanel(p,a,b)
#define IDirectInputDevice8_Initialize(p,a,b,c) (p)->lpVtbl->Initialize(p,a,b,c)
#define IDirectInputDevice8_CreateEffect(p,a,b,c,d) (p)->lpVtbl->CreateEffect(p,a,b,c,d)
#define IDirectInputDevice8_EnumEffects(p,a,b,c) (p)->lpVtbl->EnumEffects(p,a,b,c)
#define IDirectInputDevice8_GetEffectInfo(p,a,b) (p)->lpVtbl->GetEffectInfo(p,a,b)
#define IDirectInputDevice8_GetForceFeedbackState(p,a) (p)->lpVtbl->GetForceFeedbackState(p,a)
#define IDirectInputDevice8_SendForceFeedbackCommand(p,a) (p)->lpVtbl->SendForceFeedbackCommand(p,a)
#define IDirectInputDevice8_EnumCreatedEffectObjects(p,a,b,c) (p)->lpVtbl->EnumCreatedEffectObjects(p,a,b,c)
#define IDirectInputDevice8_Escape(p,a) (p)->lpVtbl->Escape(p,a)
#define IDirectInputDevice8_Poll(p) (p)->lpVtbl->Poll(p)
#define IDirectInputDevice8_SendDeviceData(p,a,b,c,d) (p)->lpVtbl->SendDeviceData(p,a,b,c,d)
#define IDirectInputDevice8_EnumEffectsInFile(p,a,b,c,d) (p)->lpVtbl->EnumEffectsInFile(p,a,b,c,d)
#define IDirectInputDevice8_WriteEffectToFile(p,a,b,c,d) (p)->lpVtbl->WriteEffectToFile(p,a,b,c,d)
#define IDirectInputDevice8_BuildActionMap(p,a,b,c) (p)->lpVtbl->BuildActionMap(p,a,b,c)
#define IDirectInputDevice8_SetActionMap(p,a,b,c) (p)->lpVtbl->SetActionMap(p,a,b,c)
#define IDirectInputDevice8_GetImageInfo(p,a) (p)->lpVtbl->GetImageInfo(p,a)
#else
#define IDirectInputDevice8_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInputDevice8_AddRef(p) (p)->AddRef()
#define IDirectInputDevice8_Release(p) (p)->Release()
#define IDirectInputDevice8_GetCapabilities(p,a) (p)->GetCapabilities(a)
#define IDirectInputDevice8_EnumObjects(p,a,b,c) (p)->EnumObjects(a,b,c)
#define IDirectInputDevice8_GetProperty(p,a,b) (p)->GetProperty(a,b)
#define IDirectInputDevice8_SetProperty(p,a,b) (p)->SetProperty(a,b)
#define IDirectInputDevice8_Acquire(p) (p)->Acquire()
#define IDirectInputDevice8_Unacquire(p) (p)->Unacquire()
#define IDirectInputDevice8_GetDeviceState(p,a,b) (p)->GetDeviceState(a,b)
#define IDirectInputDevice8_GetDeviceData(p,a,b,c,d) (p)->GetDeviceData(a,b,c,d)
#define IDirectInputDevice8_SetDataFormat(p,a) (p)->SetDataFormat(a)
#define IDirectInputDevice8_SetEventNotification(p,a) (p)->SetEventNotification(a)
#define IDirectInputDevice8_SetCooperativeLevel(p,a,b) (p)->SetCooperativeLevel(a,b)
#define IDirectInputDevice8_GetObjectInfo(p,a,b,c) (p)->GetObjectInfo(a,b,c)
#define IDirectInputDevice8_GetDeviceInfo(p,a) (p)->GetDeviceInfo(a)
#define IDirectInputDevice8_RunControlPanel(p,a,b) (p)->RunControlPanel(a,b)
#define IDirectInputDevice8_Initialize(p,a,b,c) (p)->Initialize(a,b,c)
#define IDirectInputDevice8_CreateEffect(p,a,b,c,d) (p)->CreateEffect(a,b,c,d)
#define IDirectInputDevice8_EnumEffects(p,a,b,c) (p)->EnumEffects(a,b,c)
#define IDirectInputDevice8_GetEffectInfo(p,a,b) (p)->GetEffectInfo(a,b)
#define IDirectInputDevice8_GetForceFeedbackState(p,a) (p)->GetForceFeedbackState(a)
#define IDirectInputDevice8_SendForceFeedbackCommand(p,a) (p)->SendForceFeedbackCommand(a)
#define IDirectInputDevice8_EnumCreatedEffectObjects(p,a,b,c) (p)->EnumCreatedEffectObjects(a,b,c)
#define IDirectInputDevice8_Escape(p,a) (p)->Escape(a)
#define IDirectInputDevice8_Poll(p) (p)->Poll()
#define IDirectInputDevice8_SendDeviceData(p,a,b,c,d) (p)->SendDeviceData(a,b,c,d)
#define IDirectInputDevice8_EnumEffectsInFile(p,a,b,c,d) (p)->EnumEffectsInFile(a,b,c,d)
#define IDirectInputDevice8_WriteEffectToFile(p,a,b,c,d) (p)->WriteEffectToFile(a,b,c,d)
#define IDirectInputDevice8_BuildActionMap(p,a,b,c) (p)->BuildActionMap(a,b,c)
#define IDirectInputDevice8_SetActionMap(p,a,b,c) (p)->SetActionMap(a,b,c)
#define IDirectInputDevice8_GetImageInfo(p,a) (p)->GetImageInfo(a)
#endif

#endif /* DIJ_RINGZERO */

#endif /* DIRECTINPUT_VERSION >= 0x0800 */

/****************************************************************************
 *
 *      Mouse
 *
 ****************************************************************************/

#ifndef DIJ_RINGZERO

typedef struct _DIMOUSESTATE {
    LONG    lX;
    LONG    lY;
    LONG    lZ;
    BYTE    rgbButtons[4];
} DIMOUSESTATE, *LPDIMOUSESTATE;

#if DIRECTINPUT_VERSION >= 0x0700
typedef struct _DIMOUSESTATE2 {
    LONG    lX;
    LONG    lY;
    LONG    lZ;
    BYTE    rgbButtons[8];
} DIMOUSESTATE2, *LPDIMOUSESTATE2;
#endif


#define DIMOFS_X        FIELD_OFFSET(DIMOUSESTATE, lX)
#define DIMOFS_Y        FIELD_OFFSET(DIMOUSESTATE, lY)
#define DIMOFS_Z        FIELD_OFFSET(DIMOUSESTATE, lZ)
#define DIMOFS_BUTTON0 (FIELD_OFFSET(DIMOUSESTATE, rgbButtons) + 0)
#define DIMOFS_BUTTON1 (FIELD_OFFSET(DIMOUSESTATE, rgbButtons) + 1)
#define DIMOFS_BUTTON2 (FIELD_OFFSET(DIMOUSESTATE, rgbButtons) + 2)
#define DIMOFS_BUTTON3 (FIELD_OFFSET(DIMOUSESTATE, rgbButtons) + 3)
#if (DIRECTINPUT_VERSION >= 0x0700)
#define DIMOFS_BUTTON4 (FIELD_OFFSET(DIMOUSESTATE2, rgbButtons) + 4)
#define DIMOFS_BUTTON5 (FIELD_OFFSET(DIMOUSESTATE2, rgbButtons) + 5)
#define DIMOFS_BUTTON6 (FIELD_OFFSET(DIMOUSESTATE2, rgbButtons) + 6)
#define DIMOFS_BUTTON7 (FIELD_OFFSET(DIMOUSESTATE2, rgbButtons) + 7)
#endif
#endif /* DIJ_RINGZERO */

/****************************************************************************
 *
 *      Keyboard
 *
 ****************************************************************************/

#ifndef DIJ_RINGZERO

/****************************************************************************
 *
 *      DirectInput keyboard scan codes
 *
 ****************************************************************************/
#define DIK_ESCAPE          0x01
#define DIK_1               0x02
#define DIK_2               0x03
#define DIK_3               0x04
#define DIK_4               0x05
#define DIK_5               0x06
#define DIK_6               0x07
#define DIK_7               0x08
#define DIK_8               0x09
#define DIK_9               0x0A
#define DIK_0               0x0B
#define DIK_MINUS           0x0C    /* - on main keyboard */
#define DIK_EQUALS          0x0D
#define DIK_BACK            0x0E    /* backspace */
#define DIK_TAB             0x0F
#define DIK_Q               0x10
#define DIK_W               0x11
#define DIK_E               0x12
#define DIK_R               0x13
#define DIK_T               0x14
#define DIK_Y               0x15
#define DIK_U               0x16
#define DIK_I               0x17
#define DIK_O               0x18
#define DIK_P               0x19
#define DIK_LBRACKET        0x1A
#define DIK_RBRACKET        0x1B
#define DIK_RETURN          0x1C    /* Enter on main keyboard */
#define DIK_LCONTROL        0x1D
#define DIK_A               0x1E
#define DIK_S               0x1F
#define DIK_D               0x20
#define DIK_F               0x21
#define DIK_G               0x22
#define DIK_H               0x23
#define DIK_J               0x24
#define DIK_K               0x25
#define DIK_L               0x26
#define DIK_SEMICOLON       0x27
#define DIK_APOSTROPHE      0x28
#define DIK_GRAVE           0x29    /* accent grave */
#define DIK_LSHIFT          0x2A
#define DIK_BACKSLASH       0x2B
#define DIK_Z               0x2C
#define DIK_X               0x2D
#define DIK_C               0x2E
#define DIK_V               0x2F
#define DIK_B               0x30
#define DIK_N               0x31
#define DIK_M               0x32
#define DIK_COMMA           0x33
#define DIK_PERIOD          0x34    /* . on main keyboard */
#define DIK_SLASH           0x35    /* / on main keyboard */
#define DIK_RSHIFT          0x36
#define DIK_MULTIPLY        0x37    /* * on numeric keypad */
#define DIK_LMENU           0x38    /* left Alt */
#define DIK_SPACE           0x39
#define DIK_CAPITAL         0x3A
#define DIK_F1              0x3B
#define DIK_F2              0x3C
#define DIK_F3              0x3D
#define DIK_F4              0x3E
#define DIK_F5              0x3F
#define DIK_F6              0x40
#define DIK_F7              0x41
#define DIK_F8              0x42
#define DIK_F9              0x43
#define DIK_F10             0x44
#define DIK_NUMLOCK         0x45
#define DIK_SCROLL          0x46    /* Scroll Lock */
#define DIK_NUMPAD7         0x47
#define DIK_NUMPAD8         0x48
#define DIK_NUMPAD9         0x49
#define DIK_SUBTRACT        0x4A    /* - on numeric keypad */
#define DIK_NUMPAD4         0x4B
#define DIK_NUMPAD5         0x4C
#define DIK_NUMPAD6         0x4D
#define DIK_ADD             0x4E    /* + on numeric keypad */
#define DIK_NUMPAD1         0x4F
#define DIK_NUMPAD2         0x50
#define DIK_NUMPAD3         0x51
#define DIK_NUMPAD0         0x52
#define DIK_DECIMAL         0x53    /* . on numeric keypad */
#define DIK_OEM_102         0x56    /* <> or \| on RT 102-key keyboard (Non-U.S.) */
#define DIK_F11             0x57
#define DIK_F12             0x58
#define DIK_F13             0x64    /*                     (NEC PC98) */
#define DIK_F14             0x65    /*                     (NEC PC98) */
#define DIK_F15             0x66    /*                     (NEC PC98) */
#define DIK_KANA            0x70    /* (Japanese keyboard)            */
#define DIK_ABNT_C1         0x73    /* /? on Brazilian keyboard */
#define DIK_CONVERT         0x79    /* (Japanese keyboard)            */
#define DIK_NOCONVERT       0x7B    /* (Japanese keyboard)            */
#define DIK_YEN             0x7D    /* (Japanese keyboard)            */
#define DIK_ABNT_C2         0x7E    /* Numpad . on Brazilian keyboard */
#define DIK_NUMPADEQUALS    0x8D    /* = on numeric keypad (NEC PC98) */
#define DIK_PREVTRACK       0x90    /* Previous Track (DIK_CIRCUMFLEX on Japanese keyboard) */
#define DIK_AT              0x91    /*                     (NEC PC98) */
#define DIK_COLON           0x92    /*                     (NEC PC98) */
#define DIK_UNDERLINE       0x93    /*                     (NEC PC98) */
#define DIK_KANJI           0x94    /* (Japanese keyboard)            */
#define DIK_STOP            0x95    /*                     (NEC PC98) */
#define DIK_AX              0x96    /*                     (Japan AX) */
#define DIK_UNLABELED       0x97    /*                        (J3100) */
#define DIK_NEXTTRACK       0x99    /* Next Track */
#define DIK_NUMPADENTER     0x9C    /* Enter on numeric keypad */
#define DIK_RCONTROL        0x9D
#define DIK_MUTE            0xA0    /* Mute */
#define DIK_CALCULATOR      0xA1    /* Calculator */
#define DIK_PLAYPAUSE       0xA2    /* Play / Pause */
#define DIK_MEDIASTOP       0xA4    /* Media Stop */
#define DIK_VOLUMEDOWN      0xAE    /* Volume - */
#define DIK_VOLUMEUP        0xB0    /* Volume + */
#define DIK_WEBHOME         0xB2    /* Web home */
#define DIK_NUMPADCOMMA     0xB3    /* , on numeric keypad (NEC PC98) */
#define DIK_DIVIDE          0xB5    /* / on numeric keypad */
#define DIK_SYSRQ           0xB7
#define DIK_RMENU           0xB8    /* right Alt */
#define DIK_PAUSE           0xC5    /* Pause */
#define DIK_HOME            0xC7    /* Home on arrow keypad */
#define DIK_UP              0xC8    /* UpArrow on arrow keypad */
#define DIK_PRIOR           0xC9    /* PgUp on arrow keypad */
#define DIK_LEFT            0xCB    /* LeftArrow on arrow keypad */
#define DIK_RIGHT           0xCD    /* RightArrow on arrow keypad */
#define DIK_END             0xCF    /* End on arrow keypad */
#define DIK_DOWN            0xD0    /* DownArrow on arrow keypad */
#define DIK_NEXT            0xD1    /* PgDn on arrow keypad */
#define DIK_INSERT          0xD2    /* Insert on arrow keypad */
#define DIK_DELETE          0xD3    /* Delete on arrow keypad */
#define DIK_LWIN            0xDB    /* Left Windows key */
#define DIK_RWIN            0xDC    /* Right Windows key */
#define DIK_APPS            0xDD    /* AppMenu key */
#define DIK_POWER           0xDE    /* System Power */
#define DIK_SLEEP           0xDF    /* System Sleep */
#define DIK_WAKE            0xE3    /* System Wake */
#define DIK_WEBSEARCH       0xE5    /* Web Search */
#define DIK_WEBFAVORITES    0xE6    /* Web Favorites */
#define DIK_WEBREFRESH      0xE7    /* Web Refresh */
#define DIK_WEBSTOP         0xE8    /* Web Stop */
#define DIK_WEBFORWARD      0xE9    /* Web Forward */
#define DIK_WEBBACK         0xEA    /* Web Back */
#define DIK_MYCOMPUTER      0xEB    /* My Computer */
#define DIK_MAIL            0xEC    /* Mail */
#define DIK_MEDIASELECT     0xED    /* Media Select */

/*
 *  Alternate names for keys, to facilitate transition from DOS.
 */
#define DIK_BACKSPACE       DIK_BACK            /* backspace */
#define DIK_NUMPADSTAR      DIK_MULTIPLY        /* * on numeric keypad */
#define DIK_LALT            DIK_LMENU           /* left Alt */
#define DIK_CAPSLOCK        DIK_CAPITAL         /* CapsLock */
#define DIK_NUMPADMINUS     DIK_SUBTRACT        /* - on numeric keypad */
#define DIK_NUMPADPLUS      DIK_ADD             /* + on numeric keypad */
#define DIK_NUMPADPERIOD    DIK_DECIMAL         /* . on numeric keypad */
#define DIK_NUMPADSLASH     DIK_DIVIDE          /* / on numeric keypad */
#define DIK_RALT            DIK_RMENU           /* right Alt */
#define DIK_UPARROW         DIK_UP              /* UpArrow on arrow keypad */
#define DIK_PGUP            DIK_PRIOR           /* PgUp on arrow keypad */
#define DIK_LEFTARROW       DIK_LEFT            /* LeftArrow on arrow keypad */
#define DIK_RIGHTARROW      DIK_RIGHT           /* RightArrow on arrow keypad */
#define DIK_DOWNARROW       DIK_DOWN            /* DownArrow on arrow keypad */
#define DIK_PGDN            DIK_NEXT            /* PgDn on arrow keypad */

/*
 *  Alternate names for keys originally not used on US keyboards.
 */
#define DIK_CIRCUMFLEX      DIK_PREVTRACK       /* Japanese keyboard */

#endif /* DIJ_RINGZERO */

/****************************************************************************
 *
 *      Joystick
 *
 ****************************************************************************/

#ifndef DIJ_RINGZERO

typedef struct DIJOYSTATE {
    LONG    lX;                     /* x-axis position              */
    LONG    lY;                     /* y-axis position              */
    LONG    lZ;                     /* z-axis position              */
    LONG    lRx;                    /* x-axis rotation              */
    LONG    lRy;                    /* y-axis rotation              */
    LONG    lRz;                    /* z-axis rotation              */
    LONG    rglSlider[2];           /* extra axes positions         */
    DWORD   rgdwPOV[4];             /* POV directions               */
    BYTE    rgbButtons[32];         /* 32 buttons                   */
} DIJOYSTATE, *LPDIJOYSTATE;

typedef struct DIJOYSTATE2 {
    LONG    lX;                     /* x-axis position              */
    LONG    lY;                     /* y-axis position              */
    LONG    lZ;                     /* z-axis position              */
    LONG    lRx;                    /* x-axis rotation              */
    LONG    lRy;                    /* y-axis rotation              */
    LONG    lRz;                    /* z-axis rotation              */
    LONG    rglSlider[2];           /* extra axes positions         */
    DWORD   rgdwPOV[4];             /* POV directions               */
    BYTE    rgbButtons[128];        /* 128 buttons                  */
    LONG    lVX;                    /* x-axis velocity              */
    LONG    lVY;                    /* y-axis velocity              */
    LONG    lVZ;                    /* z-axis velocity              */
    LONG    lVRx;                   /* x-axis angular velocity      */
    LONG    lVRy;                   /* y-axis angular velocity      */
    LONG    lVRz;                   /* z-axis angular velocity      */
    LONG    rglVSlider[2];          /* extra axes velocities        */
    LONG    lAX;                    /* x-axis acceleration          */
    LONG    lAY;                    /* y-axis acceleration          */
    LONG    lAZ;                    /* z-axis acceleration          */
    LONG    lARx;                   /* x-axis angular acceleration  */
    LONG    lARy;                   /* y-axis angular acceleration  */
    LONG    lARz;                   /* z-axis angular acceleration  */
    LONG    rglASlider[2];          /* extra axes accelerations     */
    LONG    lFX;                    /* x-axis force                 */
    LONG    lFY;                    /* y-axis force                 */
    LONG    lFZ;                    /* z-axis force                 */
    LONG    lFRx;                   /* x-axis torque                */
    LONG    lFRy;                   /* y-axis torque                */
    LONG    lFRz;                   /* z-axis torque                */
    LONG    rglFSlider[2];          /* extra axes forces            */
} DIJOYSTATE2, *LPDIJOYSTATE2;

#define DIJOFS_X            FIELD_OFFSET(DIJOYSTATE, lX)
#define DIJOFS_Y            FIELD_OFFSET(DIJOYSTATE, lY)
#define DIJOFS_Z            FIELD_OFFSET(DIJOYSTATE, lZ)
#define DIJOFS_RX           FIELD_OFFSET(DIJOYSTATE, lRx)
#define DIJOFS_RY           FIELD_OFFSET(DIJOYSTATE, lRy)
#define DIJOFS_RZ           FIELD_OFFSET(DIJOYSTATE, lRz)
#define DIJOFS_SLIDER(n)   (FIELD_OFFSET(DIJOYSTATE, rglSlider) + \
                                                        (n) * sizeof(LONG))
#define DIJOFS_POV(n)      (FIELD_OFFSET(DIJOYSTATE, rgdwPOV) + \
                                                        (n) * sizeof(DWORD))
#define DIJOFS_BUTTON(n)   (FIELD_OFFSET(DIJOYSTATE, rgbButtons) + (n))
#define DIJOFS_BUTTON0      DIJOFS_BUTTON(0)
#define DIJOFS_BUTTON1      DIJOFS_BUTTON(1)
#define DIJOFS_BUTTON2      DIJOFS_BUTTON(2)
#define DIJOFS_BUTTON3      DIJOFS_BUTTON(3)
#define DIJOFS_BUTTON4      DIJOFS_BUTTON(4)
#define DIJOFS_BUTTON5      DIJOFS_BUTTON(5)
#define DIJOFS_BUTTON6      DIJOFS_BUTTON(6)
#define DIJOFS_BUTTON7      DIJOFS_BUTTON(7)
#define DIJOFS_BUTTON8      DIJOFS_BUTTON(8)
#define DIJOFS_BUTTON9      DIJOFS_BUTTON(9)
#define DIJOFS_BUTTON10     DIJOFS_BUTTON(10)
#define DIJOFS_BUTTON11     DIJOFS_BUTTON(11)
#define DIJOFS_BUTTON12     DIJOFS_BUTTON(12)
#define DIJOFS_BUTTON13     DIJOFS_BUTTON(13)
#define DIJOFS_BUTTON14     DIJOFS_BUTTON(14)
#define DIJOFS_BUTTON15     DIJOFS_BUTTON(15)
#define DIJOFS_BUTTON16     DIJOFS_BUTTON(16)
#define DIJOFS_BUTTON17     DIJOFS_BUTTON(17)
#define DIJOFS_BUTTON18     DIJOFS_BUTTON(18)
#define DIJOFS_BUTTON19     DIJOFS_BUTTON(19)
#define DIJOFS_BUTTON20     DIJOFS_BUTTON(20)
#define DIJOFS_BUTTON21     DIJOFS_BUTTON(21)
#define DIJOFS_BUTTON22     DIJOFS_BUTTON(22)
#define DIJOFS_BUTTON23     DIJOFS_BUTTON(23)
#define DIJOFS_BUTTON24     DIJOFS_BUTTON(24)
#define DIJOFS_BUTTON25     DIJOFS_BUTTON(25)
#define DIJOFS_BUTTON26     DIJOFS_BUTTON(26)
#define DIJOFS_BUTTON27     DIJOFS_BUTTON(27)
#define DIJOFS_BUTTON28     DIJOFS_BUTTON(28)
#define DIJOFS_BUTTON29     DIJOFS_BUTTON(29)
#define DIJOFS_BUTTON30     DIJOFS_BUTTON(30)
#define DIJOFS_BUTTON31     DIJOFS_BUTTON(31)


#endif /* DIJ_RINGZERO */

/****************************************************************************
 *
 *  IDirectInput
 *
 ****************************************************************************/

#ifndef DIJ_RINGZERO

#define DIENUM_STOP             0
#define DIENUM_CONTINUE         1

typedef BOOL (FAR PASCAL * LPDIENUMDEVICESCALLBACKA)(LPCDIDEVICEINSTANCEA, LPVOID);
typedef BOOL (FAR PASCAL * LPDIENUMDEVICESCALLBACKW)(LPCDIDEVICEINSTANCEW, LPVOID);
#ifdef UNICODE
#define LPDIENUMDEVICESCALLBACK  LPDIENUMDEVICESCALLBACKW
#else
#define LPDIENUMDEVICESCALLBACK  LPDIENUMDEVICESCALLBACKA
#endif // !UNICODE
typedef BOOL (FAR PASCAL * LPDICONFIGUREDEVICESCALLBACK)(IUnknown FAR *, LPVOID);

#define DIEDFL_ALLDEVICES       0x00000000
#define DIEDFL_ATTACHEDONLY     0x00000001
#if(DIRECTINPUT_VERSION >= 0x0500)
#define DIEDFL_FORCEFEEDBACK    0x00000100
#endif /* DIRECTINPUT_VERSION >= 0x0500 */
#if(DIRECTINPUT_VERSION >= 0x050a)
#define DIEDFL_INCLUDEALIASES   0x00010000
#define DIEDFL_INCLUDEPHANTOMS  0x00020000
#endif /* DIRECTINPUT_VERSION >= 0x050a */
#if(DIRECTINPUT_VERSION >= 0x0800)
#define DIEDFL_INCLUDEHIDDEN    0x00040000
#endif /* DIRECTINPUT_VERSION >= 0x0800 */


#if(DIRECTINPUT_VERSION >= 0x0800)
typedef BOOL (FAR PASCAL * LPDIENUMDEVICESBYSEMANTICSCBA)(LPCDIDEVICEINSTANCEA, LPDIRECTINPUTDEVICE8A, DWORD, DWORD, LPVOID);
typedef BOOL (FAR PASCAL * LPDIENUMDEVICESBYSEMANTICSCBW)(LPCDIDEVICEINSTANCEW, LPDIRECTINPUTDEVICE8W, DWORD, DWORD, LPVOID);
#ifdef UNICODE
#define LPDIENUMDEVICESBYSEMANTICSCB  LPDIENUMDEVICESBYSEMANTICSCBW
#else
#define LPDIENUMDEVICESBYSEMANTICSCB  LPDIENUMDEVICESBYSEMANTICSCBA
#endif // !UNICODE
#endif /* DIRECTINPUT_VERSION >= 0x0800 */

#if(DIRECTINPUT_VERSION >= 0x0800)
#define DIEDBS_MAPPEDPRI1         0x00000001
#define DIEDBS_MAPPEDPRI2         0x00000002
#define DIEDBS_RECENTDEVICE       0x00000010
#define DIEDBS_NEWDEVICE          0x00000020
#endif /* DIRECTINPUT_VERSION >= 0x0800 */

#if(DIRECTINPUT_VERSION >= 0x0800)
#define DIEDBSFL_ATTACHEDONLY       0x00000000
#define DIEDBSFL_THISUSER           0x00000010
#define DIEDBSFL_FORCEFEEDBACK      DIEDFL_FORCEFEEDBACK
#define DIEDBSFL_AVAILABLEDEVICES   0x00001000
#define DIEDBSFL_MULTIMICEKEYBOARDS 0x00002000
#define DIEDBSFL_NONGAMINGDEVICES   0x00004000
#define DIEDBSFL_VALID              0x00007110
#endif /* DIRECTINPUT_VERSION >= 0x0800 */

#undef INTERFACE
#define INTERFACE IDirectInputW

DECLARE_INTERFACE_(IDirectInputW, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputW methods ***/
    STDMETHOD(CreateDevice)(THIS_ REFGUID,LPDIRECTINPUTDEVICEW *,LPUNKNOWN) PURE;
    STDMETHOD(EnumDevices)(THIS_ DWORD,LPDIENUMDEVICESCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetDeviceStatus)(THIS_ REFGUID) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD) PURE;
};

typedef struct IDirectInputW *LPDIRECTINPUTW;

#undef INTERFACE
#define INTERFACE IDirectInputA

DECLARE_INTERFACE_(IDirectInputA, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputA methods ***/
    STDMETHOD(CreateDevice)(THIS_ REFGUID,LPDIRECTINPUTDEVICEA *,LPUNKNOWN) PURE;
    STDMETHOD(EnumDevices)(THIS_ DWORD,LPDIENUMDEVICESCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetDeviceStatus)(THIS_ REFGUID) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD) PURE;
};

typedef struct IDirectInputA *LPDIRECTINPUTA;

#ifdef UNICODE
#define IID_IDirectInput IID_IDirectInputW
#define IDirectInput IDirectInputW
#define IDirectInputVtbl IDirectInputWVtbl
#else
#define IID_IDirectInput IID_IDirectInputA
#define IDirectInput IDirectInputA
#define IDirectInputVtbl IDirectInputAVtbl
#endif
typedef struct IDirectInput *LPDIRECTINPUT;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInput_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInput_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInput_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInput_CreateDevice(p,a,b,c) (p)->lpVtbl->CreateDevice(p,a,b,c)
#define IDirectInput_EnumDevices(p,a,b,c,d) (p)->lpVtbl->EnumDevices(p,a,b,c,d)
#define IDirectInput_GetDeviceStatus(p,a) (p)->lpVtbl->GetDeviceStatus(p,a)
#define IDirectInput_RunControlPanel(p,a,b) (p)->lpVtbl->RunControlPanel(p,a,b)
#define IDirectInput_Initialize(p,a,b) (p)->lpVtbl->Initialize(p,a,b)
#else
#define IDirectInput_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInput_AddRef(p) (p)->AddRef()
#define IDirectInput_Release(p) (p)->Release()
#define IDirectInput_CreateDevice(p,a,b,c) (p)->CreateDevice(a,b,c)
#define IDirectInput_EnumDevices(p,a,b,c,d) (p)->EnumDevices(a,b,c,d)
#define IDirectInput_GetDeviceStatus(p,a) (p)->GetDeviceStatus(a)
#define IDirectInput_RunControlPanel(p,a,b) (p)->RunControlPanel(a,b)
#define IDirectInput_Initialize(p,a,b) (p)->Initialize(a,b)
#endif

#undef INTERFACE
#define INTERFACE IDirectInput2W

DECLARE_INTERFACE_(IDirectInput2W, IDirectInputW)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputW methods ***/
    STDMETHOD(CreateDevice)(THIS_ REFGUID,LPDIRECTINPUTDEVICEW *,LPUNKNOWN) PURE;
    STDMETHOD(EnumDevices)(THIS_ DWORD,LPDIENUMDEVICESCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetDeviceStatus)(THIS_ REFGUID) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD) PURE;

    /*** IDirectInput2W methods ***/
    STDMETHOD(FindDevice)(THIS_ REFGUID,LPCWSTR,LPGUID) PURE;
};

typedef struct IDirectInput2W *LPDIRECTINPUT2W;

#undef INTERFACE
#define INTERFACE IDirectInput2A

DECLARE_INTERFACE_(IDirectInput2A, IDirectInputA)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInputA methods ***/
    STDMETHOD(CreateDevice)(THIS_ REFGUID,LPDIRECTINPUTDEVICEA *,LPUNKNOWN) PURE;
    STDMETHOD(EnumDevices)(THIS_ DWORD,LPDIENUMDEVICESCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetDeviceStatus)(THIS_ REFGUID) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD) PURE;

    /*** IDirectInput2A methods ***/
    STDMETHOD(FindDevice)(THIS_ REFGUID,LPCSTR,LPGUID) PURE;
};

typedef struct IDirectInput2A *LPDIRECTINPUT2A;

#ifdef UNICODE
#define IID_IDirectInput2 IID_IDirectInput2W
#define IDirectInput2 IDirectInput2W
#define IDirectInput2Vtbl IDirectInput2WVtbl
#else
#define IID_IDirectInput2 IID_IDirectInput2A
#define IDirectInput2 IDirectInput2A
#define IDirectInput2Vtbl IDirectInput2AVtbl
#endif
typedef struct IDirectInput2 *LPDIRECTINPUT2;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInput2_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInput2_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInput2_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInput2_CreateDevice(p,a,b,c) (p)->lpVtbl->CreateDevice(p,a,b,c)
#define IDirectInput2_EnumDevices(p,a,b,c,d) (p)->lpVtbl->EnumDevices(p,a,b,c,d)
#define IDirectInput2_GetDeviceStatus(p,a) (p)->lpVtbl->GetDeviceStatus(p,a)
#define IDirectInput2_RunControlPanel(p,a,b) (p)->lpVtbl->RunControlPanel(p,a,b)
#define IDirectInput2_Initialize(p,a,b) (p)->lpVtbl->Initialize(p,a,b)
#define IDirectInput2_FindDevice(p,a,b,c) (p)->lpVtbl->FindDevice(p,a,b,c)
#else
#define IDirectInput2_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInput2_AddRef(p) (p)->AddRef()
#define IDirectInput2_Release(p) (p)->Release()
#define IDirectInput2_CreateDevice(p,a,b,c) (p)->CreateDevice(a,b,c)
#define IDirectInput2_EnumDevices(p,a,b,c,d) (p)->EnumDevices(a,b,c,d)
#define IDirectInput2_GetDeviceStatus(p,a) (p)->GetDeviceStatus(a)
#define IDirectInput2_RunControlPanel(p,a,b) (p)->RunControlPanel(a,b)
#define IDirectInput2_Initialize(p,a,b) (p)->Initialize(a,b)
#define IDirectInput2_FindDevice(p,a,b,c) (p)->FindDevice(a,b,c)
#endif


#undef INTERFACE
#define INTERFACE IDirectInput7W

DECLARE_INTERFACE_(IDirectInput7W, IDirectInput2W)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInput2W methods ***/
    STDMETHOD(CreateDevice)(THIS_ REFGUID,LPDIRECTINPUTDEVICEW *,LPUNKNOWN) PURE;
    STDMETHOD(EnumDevices)(THIS_ DWORD,LPDIENUMDEVICESCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetDeviceStatus)(THIS_ REFGUID) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD) PURE;
    STDMETHOD(FindDevice)(THIS_ REFGUID,LPCWSTR,LPGUID) PURE;

    /*** IDirectInput7W methods ***/
    STDMETHOD(CreateDeviceEx)(THIS_ REFGUID,REFIID,LPVOID *,LPUNKNOWN) PURE;
};

typedef struct IDirectInput7W *LPDIRECTINPUT7W;

#undef INTERFACE
#define INTERFACE IDirectInput7A

DECLARE_INTERFACE_(IDirectInput7A, IDirectInput2A)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInput2A methods ***/
    STDMETHOD(CreateDevice)(THIS_ REFGUID,LPDIRECTINPUTDEVICEA *,LPUNKNOWN) PURE;
    STDMETHOD(EnumDevices)(THIS_ DWORD,LPDIENUMDEVICESCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetDeviceStatus)(THIS_ REFGUID) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD) PURE;
    STDMETHOD(FindDevice)(THIS_ REFGUID,LPCSTR,LPGUID) PURE;

    /*** IDirectInput7A methods ***/
    STDMETHOD(CreateDeviceEx)(THIS_ REFGUID,REFIID,LPVOID *,LPUNKNOWN) PURE;
};

typedef struct IDirectInput7A *LPDIRECTINPUT7A;

#ifdef UNICODE
#define IID_IDirectInput7 IID_IDirectInput7W
#define IDirectInput7 IDirectInput7W
#define IDirectInput7Vtbl IDirectInput7WVtbl
#else
#define IID_IDirectInput7 IID_IDirectInput7A
#define IDirectInput7 IDirectInput7A
#define IDirectInput7Vtbl IDirectInput7AVtbl
#endif
typedef struct IDirectInput7 *LPDIRECTINPUT7;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInput7_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInput7_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInput7_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInput7_CreateDevice(p,a,b,c) (p)->lpVtbl->CreateDevice(p,a,b,c)
#define IDirectInput7_EnumDevices(p,a,b,c,d) (p)->lpVtbl->EnumDevices(p,a,b,c,d)
#define IDirectInput7_GetDeviceStatus(p,a) (p)->lpVtbl->GetDeviceStatus(p,a)
#define IDirectInput7_RunControlPanel(p,a,b) (p)->lpVtbl->RunControlPanel(p,a,b)
#define IDirectInput7_Initialize(p,a,b) (p)->lpVtbl->Initialize(p,a,b)
#define IDirectInput7_FindDevice(p,a,b,c) (p)->lpVtbl->FindDevice(p,a,b,c)
#define IDirectInput7_CreateDeviceEx(p,a,b,c,d) (p)->lpVtbl->CreateDeviceEx(p,a,b,c,d)
#else
#define IDirectInput7_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInput7_AddRef(p) (p)->AddRef()
#define IDirectInput7_Release(p) (p)->Release()
#define IDirectInput7_CreateDevice(p,a,b,c) (p)->CreateDevice(a,b,c)
#define IDirectInput7_EnumDevices(p,a,b,c,d) (p)->EnumDevices(a,b,c,d)
#define IDirectInput7_GetDeviceStatus(p,a) (p)->GetDeviceStatus(a)
#define IDirectInput7_RunControlPanel(p,a,b) (p)->RunControlPanel(a,b)
#define IDirectInput7_Initialize(p,a,b) (p)->Initialize(a,b)
#define IDirectInput7_FindDevice(p,a,b,c) (p)->FindDevice(a,b,c)
#define IDirectInput7_CreateDeviceEx(p,a,b,c,d) (p)->CreateDeviceEx(a,b,c,d)
#endif

#if(DIRECTINPUT_VERSION >= 0x0800)
#undef INTERFACE
#define INTERFACE IDirectInput8W

DECLARE_INTERFACE_(IDirectInput8W, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInput8W methods ***/
    STDMETHOD(CreateDevice)(THIS_ REFGUID,LPDIRECTINPUTDEVICE8W *,LPUNKNOWN) PURE;
    STDMETHOD(EnumDevices)(THIS_ DWORD,LPDIENUMDEVICESCALLBACKW,LPVOID,DWORD) PURE;
    STDMETHOD(GetDeviceStatus)(THIS_ REFGUID) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD) PURE;
    STDMETHOD(FindDevice)(THIS_ REFGUID,LPCWSTR,LPGUID) PURE;
    STDMETHOD(EnumDevicesBySemantics)(THIS_ LPCWSTR,LPDIACTIONFORMATW,LPDIENUMDEVICESBYSEMANTICSCBW,LPVOID,DWORD) PURE;
    STDMETHOD(ConfigureDevices)(THIS_ LPDICONFIGUREDEVICESCALLBACK,LPDICONFIGUREDEVICESPARAMSW,DWORD,LPVOID) PURE;
};

typedef struct IDirectInput8W *LPDIRECTINPUT8W;

#undef INTERFACE
#define INTERFACE IDirectInput8A

DECLARE_INTERFACE_(IDirectInput8A, IUnknown)
{
    /*** IUnknown methods ***/
    STDMETHOD(QueryInterface)(THIS_ REFIID riid, LPVOID * ppvObj) PURE;
    STDMETHOD_(ULONG,AddRef)(THIS) PURE;
    STDMETHOD_(ULONG,Release)(THIS) PURE;

    /*** IDirectInput8A methods ***/
    STDMETHOD(CreateDevice)(THIS_ REFGUID,LPDIRECTINPUTDEVICE8A *,LPUNKNOWN) PURE;
    STDMETHOD(EnumDevices)(THIS_ DWORD,LPDIENUMDEVICESCALLBACKA,LPVOID,DWORD) PURE;
    STDMETHOD(GetDeviceStatus)(THIS_ REFGUID) PURE;
    STDMETHOD(RunControlPanel)(THIS_ HWND,DWORD) PURE;
    STDMETHOD(Initialize)(THIS_ HINSTANCE,DWORD) PURE;
    STDMETHOD(FindDevice)(THIS_ REFGUID,LPCSTR,LPGUID) PURE;
    STDMETHOD(EnumDevicesBySemantics)(THIS_ LPCSTR,LPDIACTIONFORMATA,LPDIENUMDEVICESBYSEMANTICSCBA,LPVOID,DWORD) PURE;
    STDMETHOD(ConfigureDevices)(THIS_ LPDICONFIGUREDEVICESCALLBACK,LPDICONFIGUREDEVICESPARAMSA,DWORD,LPVOID) PURE;
};

typedef struct IDirectInput8A *LPDIRECTINPUT8A;

#ifdef UNICODE
#define IID_IDirectInput8 IID_IDirectInput8W
#define IDirectInput8 IDirectInput8W
#define IDirectInput8Vtbl IDirectInput8WVtbl
#else
#define IID_IDirectInput8 IID_IDirectInput8A
#define IDirectInput8 IDirectInput8A
#define IDirectInput8Vtbl IDirectInput8AVtbl
#endif
typedef struct IDirectInput8 *LPDIRECTINPUT8;

#if !defined(__cplusplus) || defined(CINTERFACE)
#define IDirectInput8_QueryInterface(p,a,b) (p)->lpVtbl->QueryInterface(p,a,b)
#define IDirectInput8_AddRef(p) (p)->lpVtbl->AddRef(p)
#define IDirectInput8_Release(p) (p)->lpVtbl->Release(p)
#define IDirectInput8_CreateDevice(p,a,b,c) (p)->lpVtbl->CreateDevice(p,a,b,c)
#define IDirectInput8_EnumDevices(p,a,b,c,d) (p)->lpVtbl->EnumDevices(p,a,b,c,d)
#define IDirectInput8_GetDeviceStatus(p,a) (p)->lpVtbl->GetDeviceStatus(p,a)
#define IDirectInput8_RunControlPanel(p,a,b) (p)->lpVtbl->RunControlPanel(p,a,b)
#define IDirectInput8_Initialize(p,a,b) (p)->lpVtbl->Initialize(p,a,b)
#define IDirectInput8_FindDevice(p,a,b,c) (p)->lpVtbl->FindDevice(p,a,b,c)
#define IDirectInput8_EnumDevicesBySemantics(p,a,b,c,d,e) (p)->lpVtbl->EnumDevicesBySemantics(p,a,b,c,d,e)
#define IDirectInput8_ConfigureDevices(p,a,b,c,d) (p)->lpVtbl->ConfigureDevices(p,a,b,c,d)
#else
#define IDirectInput8_QueryInterface(p,a,b) (p)->QueryInterface(a,b)
#define IDirectInput8_AddRef(p) (p)->AddRef()
#define IDirectInput8_Release(p) (p)->Release()
#define IDirectInput8_CreateDevice(p,a,b,c) (p)->CreateDevice(a,b,c)
#define IDirectInput8_EnumDevices(p,a,b,c,d) (p)->EnumDevices(a,b,c,d)
#define IDirectInput8_GetDeviceStatus(p,a) (p)->GetDeviceStatus(a)
#define IDirectInput8_RunControlPanel(p,a,b) (p)->RunControlPanel(a,b)
#define IDirectInput8_Initialize(p,a,b) (p)->Initialize(a,b)
#define IDirectInput8_FindDevice(p,a,b,c) (p)->FindDevice(a,b,c)
#define IDirectInput8_EnumDevicesBySemantics(p,a,b,c,d,e) (p)->EnumDevicesBySemantics(a,b,c,d,e)
#define IDirectInput8_ConfigureDevices(p,a,b,c,d) (p)->ConfigureDevices(a,b,c,d)
#endif
#endif /* DIRECTINPUT_VERSION >= 0x0800 */

#if DIRECTINPUT_VERSION > 0x0700

extern HRESULT WINAPI DirectInput8Create(HINSTANCE hinst, DWORD dwVersion, REFIID riidltf, LPVOID *ppvOut, LPUNKNOWN punkOuter);

#else
extern HRESULT WINAPI DirectInputCreateA(HINSTANCE hinst, DWORD dwVersion, LPDIRECTINPUTA *ppDI, LPUNKNOWN punkOuter);
extern HRESULT WINAPI DirectInputCreateW(HINSTANCE hinst, DWORD dwVersion, LPDIRECTINPUTW *ppDI, LPUNKNOWN punkOuter);
#ifdef UNICODE
#define DirectInputCreate  DirectInputCreateW
#else
#define DirectInputCreate  DirectInputCreateA
#endif // !UNICODE

extern HRESULT WINAPI DirectInputCreateEx(HINSTANCE hinst, DWORD dwVersion, REFIID riidltf, LPVOID *ppvOut, LPUNKNOWN punkOuter);

#endif /* DIRECTINPUT_VERSION > 0x700 */

#endif /* DIJ_RINGZERO */


/****************************************************************************
 *
 *  Return Codes
 *
 ****************************************************************************/

/*
 *  The operation completed successfully.
 */
#define DI_OK                           S_OK

/*
 *  The device exists but is not currently attached.
 */
#define DI_NOTATTACHED                  S_FALSE

/*
 *  The device buffer overflowed.  Some input was lost.
 */
#define DI_BUFFEROVERFLOW               S_FALSE

/*
 *  The change in device properties had no effect.
 */
#define DI_PROPNOEFFECT                 S_FALSE

/*
 *  The operation had no effect.
 */
#define DI_NOEFFECT                     S_FALSE

/*
 *  The device is a polled device.  As a result, device buffering
 *  will not collect any data and event notifications will not be
 *  signalled until GetDeviceState is called.
 */
#define DI_POLLEDDEVICE                 ((HRESULT)0x00000002L)

/*
 *  The parameters of the effect were successfully updated by
 *  IDirectInputEffect::SetParameters, but the effect was not
 *  downloaded because the device is not exclusively acquired
 *  or because the DIEP_NODOWNLOAD flag was passed.
 */
#define DI_DOWNLOADSKIPPED              ((HRESULT)0x00000003L)

/*
 *  The parameters of the effect were successfully updated by
 *  IDirectInputEffect::SetParameters, but in order to change
 *  the parameters, the effect needed to be restarted.
 */
#define DI_EFFECTRESTARTED              ((HRESULT)0x00000004L)

/*
 *  The parameters of the effect were successfully updated by
 *  IDirectInputEffect::SetParameters, but some of them were
 *  beyond the capabilities of the device and were truncated.
 */
#define DI_TRUNCATED                    ((HRESULT)0x00000008L)

/*
 *  The settings have been successfully applied but could not be 
 *  persisted. 
 */
#define DI_SETTINGSNOTSAVED				((HRESULT)0x0000000BL)

/*
 *  Equal to DI_EFFECTRESTARTED | DI_TRUNCATED.
 */
#define DI_TRUNCATEDANDRESTARTED        ((HRESULT)0x0000000CL)

/*
 *  A SUCCESS code indicating that settings cannot be modified.
 */
#define DI_WRITEPROTECT                 ((HRESULT)0x00000013L)

/*
 *  The application requires a newer version of DirectInput.
 */
#define DIERR_OLDDIRECTINPUTVERSION     \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_OLD_WIN_VERSION)

/*
 *  The application was written for an unsupported prerelease version
 *  of DirectInput.
 */
#define DIERR_BETADIRECTINPUTVERSION    \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_RMODE_APP)

/*
 *  The object could not be created due to an incompatible driver version
 *  or mismatched or incomplete driver components.
 */
#define DIERR_BADDRIVERVER              \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_BAD_DRIVER_LEVEL)

/*
 * The device or device instance or effect is not registered with DirectInput.
 */
#define DIERR_DEVICENOTREG              REGDB_E_CLASSNOTREG

/*
 * The requested object does not exist.
 */
#define DIERR_NOTFOUND                  \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_FILE_NOT_FOUND)

/*
 * The requested object does not exist.
 */
#define DIERR_OBJECTNOTFOUND            \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_FILE_NOT_FOUND)

/*
 * An invalid parameter was passed to the returning function,
 * or the object was not in a state that admitted the function
 * to be called.
 */
#define DIERR_INVALIDPARAM              E_INVALIDARG

/*
 * The specified interface is not supported by the object
 */
#define DIERR_NOINTERFACE               E_NOINTERFACE

/*
 * An undetermined error occured inside the DInput subsystem
 */
#define DIERR_GENERIC                   E_FAIL

/*
 * The DInput subsystem couldn't allocate sufficient memory to complete the
 * caller's request.
 */
#define DIERR_OUTOFMEMORY               E_OUTOFMEMORY

/*
 * The function called is not supported at this time
 */
#define DIERR_UNSUPPORTED               E_NOTIMPL

/*
 * This object has not been initialized
 */
#define DIERR_NOTINITIALIZED            \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_NOT_READY)

/*
 * This object is already initialized
 */
#define DIERR_ALREADYINITIALIZED        \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_ALREADY_INITIALIZED)

/*
 * This object does not support aggregation
 */
#define DIERR_NOAGGREGATION             CLASS_E_NOAGGREGATION

/*
 * Another app has a higher priority level, preventing this call from
 * succeeding.
 */
#define DIERR_OTHERAPPHASPRIO           E_ACCESSDENIED

/*
 * Access to the device has been lost.  It must be re-acquired.
 */
#define DIERR_INPUTLOST                 \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_READ_FAULT)

/*
 * The operation cannot be performed while the device is acquired.
 */
#define DIERR_ACQUIRED                  \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_BUSY)

/*
 * The operation cannot be performed unless the device is acquired.
 */
#define DIERR_NOTACQUIRED               \
    MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, ERROR_INVALID_ACCESS)

/*
 * The specified property cannot be changed.
 */
#define DIERR_READONLY                  E_ACCESSDENIED

/*
 * The device already has an event notification associated with it.
 */
#define DIERR_HANDLEEXISTS              E_ACCESSDENIED

/*
 * Data is not yet available.
 */
#ifndef E_PENDING
#define E_PENDING                       0x8000000AL
#endif

/*
 * Unable to IDirectInputJoyConfig_Acquire because the user
 * does not have sufficient privileges to change the joystick
 * configuration.
 */
#define DIERR_INSUFFICIENTPRIVS         0x80040200L

/*
 * The device is full.
 */
#define DIERR_DEVICEFULL                0x80040201L

/*
 * Not all the requested information fit into the buffer.
 */
#define DIERR_MOREDATA                  0x80040202L

/*
 * The effect is not downloaded.
 */
#define DIERR_NOTDOWNLOADED             0x80040203L

/*
 *  The device cannot be reinitialized because there are still effects
 *  attached to it.
 */
#define DIERR_HASEFFECTS                0x80040204L

/*
 *  The operation cannot be performed unless the device is acquired
 *  in DISCL_EXCLUSIVE mode.
 */
#define DIERR_NOTEXCLUSIVEACQUIRED      0x80040205L

/*
 *  The effect could not be downloaded because essential information
 *  is missing.  For example, no axes have been associated with the
 *  effect, or no type-specific information has been created.
 */
#define DIERR_INCOMPLETEEFFECT          0x80040206L

/*
 *  Attempted to read buffered device data from a device that is
 *  not buffered.
 */
#define DIERR_NOTBUFFERED               0x80040207L

/*
 *  An attempt was made to modify parameters of an effect while it is
 *  playing.  Not all hardware devices support altering the parameters
 *  of an effect while it is playing.
 */
#define DIERR_EFFECTPLAYING             0x80040208L

/*
 *  The operation could not be completed because the device is not
 *  plugged in.
 */
#define DIERR_UNPLUGGED                 0x80040209L

/*
 *  SendDeviceData failed because more information was requested
 *  to be sent than can be sent to the device.  Some devices have
 *  restrictions on how much data can be sent to them.  (For example,
 *  there might be a limit on the number of buttons that can be
 *  pressed at once.)
 */
#define DIERR_REPORTFULL                0x8004020AL


/*
 *  A mapper file function failed because reading or writing the user or IHV 
 *  settings file failed.
 */
#define DIERR_MAPFILEFAIL               0x8004020BL


/*--- DINPUT Mapper Definitions: New for Dx8         ---*/


/*--- Keyboard
      Physical Keyboard Device       ---*/

#define DIKEYBOARD_ESCAPE                       0x81000401
#define DIKEYBOARD_1                            0x81000402
#define DIKEYBOARD_2                            0x81000403
#define DIKEYBOARD_3                            0x81000404
#define DIKEYBOARD_4                            0x81000405
#define DIKEYBOARD_5                            0x81000406
#define DIKEYBOARD_6                            0x81000407
#define DIKEYBOARD_7                            0x81000408
#define DIKEYBOARD_8                            0x81000409
#define DIKEYBOARD_9                            0x8100040A
#define DIKEYBOARD_0                            0x8100040B
#define DIKEYBOARD_MINUS                        0x8100040C    /* - on main keyboard */
#define DIKEYBOARD_EQUALS                       0x8100040D
#define DIKEYBOARD_BACK                         0x8100040E    /* backspace */
#define DIKEYBOARD_TAB                          0x8100040F
#define DIKEYBOARD_Q                            0x81000410
#define DIKEYBOARD_W                            0x81000411
#define DIKEYBOARD_E                            0x81000412
#define DIKEYBOARD_R                            0x81000413
#define DIKEYBOARD_T                            0x81000414
#define DIKEYBOARD_Y                            0x81000415
#define DIKEYBOARD_U                            0x81000416
#define DIKEYBOARD_I                            0x81000417
#define DIKEYBOARD_O                            0x81000418
#define DIKEYBOARD_P                            0x81000419
#define DIKEYBOARD_LBRACKET                     0x8100041A
#define DIKEYBOARD_RBRACKET                     0x8100041B
#define DIKEYBOARD_RETURN                       0x8100041C    /* Enter on main keyboard */
#define DIKEYBOARD_LCONTROL                     0x8100041D
#define DIKEYBOARD_A                            0x8100041E
#define DIKEYBOARD_S                            0x8100041F
#define DIKEYBOARD_D                            0x81000420
#define DIKEYBOARD_F                            0x81000421
#define DIKEYBOARD_G                            0x81000422
#define DIKEYBOARD_H                            0x81000423
#define DIKEYBOARD_J                            0x81000424
#define DIKEYBOARD_K                            0x81000425
#define DIKEYBOARD_L                            0x81000426
#define DIKEYBOARD_SEMICOLON                    0x81000427
#define DIKEYBOARD_APOSTROPHE                   0x81000428
#define DIKEYBOARD_GRAVE                        0x81000429    /* accent grave */
#define DIKEYBOARD_LSHIFT                       0x8100042A
#define DIKEYBOARD_BACKSLASH                    0x8100042B
#define DIKEYBOARD_Z                            0x8100042C
#define DIKEYBOARD_X                            0x8100042D
#define DIKEYBOARD_C                            0x8100042E
#define DIKEYBOARD_V                            0x8100042F
#define DIKEYBOARD_B                            0x81000430
#define DIKEYBOARD_N                            0x81000431
#define DIKEYBOARD_M                            0x81000432
#define DIKEYBOARD_COMMA                        0x81000433
#define DIKEYBOARD_PERIOD                       0x81000434    /* . on main keyboard */
#define DIKEYBOARD_SLASH                        0x81000435    /* / on main keyboard */
#define DIKEYBOARD_RSHIFT                       0x81000436
#define DIKEYBOARD_MULTIPLY                     0x81000437    /* * on numeric keypad */
#define DIKEYBOARD_LMENU                        0x81000438    /* left Alt */
#define DIKEYBOARD_SPACE                        0x81000439
#define DIKEYBOARD_CAPITAL                      0x8100043A
#define DIKEYBOARD_F1                           0x8100043B
#define DIKEYBOARD_F2                           0x8100043C
#define DIKEYBOARD_F3                           0x8100043D
#define DIKEYBOARD_F4                           0x8100043E
#define DIKEYBOARD_F5                           0x8100043F
#define DIKEYBOARD_F6                           0x81000440
#define DIKEYBOARD_F7                           0x81000441
#define DIKEYBOARD_F8                           0x81000442
#define DIKEYBOARD_F9                           0x81000443
#define DIKEYBOARD_F10                          0x81000444
#define DIKEYBOARD_NUMLOCK                      0x81000445
#define DIKEYBOARD_SCROLL                       0x81000446    /* Scroll Lock */
#define DIKEYBOARD_NUMPAD7                      0x81000447
#define DIKEYBOARD_NUMPAD8                      0x81000448
#define DIKEYBOARD_NUMPAD9                      0x81000449
#define DIKEYBOARD_SUBTRACT                     0x8100044A    /* - on numeric keypad */
#define DIKEYBOARD_NUMPAD4                      0x8100044B
#define DIKEYBOARD_NUMPAD5                      0x8100044C
#define DIKEYBOARD_NUMPAD6                      0x8100044D
#define DIKEYBOARD_ADD                          0x8100044E    /* + on numeric keypad */
#define DIKEYBOARD_NUMPAD1                      0x8100044F
#define DIKEYBOARD_NUMPAD2                      0x81000450
#define DIKEYBOARD_NUMPAD3                      0x81000451
#define DIKEYBOARD_NUMPAD0                      0x81000452
#define DIKEYBOARD_DECIMAL                      0x81000453    /* . on numeric keypad */
#define DIKEYBOARD_OEM_102                      0x81000456    /* <> or \| on RT 102-key keyboard (Non-U.S.) */
#define DIKEYBOARD_F11                          0x81000457
#define DIKEYBOARD_F12                          0x81000458
#define DIKEYBOARD_F13                          0x81000464    /*                     (NEC PC98) */
#define DIKEYBOARD_F14                          0x81000465    /*                     (NEC PC98) */
#define DIKEYBOARD_F15                          0x81000466    /*                     (NEC PC98) */
#define DIKEYBOARD_KANA                         0x81000470    /* (Japanese keyboard)            */
#define DIKEYBOARD_ABNT_C1                      0x81000473    /* /? on Brazilian keyboard */
#define DIKEYBOARD_CONVERT                      0x81000479    /* (Japanese keyboard)            */
#define DIKEYBOARD_NOCONVERT                    0x8100047B    /* (Japanese keyboard)            */
#define DIKEYBOARD_YEN                          0x8100047D    /* (Japanese keyboard)            */
#define DIKEYBOARD_ABNT_C2                      0x8100047E    /* Numpad . on Brazilian keyboard */
#define DIKEYBOARD_NUMPADEQUALS                 0x8100048D    /* = on numeric keypad (NEC PC98) */
#define DIKEYBOARD_PREVTRACK                    0x81000490    /* Previous Track (DIK_CIRCUMFLEX on Japanese keyboard) */
#define DIKEYBOARD_AT                           0x81000491    /*                     (NEC PC98) */
#define DIKEYBOARD_COLON                        0x81000492    /*                     (NEC PC98) */
#define DIKEYBOARD_UNDERLINE                    0x81000493    /*                     (NEC PC98) */
#define DIKEYBOARD_KANJI                        0x81000494    /* (Japanese keyboard)            */
#define DIKEYBOARD_STOP                         0x81000495    /*                     (NEC PC98) */
#define DIKEYBOARD_AX                           0x81000496    /*                     (Japan AX) */
#define DIKEYBOARD_UNLABELED                    0x81000497    /*                        (J3100) */
#define DIKEYBOARD_NEXTTRACK                    0x81000499    /* Next Track */
#define DIKEYBOARD_NUMPADENTER                  0x8100049C    /* Enter on numeric keypad */
#define DIKEYBOARD_RCONTROL                     0x8100049D
#define DIKEYBOARD_MUTE                         0x810004A0    /* Mute */
#define DIKEYBOARD_CALCULATOR                   0x810004A1    /* Calculator */
#define DIKEYBOARD_PLAYPAUSE                    0x810004A2    /* Play / Pause */
#define DIKEYBOARD_MEDIASTOP                    0x810004A4    /* Media Stop */
#define DIKEYBOARD_VOLUMEDOWN                   0x810004AE    /* Volume - */
#define DIKEYBOARD_VOLUMEUP                     0x810004B0    /* Volume + */
#define DIKEYBOARD_WEBHOME                      0x810004B2    /* Web home */
#define DIKEYBOARD_NUMPADCOMMA                  0x810004B3    /* , on numeric keypad (NEC PC98) */
#define DIKEYBOARD_DIVIDE                       0x810004B5    /* / on numeric keypad */
#define DIKEYBOARD_SYSRQ                        0x810004B7
#define DIKEYBOARD_RMENU                        0x810004B8    /* right Alt */
#define DIKEYBOARD_PAUSE                        0x810004C5    /* Pause */
#define DIKEYBOARD_HOME                         0x810004C7    /* Home on arrow keypad */
#define DIKEYBOARD_UP                           0x810004C8    /* UpArrow on arrow keypad */
#define DIKEYBOARD_PRIOR                        0x810004C9    /* PgUp on arrow keypad */
#define DIKEYBOARD_LEFT                         0x810004CB    /* LeftArrow on arrow keypad */
#define DIKEYBOARD_RIGHT                        0x810004CD    /* RightArrow on arrow keypad */
#define DIKEYBOARD_END                          0x810004CF    /* End on arrow keypad */
#define DIKEYBOARD_DOWN                         0x810004D0    /* DownArrow on arrow keypad */
#define DIKEYBOARD_NEXT                         0x810004D1    /* PgDn on arrow keypad */
#define DIKEYBOARD_INSERT                       0x810004D2    /* Insert on arrow keypad */
#define DIKEYBOARD_DELETE                       0x810004D3    /* Delete on arrow keypad */
#define DIKEYBOARD_LWIN                         0x810004DB    /* Left Windows key */
#define DIKEYBOARD_RWIN                         0x810004DC    /* Right Windows key */
#define DIKEYBOARD_APPS                         0x810004DD    /* AppMenu key */
#define DIKEYBOARD_POWER                        0x810004DE    /* System Power */
#define DIKEYBOARD_SLEEP                        0x810004DF    /* System Sleep */
#define DIKEYBOARD_WAKE                         0x810004E3    /* System Wake */
#define DIKEYBOARD_WEBSEARCH                    0x810004E5    /* Web Search */
#define DIKEYBOARD_WEBFAVORITES                 0x810004E6    /* Web Favorites */
#define DIKEYBOARD_WEBREFRESH                   0x810004E7    /* Web Refresh */
#define DIKEYBOARD_WEBSTOP                      0x810004E8    /* Web Stop */
#define DIKEYBOARD_WEBFORWARD                   0x810004E9    /* Web Forward */
#define DIKEYBOARD_WEBBACK                      0x810004EA    /* Web Back */
#define DIKEYBOARD_MYCOMPUTER                   0x810004EB    /* My Computer */
#define DIKEYBOARD_MAIL                         0x810004EC    /* Mail */
#define DIKEYBOARD_MEDIASELECT                  0x810004ED    /* Media Select */
  

/*--- MOUSE
      Physical Mouse Device             ---*/

#define DIMOUSE_XAXISAB                         (0x82000200 |DIMOFS_X ) /* X Axis-absolute: Some mice natively report absolute coordinates  */ 
#define DIMOUSE_YAXISAB                         (0x82000200 |DIMOFS_Y ) /* Y Axis-absolute: Some mice natively report absolute coordinates */
#define DIMOUSE_XAXIS                           (0x82000300 |DIMOFS_X ) /* X Axis */
#define DIMOUSE_YAXIS                           (0x82000300 |DIMOFS_Y ) /* Y Axis */
#define DIMOUSE_WHEEL                           (0x82000300 |DIMOFS_Z ) /* Z Axis */
#define DIMOUSE_BUTTON0                         (0x82000400 |DIMOFS_BUTTON0) /* Button 0 */
#define DIMOUSE_BUTTON1                         (0x82000400 |DIMOFS_BUTTON1) /* Button 1 */
#define DIMOUSE_BUTTON2                         (0x82000400 |DIMOFS_BUTTON2) /* Button 2 */
#define DIMOUSE_BUTTON3                         (0x82000400 |DIMOFS_BUTTON3) /* Button 3 */
#define DIMOUSE_BUTTON4                         (0x82000400 |DIMOFS_BUTTON4) /* Button 4 */
#define DIMOUSE_BUTTON5                         (0x82000400 |DIMOFS_BUTTON5) /* Button 5 */
#define DIMOUSE_BUTTON6                         (0x82000400 |DIMOFS_BUTTON6) /* Button 6 */
#define DIMOUSE_BUTTON7                         (0x82000400 |DIMOFS_BUTTON7) /* Button 7 */


/*--- VOICE
      Physical Dplay Voice Device       ---*/

#define DIVOICE_CHANNEL1                        0x83000401
#define DIVOICE_CHANNEL2                        0x83000402
#define DIVOICE_CHANNEL3                        0x83000403
#define DIVOICE_CHANNEL4                        0x83000404
#define DIVOICE_CHANNEL5                        0x83000405
#define DIVOICE_CHANNEL6                        0x83000406
#define DIVOICE_CHANNEL7                        0x83000407
#define DIVOICE_CHANNEL8                        0x83000408
#define DIVOICE_TEAM                            0x83000409
#define DIVOICE_ALL                             0x8300040A
#define DIVOICE_RECORDMUTE                      0x8300040B
#define DIVOICE_PLAYBACKMUTE                    0x8300040C
#define DIVOICE_TRANSMIT                        0x8300040D

#define DIVOICE_VOICECOMMAND                    0x83000410


/*--- Driving Simulator - Racing
      Vehicle control is primary objective  ---*/
#define DIVIRTUAL_DRIVING_RACE                  0x01000000
#define DIAXIS_DRIVINGR_STEER                   0x01008A01 /* Steering */
#define DIAXIS_DRIVINGR_ACCELERATE              0x01039202 /* Accelerate */
#define DIAXIS_DRIVINGR_BRAKE                   0x01041203 /* Brake-Axis */
#define DIBUTTON_DRIVINGR_SHIFTUP               0x01000C01 /* Shift to next higher gear */
#define DIBUTTON_DRIVINGR_SHIFTDOWN             0x01000C02 /* Shift to next lower gear */
#define DIBUTTON_DRIVINGR_VIEW                  0x01001C03 /* Cycle through view options */
#define DIBUTTON_DRIVINGR_MENU                  0x010004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIAXIS_DRIVINGR_ACCEL_AND_BRAKE         0x01014A04 /* Some devices combine accelerate and brake in a single axis */
#define DIHATSWITCH_DRIVINGR_GLANCE             0x01004601 /* Look around */
#define DIBUTTON_DRIVINGR_BRAKE                 0x01004C04 /* Brake-button */
#define DIBUTTON_DRIVINGR_DASHBOARD             0x01004405 /* Select next dashboard option */
#define DIBUTTON_DRIVINGR_AIDS                  0x01004406 /* Driver correction aids */
#define DIBUTTON_DRIVINGR_MAP                   0x01004407 /* Display Driving Map */
#define DIBUTTON_DRIVINGR_BOOST                 0x01004408 /* Turbo Boost */
#define DIBUTTON_DRIVINGR_PIT                   0x01004409 /* Pit stop notification */
#define DIBUTTON_DRIVINGR_ACCELERATE_LINK       0x0103D4E0 /* Fallback Accelerate button */
#define DIBUTTON_DRIVINGR_STEER_LEFT_LINK       0x0100CCE4 /* Fallback Steer Left button */
#define DIBUTTON_DRIVINGR_STEER_RIGHT_LINK      0x0100CCEC /* Fallback Steer Right button */
#define DIBUTTON_DRIVINGR_GLANCE_LEFT_LINK      0x0107C4E4 /* Fallback Glance Left button */
#define DIBUTTON_DRIVINGR_GLANCE_RIGHT_LINK     0x0107C4EC /* Fallback Glance Right button */
#define DIBUTTON_DRIVINGR_DEVICE                0x010044FE /* Show input device and controls */
#define DIBUTTON_DRIVINGR_PAUSE                 0x010044FC /* Start / Pause / Restart game */

/*--- Driving Simulator - Combat
      Combat from within a vehicle is primary objective  ---*/
#define DIVIRTUAL_DRIVING_COMBAT                0x02000000
#define DIAXIS_DRIVINGC_STEER                   0x02008A01 /* Steering  */
#define DIAXIS_DRIVINGC_ACCELERATE              0x02039202 /* Accelerate */
#define DIAXIS_DRIVINGC_BRAKE                   0x02041203 /* Brake-axis */
#define DIBUTTON_DRIVINGC_FIRE                  0x02000C01 /* Fire */
#define DIBUTTON_DRIVINGC_WEAPONS               0x02000C02 /* Select next weapon */
#define DIBUTTON_DRIVINGC_TARGET                0x02000C03 /* Select next available target */
#define DIBUTTON_DRIVINGC_MENU                  0x020004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIAXIS_DRIVINGC_ACCEL_AND_BRAKE         0x02014A04 /* Some devices combine accelerate and brake in a single axis */
#define DIHATSWITCH_DRIVINGC_GLANCE             0x02004601 /* Look around */
#define DIBUTTON_DRIVINGC_SHIFTUP               0x02004C04 /* Shift to next higher gear */
#define DIBUTTON_DRIVINGC_SHIFTDOWN             0x02004C05 /* Shift to next lower gear */
#define DIBUTTON_DRIVINGC_DASHBOARD             0x02004406 /* Select next dashboard option */
#define DIBUTTON_DRIVINGC_AIDS                  0x02004407 /* Driver correction aids */
#define DIBUTTON_DRIVINGC_BRAKE                 0x02004C08 /* Brake-button */
#define DIBUTTON_DRIVINGC_FIRESECONDARY         0x02004C09 /* Alternative fire button */
#define DIBUTTON_DRIVINGC_ACCELERATE_LINK       0x0203D4E0 /* Fallback Accelerate button */
#define DIBUTTON_DRIVINGC_STEER_LEFT_LINK       0x0200CCE4 /* Fallback Steer Left button */
#define DIBUTTON_DRIVINGC_STEER_RIGHT_LINK      0x0200CCEC /* Fallback Steer Right button */
#define DIBUTTON_DRIVINGC_GLANCE_LEFT_LINK      0x0207C4E4 /* Fallback Glance Left button */
#define DIBUTTON_DRIVINGC_GLANCE_RIGHT_LINK     0x0207C4EC /* Fallback Glance Right button */
#define DIBUTTON_DRIVINGC_DEVICE                0x020044FE /* Show input device and controls */
#define DIBUTTON_DRIVINGC_PAUSE                 0x020044FC /* Start / Pause / Restart game */

/*--- Driving Simulator - Tank
      Combat from withing a tank is primary objective  ---*/
#define DIVIRTUAL_DRIVING_TANK                  0x03000000
#define DIAXIS_DRIVINGT_STEER                   0x03008A01 /* Turn tank left / right */
#define DIAXIS_DRIVINGT_BARREL                  0x03010202 /* Raise / lower barrel */
#define DIAXIS_DRIVINGT_ACCELERATE              0x03039203 /* Accelerate */
#define DIAXIS_DRIVINGT_ROTATE                  0x03020204 /* Turn barrel left / right */
#define DIBUTTON_DRIVINGT_FIRE                  0x03000C01 /* Fire */
#define DIBUTTON_DRIVINGT_WEAPONS               0x03000C02 /* Select next weapon */
#define DIBUTTON_DRIVINGT_TARGET                0x03000C03 /* Selects next available target */
#define DIBUTTON_DRIVINGT_MENU                  0x030004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_DRIVINGT_GLANCE             0x03004601 /* Look around */
#define DIAXIS_DRIVINGT_BRAKE                   0x03045205 /* Brake-axis */
#define DIAXIS_DRIVINGT_ACCEL_AND_BRAKE         0x03014A06 /* Some devices combine accelerate and brake in a single axis */
#define DIBUTTON_DRIVINGT_VIEW                  0x03005C04 /* Cycle through view options */
#define DIBUTTON_DRIVINGT_DASHBOARD             0x03005C05 /* Select next dashboard option */
#define DIBUTTON_DRIVINGT_BRAKE                 0x03004C06 /* Brake-button */
#define DIBUTTON_DRIVINGT_FIRESECONDARY         0x03004C07 /* Alternative fire button */
#define DIBUTTON_DRIVINGT_ACCELERATE_LINK       0x0303D4E0 /* Fallback Accelerate button */
#define DIBUTTON_DRIVINGT_STEER_LEFT_LINK       0x0300CCE4 /* Fallback Steer Left button */
#define DIBUTTON_DRIVINGT_STEER_RIGHT_LINK      0x0300CCEC /* Fallback Steer Right button */
#define DIBUTTON_DRIVINGT_BARREL_UP_LINK        0x030144E0 /* Fallback Barrel up button */
#define DIBUTTON_DRIVINGT_BARREL_DOWN_LINK      0x030144E8 /* Fallback Barrel down button */
#define DIBUTTON_DRIVINGT_ROTATE_LEFT_LINK      0x030244E4 /* Fallback Rotate left button */
#define DIBUTTON_DRIVINGT_ROTATE_RIGHT_LINK     0x030244EC /* Fallback Rotate right button */
#define DIBUTTON_DRIVINGT_GLANCE_LEFT_LINK      0x0307C4E4 /* Fallback Glance Left button */
#define DIBUTTON_DRIVINGT_GLANCE_RIGHT_LINK     0x0307C4EC /* Fallback Glance Right button */
#define DIBUTTON_DRIVINGT_DEVICE                0x030044FE /* Show input device and controls */
#define DIBUTTON_DRIVINGT_PAUSE                 0x030044FC /* Start / Pause / Restart game */

/*--- Flight Simulator - Civilian 
      Plane control is the primary objective  ---*/
#define DIVIRTUAL_FLYING_CIVILIAN               0x04000000
#define DIAXIS_FLYINGC_BANK                     0x04008A01 /* Roll ship left / right */
#define DIAXIS_FLYINGC_PITCH                    0x04010A02 /* Nose up / down */
#define DIAXIS_FLYINGC_THROTTLE                 0x04039203 /* Throttle */
#define DIBUTTON_FLYINGC_VIEW                   0x04002401 /* Cycle through view options */
#define DIBUTTON_FLYINGC_DISPLAY                0x04002402 /* Select next dashboard / heads up display option */
#define DIBUTTON_FLYINGC_GEAR                   0x04002C03 /* Gear up / down */
#define DIBUTTON_FLYINGC_MENU                   0x040004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_FLYINGC_GLANCE              0x04004601 /* Look around */
#define DIAXIS_FLYINGC_BRAKE                    0x04046A04 /* Apply Brake */
#define DIAXIS_FLYINGC_RUDDER                   0x04025205 /* Yaw ship left/right */
#define DIAXIS_FLYINGC_FLAPS                    0x04055A06 /* Flaps */
#define DIBUTTON_FLYINGC_FLAPSUP                0x04006404 /* Increment stepping up until fully retracted */
#define DIBUTTON_FLYINGC_FLAPSDOWN              0x04006405 /* Decrement stepping down until fully extended */
#define DIBUTTON_FLYINGC_BRAKE_LINK             0x04046CE0 /* Fallback brake button */
#define DIBUTTON_FLYINGC_FASTER_LINK            0x0403D4E0 /* Fallback throttle up button */
#define DIBUTTON_FLYINGC_SLOWER_LINK            0x0403D4E8 /* Fallback throttle down button */
#define DIBUTTON_FLYINGC_GLANCE_LEFT_LINK       0x0407C4E4 /* Fallback Glance Left button */
#define DIBUTTON_FLYINGC_GLANCE_RIGHT_LINK      0x0407C4EC /* Fallback Glance Right button */
#define DIBUTTON_FLYINGC_GLANCE_UP_LINK         0x0407C4E0 /* Fallback Glance Up button */
#define DIBUTTON_FLYINGC_GLANCE_DOWN_LINK       0x0407C4E8 /* Fallback Glance Down button */
#define DIBUTTON_FLYINGC_DEVICE                 0x040044FE /* Show input device and controls */
#define DIBUTTON_FLYINGC_PAUSE                  0x040044FC /* Start / Pause / Restart game */

/*--- Flight Simulator - Military 
      Aerial combat is the primary objective  ---*/
#define DIVIRTUAL_FLYING_MILITARY               0x05000000
#define DIAXIS_FLYINGM_BANK                     0x05008A01 /* Bank - Roll ship left / right */
#define DIAXIS_FLYINGM_PITCH                    0x05010A02 /* Pitch - Nose up / down */
#define DIAXIS_FLYINGM_THROTTLE                 0x05039203 /* Throttle - faster / slower */
#define DIBUTTON_FLYINGM_FIRE                   0x05000C01 /* Fire */
#define DIBUTTON_FLYINGM_WEAPONS                0x05000C02 /* Select next weapon */
#define DIBUTTON_FLYINGM_TARGET                 0x05000C03 /* Selects next available target */
#define DIBUTTON_FLYINGM_MENU                   0x050004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_FLYINGM_GLANCE              0x05004601 /* Look around */
#define DIBUTTON_FLYINGM_COUNTER                0x05005C04 /* Activate counter measures */
#define DIAXIS_FLYINGM_RUDDER                   0x05024A04 /* Rudder - Yaw ship left/right */
#define DIAXIS_FLYINGM_BRAKE                    0x05046205 /* Brake-axis */
#define DIBUTTON_FLYINGM_VIEW                   0x05006405 /* Cycle through view options */
#define DIBUTTON_FLYINGM_DISPLAY                0x05006406 /* Select next dashboard option */
#define DIAXIS_FLYINGM_FLAPS                    0x05055206 /* Flaps */
#define DIBUTTON_FLYINGM_FLAPSUP                0x05005407 /* Increment stepping up until fully retracted */
#define DIBUTTON_FLYINGM_FLAPSDOWN              0x05005408 /* Decrement stepping down until fully extended */
#define DIBUTTON_FLYINGM_FIRESECONDARY          0x05004C09 /* Alternative fire button */
#define DIBUTTON_FLYINGM_GEAR                   0x0500640A /* Gear up / down */
#define DIBUTTON_FLYINGM_BRAKE_LINK             0x050464E0 /* Fallback brake button */
#define DIBUTTON_FLYINGM_FASTER_LINK            0x0503D4E0 /* Fallback throttle up button */
#define DIBUTTON_FLYINGM_SLOWER_LINK            0x0503D4E8 /* Fallback throttle down button */
#define DIBUTTON_FLYINGM_GLANCE_LEFT_LINK       0x0507C4E4 /* Fallback Glance Left button */
#define DIBUTTON_FLYINGM_GLANCE_RIGHT_LINK      0x0507C4EC /* Fallback Glance Right button */
#define DIBUTTON_FLYINGM_GLANCE_UP_LINK         0x0507C4E0 /* Fallback Glance Up button */
#define DIBUTTON_FLYINGM_GLANCE_DOWN_LINK       0x0507C4E8 /* Fallback Glance Down button */
#define DIBUTTON_FLYINGM_DEVICE                 0x050044FE /* Show input device and controls */
#define DIBUTTON_FLYINGM_PAUSE                  0x050044FC /* Start / Pause / Restart game */

/*--- Flight Simulator - Combat Helicopter
      Combat from helicopter is primary objective  ---*/
#define DIVIRTUAL_FLYING_HELICOPTER             0x06000000
#define DIAXIS_FLYINGH_BANK                     0x06008A01 /* Bank - Roll ship left / right */
#define DIAXIS_FLYINGH_PITCH                    0x06010A02 /* Pitch - Nose up / down */
#define DIAXIS_FLYINGH_COLLECTIVE               0x06018A03 /* Collective - Blade pitch/power */
#define DIBUTTON_FLYINGH_FIRE                   0x06001401 /* Fire */
#define DIBUTTON_FLYINGH_WEAPONS                0x06001402 /* Select next weapon */
#define DIBUTTON_FLYINGH_TARGET                 0x06001403 /* Selects next available target */
#define DIBUTTON_FLYINGH_MENU                   0x060004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_FLYINGH_GLANCE              0x06004601 /* Look around */
#define DIAXIS_FLYINGH_TORQUE                   0x06025A04 /* Torque - Rotate ship around left / right axis */
#define DIAXIS_FLYINGH_THROTTLE                 0x0603DA05 /* Throttle */
#define DIBUTTON_FLYINGH_COUNTER                0x06005404 /* Activate counter measures */
#define DIBUTTON_FLYINGH_VIEW                   0x06006405 /* Cycle through view options */
#define DIBUTTON_FLYINGH_GEAR                   0x06006406 /* Gear up / down */
#define DIBUTTON_FLYINGH_FIRESECONDARY          0x06004C07 /* Alternative fire button */
#define DIBUTTON_FLYINGH_FASTER_LINK            0x0603DCE0 /* Fallback throttle up button */
#define DIBUTTON_FLYINGH_SLOWER_LINK            0x0603DCE8 /* Fallback throttle down button */
#define DIBUTTON_FLYINGH_GLANCE_LEFT_LINK       0x0607C4E4 /* Fallback Glance Left button */
#define DIBUTTON_FLYINGH_GLANCE_RIGHT_LINK      0x0607C4EC /* Fallback Glance Right button */
#define DIBUTTON_FLYINGH_GLANCE_UP_LINK         0x0607C4E0 /* Fallback Glance Up button */
#define DIBUTTON_FLYINGH_GLANCE_DOWN_LINK       0x0607C4E8 /* Fallback Glance Down button */
#define DIBUTTON_FLYINGH_DEVICE                 0x060044FE /* Show input device and controls */
#define DIBUTTON_FLYINGH_PAUSE                  0x060044FC /* Start / Pause / Restart game */

/*--- Space Simulator - Combat
      Space Simulator with weapons  ---*/
#define DIVIRTUAL_SPACESIM                      0x07000000
#define DIAXIS_SPACESIM_LATERAL                 0x07008201 /* Move ship left / right */
#define DIAXIS_SPACESIM_MOVE                    0x07010202 /* Move ship forward/backward */
#define DIAXIS_SPACESIM_THROTTLE                0x07038203 /* Throttle - Engine speed */
#define DIBUTTON_SPACESIM_FIRE                  0x07000401 /* Fire */
#define DIBUTTON_SPACESIM_WEAPONS               0x07000402 /* Select next weapon */
#define DIBUTTON_SPACESIM_TARGET                0x07000403 /* Selects next available target */
#define DIBUTTON_SPACESIM_MENU                  0x070004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_SPACESIM_GLANCE             0x07004601 /* Look around */
#define DIAXIS_SPACESIM_CLIMB                   0x0701C204 /* Climb - Pitch ship up/down */
#define DIAXIS_SPACESIM_ROTATE                  0x07024205 /* Rotate - Turn ship left/right */
#define DIBUTTON_SPACESIM_VIEW                  0x07004404 /* Cycle through view options */
#define DIBUTTON_SPACESIM_DISPLAY               0x07004405 /* Select next dashboard / heads up display option */
#define DIBUTTON_SPACESIM_RAISE                 0x07004406 /* Raise ship while maintaining current pitch */
#define DIBUTTON_SPACESIM_LOWER                 0x07004407 /* Lower ship while maintaining current pitch */
#define DIBUTTON_SPACESIM_GEAR                  0x07004408 /* Gear up / down */
#define DIBUTTON_SPACESIM_FIRESECONDARY         0x07004409 /* Alternative fire button */
#define DIBUTTON_SPACESIM_LEFT_LINK             0x0700C4E4 /* Fallback move left button */
#define DIBUTTON_SPACESIM_RIGHT_LINK            0x0700C4EC /* Fallback move right button */
#define DIBUTTON_SPACESIM_FORWARD_LINK          0x070144E0 /* Fallback move forward button */
#define DIBUTTON_SPACESIM_BACKWARD_LINK         0x070144E8 /* Fallback move backwards button */
#define DIBUTTON_SPACESIM_FASTER_LINK           0x0703C4E0 /* Fallback throttle up button */
#define DIBUTTON_SPACESIM_SLOWER_LINK           0x0703C4E8 /* Fallback throttle down button */
#define DIBUTTON_SPACESIM_TURN_LEFT_LINK        0x070244E4 /* Fallback turn left button */
#define DIBUTTON_SPACESIM_TURN_RIGHT_LINK       0x070244EC /* Fallback turn right button */
#define DIBUTTON_SPACESIM_GLANCE_LEFT_LINK      0x0707C4E4 /* Fallback Glance Left button */
#define DIBUTTON_SPACESIM_GLANCE_RIGHT_LINK     0x0707C4EC /* Fallback Glance Right button */
#define DIBUTTON_SPACESIM_GLANCE_UP_LINK        0x0707C4E0 /* Fallback Glance Up button */
#define DIBUTTON_SPACESIM_GLANCE_DOWN_LINK      0x0707C4E8 /* Fallback Glance Down button */
#define DIBUTTON_SPACESIM_DEVICE                0x070044FE /* Show input device and controls */
#define DIBUTTON_SPACESIM_PAUSE                 0x070044FC /* Start / Pause / Restart game */

/*--- Fighting - First Person 
      Hand to Hand combat is primary objective  ---*/
#define DIVIRTUAL_FIGHTING_HAND2HAND            0x08000000
#define DIAXIS_FIGHTINGH_LATERAL                0x08008201 /* Sidestep left/right */
#define DIAXIS_FIGHTINGH_MOVE                   0x08010202 /* Move forward/backward */
#define DIBUTTON_FIGHTINGH_PUNCH                0x08000401 /* Punch */
#define DIBUTTON_FIGHTINGH_KICK                 0x08000402 /* Kick */
#define DIBUTTON_FIGHTINGH_BLOCK                0x08000403 /* Block */
#define DIBUTTON_FIGHTINGH_CROUCH               0x08000404 /* Crouch */
#define DIBUTTON_FIGHTINGH_JUMP                 0x08000405 /* Jump */
#define DIBUTTON_FIGHTINGH_SPECIAL1             0x08000406 /* Apply first special move */
#define DIBUTTON_FIGHTINGH_SPECIAL2             0x08000407 /* Apply second special move */
#define DIBUTTON_FIGHTINGH_MENU                 0x080004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_FIGHTINGH_SELECT               0x08004408 /* Select special move */
#define DIHATSWITCH_FIGHTINGH_SLIDE             0x08004601 /* Look around */
#define DIBUTTON_FIGHTINGH_DISPLAY              0x08004409 /* Shows next on-screen display option */
#define DIAXIS_FIGHTINGH_ROTATE                 0x08024203 /* Rotate - Turn body left/right */
#define DIBUTTON_FIGHTINGH_DODGE                0x0800440A /* Dodge */
#define DIBUTTON_FIGHTINGH_LEFT_LINK            0x0800C4E4 /* Fallback left sidestep button */
#define DIBUTTON_FIGHTINGH_RIGHT_LINK           0x0800C4EC /* Fallback right sidestep button */
#define DIBUTTON_FIGHTINGH_FORWARD_LINK         0x080144E0 /* Fallback forward button */
#define DIBUTTON_FIGHTINGH_BACKWARD_LINK        0x080144E8 /* Fallback backward button */
#define DIBUTTON_FIGHTINGH_DEVICE               0x080044FE /* Show input device and controls */
#define DIBUTTON_FIGHTINGH_PAUSE                0x080044FC /* Start / Pause / Restart game */

/*--- Fighting - First Person Shooting
      Navigation and combat are primary objectives  ---*/
#define DIVIRTUAL_FIGHTING_FPS                  0x09000000
#define DIAXIS_FPS_ROTATE                       0x09008201 /* Rotate character left/right */
#define DIAXIS_FPS_MOVE                         0x09010202 /* Move forward/backward */
#define DIBUTTON_FPS_FIRE                       0x09000401 /* Fire */
#define DIBUTTON_FPS_WEAPONS                    0x09000402 /* Select next weapon */
#define DIBUTTON_FPS_APPLY                      0x09000403 /* Use item */
#define DIBUTTON_FPS_SELECT                     0x09000404 /* Select next inventory item */
#define DIBUTTON_FPS_CROUCH                     0x09000405 /* Crouch/ climb down/ swim down */
#define DIBUTTON_FPS_JUMP                       0x09000406 /* Jump/ climb up/ swim up */
#define DIAXIS_FPS_LOOKUPDOWN                   0x09018203 /* Look up / down  */
#define DIBUTTON_FPS_STRAFE                     0x09000407 /* Enable strafing while active */
#define DIBUTTON_FPS_MENU                       0x090004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_FPS_GLANCE                  0x09004601 /* Look around */
#define DIBUTTON_FPS_DISPLAY                    0x09004408 /* Shows next on-screen display option/ map */
#define DIAXIS_FPS_SIDESTEP                     0x09024204 /* Sidestep */
#define DIBUTTON_FPS_DODGE                      0x09004409 /* Dodge */
#define DIBUTTON_FPS_GLANCEL                    0x0900440A /* Glance Left */
#define DIBUTTON_FPS_GLANCER                    0x0900440B /* Glance Right */
#define DIBUTTON_FPS_FIRESECONDARY              0x0900440C /* Alternative fire button */
#define DIBUTTON_FPS_ROTATE_LEFT_LINK           0x0900C4E4 /* Fallback rotate left button */
#define DIBUTTON_FPS_ROTATE_RIGHT_LINK          0x0900C4EC /* Fallback rotate right button */
#define DIBUTTON_FPS_FORWARD_LINK               0x090144E0 /* Fallback forward button */
#define DIBUTTON_FPS_BACKWARD_LINK              0x090144E8 /* Fallback backward button */
#define DIBUTTON_FPS_GLANCE_UP_LINK             0x0901C4E0 /* Fallback look up button */
#define DIBUTTON_FPS_GLANCE_DOWN_LINK           0x0901C4E8 /* Fallback look down button */
#define DIBUTTON_FPS_STEP_LEFT_LINK             0x090244E4 /* Fallback step left button */
#define DIBUTTON_FPS_STEP_RIGHT_LINK            0x090244EC /* Fallback step right button */
#define DIBUTTON_FPS_DEVICE                     0x090044FE /* Show input device and controls */
#define DIBUTTON_FPS_PAUSE                      0x090044FC /* Start / Pause / Restart game */

/*--- Fighting - Third Person action
      Perspective of camera is behind the main character  ---*/
#define DIVIRTUAL_FIGHTING_THIRDPERSON          0x0A000000
#define DIAXIS_TPS_TURN                         0x0A020201 /* Turn left/right */
#define DIAXIS_TPS_MOVE                         0x0A010202 /* Move forward/backward */
#define DIBUTTON_TPS_RUN                        0x0A000401 /* Run or walk toggle switch */
#define DIBUTTON_TPS_ACTION                     0x0A000402 /* Action Button */
#define DIBUTTON_TPS_SELECT                     0x0A000403 /* Select next weapon */
#define DIBUTTON_TPS_USE                        0x0A000404 /* Use inventory item currently selected */
#define DIBUTTON_TPS_JUMP                       0x0A000405 /* Character Jumps */
#define DIBUTTON_TPS_MENU                       0x0A0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_TPS_GLANCE                  0x0A004601 /* Look around */
#define DIBUTTON_TPS_VIEW                       0x0A004406 /* Select camera view */
#define DIBUTTON_TPS_STEPLEFT                   0x0A004407 /* Character takes a left step */
#define DIBUTTON_TPS_STEPRIGHT                  0x0A004408 /* Character takes a right step */
#define DIAXIS_TPS_STEP                         0x0A00C203 /* Character steps left/right */
#define DIBUTTON_TPS_DODGE                      0x0A004409 /* Character dodges or ducks */
#define DIBUTTON_TPS_INVENTORY                  0x0A00440A /* Cycle through inventory */
#define DIBUTTON_TPS_TURN_LEFT_LINK             0x0A0244E4 /* Fallback turn left button */
#define DIBUTTON_TPS_TURN_RIGHT_LINK            0x0A0244EC /* Fallback turn right button */
#define DIBUTTON_TPS_FORWARD_LINK               0x0A0144E0 /* Fallback forward button */
#define DIBUTTON_TPS_BACKWARD_LINK              0x0A0144E8 /* Fallback backward button */
#define DIBUTTON_TPS_GLANCE_UP_LINK             0x0A07C4E0 /* Fallback look up button */
#define DIBUTTON_TPS_GLANCE_DOWN_LINK           0x0A07C4E8 /* Fallback look down button */
#define DIBUTTON_TPS_GLANCE_LEFT_LINK           0x0A07C4E4 /* Fallback glance up button */
#define DIBUTTON_TPS_GLANCE_RIGHT_LINK          0x0A07C4EC /* Fallback glance right button */
#define DIBUTTON_TPS_DEVICE                     0x0A0044FE /* Show input device and controls */
#define DIBUTTON_TPS_PAUSE                      0x0A0044FC /* Start / Pause / Restart game */

/*--- Strategy - Role Playing
      Navigation and problem solving are primary actions  ---*/
#define DIVIRTUAL_STRATEGY_ROLEPLAYING          0x0B000000
#define DIAXIS_STRATEGYR_LATERAL                0x0B008201 /* sidestep - left/right */
#define DIAXIS_STRATEGYR_MOVE                   0x0B010202 /* move forward/backward */
#define DIBUTTON_STRATEGYR_GET                  0x0B000401 /* Acquire item */
#define DIBUTTON_STRATEGYR_APPLY                0x0B000402 /* Use selected item */
#define DIBUTTON_STRATEGYR_SELECT               0x0B000403 /* Select nextitem */
#define DIBUTTON_STRATEGYR_ATTACK               0x0B000404 /* Attack */
#define DIBUTTON_STRATEGYR_CAST                 0x0B000405 /* Cast Spell */
#define DIBUTTON_STRATEGYR_CROUCH               0x0B000406 /* Crouch */
#define DIBUTTON_STRATEGYR_JUMP                 0x0B000407 /* Jump */
#define DIBUTTON_STRATEGYR_MENU                 0x0B0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_STRATEGYR_GLANCE            0x0B004601 /* Look around */
#define DIBUTTON_STRATEGYR_MAP                  0x0B004408 /* Cycle through map options */
#define DIBUTTON_STRATEGYR_DISPLAY              0x0B004409 /* Shows next on-screen display option */
#define DIAXIS_STRATEGYR_ROTATE                 0x0B024203 /* Turn body left/right */
#define DIBUTTON_STRATEGYR_LEFT_LINK            0x0B00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_STRATEGYR_RIGHT_LINK           0x0B00C4EC /* Fallback sidestep right button */
#define DIBUTTON_STRATEGYR_FORWARD_LINK         0x0B0144E0 /* Fallback move forward button */
#define DIBUTTON_STRATEGYR_BACK_LINK            0x0B0144E8 /* Fallback move backward button */
#define DIBUTTON_STRATEGYR_ROTATE_LEFT_LINK     0x0B0244E4 /* Fallback turn body left button */
#define DIBUTTON_STRATEGYR_ROTATE_RIGHT_LINK    0x0B0244EC /* Fallback turn body right button */
#define DIBUTTON_STRATEGYR_DEVICE               0x0B0044FE /* Show input device and controls */
#define DIBUTTON_STRATEGYR_PAUSE                0x0B0044FC /* Start / Pause / Restart game */

/*--- Strategy - Turn based
      Navigation and problem solving are primary actions  ---*/
#define DIVIRTUAL_STRATEGY_TURN                 0x0C000000
#define DIAXIS_STRATEGYT_LATERAL                0x0C008201 /* Sidestep left/right */
#define DIAXIS_STRATEGYT_MOVE                   0x0C010202 /* Move forward/backwards */
#define DIBUTTON_STRATEGYT_SELECT               0x0C000401 /* Select unit or object */
#define DIBUTTON_STRATEGYT_INSTRUCT             0x0C000402 /* Cycle through instructions */
#define DIBUTTON_STRATEGYT_APPLY                0x0C000403 /* Apply selected instruction */
#define DIBUTTON_STRATEGYT_TEAM                 0x0C000404 /* Select next team / cycle through all */
#define DIBUTTON_STRATEGYT_TURN                 0x0C000405 /* Indicate turn over */
#define DIBUTTON_STRATEGYT_MENU                 0x0C0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_STRATEGYT_ZOOM                 0x0C004406 /* Zoom - in / out */
#define DIBUTTON_STRATEGYT_MAP                  0x0C004407 /* cycle through map options */
#define DIBUTTON_STRATEGYT_DISPLAY              0x0C004408 /* shows next on-screen display options */
#define DIBUTTON_STRATEGYT_LEFT_LINK            0x0C00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_STRATEGYT_RIGHT_LINK           0x0C00C4EC /* Fallback sidestep right button */
#define DIBUTTON_STRATEGYT_FORWARD_LINK         0x0C0144E0 /* Fallback move forward button */
#define DIBUTTON_STRATEGYT_BACK_LINK            0x0C0144E8 /* Fallback move back button */
#define DIBUTTON_STRATEGYT_DEVICE               0x0C0044FE /* Show input device and controls */
#define DIBUTTON_STRATEGYT_PAUSE                0x0C0044FC /* Start / Pause / Restart game */

/*--- Sports - Hunting
      Hunting                ---*/
#define DIVIRTUAL_SPORTS_HUNTING                0x0D000000
#define DIAXIS_HUNTING_LATERAL                  0x0D008201 /* sidestep left/right */
#define DIAXIS_HUNTING_MOVE                     0x0D010202 /* move forward/backwards */
#define DIBUTTON_HUNTING_FIRE                   0x0D000401 /* Fire selected weapon */
#define DIBUTTON_HUNTING_AIM                    0x0D000402 /* Select aim/move */
#define DIBUTTON_HUNTING_WEAPON                 0x0D000403 /* Select next weapon */
#define DIBUTTON_HUNTING_BINOCULAR              0x0D000404 /* Look through Binoculars */
#define DIBUTTON_HUNTING_CALL                   0x0D000405 /* Make animal call */
#define DIBUTTON_HUNTING_MAP                    0x0D000406 /* View Map */
#define DIBUTTON_HUNTING_SPECIAL                0x0D000407 /* Special game operation */
#define DIBUTTON_HUNTING_MENU                   0x0D0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_HUNTING_GLANCE              0x0D004601 /* Look around */
#define DIBUTTON_HUNTING_DISPLAY                0x0D004408 /* show next on-screen display option */
#define DIAXIS_HUNTING_ROTATE                   0x0D024203 /* Turn body left/right */
#define DIBUTTON_HUNTING_CROUCH                 0x0D004409 /* Crouch/ Climb / Swim down */
#define DIBUTTON_HUNTING_JUMP                   0x0D00440A /* Jump/ Climb up / Swim up */
#define DIBUTTON_HUNTING_FIRESECONDARY          0x0D00440B /* Alternative fire button */
#define DIBUTTON_HUNTING_LEFT_LINK              0x0D00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_HUNTING_RIGHT_LINK             0x0D00C4EC /* Fallback sidestep right button */
#define DIBUTTON_HUNTING_FORWARD_LINK           0x0D0144E0 /* Fallback move forward button */
#define DIBUTTON_HUNTING_BACK_LINK              0x0D0144E8 /* Fallback move back button */
#define DIBUTTON_HUNTING_ROTATE_LEFT_LINK       0x0D0244E4 /* Fallback turn body left button */
#define DIBUTTON_HUNTING_ROTATE_RIGHT_LINK      0x0D0244EC /* Fallback turn body right button */
#define DIBUTTON_HUNTING_DEVICE                 0x0D0044FE /* Show input device and controls */
#define DIBUTTON_HUNTING_PAUSE                  0x0D0044FC /* Start / Pause / Restart game */

/*--- Sports - Fishing
      Catching Fish is primary objective   ---*/
#define DIVIRTUAL_SPORTS_FISHING                0x0E000000
#define DIAXIS_FISHING_LATERAL                  0x0E008201 /* sidestep left/right */
#define DIAXIS_FISHING_MOVE                     0x0E010202 /* move forward/backwards */
#define DIBUTTON_FISHING_CAST                   0x0E000401 /* Cast line */
#define DIBUTTON_FISHING_TYPE                   0x0E000402 /* Select cast type */
#define DIBUTTON_FISHING_BINOCULAR              0x0E000403 /* Look through Binocular */
#define DIBUTTON_FISHING_BAIT                   0x0E000404 /* Select type of Bait */
#define DIBUTTON_FISHING_MAP                    0x0E000405 /* View Map */
#define DIBUTTON_FISHING_MENU                   0x0E0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_FISHING_GLANCE              0x0E004601 /* Look around */
#define DIBUTTON_FISHING_DISPLAY                0x0E004406 /* Show next on-screen display option */
#define DIAXIS_FISHING_ROTATE                   0x0E024203 /* Turn character left / right */
#define DIBUTTON_FISHING_CROUCH                 0x0E004407 /* Crouch/ Climb / Swim down */
#define DIBUTTON_FISHING_JUMP                   0x0E004408 /* Jump/ Climb up / Swim up */
#define DIBUTTON_FISHING_LEFT_LINK              0x0E00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_FISHING_RIGHT_LINK             0x0E00C4EC /* Fallback sidestep right button */
#define DIBUTTON_FISHING_FORWARD_LINK           0x0E0144E0 /* Fallback move forward button */
#define DIBUTTON_FISHING_BACK_LINK              0x0E0144E8 /* Fallback move back button */
#define DIBUTTON_FISHING_ROTATE_LEFT_LINK       0x0E0244E4 /* Fallback turn body left button */
#define DIBUTTON_FISHING_ROTATE_RIGHT_LINK      0x0E0244EC /* Fallback turn body right button */
#define DIBUTTON_FISHING_DEVICE                 0x0E0044FE /* Show input device and controls */
#define DIBUTTON_FISHING_PAUSE                  0x0E0044FC /* Start / Pause / Restart game */

/*--- Sports - Baseball - Batting
      Batter control is primary objective  ---*/
#define DIVIRTUAL_SPORTS_BASEBALL_BAT           0x0F000000
#define DIAXIS_BASEBALLB_LATERAL                0x0F008201 /* Aim left / right */
#define DIAXIS_BASEBALLB_MOVE                   0x0F010202 /* Aim up / down */
#define DIBUTTON_BASEBALLB_SELECT               0x0F000401 /* cycle through swing options */
#define DIBUTTON_BASEBALLB_NORMAL               0x0F000402 /* normal swing */
#define DIBUTTON_BASEBALLB_POWER                0x0F000403 /* swing for the fence */
#define DIBUTTON_BASEBALLB_BUNT                 0x0F000404 /* bunt */
#define DIBUTTON_BASEBALLB_STEAL                0x0F000405 /* Base runner attempts to steal a base */
#define DIBUTTON_BASEBALLB_BURST                0x0F000406 /* Base runner invokes burst of speed */
#define DIBUTTON_BASEBALLB_SLIDE                0x0F000407 /* Base runner slides into base */
#define DIBUTTON_BASEBALLB_CONTACT              0x0F000408 /* Contact swing */
#define DIBUTTON_BASEBALLB_MENU                 0x0F0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_BASEBALLB_NOSTEAL              0x0F004409 /* Base runner goes back to a base */
#define DIBUTTON_BASEBALLB_BOX                  0x0F00440A /* Enter or exit batting box */
#define DIBUTTON_BASEBALLB_LEFT_LINK            0x0F00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_BASEBALLB_RIGHT_LINK           0x0F00C4EC /* Fallback sidestep right button */
#define DIBUTTON_BASEBALLB_FORWARD_LINK         0x0F0144E0 /* Fallback move forward button */
#define DIBUTTON_BASEBALLB_BACK_LINK            0x0F0144E8 /* Fallback move back button */
#define DIBUTTON_BASEBALLB_DEVICE               0x0F0044FE /* Show input device and controls */
#define DIBUTTON_BASEBALLB_PAUSE                0x0F0044FC /* Start / Pause / Restart game */

/*--- Sports - Baseball - Pitching
      Pitcher control is primary objective   ---*/
#define DIVIRTUAL_SPORTS_BASEBALL_PITCH         0x10000000
#define DIAXIS_BASEBALLP_LATERAL                0x10008201 /* Aim left / right */
#define DIAXIS_BASEBALLP_MOVE                   0x10010202 /* Aim up / down */
#define DIBUTTON_BASEBALLP_SELECT               0x10000401 /* cycle through pitch selections */
#define DIBUTTON_BASEBALLP_PITCH                0x10000402 /* throw pitch */
#define DIBUTTON_BASEBALLP_BASE                 0x10000403 /* select base to throw to */
#define DIBUTTON_BASEBALLP_THROW                0x10000404 /* throw to base */
#define DIBUTTON_BASEBALLP_FAKE                 0x10000405 /* Fake a throw to a base */
#define DIBUTTON_BASEBALLP_MENU                 0x100004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_BASEBALLP_WALK                 0x10004406 /* Throw intentional walk / pitch out */
#define DIBUTTON_BASEBALLP_LOOK                 0x10004407 /* Look at runners on bases */
#define DIBUTTON_BASEBALLP_LEFT_LINK            0x1000C4E4 /* Fallback sidestep left button */
#define DIBUTTON_BASEBALLP_RIGHT_LINK           0x1000C4EC /* Fallback sidestep right button */
#define DIBUTTON_BASEBALLP_FORWARD_LINK         0x100144E0 /* Fallback move forward button */
#define DIBUTTON_BASEBALLP_BACK_LINK            0x100144E8 /* Fallback move back button */
#define DIBUTTON_BASEBALLP_DEVICE               0x100044FE /* Show input device and controls */
#define DIBUTTON_BASEBALLP_PAUSE                0x100044FC /* Start / Pause / Restart game */

/*--- Sports - Baseball - Fielding
      Fielder control is primary objective  ---*/
#define DIVIRTUAL_SPORTS_BASEBALL_FIELD         0x11000000
#define DIAXIS_BASEBALLF_LATERAL                0x11008201 /* Aim left / right */
#define DIAXIS_BASEBALLF_MOVE                   0x11010202 /* Aim up / down */
#define DIBUTTON_BASEBALLF_NEAREST              0x11000401 /* Switch to fielder nearest to the ball */
#define DIBUTTON_BASEBALLF_THROW1               0x11000402 /* Make conservative throw */
#define DIBUTTON_BASEBALLF_THROW2               0x11000403 /* Make aggressive throw */
#define DIBUTTON_BASEBALLF_BURST                0x11000404 /* Invoke burst of speed */
#define DIBUTTON_BASEBALLF_JUMP                 0x11000405 /* Jump to catch ball */
#define DIBUTTON_BASEBALLF_DIVE                 0x11000406 /* Dive to catch ball */
#define DIBUTTON_BASEBALLF_MENU                 0x110004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_BASEBALLF_SHIFTIN              0x11004407 /* Shift the infield positioning */
#define DIBUTTON_BASEBALLF_SHIFTOUT             0x11004408 /* Shift the outfield positioning */
#define DIBUTTON_BASEBALLF_AIM_LEFT_LINK        0x1100C4E4 /* Fallback aim left button */
#define DIBUTTON_BASEBALLF_AIM_RIGHT_LINK       0x1100C4EC /* Fallback aim right button */
#define DIBUTTON_BASEBALLF_FORWARD_LINK         0x110144E0 /* Fallback move forward button */
#define DIBUTTON_BASEBALLF_BACK_LINK            0x110144E8 /* Fallback move back button */
#define DIBUTTON_BASEBALLF_DEVICE               0x110044FE /* Show input device and controls */
#define DIBUTTON_BASEBALLF_PAUSE                0x110044FC /* Start / Pause / Restart game */

/*--- Sports - Basketball - Offense
      Offense  ---*/
#define DIVIRTUAL_SPORTS_BASKETBALL_OFFENSE     0x12000000
#define DIAXIS_BBALLO_LATERAL                   0x12008201 /* left / right */
#define DIAXIS_BBALLO_MOVE                      0x12010202 /* up / down */
#define DIBUTTON_BBALLO_SHOOT                   0x12000401 /* shoot basket */
#define DIBUTTON_BBALLO_DUNK                    0x12000402 /* dunk basket */
#define DIBUTTON_BBALLO_PASS                    0x12000403 /* throw pass */
#define DIBUTTON_BBALLO_FAKE                    0x12000404 /* fake shot or pass */
#define DIBUTTON_BBALLO_SPECIAL                 0x12000405 /* apply special move */
#define DIBUTTON_BBALLO_PLAYER                  0x12000406 /* select next player */
#define DIBUTTON_BBALLO_BURST                   0x12000407 /* invoke burst */
#define DIBUTTON_BBALLO_CALL                    0x12000408 /* call for ball / pass to me */
#define DIBUTTON_BBALLO_MENU                    0x120004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_BBALLO_GLANCE               0x12004601 /* scroll view */
#define DIBUTTON_BBALLO_SCREEN                  0x12004409 /* Call for screen */
#define DIBUTTON_BBALLO_PLAY                    0x1200440A /* Call for specific offensive play */
#define DIBUTTON_BBALLO_JAB                     0x1200440B /* Initiate fake drive to basket */
#define DIBUTTON_BBALLO_POST                    0x1200440C /* Perform post move */
#define DIBUTTON_BBALLO_TIMEOUT                 0x1200440D /* Time Out */
#define DIBUTTON_BBALLO_SUBSTITUTE              0x1200440E /* substitute one player for another */
#define DIBUTTON_BBALLO_LEFT_LINK               0x1200C4E4 /* Fallback sidestep left button */
#define DIBUTTON_BBALLO_RIGHT_LINK              0x1200C4EC /* Fallback sidestep right button */
#define DIBUTTON_BBALLO_FORWARD_LINK            0x120144E0 /* Fallback move forward button */
#define DIBUTTON_BBALLO_BACK_LINK               0x120144E8 /* Fallback move back button */
#define DIBUTTON_BBALLO_DEVICE                  0x120044FE /* Show input device and controls */
#define DIBUTTON_BBALLO_PAUSE                   0x120044FC /* Start / Pause / Restart game */

/*--- Sports - Basketball - Defense
      Defense  ---*/
#define DIVIRTUAL_SPORTS_BASKETBALL_DEFENSE     0x13000000
#define DIAXIS_BBALLD_LATERAL                   0x13008201 /* left / right */
#define DIAXIS_BBALLD_MOVE                      0x13010202 /* up / down */
#define DIBUTTON_BBALLD_JUMP                    0x13000401 /* jump to block shot */
#define DIBUTTON_BBALLD_STEAL                   0x13000402 /* attempt to steal ball */
#define DIBUTTON_BBALLD_FAKE                    0x13000403 /* fake block or steal */
#define DIBUTTON_BBALLD_SPECIAL                 0x13000404 /* apply special move */
#define DIBUTTON_BBALLD_PLAYER                  0x13000405 /* select next player */
#define DIBUTTON_BBALLD_BURST                   0x13000406 /* invoke burst */
#define DIBUTTON_BBALLD_PLAY                    0x13000407 /* call for specific defensive play */
#define DIBUTTON_BBALLD_MENU                    0x130004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_BBALLD_GLANCE               0x13004601 /* scroll view */
#define DIBUTTON_BBALLD_TIMEOUT                 0x13004408 /* Time Out */
#define DIBUTTON_BBALLD_SUBSTITUTE              0x13004409 /* substitute one player for another */
#define DIBUTTON_BBALLD_LEFT_LINK               0x1300C4E4 /* Fallback sidestep left button */
#define DIBUTTON_BBALLD_RIGHT_LINK              0x1300C4EC /* Fallback sidestep right button */
#define DIBUTTON_BBALLD_FORWARD_LINK            0x130144E0 /* Fallback move forward button */
#define DIBUTTON_BBALLD_BACK_LINK               0x130144E8 /* Fallback move back button */
#define DIBUTTON_BBALLD_DEVICE                  0x130044FE /* Show input device and controls */
#define DIBUTTON_BBALLD_PAUSE                   0x130044FC /* Start / Pause / Restart game */

/*--- Sports - Football - Play
      Play selection  ---*/
#define DIVIRTUAL_SPORTS_FOOTBALL_FIELD         0x14000000
#define DIBUTTON_FOOTBALLP_PLAY                 0x14000401 /* cycle through available plays */
#define DIBUTTON_FOOTBALLP_SELECT               0x14000402 /* select play */
#define DIBUTTON_FOOTBALLP_HELP                 0x14000403 /* Bring up pop-up help */
#define DIBUTTON_FOOTBALLP_MENU                 0x140004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_FOOTBALLP_DEVICE               0x140044FE /* Show input device and controls */
#define DIBUTTON_FOOTBALLP_PAUSE                0x140044FC /* Start / Pause / Restart game */

/*--- Sports - Football - QB
      Offense: Quarterback / Kicker  ---*/
#define DIVIRTUAL_SPORTS_FOOTBALL_QBCK          0x15000000
#define DIAXIS_FOOTBALLQ_LATERAL                0x15008201 /* Move / Aim: left / right */
#define DIAXIS_FOOTBALLQ_MOVE                   0x15010202 /* Move / Aim: up / down */
#define DIBUTTON_FOOTBALLQ_SELECT               0x15000401 /* Select */
#define DIBUTTON_FOOTBALLQ_SNAP                 0x15000402 /* snap ball - start play */
#define DIBUTTON_FOOTBALLQ_JUMP                 0x15000403 /* jump over defender */
#define DIBUTTON_FOOTBALLQ_SLIDE                0x15000404 /* Dive/Slide */
#define DIBUTTON_FOOTBALLQ_PASS                 0x15000405 /* throws pass to receiver */
#define DIBUTTON_FOOTBALLQ_FAKE                 0x15000406 /* pump fake pass or fake kick */
#define DIBUTTON_FOOTBALLQ_MENU                 0x150004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_FOOTBALLQ_FAKESNAP             0x15004407 /* Fake snap  */
#define DIBUTTON_FOOTBALLQ_MOTION               0x15004408 /* Send receivers in motion */
#define DIBUTTON_FOOTBALLQ_AUDIBLE              0x15004409 /* Change offensive play at line of scrimmage */
#define DIBUTTON_FOOTBALLQ_LEFT_LINK            0x1500C4E4 /* Fallback sidestep left button */
#define DIBUTTON_FOOTBALLQ_RIGHT_LINK           0x1500C4EC /* Fallback sidestep right button */
#define DIBUTTON_FOOTBALLQ_FORWARD_LINK         0x150144E0 /* Fallback move forward button */
#define DIBUTTON_FOOTBALLQ_BACK_LINK            0x150144E8 /* Fallback move back button */
#define DIBUTTON_FOOTBALLQ_DEVICE               0x150044FE /* Show input device and controls */
#define DIBUTTON_FOOTBALLQ_PAUSE                0x150044FC /* Start / Pause / Restart game */

/*--- Sports - Football - Offense
      Offense - Runner  ---*/
#define DIVIRTUAL_SPORTS_FOOTBALL_OFFENSE       0x16000000
#define DIAXIS_FOOTBALLO_LATERAL                0x16008201 /* Move / Aim: left / right */
#define DIAXIS_FOOTBALLO_MOVE                   0x16010202 /* Move / Aim: up / down */
#define DIBUTTON_FOOTBALLO_JUMP                 0x16000401 /* jump or hurdle over defender */
#define DIBUTTON_FOOTBALLO_LEFTARM              0x16000402 /* holds out left arm */
#define DIBUTTON_FOOTBALLO_RIGHTARM             0x16000403 /* holds out right arm */
#define DIBUTTON_FOOTBALLO_THROW                0x16000404 /* throw pass or lateral ball to another runner */
#define DIBUTTON_FOOTBALLO_SPIN                 0x16000405 /* Spin to avoid defenders */
#define DIBUTTON_FOOTBALLO_MENU                 0x160004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_FOOTBALLO_JUKE                 0x16004406 /* Use special move to avoid defenders */
#define DIBUTTON_FOOTBALLO_SHOULDER             0x16004407 /* Lower shoulder to run over defenders */
#define DIBUTTON_FOOTBALLO_TURBO                0x16004408 /* Speed burst past defenders */
#define DIBUTTON_FOOTBALLO_DIVE                 0x16004409 /* Dive over defenders */
#define DIBUTTON_FOOTBALLO_ZOOM                 0x1600440A /* Zoom view in / out */
#define DIBUTTON_FOOTBALLO_SUBSTITUTE           0x1600440B /* substitute one player for another */
#define DIBUTTON_FOOTBALLO_LEFT_LINK            0x1600C4E4 /* Fallback sidestep left button */
#define DIBUTTON_FOOTBALLO_RIGHT_LINK           0x1600C4EC /* Fallback sidestep right button */
#define DIBUTTON_FOOTBALLO_FORWARD_LINK         0x160144E0 /* Fallback move forward button */
#define DIBUTTON_FOOTBALLO_BACK_LINK            0x160144E8 /* Fallback move back button */
#define DIBUTTON_FOOTBALLO_DEVICE               0x160044FE /* Show input device and controls */
#define DIBUTTON_FOOTBALLO_PAUSE                0x160044FC /* Start / Pause / Restart game */

/*--- Sports - Football - Defense
      Defense     ---*/
#define DIVIRTUAL_SPORTS_FOOTBALL_DEFENSE       0x17000000
#define DIAXIS_FOOTBALLD_LATERAL                0x17008201 /* Move / Aim: left / right */
#define DIAXIS_FOOTBALLD_MOVE                   0x17010202 /* Move / Aim: up / down */
#define DIBUTTON_FOOTBALLD_PLAY                 0x17000401 /* cycle through available plays */
#define DIBUTTON_FOOTBALLD_SELECT               0x17000402 /* select player closest to the ball */
#define DIBUTTON_FOOTBALLD_JUMP                 0x17000403 /* jump to intercept or block */
#define DIBUTTON_FOOTBALLD_TACKLE               0x17000404 /* tackler runner */
#define DIBUTTON_FOOTBALLD_FAKE                 0x17000405 /* hold down to fake tackle or intercept */
#define DIBUTTON_FOOTBALLD_SUPERTACKLE          0x17000406 /* Initiate special tackle */
#define DIBUTTON_FOOTBALLD_MENU                 0x170004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_FOOTBALLD_SPIN                 0x17004407 /* Spin to beat offensive line */
#define DIBUTTON_FOOTBALLD_SWIM                 0x17004408 /* Swim to beat the offensive line */
#define DIBUTTON_FOOTBALLD_BULLRUSH             0x17004409 /* Bull rush the offensive line */
#define DIBUTTON_FOOTBALLD_RIP                  0x1700440A /* Rip the offensive line */
#define DIBUTTON_FOOTBALLD_AUDIBLE              0x1700440B /* Change defensive play at the line of scrimmage */
#define DIBUTTON_FOOTBALLD_ZOOM                 0x1700440C /* Zoom view in / out */
#define DIBUTTON_FOOTBALLD_SUBSTITUTE           0x1700440D /* substitute one player for another */
#define DIBUTTON_FOOTBALLD_LEFT_LINK            0x1700C4E4 /* Fallback sidestep left button */
#define DIBUTTON_FOOTBALLD_RIGHT_LINK           0x1700C4EC /* Fallback sidestep right button */
#define DIBUTTON_FOOTBALLD_FORWARD_LINK         0x170144E0 /* Fallback move forward button */
#define DIBUTTON_FOOTBALLD_BACK_LINK            0x170144E8 /* Fallback move back button */
#define DIBUTTON_FOOTBALLD_DEVICE               0x170044FE /* Show input device and controls */
#define DIBUTTON_FOOTBALLD_PAUSE                0x170044FC /* Start / Pause / Restart game */

/*--- Sports - Golf
                                ---*/
#define DIVIRTUAL_SPORTS_GOLF                   0x18000000
#define DIAXIS_GOLF_LATERAL                     0x18008201 /* Move / Aim: left / right */
#define DIAXIS_GOLF_MOVE                        0x18010202 /* Move / Aim: up / down */
#define DIBUTTON_GOLF_SWING                     0x18000401 /* swing club */
#define DIBUTTON_GOLF_SELECT                    0x18000402 /* cycle between: club / swing strength / ball arc / ball spin */
#define DIBUTTON_GOLF_UP                        0x18000403 /* increase selection */
#define DIBUTTON_GOLF_DOWN                      0x18000404 /* decrease selection */
#define DIBUTTON_GOLF_TERRAIN                   0x18000405 /* shows terrain detail */
#define DIBUTTON_GOLF_FLYBY                     0x18000406 /* view the hole via a flyby */
#define DIBUTTON_GOLF_MENU                      0x180004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_GOLF_SCROLL                 0x18004601 /* scroll view */
#define DIBUTTON_GOLF_ZOOM                      0x18004407 /* Zoom view in / out */
#define DIBUTTON_GOLF_TIMEOUT                   0x18004408 /* Call for time out */
#define DIBUTTON_GOLF_SUBSTITUTE                0x18004409 /* substitute one player for another */
#define DIBUTTON_GOLF_LEFT_LINK                 0x1800C4E4 /* Fallback sidestep left button */
#define DIBUTTON_GOLF_RIGHT_LINK                0x1800C4EC /* Fallback sidestep right button */
#define DIBUTTON_GOLF_FORWARD_LINK              0x180144E0 /* Fallback move forward button */
#define DIBUTTON_GOLF_BACK_LINK                 0x180144E8 /* Fallback move back button */
#define DIBUTTON_GOLF_DEVICE                    0x180044FE /* Show input device and controls */
#define DIBUTTON_GOLF_PAUSE                     0x180044FC /* Start / Pause / Restart game */

/*--- Sports - Hockey - Offense
      Offense       ---*/
#define DIVIRTUAL_SPORTS_HOCKEY_OFFENSE         0x19000000
#define DIAXIS_HOCKEYO_LATERAL                  0x19008201 /* Move / Aim: left / right */
#define DIAXIS_HOCKEYO_MOVE                     0x19010202 /* Move / Aim: up / down */
#define DIBUTTON_HOCKEYO_SHOOT                  0x19000401 /* Shoot */
#define DIBUTTON_HOCKEYO_PASS                   0x19000402 /* pass the puck */
#define DIBUTTON_HOCKEYO_BURST                  0x19000403 /* invoke speed burst */
#define DIBUTTON_HOCKEYO_SPECIAL                0x19000404 /* invoke special move */
#define DIBUTTON_HOCKEYO_FAKE                   0x19000405 /* hold down to fake pass or kick */
#define DIBUTTON_HOCKEYO_MENU                   0x190004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_HOCKEYO_SCROLL              0x19004601 /* scroll view */
#define DIBUTTON_HOCKEYO_ZOOM                   0x19004406 /* Zoom view in / out */
#define DIBUTTON_HOCKEYO_STRATEGY               0x19004407 /* Invoke coaching menu for strategy help */
#define DIBUTTON_HOCKEYO_TIMEOUT                0x19004408 /* Call for time out */
#define DIBUTTON_HOCKEYO_SUBSTITUTE             0x19004409 /* substitute one player for another */
#define DIBUTTON_HOCKEYO_LEFT_LINK              0x1900C4E4 /* Fallback sidestep left button */
#define DIBUTTON_HOCKEYO_RIGHT_LINK             0x1900C4EC /* Fallback sidestep right button */
#define DIBUTTON_HOCKEYO_FORWARD_LINK           0x190144E0 /* Fallback move forward button */
#define DIBUTTON_HOCKEYO_BACK_LINK              0x190144E8 /* Fallback move back button */
#define DIBUTTON_HOCKEYO_DEVICE                 0x190044FE /* Show input device and controls */
#define DIBUTTON_HOCKEYO_PAUSE                  0x190044FC /* Start / Pause / Restart game */

/*--- Sports - Hockey - Defense
      Defense       ---*/
#define DIVIRTUAL_SPORTS_HOCKEY_DEFENSE         0x1A000000
#define DIAXIS_HOCKEYD_LATERAL                  0x1A008201 /* Move / Aim: left / right */
#define DIAXIS_HOCKEYD_MOVE                     0x1A010202 /* Move / Aim: up / down */
#define DIBUTTON_HOCKEYD_PLAYER                 0x1A000401 /* control player closest to the puck */
#define DIBUTTON_HOCKEYD_STEAL                  0x1A000402 /* attempt steal */
#define DIBUTTON_HOCKEYD_BURST                  0x1A000403 /* speed burst or body check */
#define DIBUTTON_HOCKEYD_BLOCK                  0x1A000404 /* block puck */
#define DIBUTTON_HOCKEYD_FAKE                   0x1A000405 /* hold down to fake tackle or intercept */
#define DIBUTTON_HOCKEYD_MENU                   0x1A0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_HOCKEYD_SCROLL              0x1A004601 /* scroll view */
#define DIBUTTON_HOCKEYD_ZOOM                   0x1A004406 /* Zoom view in / out */
#define DIBUTTON_HOCKEYD_STRATEGY               0x1A004407 /* Invoke coaching menu for strategy help */
#define DIBUTTON_HOCKEYD_TIMEOUT                0x1A004408 /* Call for time out */
#define DIBUTTON_HOCKEYD_SUBSTITUTE             0x1A004409 /* substitute one player for another */
#define DIBUTTON_HOCKEYD_LEFT_LINK              0x1A00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_HOCKEYD_RIGHT_LINK             0x1A00C4EC /* Fallback sidestep right button */
#define DIBUTTON_HOCKEYD_FORWARD_LINK           0x1A0144E0 /* Fallback move forward button */
#define DIBUTTON_HOCKEYD_BACK_LINK              0x1A0144E8 /* Fallback move back button */
#define DIBUTTON_HOCKEYD_DEVICE                 0x1A0044FE /* Show input device and controls */
#define DIBUTTON_HOCKEYD_PAUSE                  0x1A0044FC /* Start / Pause / Restart game */

/*--- Sports - Hockey - Goalie
      Goal tending  ---*/
#define DIVIRTUAL_SPORTS_HOCKEY_GOALIE          0x1B000000
#define DIAXIS_HOCKEYG_LATERAL                  0x1B008201 /* Move / Aim: left / right */
#define DIAXIS_HOCKEYG_MOVE                     0x1B010202 /* Move / Aim: up / down */
#define DIBUTTON_HOCKEYG_PASS                   0x1B000401 /* pass puck */
#define DIBUTTON_HOCKEYG_POKE                   0x1B000402 /* poke / check / hack */
#define DIBUTTON_HOCKEYG_STEAL                  0x1B000403 /* attempt steal */
#define DIBUTTON_HOCKEYG_BLOCK                  0x1B000404 /* block puck */
#define DIBUTTON_HOCKEYG_MENU                   0x1B0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_HOCKEYG_SCROLL              0x1B004601 /* scroll view */
#define DIBUTTON_HOCKEYG_ZOOM                   0x1B004405 /* Zoom view in / out */
#define DIBUTTON_HOCKEYG_STRATEGY               0x1B004406 /* Invoke coaching menu for strategy help */
#define DIBUTTON_HOCKEYG_TIMEOUT                0x1B004407 /* Call for time out */
#define DIBUTTON_HOCKEYG_SUBSTITUTE             0x1B004408 /* substitute one player for another */
#define DIBUTTON_HOCKEYG_LEFT_LINK              0x1B00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_HOCKEYG_RIGHT_LINK             0x1B00C4EC /* Fallback sidestep right button */
#define DIBUTTON_HOCKEYG_FORWARD_LINK           0x1B0144E0 /* Fallback move forward button */
#define DIBUTTON_HOCKEYG_BACK_LINK              0x1B0144E8 /* Fallback move back button */
#define DIBUTTON_HOCKEYG_DEVICE                 0x1B0044FE /* Show input device and controls */
#define DIBUTTON_HOCKEYG_PAUSE                  0x1B0044FC /* Start / Pause / Restart game */

/*--- Sports - Mountain Biking
                     ---*/
#define DIVIRTUAL_SPORTS_BIKING_MOUNTAIN        0x1C000000
#define DIAXIS_BIKINGM_TURN                     0x1C008201 /* left / right */
#define DIAXIS_BIKINGM_PEDAL                    0x1C010202 /* Pedal faster / slower / brake */
#define DIBUTTON_BIKINGM_JUMP                   0x1C000401 /* jump over obstacle */
#define DIBUTTON_BIKINGM_CAMERA                 0x1C000402 /* switch camera view */
#define DIBUTTON_BIKINGM_SPECIAL1               0x1C000403 /* perform first special move */
#define DIBUTTON_BIKINGM_SELECT                 0x1C000404 /* Select */
#define DIBUTTON_BIKINGM_SPECIAL2               0x1C000405 /* perform second special move */
#define DIBUTTON_BIKINGM_MENU                   0x1C0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_BIKINGM_SCROLL              0x1C004601 /* scroll view */
#define DIBUTTON_BIKINGM_ZOOM                   0x1C004406 /* Zoom view in / out */
#define DIAXIS_BIKINGM_BRAKE                    0x1C044203 /* Brake axis  */
#define DIBUTTON_BIKINGM_LEFT_LINK              0x1C00C4E4 /* Fallback turn left button */
#define DIBUTTON_BIKINGM_RIGHT_LINK             0x1C00C4EC /* Fallback turn right button */
#define DIBUTTON_BIKINGM_FASTER_LINK            0x1C0144E0 /* Fallback pedal faster button */
#define DIBUTTON_BIKINGM_SLOWER_LINK            0x1C0144E8 /* Fallback pedal slower button */
#define DIBUTTON_BIKINGM_BRAKE_BUTTON_LINK      0x1C0444E8 /* Fallback brake button */
#define DIBUTTON_BIKINGM_DEVICE                 0x1C0044FE /* Show input device and controls */
#define DIBUTTON_BIKINGM_PAUSE                  0x1C0044FC /* Start / Pause / Restart game */

/*--- Sports: Skiing / Snowboarding / Skateboarding
        ---*/
#define DIVIRTUAL_SPORTS_SKIING                 0x1D000000
#define DIAXIS_SKIING_TURN                      0x1D008201 /* left / right */
#define DIAXIS_SKIING_SPEED                     0x1D010202 /* faster / slower */
#define DIBUTTON_SKIING_JUMP                    0x1D000401 /* Jump */
#define DIBUTTON_SKIING_CROUCH                  0x1D000402 /* crouch down */
#define DIBUTTON_SKIING_CAMERA                  0x1D000403 /* switch camera view */
#define DIBUTTON_SKIING_SPECIAL1                0x1D000404 /* perform first special move */
#define DIBUTTON_SKIING_SELECT                  0x1D000405 /* Select */
#define DIBUTTON_SKIING_SPECIAL2                0x1D000406 /* perform second special move */
#define DIBUTTON_SKIING_MENU                    0x1D0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_SKIING_GLANCE               0x1D004601 /* scroll view */
#define DIBUTTON_SKIING_ZOOM                    0x1D004407 /* Zoom view in / out */
#define DIBUTTON_SKIING_LEFT_LINK               0x1D00C4E4 /* Fallback turn left button */
#define DIBUTTON_SKIING_RIGHT_LINK              0x1D00C4EC /* Fallback turn right button */
#define DIBUTTON_SKIING_FASTER_LINK             0x1D0144E0 /* Fallback increase speed button */
#define DIBUTTON_SKIING_SLOWER_LINK             0x1D0144E8 /* Fallback decrease speed button */
#define DIBUTTON_SKIING_DEVICE                  0x1D0044FE /* Show input device and controls */
#define DIBUTTON_SKIING_PAUSE                   0x1D0044FC /* Start / Pause / Restart game */

/*--- Sports - Soccer - Offense
      Offense       ---*/
#define DIVIRTUAL_SPORTS_SOCCER_OFFENSE         0x1E000000
#define DIAXIS_SOCCERO_LATERAL                  0x1E008201 /* Move / Aim: left / right */
#define DIAXIS_SOCCERO_MOVE                     0x1E010202 /* Move / Aim: up / down */
#define DIAXIS_SOCCERO_BEND                     0x1E018203 /* Bend to soccer shot/pass */
#define DIBUTTON_SOCCERO_SHOOT                  0x1E000401 /* Shoot the ball */
#define DIBUTTON_SOCCERO_PASS                   0x1E000402 /* Pass  */
#define DIBUTTON_SOCCERO_FAKE                   0x1E000403 /* Fake */
#define DIBUTTON_SOCCERO_PLAYER                 0x1E000404 /* Select next player */
#define DIBUTTON_SOCCERO_SPECIAL1               0x1E000405 /* Apply special move */
#define DIBUTTON_SOCCERO_SELECT                 0x1E000406 /* Select special move */
#define DIBUTTON_SOCCERO_MENU                   0x1E0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_SOCCERO_GLANCE              0x1E004601 /* scroll view */
#define DIBUTTON_SOCCERO_SUBSTITUTE             0x1E004407 /* Substitute one player for another */
#define DIBUTTON_SOCCERO_SHOOTLOW               0x1E004408 /* Shoot the ball low */
#define DIBUTTON_SOCCERO_SHOOTHIGH              0x1E004409 /* Shoot the ball high */
#define DIBUTTON_SOCCERO_PASSTHRU               0x1E00440A /* Make a thru pass */
#define DIBUTTON_SOCCERO_SPRINT                 0x1E00440B /* Sprint / turbo boost */
#define DIBUTTON_SOCCERO_CONTROL                0x1E00440C /* Obtain control of the ball */
#define DIBUTTON_SOCCERO_HEAD                   0x1E00440D /* Attempt to head the ball */
#define DIBUTTON_SOCCERO_LEFT_LINK              0x1E00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_SOCCERO_RIGHT_LINK             0x1E00C4EC /* Fallback sidestep right button */
#define DIBUTTON_SOCCERO_FORWARD_LINK           0x1E0144E0 /* Fallback move forward button */
#define DIBUTTON_SOCCERO_BACK_LINK              0x1E0144E8 /* Fallback move back button */
#define DIBUTTON_SOCCERO_DEVICE                 0x1E0044FE /* Show input device and controls */
#define DIBUTTON_SOCCERO_PAUSE                  0x1E0044FC /* Start / Pause / Restart game */

/*--- Sports - Soccer - Defense
      Defense       ---*/
#define DIVIRTUAL_SPORTS_SOCCER_DEFENSE         0x1F000000
#define DIAXIS_SOCCERD_LATERAL                  0x1F008201 /* Move / Aim: left / right */
#define DIAXIS_SOCCERD_MOVE                     0x1F010202 /* Move / Aim: up / down */
#define DIBUTTON_SOCCERD_BLOCK                  0x1F000401 /* Attempt to block shot */
#define DIBUTTON_SOCCERD_STEAL                  0x1F000402 /* Attempt to steal ball */
#define DIBUTTON_SOCCERD_FAKE                   0x1F000403 /* Fake a block or a steal */
#define DIBUTTON_SOCCERD_PLAYER                 0x1F000404 /* Select next player */
#define DIBUTTON_SOCCERD_SPECIAL                0x1F000405 /* Apply special move */
#define DIBUTTON_SOCCERD_SELECT                 0x1F000406 /* Select special move */
#define DIBUTTON_SOCCERD_SLIDE                  0x1F000407 /* Attempt a slide tackle */
#define DIBUTTON_SOCCERD_MENU                   0x1F0004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_SOCCERD_GLANCE              0x1F004601 /* scroll view */
#define DIBUTTON_SOCCERD_FOUL                   0x1F004408 /* Initiate a foul / hard-foul */
#define DIBUTTON_SOCCERD_HEAD                   0x1F004409 /* Attempt a Header */
#define DIBUTTON_SOCCERD_CLEAR                  0x1F00440A /* Attempt to clear the ball down the field */
#define DIBUTTON_SOCCERD_GOALIECHARGE           0x1F00440B /* Make the goalie charge out of the box */
#define DIBUTTON_SOCCERD_SUBSTITUTE             0x1F00440C /* Substitute one player for another */
#define DIBUTTON_SOCCERD_LEFT_LINK              0x1F00C4E4 /* Fallback sidestep left button */
#define DIBUTTON_SOCCERD_RIGHT_LINK             0x1F00C4EC /* Fallback sidestep right button */
#define DIBUTTON_SOCCERD_FORWARD_LINK           0x1F0144E0 /* Fallback move forward button */
#define DIBUTTON_SOCCERD_BACK_LINK              0x1F0144E8 /* Fallback move back button */
#define DIBUTTON_SOCCERD_DEVICE                 0x1F0044FE /* Show input device and controls */
#define DIBUTTON_SOCCERD_PAUSE                  0x1F0044FC /* Start / Pause / Restart game */

/*--- Sports - Racquet
      Tennis - Table-Tennis - Squash   ---*/
#define DIVIRTUAL_SPORTS_RACQUET                0x20000000
#define DIAXIS_RACQUET_LATERAL                  0x20008201 /* Move / Aim: left / right */
#define DIAXIS_RACQUET_MOVE                     0x20010202 /* Move / Aim: up / down */
#define DIBUTTON_RACQUET_SWING                  0x20000401 /* Swing racquet */
#define DIBUTTON_RACQUET_BACKSWING              0x20000402 /* Swing backhand */
#define DIBUTTON_RACQUET_SMASH                  0x20000403 /* Smash shot */
#define DIBUTTON_RACQUET_SPECIAL                0x20000404 /* Special shot */
#define DIBUTTON_RACQUET_SELECT                 0x20000405 /* Select special shot */
#define DIBUTTON_RACQUET_MENU                   0x200004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_RACQUET_GLANCE              0x20004601 /* scroll view */
#define DIBUTTON_RACQUET_TIMEOUT                0x20004406 /* Call for time out */
#define DIBUTTON_RACQUET_SUBSTITUTE             0x20004407 /* Substitute one player for another */
#define DIBUTTON_RACQUET_LEFT_LINK              0x2000C4E4 /* Fallback sidestep left button */
#define DIBUTTON_RACQUET_RIGHT_LINK             0x2000C4EC /* Fallback sidestep right button */
#define DIBUTTON_RACQUET_FORWARD_LINK           0x200144E0 /* Fallback move forward button */
#define DIBUTTON_RACQUET_BACK_LINK              0x200144E8 /* Fallback move back button */
#define DIBUTTON_RACQUET_DEVICE                 0x200044FE /* Show input device and controls */
#define DIBUTTON_RACQUET_PAUSE                  0x200044FC /* Start / Pause / Restart game */

/*--- Arcade- 2D
      Side to Side movement        ---*/
#define DIVIRTUAL_ARCADE_SIDE2SIDE              0x21000000
#define DIAXIS_ARCADES_LATERAL                  0x21008201 /* left / right */
#define DIAXIS_ARCADES_MOVE                     0x21010202 /* up / down */
#define DIBUTTON_ARCADES_THROW                  0x21000401 /* throw object */
#define DIBUTTON_ARCADES_CARRY                  0x21000402 /* carry object */
#define DIBUTTON_ARCADES_ATTACK                 0x21000403 /* attack */
#define DIBUTTON_ARCADES_SPECIAL                0x21000404 /* apply special move */
#define DIBUTTON_ARCADES_SELECT                 0x21000405 /* select special move */
#define DIBUTTON_ARCADES_MENU                   0x210004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_ARCADES_VIEW                0x21004601 /* scroll view left / right / up / down */
#define DIBUTTON_ARCADES_LEFT_LINK              0x2100C4E4 /* Fallback sidestep left button */
#define DIBUTTON_ARCADES_RIGHT_LINK             0x2100C4EC /* Fallback sidestep right button */
#define DIBUTTON_ARCADES_FORWARD_LINK           0x210144E0 /* Fallback move forward button */
#define DIBUTTON_ARCADES_BACK_LINK              0x210144E8 /* Fallback move back button */
#define DIBUTTON_ARCADES_VIEW_UP_LINK           0x2107C4E0 /* Fallback scroll view up button */
#define DIBUTTON_ARCADES_VIEW_DOWN_LINK         0x2107C4E8 /* Fallback scroll view down button */
#define DIBUTTON_ARCADES_VIEW_LEFT_LINK         0x2107C4E4 /* Fallback scroll view left button */
#define DIBUTTON_ARCADES_VIEW_RIGHT_LINK        0x2107C4EC /* Fallback scroll view right button */
#define DIBUTTON_ARCADES_DEVICE                 0x210044FE /* Show input device and controls */
#define DIBUTTON_ARCADES_PAUSE                  0x210044FC /* Start / Pause / Restart game */

/*--- Arcade - Platform Game
      Character moves around on screen  ---*/
#define DIVIRTUAL_ARCADE_PLATFORM               0x22000000
#define DIAXIS_ARCADEP_LATERAL                  0x22008201 /* Left / right */
#define DIAXIS_ARCADEP_MOVE                     0x22010202 /* Up / down */
#define DIBUTTON_ARCADEP_JUMP                   0x22000401 /* Jump */
#define DIBUTTON_ARCADEP_FIRE                   0x22000402 /* Fire */
#define DIBUTTON_ARCADEP_CROUCH                 0x22000403 /* Crouch */
#define DIBUTTON_ARCADEP_SPECIAL                0x22000404 /* Apply special move */
#define DIBUTTON_ARCADEP_SELECT                 0x22000405 /* Select special move */
#define DIBUTTON_ARCADEP_MENU                   0x220004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_ARCADEP_VIEW                0x22004601 /* Scroll view */
#define DIBUTTON_ARCADEP_FIRESECONDARY          0x22004406 /* Alternative fire button */
#define DIBUTTON_ARCADEP_LEFT_LINK              0x2200C4E4 /* Fallback sidestep left button */
#define DIBUTTON_ARCADEP_RIGHT_LINK             0x2200C4EC /* Fallback sidestep right button */
#define DIBUTTON_ARCADEP_FORWARD_LINK           0x220144E0 /* Fallback move forward button */
#define DIBUTTON_ARCADEP_BACK_LINK              0x220144E8 /* Fallback move back button */
#define DIBUTTON_ARCADEP_VIEW_UP_LINK           0x2207C4E0 /* Fallback scroll view up button */
#define DIBUTTON_ARCADEP_VIEW_DOWN_LINK         0x2207C4E8 /* Fallback scroll view down button */
#define DIBUTTON_ARCADEP_VIEW_LEFT_LINK         0x2207C4E4 /* Fallback scroll view left button */
#define DIBUTTON_ARCADEP_VIEW_RIGHT_LINK        0x2207C4EC /* Fallback scroll view right button */
#define DIBUTTON_ARCADEP_DEVICE                 0x220044FE /* Show input device and controls */
#define DIBUTTON_ARCADEP_PAUSE                  0x220044FC /* Start / Pause / Restart game */

/*--- CAD - 2D Object Control
      Controls to select and move objects in 2D  ---*/
#define DIVIRTUAL_CAD_2DCONTROL                 0x23000000
#define DIAXIS_2DCONTROL_LATERAL                0x23008201 /* Move view left / right */
#define DIAXIS_2DCONTROL_MOVE                   0x23010202 /* Move view up / down */
#define DIAXIS_2DCONTROL_INOUT                  0x23018203 /* Zoom - in / out */
#define DIBUTTON_2DCONTROL_SELECT               0x23000401 /* Select Object */
#define DIBUTTON_2DCONTROL_SPECIAL1             0x23000402 /* Do first special operation */
#define DIBUTTON_2DCONTROL_SPECIAL              0x23000403 /* Select special operation */
#define DIBUTTON_2DCONTROL_SPECIAL2             0x23000404 /* Do second special operation */
#define DIBUTTON_2DCONTROL_MENU                 0x230004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_2DCONTROL_HATSWITCH         0x23004601 /* Hat switch */
#define DIAXIS_2DCONTROL_ROTATEZ                0x23024204 /* Rotate view clockwise / counterclockwise */
#define DIBUTTON_2DCONTROL_DISPLAY              0x23004405 /* Shows next on-screen display options */
#define DIBUTTON_2DCONTROL_DEVICE               0x230044FE /* Show input device and controls */
#define DIBUTTON_2DCONTROL_PAUSE                0x230044FC /* Start / Pause / Restart game */

/*--- CAD - 3D object control
      Controls to select and move objects within a 3D environment  ---*/
#define DIVIRTUAL_CAD_3DCONTROL                 0x24000000
#define DIAXIS_3DCONTROL_LATERAL                0x24008201 /* Move view left / right */
#define DIAXIS_3DCONTROL_MOVE                   0x24010202 /* Move view up / down */
#define DIAXIS_3DCONTROL_INOUT                  0x24018203 /* Zoom - in / out */
#define DIBUTTON_3DCONTROL_SELECT               0x24000401 /* Select Object */
#define DIBUTTON_3DCONTROL_SPECIAL1             0x24000402 /* Do first special operation */
#define DIBUTTON_3DCONTROL_SPECIAL              0x24000403 /* Select special operation */
#define DIBUTTON_3DCONTROL_SPECIAL2             0x24000404 /* Do second special operation */
#define DIBUTTON_3DCONTROL_MENU                 0x240004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_3DCONTROL_HATSWITCH         0x24004601 /* Hat switch */
#define DIAXIS_3DCONTROL_ROTATEX                0x24034204 /* Rotate view forward or up / backward or down */
#define DIAXIS_3DCONTROL_ROTATEY                0x2402C205 /* Rotate view clockwise / counterclockwise */
#define DIAXIS_3DCONTROL_ROTATEZ                0x24024206 /* Rotate view left / right */
#define DIBUTTON_3DCONTROL_DISPLAY              0x24004405 /* Show next on-screen display options */
#define DIBUTTON_3DCONTROL_DEVICE               0x240044FE /* Show input device and controls */
#define DIBUTTON_3DCONTROL_PAUSE                0x240044FC /* Start / Pause / Restart game */

/*--- CAD - 3D Navigation - Fly through
      Controls for 3D modeling  ---*/
#define DIVIRTUAL_CAD_FLYBY                     0x25000000
#define DIAXIS_CADF_LATERAL                     0x25008201 /* move view left / right */
#define DIAXIS_CADF_MOVE                        0x25010202 /* move view up / down */
#define DIAXIS_CADF_INOUT                       0x25018203 /* in / out */
#define DIBUTTON_CADF_SELECT                    0x25000401 /* Select Object */
#define DIBUTTON_CADF_SPECIAL1                  0x25000402 /* do first special operation */
#define DIBUTTON_CADF_SPECIAL                   0x25000403 /* Select special operation */
#define DIBUTTON_CADF_SPECIAL2                  0x25000404 /* do second special operation */
#define DIBUTTON_CADF_MENU                      0x250004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_CADF_HATSWITCH              0x25004601 /* Hat switch */
#define DIAXIS_CADF_ROTATEX                     0x25034204 /* Rotate view forward or up / backward or down */
#define DIAXIS_CADF_ROTATEY                     0x2502C205 /* Rotate view clockwise / counterclockwise */
#define DIAXIS_CADF_ROTATEZ                     0x25024206 /* Rotate view left / right */
#define DIBUTTON_CADF_DISPLAY                   0x25004405 /* shows next on-screen display options */
#define DIBUTTON_CADF_DEVICE                    0x250044FE /* Show input device and controls */
#define DIBUTTON_CADF_PAUSE                     0x250044FC /* Start / Pause / Restart game */

/*--- CAD - 3D Model Control
      Controls for 3D modeling  ---*/
#define DIVIRTUAL_CAD_MODEL                     0x26000000
#define DIAXIS_CADM_LATERAL                     0x26008201 /* move view left / right */
#define DIAXIS_CADM_MOVE                        0x26010202 /* move view up / down */
#define DIAXIS_CADM_INOUT                       0x26018203 /* in / out */
#define DIBUTTON_CADM_SELECT                    0x26000401 /* Select Object */
#define DIBUTTON_CADM_SPECIAL1                  0x26000402 /* do first special operation */
#define DIBUTTON_CADM_SPECIAL                   0x26000403 /* Select special operation */
#define DIBUTTON_CADM_SPECIAL2                  0x26000404 /* do second special operation */
#define DIBUTTON_CADM_MENU                      0x260004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIHATSWITCH_CADM_HATSWITCH              0x26004601 /* Hat switch */
#define DIAXIS_CADM_ROTATEX                     0x26034204 /* Rotate view forward or up / backward or down */
#define DIAXIS_CADM_ROTATEY                     0x2602C205 /* Rotate view clockwise / counterclockwise */
#define DIAXIS_CADM_ROTATEZ                     0x26024206 /* Rotate view left / right */
#define DIBUTTON_CADM_DISPLAY                   0x26004405 /* shows next on-screen display options */
#define DIBUTTON_CADM_DEVICE                    0x260044FE /* Show input device and controls */
#define DIBUTTON_CADM_PAUSE                     0x260044FC /* Start / Pause / Restart game */

/*--- Control - Media Equipment
      Remote        ---*/
#define DIVIRTUAL_REMOTE_CONTROL                0x27000000
#define DIAXIS_REMOTE_SLIDER                    0x27050201 /* Slider for adjustment: volume / color / bass / etc */
#define DIBUTTON_REMOTE_MUTE                    0x27000401 /* Set volume on current device to zero */
#define DIBUTTON_REMOTE_SELECT                  0x27000402 /* Next/previous: channel/ track / chapter / picture / station */
#define DIBUTTON_REMOTE_PLAY                    0x27002403 /* Start or pause entertainment on current device */
#define DIBUTTON_REMOTE_CUE                     0x27002404 /* Move through current media */
#define DIBUTTON_REMOTE_REVIEW                  0x27002405 /* Move through current media */
#define DIBUTTON_REMOTE_CHANGE                  0x27002406 /* Select next device */
#define DIBUTTON_REMOTE_RECORD                  0x27002407 /* Start recording the current media */
#define DIBUTTON_REMOTE_MENU                    0x270004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIAXIS_REMOTE_SLIDER2                   0x27054202 /* Slider for adjustment: volume */
#define DIBUTTON_REMOTE_TV                      0x27005C08 /* Select TV */
#define DIBUTTON_REMOTE_CABLE                   0x27005C09 /* Select cable box */
#define DIBUTTON_REMOTE_CD                      0x27005C0A /* Select CD player */
#define DIBUTTON_REMOTE_VCR                     0x27005C0B /* Select VCR */
#define DIBUTTON_REMOTE_TUNER                   0x27005C0C /* Select tuner */
#define DIBUTTON_REMOTE_DVD                     0x27005C0D /* Select DVD player */
#define DIBUTTON_REMOTE_ADJUST                  0x27005C0E /* Enter device adjustment menu */
#define DIBUTTON_REMOTE_DIGIT0                  0x2700540F /* Digit 0 */
#define DIBUTTON_REMOTE_DIGIT1                  0x27005410 /* Digit 1 */
#define DIBUTTON_REMOTE_DIGIT2                  0x27005411 /* Digit 2 */
#define DIBUTTON_REMOTE_DIGIT3                  0x27005412 /* Digit 3 */
#define DIBUTTON_REMOTE_DIGIT4                  0x27005413 /* Digit 4 */
#define DIBUTTON_REMOTE_DIGIT5                  0x27005414 /* Digit 5 */
#define DIBUTTON_REMOTE_DIGIT6                  0x27005415 /* Digit 6 */
#define DIBUTTON_REMOTE_DIGIT7                  0x27005416 /* Digit 7 */
#define DIBUTTON_REMOTE_DIGIT8                  0x27005417 /* Digit 8 */
#define DIBUTTON_REMOTE_DIGIT9                  0x27005418 /* Digit 9 */
#define DIBUTTON_REMOTE_DEVICE                  0x270044FE /* Show input device and controls */
#define DIBUTTON_REMOTE_PAUSE                   0x270044FC /* Start / Pause / Restart game */

/*--- Control- Web
      Help or Browser            ---*/
#define DIVIRTUAL_BROWSER_CONTROL               0x28000000
#define DIAXIS_BROWSER_LATERAL                  0x28008201 /* Move on screen pointer */
#define DIAXIS_BROWSER_MOVE                     0x28010202 /* Move on screen pointer */
#define DIBUTTON_BROWSER_SELECT                 0x28000401 /* Select current item */
#define DIAXIS_BROWSER_VIEW                     0x28018203 /* Move view up/down */
#define DIBUTTON_BROWSER_REFRESH                0x28000402 /* Refresh */
#define DIBUTTON_BROWSER_MENU                   0x280004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_BROWSER_SEARCH                 0x28004403 /* Use search tool */
#define DIBUTTON_BROWSER_STOP                   0x28004404 /* Cease current update */
#define DIBUTTON_BROWSER_HOME                   0x28004405 /* Go directly to "home" location */
#define DIBUTTON_BROWSER_FAVORITES              0x28004406 /* Mark current site as favorite */
#define DIBUTTON_BROWSER_NEXT                   0x28004407 /* Select Next page */
#define DIBUTTON_BROWSER_PREVIOUS               0x28004408 /* Select Previous page */
#define DIBUTTON_BROWSER_HISTORY                0x28004409 /* Show/Hide History */
#define DIBUTTON_BROWSER_PRINT                  0x2800440A /* Print current page */
#define DIBUTTON_BROWSER_DEVICE                 0x280044FE /* Show input device and controls */
#define DIBUTTON_BROWSER_PAUSE                  0x280044FC /* Start / Pause / Restart game */

/*--- Driving Simulator - Giant Walking Robot
      Walking tank with weapons  ---*/
#define DIVIRTUAL_DRIVING_MECHA                 0x29000000
#define DIAXIS_MECHA_STEER                      0x29008201 /* Turns mecha left/right */
#define DIAXIS_MECHA_TORSO                      0x29010202 /* Tilts torso forward/backward */
#define DIAXIS_MECHA_ROTATE                     0x29020203 /* Turns torso left/right */
#define DIAXIS_MECHA_THROTTLE                   0x29038204 /* Engine Speed */
#define DIBUTTON_MECHA_FIRE                     0x29000401 /* Fire */
#define DIBUTTON_MECHA_WEAPONS                  0x29000402 /* Select next weapon group */
#define DIBUTTON_MECHA_TARGET                   0x29000403 /* Select closest enemy available target */
#define DIBUTTON_MECHA_REVERSE                  0x29000404 /* Toggles throttle in/out of reverse */
#define DIBUTTON_MECHA_ZOOM                     0x29000405 /* Zoom in/out targeting reticule */
#define DIBUTTON_MECHA_JUMP                     0x29000406 /* Fires jump jets */
#define DIBUTTON_MECHA_MENU                     0x290004FD /* Show menu options */
/*--- Priority 2 controls                            ---*/

#define DIBUTTON_MECHA_CENTER                   0x29004407 /* Center torso to legs */
#define DIHATSWITCH_MECHA_GLANCE                0x29004601 /* Look around */
#define DIBUTTON_MECHA_VIEW                     0x29004408 /* Cycle through view options */
#define DIBUTTON_MECHA_FIRESECONDARY            0x29004409 /* Alternative fire button */
#define DIBUTTON_MECHA_LEFT_LINK                0x2900C4E4 /* Fallback steer left button */
#define DIBUTTON_MECHA_RIGHT_LINK               0x2900C4EC /* Fallback steer right button */
#define DIBUTTON_MECHA_FORWARD_LINK             0x290144E0 /* Fallback tilt torso forward button */
#define DIBUTTON_MECHA_BACK_LINK                0x290144E8 /* Fallback tilt toroso backward button */
#define DIBUTTON_MECHA_ROTATE_LEFT_LINK         0x290244E4 /* Fallback rotate toroso right button */
#define DIBUTTON_MECHA_ROTATE_RIGHT_LINK        0x290244EC /* Fallback rotate torso left button */
#define DIBUTTON_MECHA_FASTER_LINK              0x2903C4E0 /* Fallback increase engine speed */
#define DIBUTTON_MECHA_SLOWER_LINK              0x2903C4E8 /* Fallback decrease engine speed */
#define DIBUTTON_MECHA_DEVICE                   0x290044FE /* Show input device and controls */
#define DIBUTTON_MECHA_PAUSE                    0x290044FC /* Start / Pause / Restart game */

/*
 *  "ANY" semantics can be used as a last resort to get mappings for actions 
 *  that match nothing in the chosen virtual genre.  These semantics will be 
 *  mapped at a lower priority that virtual genre semantics.  Also, hardware 
 *  vendors will not be able to provide sensible mappings for these unless 
 *  they provide application specific mappings.
 */
#define DIAXIS_ANY_X_1                          0xFF00C201 
#define DIAXIS_ANY_X_2                          0xFF00C202 
#define DIAXIS_ANY_Y_1                          0xFF014201 
#define DIAXIS_ANY_Y_2                          0xFF014202 
#define DIAXIS_ANY_Z_1                          0xFF01C201 
#define DIAXIS_ANY_Z_2                          0xFF01C202 
#define DIAXIS_ANY_R_1                          0xFF024201 
#define DIAXIS_ANY_R_2                          0xFF024202 
#define DIAXIS_ANY_U_1                          0xFF02C201 
#define DIAXIS_ANY_U_2                          0xFF02C202 
#define DIAXIS_ANY_V_1                          0xFF034201 
#define DIAXIS_ANY_V_2                          0xFF034202 
#define DIAXIS_ANY_A_1                          0xFF03C201 
#define DIAXIS_ANY_A_2                          0xFF03C202 
#define DIAXIS_ANY_B_1                          0xFF044201 
#define DIAXIS_ANY_B_2                          0xFF044202 
#define DIAXIS_ANY_C_1                          0xFF04C201 
#define DIAXIS_ANY_C_2                          0xFF04C202 
#define DIAXIS_ANY_S_1                          0xFF054201 
#define DIAXIS_ANY_S_2                          0xFF054202 

#define DIAXIS_ANY_1                            0xFF004201 
#define DIAXIS_ANY_2                            0xFF004202 
#define DIAXIS_ANY_3                            0xFF004203 
#define DIAXIS_ANY_4                            0xFF004204 

#define DIPOV_ANY_1                             0xFF004601 
#define DIPOV_ANY_2                             0xFF004602 
#define DIPOV_ANY_3                             0xFF004603 
#define DIPOV_ANY_4                             0xFF004604 

#define DIBUTTON_ANY(instance)                  ( 0xFF004400 | instance )


#ifdef __cplusplus
};
#endif

#endif  /* __DINPUT_INCLUDED__ */

/****************************************************************************
 *
 *  Definitions for non-IDirectInput (VJoyD) features defined more recently
 *  than the current sdk files
 *
 ****************************************************************************/

#ifdef _INC_MMSYSTEM
#ifndef MMNOJOY

#ifndef __VJOYDX_INCLUDED__
#define __VJOYDX_INCLUDED__

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Flag to indicate that the dwReserved2 field of the JOYINFOEX structure
 * contains mini-driver specific data to be passed by VJoyD to the mini-
 * driver instead of doing a poll.
 */
#define JOY_PASSDRIVERDATA          0x10000000l

/*
 * Informs the joystick driver that the configuration has been changed
 * and should be reloaded from the registery.
 * dwFlags is reserved and should be set to zero
 */
WINMMAPI MMRESULT WINAPI joyConfigChanged( DWORD dwFlags );

#ifndef DIJ_RINGZERO
/*
 * Invoke the joystick control panel directly, using the passed window handle 
 * as the parent of the dialog.  This API is only supported for compatibility 
 * purposes; new applications should use the RunControlPanel method of a 
 * device interface for a game controller.
 * The API is called by using the function pointer returned by
 * GetProcAddress( hCPL, TEXT("ShowJoyCPL") ) where hCPL is a HMODULE returned 
 * by LoadLibrary( TEXT("joy.cpl") ).  The typedef is provided to allow 
 * declaration and casting of an appropriately typed variable.
 */
void WINAPI ShowJoyCPL( HWND hWnd );
typedef void (WINAPI* LPFNSHOWJOYCPL)( HWND hWnd );
#endif /* DIJ_RINGZERO */


/*
 * Hardware Setting indicating that the device is a headtracker
 */
#define JOY_HWS_ISHEADTRACKER       0x02000000l

/*
 * Hardware Setting indicating that the VxD is used to replace
 * the standard analog polling
 */
#define JOY_HWS_ISGAMEPORTDRIVER    0x04000000l

/*
 * Hardware Setting indicating that the driver needs a standard
 * gameport in order to communicate with the device.
 */
#define JOY_HWS_ISANALOGPORTDRIVER  0x08000000l

/*
 * Hardware Setting indicating that VJoyD should not load this
 * driver, it will be loaded externally and will register with
 * VJoyD of it's own accord.
 */
#define JOY_HWS_AUTOLOAD            0x10000000l

/*
 * Hardware Setting indicating that the driver acquires any
 * resources needed without needing a devnode through VJoyD.
 */
#define JOY_HWS_NODEVNODE           0x20000000l


/*
 * Hardware Setting indicating that the device is a gameport bus
 */
#define JOY_HWS_ISGAMEPORTBUS       0x80000000l
#define JOY_HWS_GAMEPORTBUSBUSY     0x00000001l

/*
 * Usage Setting indicating that the settings are volatile and
 * should be removed if still present on a reboot.
 */
#define JOY_US_VOLATILE             0x00000008L

#ifdef __cplusplus
};
#endif

#endif  /* __VJOYDX_INCLUDED__ */

#endif  /* not MMNOJOY */
#endif  /* _INC_MMSYSTEM */

/****************************************************************************
 *
 *  Definitions for non-IDirectInput (VJoyD) features defined more recently
 *  than the current ddk files
 *
 ****************************************************************************/

#ifndef DIJ_RINGZERO

#ifdef _INC_MMDDK
#ifndef MMNOJOYDEV

#ifndef __VJOYDXD_INCLUDED__
#define __VJOYDXD_INCLUDED__
/*
 * Poll type in which the do_other field of the JOYOEMPOLLDATA
 * structure contains mini-driver specific data passed from an app.
 */
#define JOY_OEMPOLL_PASSDRIVERDATA  7

#endif  /* __VJOYDXD_INCLUDED__ */

#endif  /* not MMNOJOYDEV */
#endif  /* _INC_MMDDK */

#endif /* DIJ_RINGZERO */

