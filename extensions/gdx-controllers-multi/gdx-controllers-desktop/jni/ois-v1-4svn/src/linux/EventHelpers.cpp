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
#include "linux/EventHelpers.h"
#include "linux/LinuxPrereqs.h"
#include "linux/LinuxForceFeedback.h"
#include "OISException.h"
#include "OISJoyStick.h"

#include <linux/input.h>
#include <cstring>

//#define OIS_LINUX_JOY_DEBUG

#ifdef OIS_LINUX_JOY_DEBUG
# include <iostream>
#endif

using namespace std;
using namespace OIS;

class DeviceComponentInfo
{
public:
	vector<int> buttons, relAxes, absAxes, hats;
};

bool inline isBitSet(unsigned char bits[], unsigned int bit)
{
  return (bits[(bit)/(sizeof(unsigned char)*8)] >> ((bit)%(sizeof(unsigned char)*8))) & 1;
}

//-----------------------------------------------------------------------------//
DeviceComponentInfo getComponentInfo( int deviceID )
{
	unsigned char ev_bits[1 + EV_MAX/8/sizeof(unsigned char)];
	memset( ev_bits, 0, sizeof(ev_bits) );

	//Read "all" (hence 0) components of the device
#ifdef OIS_LINUX_JOY_DEBUG
	cout << "EventUtils::getComponentInfo(" << deviceID 
		 << ") : Reading device events features" << endl;
#endif
	if (ioctl(deviceID, EVIOCGBIT(0, sizeof(ev_bits)), ev_bits) == -1)
		OIS_EXCEPT( E_General, "Could not read device events features");

	DeviceComponentInfo components;

	for (int i = 0; i < EV_MAX; i++)
	{
		if( isBitSet(ev_bits, i) )
		{
		    // Absolute axis.
		    if(i == EV_ABS)
			{
			    unsigned char abs_bits[1 + ABS_MAX/8/sizeof(unsigned char)];
			    memset( abs_bits, 0, sizeof(abs_bits) );

#ifdef OIS_LINUX_JOY_DEBUG
				cout << "EventUtils::getComponentInfo(" << deviceID 
					 << ") : Reading device absolute axis features" << endl;
#endif

				if (ioctl(deviceID, EVIOCGBIT(i, sizeof(abs_bits)), abs_bits) == -1)
				    OIS_EXCEPT( E_General, "Could not read device absolute axis features");

				for (int j = 0; j < ABS_MAX; j++)
				{
				    if( isBitSet(abs_bits, j) )
					{
						//input_absinfo abInfo;
						//ioctl( fd, EVIOCGABS(j), abInfo );
						if( j >= ABS_HAT0X && j <= ABS_HAT3Y )
						{
							components.hats.push_back(j);
						}
						else
						{
							components.absAxes.push_back(j);
							//input_absinfo absinfo;
							//ioctl(deviceID, EVIOCGABS(j), &absinfo);
							//We cannot actually change these values :|
							//absinfo.minimum = JoyStick::MIN_AXIS;
							//absinfo.maximum = JoyStick::MAX_AXIS;
							//ioctl(deviceID, EVIOCSABS(j), &absinfo);
						}
					}
				}
			}
			else if(i == EV_REL)
			{
			    unsigned char rel_bits[1 + REL_MAX/8/sizeof(unsigned char)];
				memset( rel_bits, 0, sizeof(rel_bits) );
				
#ifdef OIS_LINUX_JOY_DEBUG
				cout << "EventUtils::getComponentInfo(" << deviceID 
					 << ") : Reading device relative axis features" << endl;
#endif

				if (ioctl(deviceID, EVIOCGBIT(i, sizeof(rel_bits)), rel_bits) == -1)
				    OIS_EXCEPT( E_General, "Could not read device relative axis features");
				
				for (int j = 0; j < REL_MAX; j++)
				{
				    if( isBitSet(rel_bits, j) )
					{
					    components.relAxes.push_back(j);
					}
				}
			}
			else if(i == EV_KEY)
			{
			    unsigned char key_bits[1 + KEY_MAX/8/sizeof(unsigned char)];
				memset( key_bits, 0, sizeof(key_bits) );
				
#ifdef OIS_LINUX_JOY_DEBUG
				cout << "EventUtils::getComponentInfo(" << deviceID 
					 << ") : Reading device buttons features" << endl;
#endif

				if (ioctl(deviceID, EVIOCGBIT(i, sizeof(key_bits)), key_bits) == -1)
				    OIS_EXCEPT( E_General, "Could not read device buttons features");
				
				for (int j = 0; j < KEY_MAX; j++)
				{
				    if( isBitSet(key_bits, j) )
					{
					    components.buttons.push_back(j);
					}
				}
			}
		}
	}

	return components;
}

