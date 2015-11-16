/*
The zlib/libpng License

Copyright (c) 2005-2007 Phillip Castaneda (pjcast -- www.wreckedgames.com)

This software is provided 'as-is', without any express or implied warranty. In no event will
the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial
applications, and to alter it and redistribute it freely, subject to the following
restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that
		you wrote the original software. If you use this software in a product,
		an acknowledgment in the product documentation would be appreciated but is
		not required.

    2. Altered source versions must be plainly marked as such, and must not be
		misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
*/
#include "win32/Win32ForceFeedback.h"
#include "OISException.h"
#include <math.h>

// 0 = No trace; 1 = Important traces; 2 = Debug traces
#define OIS_WIN32_JOYFF_DEBUG 1

#if (defined (_DEBUG) || defined(OIS_WIN32_JOYFF_DEBUG))
  #include <iostream>
  #include <sstream>
  using namespace std;
#endif

using namespace OIS;

//--------------------------------------------------------------//
Win32ForceFeedback::Win32ForceFeedback(IDirectInputDevice8* pDIJoy, const DIDEVCAPS* pDIJoyCaps) :
  mHandles(0), mJoyStick(pDIJoy), mFFAxes(0), mpDIJoyCaps(pDIJoyCaps)
{
#if (OIS_WIN32_JOYFF_DEBUG > 0)
  cout << "FFSamplePeriod      : " << mpDIJoyCaps->dwFFSamplePeriod << " mu-s, "
	   << "FFMinTimeResolution : " << mpDIJoyCaps->dwFFMinTimeResolution << " mu-s,"
	   << "" << endl;
#endif
}

//--------------------------------------------------------------//
Win32ForceFeedback::~Win32ForceFeedback()
{
	//Get the effect - if it exists
	for(EffectList::iterator i = mEffectList.begin(); i != mEffectList.end(); ++i )
	{
		LPDIRECTINPUTEFFECT dxEffect = i->second;
		if( dxEffect )
		{
			dxEffect->Unload();
			dxEffect->Release();
		}
	}

	mEffectList.clear();
}

//--------------------------------------------------------------//
short Win32ForceFeedback::getFFAxesNumber()
{
	return mFFAxes;
}

//--------------------------------------------------------------//
unsigned short Win32ForceFeedback::getFFMemoryLoad()
{
    DIPROPDWORD dipdw;  // DIPROPDWORD contains a DIPROPHEADER structure. 
	dipdw.diph.dwSize       = sizeof(DIPROPDWORD); 
	dipdw.diph.dwHeaderSize = sizeof(DIPROPHEADER); 
	dipdw.diph.dwObj        = 0; // device property 
	dipdw.diph.dwHow        = DIPH_DEVICE;
	dipdw.dwData            = 0; // In case of any error.

	const HRESULT hr = mJoyStick->GetProperty(DIPROP_FFLOAD, &dipdw.diph);
	if(FAILED(hr))
	{
	    if (hr == DIERR_NOTEXCLUSIVEACQUIRED)
		    OIS_EXCEPT(E_General, "Can't query FF memory load as device was not acquired in exclusive mode");
		else
		    OIS_EXCEPT(E_General, "Unknown error querying FF memory load ->..");
	}

	return (unsigned short)dipdw.dwData;
}

//--------------------------------------------------------------//
void Win32ForceFeedback::upload( const Effect* effect )
{
	switch( effect->force )
	{
		case OIS::Effect::ConstantForce: _updateConstantEffect(effect);	break;
		case OIS::Effect::RampForce: _updateRampEffect(effect);	break;
		case OIS::Effect::PeriodicForce: _updatePeriodicEffect(effect);	break;
		case OIS::Effect::ConditionalForce:	_updateConditionalEffect(effect); break;
		//case OIS::Effect::CustomForce: _updateCustomEffect(effect); break;
		default: OIS_EXCEPT(E_NotImplemented, "Requested Force not Implemented yet, sorry!"); break;
	}
}

