#ifndef INCLUDED_IMF_PXR24_COMPRESSOR_H
#define INCLUDED_IMF_PXR24_COMPRESSOR_H

/////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2004, Pixar Animation Studios
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions  are
// met:
// *       Redistributions of source code must retain the above  copyright
// notice, this list of conditions and the following disclaimer.
// *       Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following  disclaimer
// in the documentation and/or other materials provided with the
// distribution.
// *       Neither the name of Pixar Animation Studios nor the names of
// its contributors may be used to endorse or promote products derived
// from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
/////////////////////////////////////////////////////////////////////////////

//-----------------------------------------------------------------------------
//
//	class Pxr24Compressor -- Loren Carpenter's 24-bit float compressor
//
//-----------------------------------------------------------------------------

#include <ImfCompressor.h>

namespace Imf {

class ChannelList;


class Pxr24Compressor: public Compressor
{
  public:

    Pxr24Compressor (const Header &hdr, 
                     size_t maxScanLineSize,
                     size_t numScanLines);

    virtual ~Pxr24Compressor ();

    virtual int		numScanLines () const;

    virtual Format	format () const;

    virtual int		compress (const char *inPtr,
				  int inSize,
				  int minY,
				  const char *&outPtr);                  
                  
    virtual int		compressTile (const char *inPtr,
				      int inSize,
				      Imath::Box2i range,
				      const char *&outPtr);

    virtual int		uncompress (const char *inPtr,
				    int inSize,
				    int minY,
				    const char *&outPtr);
                    
    virtual int		uncompressTile (const char *inPtr,
					int inSize,
					Imath::Box2i range,
					const char *&outPtr);
  private:

    int			compress (const char *inPtr,
				  int inSize,
				  Imath::Box2i range,
				  const char *&outPtr);
 
    int			uncompress (const char *inPtr,
				    int inSize,
				    Imath::Box2i range,
				    const char *&outPtr);

    int			_maxScanLineSize;
    int			_numScanLines;
    unsigned char *	_tmpBuffer;
    char *		_outBuffer;
    const ChannelList &	_channels;
    int			_minX;
    int			_maxX;
    int			_maxY;
};


} // namespace Imf

#endif
