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
#ifndef OIS_Effect_H
#define OIS_Effect_H

#include "OISPrereqs.h"

namespace OIS
{
	//Predeclare some Effect Property structs
	class ForceEffect;
	class ConstantEffect;
	class RampEffect;
	class PeriodicEffect;
	class ConditionalEffect;

	/**
		Force Feedback is a relatively complex set of properties to upload to a device.
		The best place for information on the different properties, effects, etc is in
		the DX Documentation and MSDN - there are even pretty graphs ther =)
		As this class is modeled on the the DX interface you can apply that same
		knowledge to creating effects via this class on any OS supported by OIS.

		In anycase, this is the main class you will be using. There is *absolutely* no
		need to instance any of the supporting ForceEffect classes yourself.
	*/
	class _OISExport Effect
	{
		/**
			hidden so this class cannot be instanced with default constructor
		*/
		Effect();
	public:
		//! Type of force
		enum EForce
		{
			UnknownForce = 0,
			ConstantForce,
			RampForce,
			PeriodicForce,
			ConditionalForce,
			CustomForce,
			_ForcesNumber // Always keep in last position.
		};

		static const char* getForceTypeName(EForce eValue);

		//! Type of effect
		enum EType
		{
			//Type ----- Pairs with force:
			Unknown = 0, //UnknownForce
			Constant,    //ConstantForce
			Ramp,        //RampForce
			Square,      //PeriodicForce
			Triangle,    //PeriodicForce
            Sine,        //PeriodicForce
			SawToothUp,  //PeriodicForce
			SawToothDown,//PeriodicForce
			Friction,    //ConditionalForce
			Damper,      //ConditionalForce
			Inertia,     //ConditionalForce
			Spring,      //ConditionalForce
			Custom,      //CustomForce
			_TypesNumber // Always keep in last position.
		};

		static const char* getEffectTypeName(EType eValue);

		//! Direction of the Force
		enum EDirection
		{
			NorthWest,
			North,
			NorthEast,
			East,
			SouthEast,
			South,
			SouthWest,
			West,
			_DirectionsNumber // Always keep in last position.
		};

		static const char* getDirectionName(EDirection eValue);

		/**
			This constructor allows you to set the force type and effect.
		*/
		Effect(EForce ef, EType et);
		virtual ~Effect();

		const EForce force;
		const EType type;

		//Infinite Time
		static const unsigned int OIS_INFINITE = 0xFFFFFFFF;

		//-------------------------------------------------------------------//
		//--- Set these variables before uploading or modifying an effect ---//

		//Direction to apply to the force - affects two axes+ effects
		EDirection direction;

		//Number of button triggering an effect (-1 means no trigger)
		short trigger_button;

		//Time to wait before an effect can be re-triggered (microseconds)
		unsigned int trigger_interval;

		//Duration of an effect (microseconds)
		unsigned int replay_length;

		//Time to wait before to start playing an effect (microseconds)
		unsigned int replay_delay;

		//Get the specific Force Effect. This should be cast depending on the EForce
		ForceEffect* getForceEffect() const;

		/**
		@remarks
			Set the number of Axes to use before the initial creation of the effect.
			Can only be done prior to creation! Use the FF interface to determine
			how many axes can be used (are availiable)
		*/
		void setNumAxes(short nAxes);

		/**
		@remarks
			Returns the number of axes used in this effect
		*/
		short getNumAxes() const;

		//------------- Library Internal -------------------------------------//
		/**
			set internally.. do not change or you will not be able to upload/stop
			this effect any more. It will become lost. It is mutable so even
			with const reference it can/will be changed by this lib
		*/
		mutable int _handle;
	protected:
		ForceEffect* effect; //Properties depend on EForce
		short axes;          //Number of axes to use in effect
	};

	//-----------------------------------------------------------------------------//
	/**
		Base class of all effect property classes
	*/
	class _OISExport ForceEffect
	{
	public:
		virtual ~ForceEffect() {}
	};

	//-----------------------------------------------------------------------------//
	/**
		An optional envelope to be applied to the start/end of an effect. If any of
		these values are nonzero, then the envelope will be used in setting up the
		effect.
	*/
	class _OISExport Envelope : public ForceEffect
	{
	public:
		Envelope() : attackLength(0), attackLevel(0), fadeLength(0), fadeLevel(0) {}
#if defined(OIS_MSVC_COMPILER)
  #pragma warning (push)
  #pragma warning (disable : 4800)
#endif
		bool isUsed() const { return attackLength | attackLevel | fadeLength | fadeLevel; }
#if defined(OIS_MSVC_COMPILER)
  #pragma warning (pop)
#endif

		// Duration of the attack (microseconds)
		unsigned int attackLength;

		// Absolute level at the beginning of the attack (0 to 10K)
		// (automatically signed when necessary by FF core according to effect level sign)
		unsigned short attackLevel;

		// Duration of fade (microseconds)
		unsigned int fadeLength;

		// Absolute level at the end of fade (0 to 10K)
		// (automatically signed when necessary by FF core according to effect level sign)
		unsigned short fadeLevel;
	};

	//-----------------------------------------------------------------------------//
	/**
		Use this class when dealing with Force type of Constant
	*/
	class _OISExport ConstantEffect : public ForceEffect
	{
	public:
		ConstantEffect() : level(5000) {}

		class Envelope envelope; //Optional envolope
		signed short level;       //-10K to +10k
	};

	//-----------------------------------------------------------------------------//
	/**
		Use this class when dealing with Force type of Ramp
	*/
	class _OISExport RampEffect : public ForceEffect
	{
	public:
		RampEffect() : startLevel(0), endLevel(0) {}

        class Envelope envelope; //Optional envelope
		signed short startLevel;  //-10K to +10k
		signed short endLevel;    //-10K to +10k
	};

	//-----------------------------------------------------------------------------//
	/**
		Use this class when dealing with Force type of Periodic
	*/
	class _OISExport PeriodicEffect : public ForceEffect
	{
	public:
		PeriodicEffect() : magnitude(0), offset(0), phase(0), period(0) {}

		class Envelope envelope;  //Optional Envelope

		unsigned short magnitude;  //0 to 10,0000
		signed short   offset;
		unsigned short phase;      //Position at which playback begins 0 to 35,999
		unsigned int   period;     //Period of effect (microseconds)
	};

	//-----------------------------------------------------------------------------//
	/**
		Use this class when dealing with Force type of Condional
	*/
	class _OISExport ConditionalEffect : public ForceEffect
	{
	public:
		ConditionalEffect() :
            rightCoeff(0), leftCoeff(0), rightSaturation(0), leftSaturation(0),
			deadband(0), center(0) {}

		signed short   rightCoeff;      //-10k to +10k (Positive Coeff)
		signed short   leftCoeff;       //-10k to +10k (Negative Coeff)

		unsigned short rightSaturation; //0 to 10k (Pos Saturation)
		unsigned short leftSaturation;  //0 to 10k (Neg Saturation)

		//Region around center in which the condition is not active, in the range
		//from 0 through 10,000
		unsigned short deadband;

		//(Offset in DX) -10k and 10k
		signed short center;
	};
}
#endif //OIS_Effect_H