//--------------------------------------------------------------//
void Win32ForceFeedback::modify( const Effect* eff )
{
	//Modifying is essentially the same as an upload, so, just reuse that function
	upload(eff);
}

//--------------------------------------------------------------//
void Win32ForceFeedback::remove( const Effect* eff )
{
	//Get the effect - if it exists
	EffectList::iterator i = mEffectList.find(eff->_handle);
	if( i != mEffectList.end() )
	{
		LPDIRECTINPUTEFFECT dxEffect = i->second;
		if( dxEffect )
		{
			dxEffect->Stop();
			//We care about the return value - as the effect might not
			//have been unlaoded
			if( SUCCEEDED(dxEffect->Unload()) )
			{
			    dxEffect->Release();
				mEffectList.erase(i);
			}
		}
		else
			mEffectList.erase(i);
	}
}

//--------------------------------------------------------------//
void Win32ForceFeedback::setMasterGain( float level )
{
	//Between 0 - 10,000
	int gain_level = (int)(10000.0f * level);

	if( gain_level > 10000 )
		gain_level = 10000;
	else if( gain_level < 0 )
		gain_level = 0;

	DIPROPDWORD DIPropGain;
	DIPropGain.diph.dwSize       = sizeof(DIPropGain);
	DIPropGain.diph.dwHeaderSize = sizeof(DIPROPHEADER);
	DIPropGain.diph.dwObj        = 0;
	DIPropGain.diph.dwHow        = DIPH_DEVICE;
	DIPropGain.dwData            = gain_level;

#if (OIS_WIN32_JOYFF_DEBUG > 0)
	cout << "Win32ForceFeedback("<< mJoyStick << ") : Setting master gain to " 
		 << level << " => " << DIPropGain.dwData << endl;
#endif

	const HRESULT hr = mJoyStick->SetProperty(DIPROP_FFGAIN, &DIPropGain.diph);

#if defined (_DEBUG)
	if(FAILED(hr))
	    cout << "Failed to change master gain" << endl;
#endif
}

