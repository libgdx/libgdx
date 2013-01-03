#include "OISConfig.h"
#ifdef OIS_WIN32_WIIMOTE_SUPPORT
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

 # ------------------------#
 # Original License follows:
 # ------------------------#

 * PortAudio Portable Real-Time Audio Library
 * Latest version at: http://www.audiomulch.com/portaudio/
 * <platform> Implementation
 * Copyright (c) 1999-2000 <author(s)>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * Any person wishing to distribute modifications to the Software is
 * requested to send the modifications to the original developer so that
 * they can be incorporated into the canonical version.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
#ifndef OIS_WiiMoteRingBuffer_H
#define OIS_WiiMoteRingBuffer_H

#include "OISPrereqs.h"

namespace OIS
{
	struct WiiMoteEvent
	{
		//! (7 buttons) If a button was just pressed, the bit will be set
		unsigned int pushedButtons;

		//! (7 buttons) If a button was just released, the bit will be set
		unsigned int releasedButtons;

		//! Will be true if POV changed this event
		bool povChanged;

		//! Will be valid if povChanged = true
		unsigned int povDirection;

		//! Will be valid if a movement just occurred on main motion sensing
		bool movement;

		//Values of main orientation vector
		float x, y, z;

		//! Will be valid if a movement just occurred on main motion sensing
		bool movementChuck;

		//Values of main orientation vector
		float nunChuckx, nunChucky, nunChuckz;

		//Used to flag when a Nunchuck axis moved
		bool nunChuckXAxisMoved, nunChuckYAxisMoved;

		//Values of NunChuck JoyStick
		int nunChuckXAxis, nunChuckYAxis;

		//! clear initial state
		void clear()
		{
			pushedButtons = releasedButtons = 0;
			povChanged = false;
			povDirection = 0;

			movement = false;
			x = y = z = 0.0f;

			nunChuckx = nunChucky = nunChuckz = 0;
			movementChuck = false;

			nunChuckXAxisMoved = nunChuckYAxisMoved = false;
			nunChuckXAxis = nunChuckYAxis = 0;
		}
	};

	/// <summary>
	/// Ring Buffer (fifo) used to store 16bit pcm data
	/// </summary>
	class WiiMoteRingBuffer
	{
	private:
		//! Number of bytes in FIFO. Power of 2. Set by RingBuffer_Init
		int bufferSize;
		//! Used for wrapping indices with extra bit to distinguish full/empty.
		int bigMask;
		// Used for fitting indices to buffer.
		int smallMask;

		// Buffer holding the actual event buffers
		WiiMoteEvent *buffer;

		//! Index of next writable byte. Set by RingBuffer_AdvanceWriteIndex.
		volatile int writeIndex; 
		
		//! Index of next readable byte. Set by RingBuffer_AdvanceReadIndex.
		volatile int readIndex;	

	public:
		WiiMoteRingBuffer( unsigned int numEntries )
		{
			numEntries = RoundUpToNextPowerOf2( numEntries );

			//2 bytes per short
			bufferSize = (int)numEntries;
			buffer = new WiiMoteEvent[numEntries];
		
			Flush();
		
			bigMask = (int)(numEntries*2)-1;
			smallMask = (int)(numEntries)-1;
		}

		~WiiMoteRingBuffer()
		{
			delete buffer;
		}

		unsigned int RoundUpToNextPowerOf2( unsigned int n )
		{
			int numBits = 0;
			if( ((n-1) & n) == 0) 
			return n; //Already Power of two.

			while( n > 0 )
			{
				n= n>>1;
				numBits++;
			}
			return (unsigned int)(1<<numBits);
		}


		int GetReadAvailable( )
		{
			return ( (writeIndex - readIndex) & bigMask );
		}


		int GetWriteAvailable( )
		{
			return ( bufferSize - GetReadAvailable());
		}


