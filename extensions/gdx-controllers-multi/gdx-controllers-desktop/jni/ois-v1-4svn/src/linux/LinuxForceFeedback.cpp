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
#include "linux/LinuxForceFeedback.h"
#include "OISException.h"

#include <cstdlib>
#include <errno.h>
#include <memory.h>

using namespace OIS;

// 0 = No trace; 1 = Important traces; 2 = Debug traces
#define OIS_LINUX_JOYFF_DEBUG 1

#ifdef OIS_LINUX_JOYFF_DEBUG
# include <iostream>
  using namespace std;
#endif

//--------------------------------------------------------------//
LinuxForceFeedback::LinuxForceFeedback(int deviceID) :
	ForceFeedback(), mJoyStick(deviceID)
{
}

//--------------------------------------------------------------//
LinuxForceFeedback::~LinuxForceFeedback()
{
	// Unload all effects.
	for(EffectList::iterator i = mEffectList.begin(); i != mEffectList.end(); ++i )
	{
		struct ff_effect *linEffect = i->second;
		if( linEffect )
			_unload(linEffect->id);
	}

	mEffectList.clear();
}

//--------------------------------------------------------------//
unsigned short LinuxForceFeedback::getFFMemoryLoad()
{
	int nEffects = -1;
	if (ioctl(mJoyStick, EVIOCGEFFECTS, &nEffects) == -1)
		OIS_EXCEPT(E_General, "Unknown error reading max number of uploaded effects.");
#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "LinuxForceFeedback("<< mJoyStick  
		 << ") : Read device max number of uploaded effects : " << nEffects << endl;
#endif

	return (unsigned short int)(nEffects > 0 ? 100.0*mEffectList.size()/nEffects : 100);
}

//--------------------------------------------------------------//
void LinuxForceFeedback::setMasterGain(float value)
{
	if (!mSetGainSupport)
	{
#if (OIS_LINUX_JOYFF_DEBUG > 0)
		cout << "LinuxForceFeedback("<< mJoyStick << ") : Setting master gain " 
			 << "is not supported by the device" << endl;
#endif
		return;
	}

	struct input_event event;

	memset(&event, 0, sizeof(event));
	event.type = EV_FF;
	event.code = FF_GAIN;
	if (value < 0.0)
		value = 0.0;
	else if (value > 1.0)
		value = 1.0;
	event.value = (__s32)(value * 0xFFFFUL);

#if (OIS_LINUX_JOYFF_DEBUG > 0)
	cout << "LinuxForceFeedback("<< mJoyStick << ") : Setting master gain to " 
		 << value << " => " << event.value << endl;
#endif

	if (write(mJoyStick, &event, sizeof(event)) != sizeof(event)) {
		OIS_EXCEPT(E_General, "Unknown error changing master gain.");
	}
}

//--------------------------------------------------------------//
void LinuxForceFeedback::setAutoCenterMode(bool enabled)
{
	if (!mSetAutoCenterSupport)
	{
#if (OIS_LINUX_JOYFF_DEBUG > 0)
		cout << "LinuxForceFeedback("<< mJoyStick << ") : Setting auto-center mode " 
			 << "is not supported by the device" << endl;
#endif
		return;
	}

	struct input_event event;

	memset(&event, 0, sizeof(event));
	event.type = EV_FF;
	event.code = FF_AUTOCENTER;
	event.value = (__s32)(enabled*0xFFFFFFFFUL);

#if (OIS_LINUX_JOYFF_DEBUG > 0)
	cout << "LinuxForceFeedback("<< mJoyStick << ") : Toggling auto-center to " 
		 << enabled << " => 0x" << hex << event.value << dec << endl;
#endif

	if (write(mJoyStick, &event, sizeof(event)) != sizeof(event)) {
		OIS_EXCEPT(E_General, "Unknown error toggling auto-center.");
	}
}

//--------------------------------------------------------------//
void LinuxForceFeedback::upload( const Effect* effect )
{
	switch( effect->force )
	{
		case OIS::Effect::ConstantForce: 
			_updateConstantEffect(effect);	
			break;
		case OIS::Effect::ConditionalForce: 
			_updateConditionalEffect(effect);
			break;
		case OIS::Effect::PeriodicForce: 
			_updatePeriodicEffect(effect);
			break;
		case OIS::Effect::RampForce: 
			_updateRampEffect(effect);	
			break;
		case OIS::Effect::CustomForce: 
			//_updateCustomEffect(effect);
			//break;
		default: 
			OIS_EXCEPT(E_NotImplemented, "Requested force not implemented yet, sorry!"); 
			break;
	}
}

//--------------------------------------------------------------//
void LinuxForceFeedback::modify( const Effect* effect )
{
	upload(effect);
}

