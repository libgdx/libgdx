///////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2002, Industrial Light & Magic, a division of Lucas
// Digital Ltd. LLC
// 
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
// *       Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// *       Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
// *       Neither the name of Industrial Light & Magic nor the names of
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
///////////////////////////////////////////////////////////////////////////



#ifndef INCLUDED_IMF_MATRIX_ATTRIBUTE_H
#define INCLUDED_IMF_MATRIX_ATTRIBUTE_H

//-----------------------------------------------------------------------------
//
//	class M33fAttribute
//	class M33dAttribute
//	class M44fAttribute
//	class M44dAttribute
//
//-----------------------------------------------------------------------------

#include <ImfAttribute.h>
#include "ImathMatrix.h"


namespace Imf {


typedef TypedAttribute<Imath::M33f> M33fAttribute;
template <> const char *M33fAttribute::staticTypeName ();
template <> void M33fAttribute::writeValueTo (OStream &, int) const;
template <> void M33fAttribute::readValueFrom (IStream &, int, int);


typedef TypedAttribute<Imath::M33d> M33dAttribute;
template <> const char *M33dAttribute::staticTypeName ();
template <> void M33dAttribute::writeValueTo (OStream &, int) const;
template <> void M33dAttribute::readValueFrom (IStream &, int, int);


typedef TypedAttribute<Imath::M44f> M44fAttribute;
template <> const char *M44fAttribute::staticTypeName ();
template <> void M44fAttribute::writeValueTo (OStream &, int) const;
template <> void M44fAttribute::readValueFrom (IStream &, int, int);


typedef TypedAttribute<Imath::M44d> M44dAttribute;
template <> const char *M44dAttribute::staticTypeName ();
template <> void M44dAttribute::writeValueTo (OStream &, int) const;
template <> void M44dAttribute::readValueFrom (IStream &, int, int);


} // namespace Imf

// Metrowerks compiler wants the .cpp file inlined, too
#ifdef __MWERKS__
#include <ImfMatrixAttribute.cpp>
#endif

#endif