		int Write( WiiMoteEvent *data, int numEntries )
		{
			int size1 = 0, size2 = 0, numWritten;
			int data1Ptr = 0, data2Ptr = 0;
			
			numWritten = GetWriteRegions( numEntries, data1Ptr, size1, data2Ptr, size2 );

			if( size2 > 0 )
			{
				//copy to two parts
				memcpy( &buffer[data1Ptr], data, sizeof(WiiMoteEvent) * size1 );
				//Array.Copy( data, offsetPtr, buffer, data1Ptr, size1 );
				memcpy( &buffer[data2Ptr], &data[size1], sizeof(WiiMoteEvent) * size2 );
				//Array.Copy( data, offsetPtr + size1, buffer, data2Ptr, size2 );
			}
			else
			{	//Copy all continous
				memcpy( &buffer[data1Ptr], data, sizeof(WiiMoteEvent) * size1 );
				//Array.Copy( data, offsetPtr, buffer, data1Ptr, size1 );
			}
			AdvanceWriteIndex( numWritten );
			return numWritten;
		}


		/// <summary>
		/// Reads requested number of entries into sent array.
		/// Returns number written
		/// </summary>
		int Read( WiiMoteEvent *data, int numEntries )
		{
			int size1 = 0, size2 = 0, numRead, data1Ptr = 0, data2Ptr = 0;
			
			numRead = GetReadRegions( numEntries, data1Ptr, size1, data2Ptr, size2 );
			
			if( size2 > 0 )
			{
				memcpy( data, &buffer[data1Ptr], sizeof(WiiMoteEvent) * size1 );
				//Array.Copy( buffer, data1Ptr, data, 0, size1 );
				memcpy( &data[size1], &buffer[data2Ptr], sizeof(WiiMoteEvent) * size2 );
				//Array.Copy( buffer, data2Ptr, data, size1, size2 );
			}
			else
				memcpy( data, &buffer[data1Ptr], sizeof(WiiMoteEvent) * size1 );
				//Array.Copy( buffer, data1Ptr, data, 0, size1 );

			AdvanceReadIndex( numRead );
			return numRead;
		}

	private:

		int GetWriteRegions( int numEntries, int &dataPtr1, int &sizePtr1,
							 int &dataPtr2, int &sizePtr2 )
		{
			int   index;
			int   available = GetWriteAvailable();
			if( numEntries > available ) 
				numEntries = available;
		
			//Check to see if write is not contiguous.
			index = writeIndex & smallMask;
			if( (index + numEntries) > bufferSize )
			{
				//Write data in two blocks that wrap the buffer.
				int   firstHalf = bufferSize - index;
				dataPtr1 = index;//&buffer[index];
				sizePtr1 = firstHalf;
				dataPtr2 = 0;//&buffer[0];
				sizePtr2 = numEntries - firstHalf;
			}
			else
			{
				dataPtr1 = index;//&buffer[index];
				sizePtr1 = numEntries;
				dataPtr2 = 0;
				sizePtr2 = 0;
			}
			return numEntries;
		}
	

		int GetReadRegions( int numEntries, int &dataPtr1, int &sizePtr1, int &dataPtr2, int &sizePtr2 )
		{
			int   index;
			int   available = GetReadAvailable( );
			if( numEntries > available ) 
				numEntries = available;
			
			// Check to see if read is not contiguous
			index = readIndex & smallMask;
			if( (index + numEntries) > bufferSize )
			{
				// Write data in two blocks that wrap the buffer
				int firstHalf = bufferSize - index;
				dataPtr1 = index;//&buffer[index];
				sizePtr1 = firstHalf;
				dataPtr2 = 0;//&buffer[0];
				sizePtr2 = numEntries - firstHalf;
			}
			else
			{
				dataPtr1 = index;//&buffer[index];
				sizePtr1 = numEntries;
				dataPtr2 = 0;
				sizePtr2 = 0;
			}
			return numEntries;
		}


		int AdvanceWriteIndex( int numEntries )
		{
			 return writeIndex = (writeIndex + numEntries) & bigMask;
		}


		int AdvanceReadIndex( int numEntries )
		{
			return readIndex = (readIndex + numEntries) & bigMask;
		}


		void Flush( )
		{
			writeIndex = readIndex = 0;
		}
	};
}
#endif //#define OIS_WiiMoteRingBuffer_H
#endif