//--------------------------------------------------------------//
void Win32ForceFeedback::setAutoCenterMode( bool auto_on )
{
	DIPROPDWORD DIPropAutoCenter;
	DIPropAutoCenter.diph.dwSize       = sizeof(DIPropAutoCenter);
	DIPropAutoCenter.diph.dwHeaderSize = sizeof(DIPROPHEADER);
	DIPropAutoCenter.diph.dwObj        = 0;
	DIPropAutoCenter.diph.dwHow        = DIPH_DEVICE;
	DIPropAutoCenter.dwData            = (auto_on ? DIPROPAUTOCENTER_ON : DIPROPAUTOCENTER_OFF);

#if (OIS_WIN32_JOYFF_DEBUG > 0)
	cout << "Win32ForceFeedback("<< mJoyStick << ") : Setting auto-center mode to " 
		 << auto_on << " => " << DIPropAutoCenter.dwData << endl;
#endif

	const HRESULT hr = mJoyStick->SetProperty(DIPROP_AUTOCENTER, &DIPropAutoCenter.diph);

#if defined (_DEBUG)
	if(FAILED(hr))
	    cout << "Failed to change auto-center mode" << endl;
#endif
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_updateConstantEffect( const Effect* effect )
{
	ConstantEffect *eff = static_cast<ConstantEffect*>(effect->getForceEffect());

	DWORD           rgdwAxes[2]     = { DIJOFS_X, DIJOFS_Y };
	LONG            rglDirection[2] = { 0, 0 };
	DIENVELOPE      diEnvelope;
	DICONSTANTFORCE cf;
	DIEFFECT        diEffect;

	//Currently only support 1 axis
	//if( effect->getNumAxes() == 1 )
	cf.lMagnitude = eff->level;

#if (OIS_WIN32_JOYFF_DEBUG > 1)
	cout << "  Level : " << eff->level
		 << " => " << cf.lMagnitude << endl;
#endif

	_setCommonProperties(&diEffect, rgdwAxes, rglDirection, &diEnvelope, sizeof(DICONSTANTFORCE), &cf, effect, &eff->envelope);
	_upload(GUID_ConstantForce, &diEffect, effect);
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_updateRampEffect( const Effect* effect )
{
	RampEffect *eff = static_cast<RampEffect*>(effect->getForceEffect());

	DWORD           rgdwAxes[2]     = { DIJOFS_X, DIJOFS_Y };
	LONG            rglDirection[2] = { 0, 0 };
	DIENVELOPE      diEnvelope;
	DIRAMPFORCE     rf;
	DIEFFECT        diEffect;

	//Currently only support 1 axis
	rf.lStart = eff->startLevel;
	rf.lEnd = eff->endLevel;

	_setCommonProperties(&diEffect, rgdwAxes, rglDirection, &diEnvelope, sizeof(DIRAMPFORCE), &rf, effect, &eff->envelope );
	_upload(GUID_RampForce, &diEffect, effect);
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_updatePeriodicEffect( const Effect* effect )
{
	PeriodicEffect *eff = static_cast<PeriodicEffect*>(effect->getForceEffect());

	DWORD           rgdwAxes[2]     = { DIJOFS_X, DIJOFS_Y };
	LONG            rglDirection[2] = { 0, 0 };
	DIENVELOPE      diEnvelope;
	DIPERIODIC      pf;
	DIEFFECT        diEffect;

	//Currently only support 1 axis
	pf.dwMagnitude = eff->magnitude;
	pf.lOffset = eff->offset;
	pf.dwPhase = eff->phase;
	pf.dwPeriod = eff->period;

	_setCommonProperties(&diEffect, rgdwAxes, rglDirection, &diEnvelope, sizeof(DIPERIODIC), &pf, effect, &eff->envelope );

	switch( effect->type )
	{
	case OIS::Effect::Square: _upload(GUID_Square, &diEffect, effect); break;
	case OIS::Effect::Triangle: _upload(GUID_Triangle, &diEffect, effect); break;
	case OIS::Effect::Sine: _upload(GUID_Sine, &diEffect, effect); break;
	case OIS::Effect::SawToothUp: _upload(GUID_SawtoothUp, &diEffect, effect); break;
	case OIS::Effect::SawToothDown:	_upload(GUID_SawtoothDown, &diEffect, effect); break;
	default: break;
	}
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_updateConditionalEffect( const Effect* effect )
{
	ConditionalEffect *eff = static_cast<ConditionalEffect*>(effect->getForceEffect());

	DWORD           rgdwAxes[2]     = { DIJOFS_X, DIJOFS_Y };
	LONG            rglDirection[2] = { 0, 0 };
	DIENVELOPE      diEnvelope;
	DICONDITION     cf;
	DIEFFECT        diEffect;

	cf.lOffset = eff->deadband;
	cf.lPositiveCoefficient = eff->rightCoeff;
	cf.lNegativeCoefficient = eff->leftCoeff;
	cf.dwPositiveSaturation = eff->rightSaturation;
	cf.dwNegativeSaturation = eff->leftSaturation;
	cf.lDeadBand = eff->deadband;

	_setCommonProperties(&diEffect, rgdwAxes, rglDirection, &diEnvelope, sizeof(DICONDITION), &cf, effect, 0 );

	switch( effect->type )
	{
	case OIS::Effect::Friction:	_upload(GUID_Friction, &diEffect, effect); break;
	case OIS::Effect::Damper: _upload(GUID_Damper, &diEffect, effect); break;
	case OIS::Effect::Inertia: _upload(GUID_Inertia, &diEffect, effect); break;
	case OIS::Effect::Spring: _upload(GUID_Spring, &diEffect, effect); break;
	default: break;
	}
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_updateCustomEffect( const Effect* /*effect*/ )
{
    //CustomEffect *eff = static_cast<CustomEffect*>(effect->getForceEffect());
    //
	//DWORD           rgdwAxes[2]     = { DIJOFS_X, DIJOFS_Y };
	//LONG            rglDirection[2] = { 0, 0 };
	//DIENVELOPE      diEnvelope;
	//DICUSTOMFORCE cf;
	//DIEFFECT        diEffect;
	//cf.cChannels = 0;
	//cf.dwSamplePeriod = 0;
	//cf.cSamples = 0;
	//cf.rglForceData = 0;
	//_setCommonProperties(&diEffect, rgdwAxes, rglDirection, &diEnvelope, sizeof(DICUSTOMFORCE), &cf, effect, &eff->envelope);
	//_upload(GUID_CustomForce, &diEffect, effect);
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_setCommonProperties(
		DIEFFECT* diEffect, DWORD* rgdwAxes,
		LONG* rglDirection, DIENVELOPE* diEnvelope, DWORD struct_size,
		LPVOID struct_type, const Effect* effect, const Envelope* envelope )
{
	ZeroMemory(diEffect, sizeof(DIEFFECT));

	diEffect->dwSize                  = sizeof(DIEFFECT);
	diEffect->dwFlags                 = DIEFF_CARTESIAN | DIEFF_OBJECTOFFSETS;
	diEffect->dwGain                  = DI_FFNOMINALMAX;

	diEffect->dwTriggerButton         = DIEB_NOTRIGGER; // effect->trigger_button; // TODO: Conversion
	diEffect->dwTriggerRepeatInterval = effect->trigger_interval;

#if (OIS_WIN32_JOYFF_DEBUG > 1)
	cout << "  Trigger :" << endl
		 << "    Button   : " << effect->trigger_button 
		 << " => " << diEffect->dwTriggerButton << endl
		 << "    Interval : " << effect->trigger_interval 
		 << " => " << diEffect->dwTriggerRepeatInterval << endl;
#endif

	diEffect->cAxes                   = 1; // effect->getNumAxes();
	diEffect->rgdwAxes                = rgdwAxes;

	diEffect->rglDirection            = rglDirection; // TODO: conversion from effect->direction

#if (OIS_WIN32_JOYFF_DEBUG > 1)
	cout << "  Direction : " << Effect::getDirectionName(effect->direction)
		 << " => {";
	for (int iDir=0; iDir < (int)diEffect->cAxes; iDir++)
	  cout << " " << diEffect->rglDirection[iDir];
	cout << "}" << endl;
#endif

	if (diEnvelope && envelope && envelope->isUsed())
	{
	    diEnvelope->dwSize = sizeof(DIENVELOPE);
	    diEnvelope->dwAttackLevel = envelope->attackLevel;
	    diEnvelope->dwAttackTime  = envelope->attackLength;
	    diEnvelope->dwFadeLevel   = envelope->fadeLevel;
	    diEnvelope->dwFadeTime    = envelope->fadeLength;
	    diEffect->lpEnvelope = diEnvelope;
	}
	else
	    diEffect->lpEnvelope = 0;

#if (OIS_WIN32_JOYFF_DEBUG > 1)
	if (diEnvelope && envelope && envelope->isUsed())
	{
		cout << "  Enveloppe :" << endl
			 << "    AttackLen : " << envelope->attackLength
			 << " => " << diEnvelope->dwAttackTime << endl 
			 << "    AttackLvl : " << envelope->attackLevel
			 << " => " << diEnvelope->dwAttackLevel << endl 
			 << "    FadeLen   : " << envelope->fadeLength
			 << " => " << diEnvelope->dwFadeTime << endl
			 << "    FadeLvl   : " << envelope->fadeLevel
			 << " => " << diEnvelope->dwFadeLevel << endl;
	}
#endif

	diEffect->dwSamplePeriod          = 0;
	diEffect->dwDuration              = effect->replay_length;
	diEffect->dwStartDelay            = effect->replay_delay;

#if (OIS_WIN32_JOYFF_DEBUG > 1)
	cout << "  Replay :" << endl
		 << "    Length : " << effect->replay_length 
		 << " => " << diEffect->dwDuration << endl
		 << "    Delay  : " << effect->replay_delay 
		 << " => " << diEffect->dwStartDelay << endl;
#endif

	diEffect->cbTypeSpecificParams    = struct_size;
	diEffect->lpvTypeSpecificParams   = struct_type;
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_upload( GUID guid, DIEFFECT* diEffect, const Effect* effect)
{
	LPDIRECTINPUTEFFECT dxEffect = 0;

	//Get the effect - if it exists
	EffectList::iterator i = mEffectList.find(effect->_handle);
	//It has been created already
	if( i != mEffectList.end() )
		dxEffect = i->second;
	else //This effect has not yet been created - generate a handle
		effect->_handle = mHandles++;

	if( dxEffect == 0 )
	{
		//This effect has not yet been created, so create it
		HRESULT hr = mJoyStick->CreateEffect(guid, diEffect, &dxEffect, NULL);
		if(SUCCEEDED(hr))
		{
			mEffectList[effect->_handle] = dxEffect;
			dxEffect->Start(INFINITE,0);
		}
		else if( hr == DIERR_DEVICEFULL )
			OIS_EXCEPT(E_DeviceFull, "Remove an effect before adding more!");
		else
			OIS_EXCEPT(E_General, "Unknown error creating effect->..");
	}
	else
	{
		//ToDo -- Update the Effect
		HRESULT hr = dxEffect->SetParameters( diEffect, DIEP_DIRECTION |
			DIEP_DURATION | DIEP_ENVELOPE | DIEP_STARTDELAY | DIEP_TRIGGERBUTTON |
			DIEP_TRIGGERREPEATINTERVAL | DIEP_TYPESPECIFICPARAMS | DIEP_START );

		if(FAILED(hr)) OIS_EXCEPT(E_InvalidParam, "Error updating device!");
	}
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_addEffectSupport( LPCDIEFFECTINFO pdei )
{
#if (OIS_WIN32_JOYFF_DEBUG > 0)
    // Dump some usefull information about the effect type.
    cout << "Adding support for '" << pdei->tszName << "' effect type" << endl;
	cout << "  Supported static params: ";
	if (pdei->dwStaticParams & DIEP_AXES) cout << " Axes";
	if (pdei->dwStaticParams & DIEP_DIRECTION) cout << " Direction";
	if (pdei->dwStaticParams & DIEP_DURATION) cout << " Duration";
	if (pdei->dwStaticParams & DIEP_ENVELOPE) cout << " Envelope";
	if (pdei->dwStaticParams & DIEP_GAIN) cout << " Gain";
	if (pdei->dwStaticParams & DIEP_SAMPLEPERIOD) cout << " SamplePeriod";
	if (pdei->dwStaticParams & DIEP_STARTDELAY) cout << " StartDelay";
	if (pdei->dwStaticParams & DIEP_TRIGGERBUTTON) cout << " TriggerButton";
	if (pdei->dwStaticParams & DIEP_TRIGGERREPEATINTERVAL) cout << " TriggerRepeatInterval";
	if (pdei->dwStaticParams & DIEP_TYPESPECIFICPARAMS) cout << " TypeSpecificParams";
	cout << endl;
	cout << "  Supported dynamic params: ";
	if (pdei->dwDynamicParams & DIEP_AXES) cout << " Axes";
	if (pdei->dwDynamicParams & DIEP_DIRECTION) cout << " Direction";
	if (pdei->dwDynamicParams & DIEP_DURATION) cout << " Duration";
	if (pdei->dwDynamicParams & DIEP_ENVELOPE) cout << " Envelope";
	if (pdei->dwDynamicParams & DIEP_GAIN) cout << " Gain";
	if (pdei->dwDynamicParams & DIEP_SAMPLEPERIOD) cout << " SamplePeriod";
	if (pdei->dwDynamicParams & DIEP_STARTDELAY) cout << " StartDelay";
	if (pdei->dwDynamicParams & DIEP_TRIGGERBUTTON) cout << " TriggerButton";
	if (pdei->dwDynamicParams & DIEP_TRIGGERREPEATINTERVAL) cout << " TriggerRepeatInterval";
	if (pdei->dwDynamicParams & DIEP_TYPESPECIFICPARAMS) cout << " TypeSpecificParams";
	cout << endl;
	cout << "  More details about supported parameters support: ";
	if (pdei->dwEffType & DIEFT_STARTDELAY) cout << " StartDelay";
	if (pdei->dwEffType & DIEFT_FFATTACK) cout << " Attack";
	if (pdei->dwEffType & DIEFT_FFFADE) cout << " Fade";
	if (pdei->dwEffType & DIEFT_DEADBAND) cout << " DeadBand";
	if (pdei->dwEffType & DIEFT_SATURATION) cout << " Saturation";
	if (pdei->dwEffType & DIEFT_POSNEGSATURATION) cout << " PosNegaturation";
	if (pdei->dwEffType & DIEFT_POSNEGCOEFFICIENTS) cout << " PosNegCoefficients";
	if (pdei->dwEffType & DIEFT_HARDWARE) cout << " HardwareSpecific";
	cout << endl;
#endif

    Effect::EForce eForce;
	switch (DIEFT_GETTYPE(pdei->dwEffType))
	{
	    case DIEFT_CONSTANTFORCE:
		    eForce = Effect::ConstantForce;
			break;
	    case DIEFT_RAMPFORCE:
		    eForce = Effect::RampForce;
			break;
	    case DIEFT_PERIODIC:
		    eForce = Effect::PeriodicForce;
			break;
	    case DIEFT_CONDITION:
		    eForce = Effect::ConditionalForce;
			break;
	    case DIEFT_CUSTOMFORCE:
		    eForce = Effect::CustomForce;
			break;
	    default:
		    eForce = Effect::UnknownForce;
#if defined (_DEBUG)
			cout << "Win32ForceFeedback: DirectInput8 Effect type support not implemented: " 
				 << "DIEFT_GETTYPE="<< (int)DIEFT_GETTYPE(pdei->dwEffType) << endl;
#endif
			return;
	}

	//Determine what the effect type is and how it corresponds to our OIS's Enums
	//We could save the GUIDs too, however, we will just use the predefined ones later
	if( pdei->guid == GUID_ConstantForce )
		_addEffectTypes(eForce, Effect::Constant );
	else if( pdei->guid == GUID_Triangle )
		_addEffectTypes(eForce, Effect::Triangle );
	else if( pdei->guid == GUID_Spring )
		_addEffectTypes(eForce, Effect::Spring );
	else if( pdei->guid == GUID_Friction )
		_addEffectTypes(eForce, Effect::Friction );
	else if( pdei->guid == GUID_Square )
		_addEffectTypes(eForce, Effect::Square );
	else if( pdei->guid == GUID_Sine )
		_addEffectTypes(eForce, Effect::Sine );
	else if( pdei->guid == GUID_SawtoothUp )
		_addEffectTypes(eForce, Effect::SawToothUp );
	else if( pdei->guid == GUID_SawtoothDown )
		_addEffectTypes(eForce, Effect::SawToothDown );
	else if( pdei->guid == GUID_Damper )
		_addEffectTypes(eForce, Effect::Damper );
	else if( pdei->guid == GUID_Inertia )
		_addEffectTypes(eForce, Effect::Inertia );
	else if( pdei->guid == GUID_CustomForce )
		_addEffectTypes(eForce, Effect::Custom );
	else if( pdei->guid == GUID_RampForce )
		_addEffectTypes(eForce, Effect::Ramp );

#if defined (_DEBUG)
	//Only care about this for Debugging Purposes
	//else
	//{
	//	std::ostringstream ss;
	//	ss << "Win32ForceFeedback, DirectInput8 Effect not found. Reported as: "
	//	   << pdei->tszName;
	//	OIS_EXCEPT( E_General, ss.str().c_str());
	//}
#endif
}

//--------------------------------------------------------------//
void Win32ForceFeedback::_addFFAxis()
{
	mFFAxes++;
}
