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
#ifndef OIS_Object_H
#define OIS_Object_H

#include "OISPrereqs.h"
#include "OISInterface.h"

namespace OIS
{
	/**	The base class of all input types. */
	class _OISExport Object
	{
	public:
		virtual ~Object() {}

		/**	@remarks Get the type of device	*/
		Type type() const { return mType; }

		/**	@remarks Get the vender string name	*/
		const std::string& vendor() const { return mVendor; }

		/**	@remarks Get buffered mode - true is buffered, false otherwise */
		virtual bool buffered() const { return mBuffered; }

		/** @remarks Returns this input object's creator */
		InputManager* getCreator() const { return mCreator; }

		/** @remarks Sets buffered mode	*/
		virtual void setBuffered(bool buffered) = 0;

		/**	@remarks Used for updating call once per frame before checking state or to update events */
		virtual void capture() = 0;

		/**	@remarks This may/may not) differentiate the different controllers based on (for instance) a port number (useful for console InputManagers) */
		virtual int getID() const {return mDevID;}

		/**
		@remarks
			If available, get an interface to write to some devices.
			Examples include, turning on and off LEDs, ForceFeedback, etc
		@param type
			The type of interface you are looking for
		*/
		virtual Interface* queryInterface(Interface::IType type) = 0;

		/**	@remarks Internal... Do not call this directly. */
		virtual void _initialize() = 0;

	protected:
		Object(const std::string &vendor, Type iType, bool buffered,
			   int devID, InputManager* creator) :
					mVendor(vendor),
					mType(iType),
					mBuffered(buffered),
					mDevID(devID),
					mCreator(creator) {}

		//! Vendor name if applicable/known
		std::string mVendor;

		//! Type of controller object
		Type mType;

		//! Buffered flag
		bool mBuffered;

		//! Not fully implemented yet
		int mDevID;

		//! The creator who created this object
		InputManager* mCreator;
	};
}
#endif