//-----------------------------------------------------------------------------//
bool EventUtils::isJoyStick( int deviceID, JoyStickInfo &js )
{
	if( deviceID == -1 ) 
		OIS_EXCEPT( E_General, "Error with File Descriptor" );

	DeviceComponentInfo info = getComponentInfo( deviceID );

	int buttons = 0;
	bool joyButtonFound = false;
	js.button_map.clear();

	#ifdef OIS_LINUX_JOY_DEBUG
	cout << endl << "Displaying ButtonMapping Status:" << endl;
	#endif
	for(vector<int>::iterator i = info.buttons.begin(), e = info.buttons.end(); i != e; ++i )
	{
		//Check to ensure we find at least one joy only button
		if( (*i >= BTN_JOYSTICK && *i < BTN_GAMEPAD)  
			|| (*i >= BTN_GAMEPAD && *i < BTN_DIGI)
			|| (*i >= BTN_WHEEL && *i < KEY_OK) )
			joyButtonFound = true;

		js.button_map[*i] = buttons++;

		#ifdef OIS_LINUX_JOY_DEBUG
		  cout << "Button Mapping ID (hex): " << hex << *i 
			   << " OIS Button Num: " << dec << buttons-1 << endl;
		#endif
	}
	#ifdef OIS_LINUX_JOY_DEBUG
	cout << endl;
	#endif

	//Joy Buttons found, so it must be a joystick or pad
	if( joyButtonFound )
	{
		js.joyFileD = deviceID;
		js.vendor = getName(deviceID);
		js.buttons = buttons;
		js.axes = info.relAxes.size() + info.absAxes.size();
		js.hats = info.hats.size();
		#ifdef OIS_LINUX_JOY_DEBUG
		  cout << endl << "Device name:" << js.vendor << endl;
		  cout << "Device unique Id:" << getUniqueId(deviceID) << endl;
		  cout << "Device physical location:" << getPhysicalLocation(deviceID) << endl;
		#endif

		//Map the Axes
		#ifdef OIS_LINUX_JOY_DEBUG
		  cout << endl << "Displaying AxisMapping Status:" << endl;
		#endif
		int axes = 0;
		for(vector<int>::iterator i = info.absAxes.begin(), e = info.absAxes.end(); i != e; ++i )
		{
			js.axis_map[*i] = axes;

#ifdef OIS_LINUX_JOY_DEBUG
			cout << "EventUtils::isJoyStick(" << deviceID 
					  << ") : Reading device absolute axis #" << *i << " features" << endl;
#endif

			input_absinfo absinfo;
			if (ioctl(deviceID, EVIOCGABS(*i), &absinfo) == -1)
				OIS_EXCEPT( E_General, "Could not read device absolute axis features");
			js.axis_range[axes] = Range(absinfo.minimum, absinfo.maximum);

			#ifdef OIS_LINUX_JOY_DEBUG
			  cout << "Axis Mapping ID (hex): " << hex << *i 
				   << " OIS Axis Num: " << dec << axes << endl;
			#endif

			++axes;
		}
	}

	return joyButtonFound;
}

//-----------------------------------------------------------------------------//
string EventUtils::getName( int deviceID )
{
#ifdef OIS_LINUX_JOY_DEBUG
	cout << "EventUtils::getName(" << deviceID 
		 << ") : Reading device name" << endl;
#endif

	char name[OIS_DEVICE_NAME];
	if (ioctl(deviceID, EVIOCGNAME(OIS_DEVICE_NAME), name) == -1)
		OIS_EXCEPT( E_General, "Could not read device name");
	return string(name);
}

//-----------------------------------------------------------------------------//
string EventUtils::getUniqueId( int deviceID )
{
#ifdef OIS_LINUX_JOY_DEBUG
	cout << "EventUtils::getUniqueId(" << deviceID 
		 << ") : Reading device unique Id" << endl;
#endif

#define OIS_DEVICE_UNIQUE_ID 128
	char uId[OIS_DEVICE_UNIQUE_ID];
	if (ioctl(deviceID, EVIOCGUNIQ(OIS_DEVICE_UNIQUE_ID), uId) == -1)
		OIS_EXCEPT( E_General, "Could not read device unique Id");
	return string(uId);
}

//-----------------------------------------------------------------------------//
string EventUtils::getPhysicalLocation( int deviceID )
{
#ifdef OIS_LINUX_JOY_DEBUG
	cout << "EventUtils::getPhysicalLocation(" << deviceID 
		 << ") : Reading device physical location" << endl;
#endif

#define OIS_DEVICE_PHYSICAL_LOCATION 128
	char physLoc[OIS_DEVICE_PHYSICAL_LOCATION];
	if (ioctl(deviceID, EVIOCGPHYS(OIS_DEVICE_PHYSICAL_LOCATION), physLoc) == -1)
		OIS_EXCEPT( E_General, "Could not read device physical location");
	return string(physLoc);
}