//--------------------------------------------------------------//
void LinuxForceFeedback::remove( const Effect* effect )
{
	//Get the effect - if it exists
	EffectList::iterator i = mEffectList.find(effect->_handle);
	if( i != mEffectList.end() )
	{
		struct ff_effect *linEffect = i->second;
		if( linEffect )
		{
			_stop(effect->_handle);

			_unload(effect->_handle);

			free(linEffect);

			mEffectList.erase(i);
		}
		else
			mEffectList.erase(i);
	}
}

//--------------------------------------------------------------//
// To Signed16/Unsigned15 safe conversions
#define MaxUnsigned15Value 0x7FFF
#define toUnsigned15(value) \
	(__u16)((value) < 0 ? 0 : ((value) > MaxUnsigned15Value ? MaxUnsigned15Value : (value)))

#define MaxSigned16Value  0x7FFF
#define MinSigned16Value -0x7FFF
#define toSigned16(value) \
  (__s16)((value) < MinSigned16Value ? MinSigned16Value : ((value) > MaxSigned16Value ? MaxSigned16Value : (value)))

// OIS to Linux duration
#define LinuxInfiniteDuration 0xFFFF
#define OISDurationUnitMS 1000 // OIS duration unit (microseconds), expressed in milliseconds (theLinux duration unit)

// linux/input.h : All duration values are expressed in ms. Values above 32767 ms (0x7fff)
//                 should not be used and have unspecified results.
#define LinuxDuration(oisDuration) ((oisDuration) == Effect::OIS_INFINITE ? LinuxInfiniteDuration \
									: toUnsigned15((oisDuration)/OISDurationUnitMS))


// OIS to Linux levels
#define OISMaxLevel 10000
#define LinuxMaxLevel 0x7FFF

// linux/input.h : Valid range for the attack and fade levels is 0x0000 - 0x7fff
#define LinuxPositiveLevel(oisLevel) toUnsigned15(LinuxMaxLevel*(long)(oisLevel)/OISMaxLevel)

#define LinuxSignedLevel(oisLevel) toSigned16(LinuxMaxLevel*(long)(oisLevel)/OISMaxLevel)


//--------------------------------------------------------------//
void LinuxForceFeedback::_setCommonProperties(struct ff_effect *event, 
											  struct ff_envelope *ffenvelope, 
											  const Effect* effect, const Envelope *envelope )
{
	memset(event, 0, sizeof(struct ff_effect));

	if (envelope && ffenvelope && envelope->isUsed()) {
		ffenvelope->attack_length = LinuxDuration(envelope->attackLength);
		ffenvelope->attack_level = LinuxPositiveLevel(envelope->attackLevel);
		ffenvelope->fade_length = LinuxDuration(envelope->fadeLength);
		ffenvelope->fade_level = LinuxPositiveLevel(envelope->fadeLevel);
	}
	
#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << endl;
	if (envelope && ffenvelope)
	{
		cout << "  Enveloppe :" << endl
			 << "    AttackLen : " << envelope->attackLength
			 << " => " << ffenvelope->attack_length << endl 
			 << "    AttackLvl : " << envelope->attackLevel
			 << " => " << ffenvelope->attack_level << endl 
			 << "    FadeLen   : " << envelope->fadeLength
			 << " => " << ffenvelope->fade_length << endl
			 << "    FadeLvl   : " << envelope->fadeLevel
			 << " => " << ffenvelope->fade_level << endl;
	}
#endif
	
	event->direction = (__u16)(1 + (effect->direction*45.0+135.0)*0xFFFFUL/360.0);

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "  Direction : " << Effect::getDirectionName(effect->direction)
		 << " => 0x" << hex << event->direction << dec << endl;
#endif

	// TODO trigger_button 0 vs. -1
	event->trigger.button = effect->trigger_button; // < 0 ? 0 : effect->trigger_button;
	event->trigger.interval = LinuxDuration(effect->trigger_interval);

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "  Trigger :" << endl
		 << "    Button   : " << effect->trigger_button 
		 << " => " << event->trigger.button << endl
		 << "    Interval : " << effect->trigger_interval 
		 << " => " << event->trigger.interval << endl;
#endif

	event->replay.length = LinuxDuration(effect->replay_length);
	event->replay.delay = LinuxDuration(effect->replay_delay);

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "  Replay :" << endl
		 << "    Length : " << effect->replay_length 
		 << " => " << event->replay.length << endl
		 << "    Delay  : " << effect->replay_delay 
		 << " => " << event->replay.delay << endl;
#endif
}