//-----------------------------------------------------------------------------//
void EventUtils::enumerateForceFeedback( int deviceID, LinuxForceFeedback** ff )
{
	//Linux Event to OIS Event Mappings
	map<int, Effect::EType> typeMap;
	typeMap[FF_CONSTANT] = Effect::Constant;
	typeMap[FF_RAMP]     = Effect::Ramp;
	typeMap[FF_SPRING]   = Effect::Spring;
	typeMap[FF_FRICTION] = Effect::Friction;
	typeMap[FF_SQUARE]   = Effect::Square;
	typeMap[FF_TRIANGLE] = Effect::Triangle;
	typeMap[FF_SINE]     = Effect::Sine;
	typeMap[FF_SAW_UP]   = Effect::SawToothUp;
	typeMap[FF_SAW_DOWN] = Effect::SawToothDown;
	typeMap[FF_DAMPER]   = Effect::Damper;
	typeMap[FF_INERTIA]  = Effect::Inertia;
	typeMap[FF_CUSTOM]   = Effect::Custom;

	map<int, Effect::EForce> forceMap;
	forceMap[FF_CONSTANT] = Effect::ConstantForce;
	forceMap[FF_RAMP]     = Effect::RampForce;
	forceMap[FF_SPRING]   = Effect::ConditionalForce;
	forceMap[FF_FRICTION] = Effect::ConditionalForce;
	forceMap[FF_SQUARE]   = Effect::PeriodicForce;
	forceMap[FF_TRIANGLE] = Effect::PeriodicForce;
	forceMap[FF_SINE]     = Effect::PeriodicForce;
	forceMap[FF_SAW_UP]   = Effect::PeriodicForce;
	forceMap[FF_SAW_DOWN] = Effect::PeriodicForce;
	forceMap[FF_DAMPER]   = Effect::ConditionalForce;
	forceMap[FF_INERTIA]  = Effect::ConditionalForce;
	forceMap[FF_CUSTOM]   = Effect::CustomForce;

	//Remove any previously existing memory and create fresh
	removeForceFeedback( ff );
	*ff = new LinuxForceFeedback(deviceID);

	//Read overall force feedback features
	unsigned char ff_bits[1 + FF_MAX/8/sizeof(unsigned char)];
	memset(ff_bits, 0, sizeof(ff_bits));

#ifdef OIS_LINUX_JOY_DEBUG
	cout << "EventUtils::enumerateForceFeedback(" << deviceID 
		 << ") : Reading device force feedback features" << endl;
#endif

	if (ioctl(deviceID, EVIOCGBIT(EV_FF, sizeof(ff_bits)), ff_bits) == -1)
		OIS_EXCEPT( E_General, "Could not read device force feedback features");


    #ifdef OIS_LINUX_JOY_DEBUG
	cout << "FF bits: " << hex;
	for (int i = 0; i < sizeof(ff_bits); i++)
		cout << (int)ff_bits[i];
	cout << endl << dec;
    #endif

	//FF Axes
	//if( isBitSet(ff_bits, ABS_X) ) //X Axis
	//if( isBitSet(ff_bits, ABS_Y) ) //Y Axis
	//if( isBitSet(ff_bits, ABS_WHEEL) ) //Wheel

	//FF Effects
	for( int effect = FF_EFFECT_MIN; effect <= FF_WAVEFORM_MAX; effect++ )
	{
		// The RUMBLE force type is ignored, as periodic force one is more powerfull.
		// The PERIODIC force type is processed later, for each associated periodic effect type.
		if (effect == FF_RUMBLE || effect == FF_PERIODIC)
			continue;

		if(isBitSet(ff_bits, effect))
		{
			#ifdef OIS_LINUX_JOY_DEBUG
		    cout << "  Effect Type: " << Effect::getEffectTypeName(typeMap[effect]) << endl;
			#endif

			(*ff)->_addEffectTypes( forceMap[effect], typeMap[effect] );
		}
	}

	//FF device properties
	if (isBitSet(ff_bits, FF_GAIN))
		(*ff)->_setGainSupport(true);
		
	if (isBitSet(ff_bits, FF_AUTOCENTER))
		(*ff)->_setAutoCenterSupport(true);

	//Check to see if any effects were added, else destroy the pointer
	const ForceFeedback::SupportedEffectList &list = (*ff)->getSupportedEffects();
	if( list.size() == 0 )
		removeForceFeedback( ff );
}

//-----------------------------------------------------------------------------//
void EventUtils::removeForceFeedback( LinuxForceFeedback** ff )
{
	delete *ff;
	*ff = 0;
}