//--------------------------------------------------------------//
void LinuxForceFeedback::_updateConstantEffect( const Effect* eff )
{
	struct ff_effect event;

	ConstantEffect *effect = static_cast<ConstantEffect*>(eff->getForceEffect());

	_setCommonProperties(&event, &event.u.constant.envelope, eff, &effect->envelope);

	event.type = FF_CONSTANT;
	event.id = -1;

	event.u.constant.level = LinuxSignedLevel(effect->level);

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "  Level : " << effect->level
		 << " => " << event.u.constant.level << endl;
#endif

	_upload(&event, eff);
}

//--------------------------------------------------------------//
void LinuxForceFeedback::_updateRampEffect( const Effect* eff )
{
	struct ff_effect event;

	RampEffect *effect = static_cast<RampEffect*>(eff->getForceEffect());

	_setCommonProperties(&event, &event.u.constant.envelope, eff, &effect->envelope);

	event.type = FF_RAMP;
	event.id = -1;

	event.u.ramp.start_level = LinuxSignedLevel(effect->startLevel);
	event.u.ramp.end_level = LinuxSignedLevel(effect->endLevel);

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "  StartLevel : " << effect->startLevel
		 << " => " << event.u.ramp.start_level << endl
		 << "  EndLevel   : " << effect->endLevel
		 << " => " << event.u.ramp.end_level << endl;
#endif

	_upload(&event, eff);
}

//--------------------------------------------------------------//
void LinuxForceFeedback::_updatePeriodicEffect( const Effect* eff )
{
	struct ff_effect event;

	PeriodicEffect *effect = static_cast<PeriodicEffect*>(eff->getForceEffect());

	_setCommonProperties(&event, &event.u.periodic.envelope, eff, &effect->envelope);

	event.type = FF_PERIODIC;
	event.id = -1;

	switch( eff->type )
	{
		case OIS::Effect::Square:
			event.u.periodic.waveform = FF_SQUARE;
			break;
		case OIS::Effect::Triangle:
			event.u.periodic.waveform = FF_TRIANGLE;
			break;
		case OIS::Effect::Sine:
			event.u.periodic.waveform = FF_SINE;
			break;
		case OIS::Effect::SawToothUp:
			event.u.periodic.waveform = FF_SAW_UP;
			break;
		case OIS::Effect::SawToothDown:
			event.u.periodic.waveform = FF_SAW_DOWN;
			break;
		// Note: No support for Custom periodic force effect for the moment
		//case OIS::Effect::Custom:
			//event.u.periodic.waveform = FF_CUSTOM;
			//break;
		default:
			OIS_EXCEPT(E_General, "No such available effect for Periodic force!"); 
			break;
	}

	event.u.periodic.period    = LinuxDuration(effect->period);
	event.u.periodic.magnitude = LinuxPositiveLevel(effect->magnitude);
	event.u.periodic.offset    = LinuxPositiveLevel(effect->offset);
	event.u.periodic.phase     = (__u16)(effect->phase*event.u.periodic.period/36000.0); // ?????

	// Note: No support for Custom periodic force effect for the moment
	event.u.periodic.custom_len = 0;
	event.u.periodic.custom_data = 0;

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "  Magnitude : " << effect->magnitude
		 << " => " << event.u.periodic.magnitude << endl
		 << "  Period    : " << effect->period
		 << " => " << event.u.periodic.period  << endl
		 << "  Offset    : " << effect->offset
		 << " => " << event.u.periodic.offset << endl
		 << "  Phase     : " << effect->phase
		 << " => " << event.u.periodic.phase << endl;
#endif

	_upload(&event, eff);
}

//--------------------------------------------------------------//
void LinuxForceFeedback::_updateConditionalEffect( const Effect* eff )
{
	struct ff_effect event;

	ConditionalEffect *effect = static_cast<ConditionalEffect*>(eff->getForceEffect());

	_setCommonProperties(&event, NULL, eff, NULL);

	switch( eff->type )
	{
		case OIS::Effect::Friction:
			event.type = FF_FRICTION; 
			break;
		case OIS::Effect::Damper:
			event.type = FF_DAMPER; 
			break;
		case OIS::Effect::Inertia:
			event.type = FF_INERTIA; 
			break;
		case OIS::Effect::Spring:
			event.type = FF_SPRING;
			break;
		default:
			OIS_EXCEPT(E_General, "No such available effect for Conditional force!"); 
			break;
	}

	event.id = -1;

	event.u.condition[0].right_saturation = LinuxSignedLevel(effect->rightSaturation);
	event.u.condition[0].left_saturation  = LinuxSignedLevel(effect->leftSaturation);
	event.u.condition[0].right_coeff      = LinuxSignedLevel(effect->rightCoeff);
	event.u.condition[0].left_coeff       = LinuxSignedLevel(effect->leftCoeff);
	event.u.condition[0].deadband         = LinuxPositiveLevel(effect->deadband);// Unit ?? 
	event.u.condition[0].center           = LinuxSignedLevel(effect->center); // Unit ?? TODO ?

	// TODO support for second condition
	event.u.condition[1] = event.u.condition[0];

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "  Condition[0] : " << endl
		 << "    RightSaturation  : " << effect->rightSaturation
		 << " => " << event.u.condition[0].right_saturation << endl
		 << "    LeftSaturation   : " << effect->leftSaturation
		 << " => " << event.u.condition[0]. left_saturation << endl
		 << "    RightCoefficient : " << effect->rightCoeff
		 << " => " << event.u.condition[0].right_coeff << endl
		 << "    LeftCoefficient : " << effect->leftCoeff
		 << " => " << event.u.condition[0].left_coeff << endl
		 << "    DeadBand        : " << effect->deadband
		 << " => " << event.u.condition[0].deadband  << endl
		 << "    Center          : " << effect->center
		 << " => " << event.u.condition[0].center << endl;
	cout << "  Condition[1] : Not implemented" << endl;
#endif
	_upload(&event, eff);
}

//--------------------------------------------------------------//
void LinuxForceFeedback::_upload( struct ff_effect* ffeffect, const Effect* effect)
{
	struct ff_effect *linEffect = 0;

	//Get the effect - if it exists
	EffectList::iterator i = mEffectList.find(effect->_handle);
	//It has been created already
	if( i != mEffectList.end() )
		linEffect = i->second;

	if( linEffect == 0 )
	{
#if (OIS_LINUX_JOYFF_DEBUG > 1)
		cout << endl << "LinuxForceFeedback("<< mJoyStick << ") : Adding new effect : " 
			 << Effect::getEffectTypeName(effect->type) << endl;
#endif

		//This effect has not yet been created, so create it in the device
		if (ioctl(mJoyStick, EVIOCSFF, ffeffect) == -1) {
			// TODO device full check
			// OIS_EXCEPT(E_DeviceFull, "Remove an effect before adding more!");
			OIS_EXCEPT(E_General, "Unknown error creating effect (may be the device is full)->..");
		}

		// Save returned effect handle
		effect->_handle = ffeffect->id;

		// Save a copy of the uploaded effect for later simple modifications
		linEffect = (struct ff_effect *)calloc(1, sizeof(struct ff_effect));
		memcpy(linEffect, ffeffect, sizeof(struct ff_effect));

		mEffectList[effect->_handle] = linEffect;

		// Start playing the effect.
		_start(effect->_handle);
	}
	else
	{
#if (OIS_LINUX_JOYFF_DEBUG > 1)
		cout << endl << "LinuxForceFeedback("<< mJoyStick << ") : Replacing effect : " 
			 << Effect::getEffectTypeName(effect->type) << endl;
#endif

		// Keep same id/handle, as this is just an update in the device.
		ffeffect->id = effect->_handle;

		// Update effect in the device.
		if (ioctl(mJoyStick, EVIOCSFF, ffeffect) == -1) {
			OIS_EXCEPT(E_General, "Unknown error updating an effect->..");
		}

		// Update local linEffect for next time.
		memcpy(linEffect, ffeffect, sizeof(struct ff_effect));
	}

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << "LinuxForceFeedback("<< mJoyStick 
		 << ") : Effect handle : " << effect->_handle << endl;
#endif
}

//--------------------------------------------------------------//
void LinuxForceFeedback::_stop( int handle) {
	struct input_event stop;

	stop.type = EV_FF;
	stop.code = handle;
	stop.value = 0;

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << endl << "LinuxForceFeedback("<< mJoyStick 
		 << ") : Stopping effect with handle " << handle << endl;
#endif

	if (write(mJoyStick, &stop, sizeof(stop)) != sizeof(stop)) {
		OIS_EXCEPT(E_General, "Unknown error stopping effect->..");
	}
}

//--------------------------------------------------------------//
void LinuxForceFeedback::_start( int handle) {
	struct input_event play;

	play.type = EV_FF;
	play.code = handle;
	play.value = 1; // Play once.

#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << endl << "LinuxForceFeedback("<< mJoyStick 
		 << ") : Starting effect with handle " << handle << endl;
#endif

	if (write(mJoyStick, &play, sizeof(play)) != sizeof(play)) {
		OIS_EXCEPT(E_General, "Unknown error playing effect->..");
	}
}

//--------------------------------------------------------------//
void LinuxForceFeedback::_unload( int handle)
{
#if (OIS_LINUX_JOYFF_DEBUG > 1)
	cout << endl << "LinuxForceFeedback("<< mJoyStick 
		 << ") : Removing effect with handle " << handle << endl;
#endif

	if (ioctl(mJoyStick, EVIOCRMFF, handle) == -1) {
		OIS_EXCEPT(E_General, "Unknown error removing effect->..");
	}
}
