///////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2004, Industrial Light & Magic, a division of Lucas
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


//-----------------------------------------------------------------------------
//
//	class OutputFile
//
//-----------------------------------------------------------------------------

#include <ImfOutputFile.h>
#include <ImfInputFile.h>
#include <ImfChannelList.h>
#include <ImfMisc.h>
#include <ImfStdIO.h>
#include <ImfCompressor.h>
#include "ImathBox.h"
#include "ImathFun.h"
#include <ImfArray.h>
#include <ImfXdr.h>
#include <ImfPreviewImageAttribute.h>
#include "IlmThreadPool.h"
#include "IlmThreadSemaphore.h"
#include "IlmThreadMutex.h"
#include "Iex.h"
#include <string>
#include <vector>
#include <fstream>
#include <assert.h>


namespace Imf {

using Imath::Box2i;
using Imath::divp;
using Imath::modp;
using std::string;
using std::vector;
using std::ofstream;
using std::min;
using std::max;
using IlmThread::Mutex;
using IlmThread::Lock;
using IlmThread::Semaphore;
using IlmThread::Task;
using IlmThread::TaskGroup;
using IlmThread::ThreadPool;

namespace {


struct OutSliceInfo
{
    PixelType		type;
    const char *	base;
    size_t		xStride;
    size_t		yStride;
    int			xSampling;
    int			ySampling;
    bool		zero;

    OutSliceInfo (PixelType type = HALF,
	          const char *base = 0,
	          size_t xStride = 0,
	          size_t yStride = 0,
	          int xSampling = 1,
	          int ySampling = 1,
	          bool zero = false);
};


OutSliceInfo::OutSliceInfo (PixelType t,
		            const char *b,
		            size_t xs, size_t ys,
		            int xsm, int ysm,
		            bool z)
:
    type (t),
    base (b),
    xStride (xs),
    yStride (ys),
    xSampling (xsm),
    ySampling (ysm),
    zero (z)
{
    // empty
}


struct LineBuffer
{
    Array<char>		buffer;
    const char *	dataPtr;
    int			dataSize;
    char *		endOfLineBufferData;
    int			minY;
    int			maxY;
    int			scanLineMin;
    int			scanLineMax;
    Compressor *	compressor;
    bool		partiallyFull;        // has incomplete data
    bool		hasException;
    string		exception;

    LineBuffer (Compressor *comp);
    ~LineBuffer ();

    void		wait () {_sem.wait();}
    void		post () {_sem.post();}

  private:

    Semaphore		_sem;
};


LineBuffer::LineBuffer (Compressor *comp) :
    dataPtr (0),
    dataSize (0),
    compressor (comp),
    partiallyFull (false),
    hasException (false),
    exception (),
    _sem (1)
{
    // empty
}


LineBuffer::~LineBuffer ()
{
    delete compressor;
}

} // namespace


struct OutputFile::Data: public Mutex
{
    Header		 header;		// the image header
    int			 version;		// file format version
    Int64		 previewPosition;       // file position for preview
    FrameBuffer		 frameBuffer;           // framebuffer to write into
    int			 currentScanLine;       // next scanline to be written
    int			 missingScanLines;      // number of lines to write
    LineOrder		 lineOrder;		// the file's lineorder
    int			 minX;			// data window's min x coord
    int			 maxX;			// data window's max x coord
    int			 minY;			// data window's min y coord
    int			 maxY;			// data window's max x coord
    vector<Int64>	 lineOffsets;		// stores offsets in file for
						// each scanline
    vector<size_t>	 bytesPerLine;          // combined size of a line over
                                                // all channels
    vector<size_t>	 offsetInLineBuffer;    // offset for each scanline in
                                                // its linebuffer
    Compressor::Format	 format;                // compressor's data format
    vector<OutSliceInfo> slices;		// info about channels in file
    OStream *		 os;			// file stream to write to
    bool		 deleteStream;
    Int64		 lineOffsetsPosition;   // file position for line
                                                // offset table
    Int64		 currentPosition;       // current file position

    vector<LineBuffer*>  lineBuffers;           // each holds one line buffer
    int			 linesInBuffer;         // number of scanlines each
                                                // buffer holds
    size_t		 lineBufferSize;        // size of the line buffer

     Data (bool deleteStream, int numThreads);
    ~Data ();


    inline LineBuffer *	getLineBuffer (int number); // hash function from line
    						    // buffer indices into our
						    // vector of line buffers
};


OutputFile::Data::Data (bool deleteStream, int numThreads):
    os (0),
    deleteStream (deleteStream),
    lineOffsetsPosition (0)
{
    //
    // We need at least one lineBuffer, but if threading is used,
    // to keep n threads busy we need 2*n lineBuffers.
    //

    lineBuffers.resize (max (1, 2 * numThreads));
}


OutputFile::Data::~Data ()
{
    if (deleteStream)
	delete os;

    for (size_t i = 0; i < lineBuffers.size(); i++)
        delete lineBuffers[i];
}


LineBuffer*
OutputFile::Data::getLineBuffer (int number)
{
    return lineBuffers[number % lineBuffers.size()];
}


namespace {


Int64
writeLineOffsets (OStream &os, const vector<Int64> &lineOffsets)
{
    Int64 pos = os.tellp();

    if (pos == -1)
	Iex::throwErrnoExc ("Cannot determine current file position (%T).");

    for (unsigned int i = 0; i < lineOffsets.size(); i++)
	Xdr::write <StreamIO> (os, lineOffsets[i]);

    return pos;
}


void
writePixelData (OutputFile::Data *ofd,
                int lineBufferMinY,
		const char pixelData[],
		int pixelDataSize)
{
    //
    // Store a block of pixel data in the output file, and try
    // to keep track of the current writing position the file
    // without calling tellp() (tellp() can be fairly expensive).
    //

    Int64 currentPosition = ofd->currentPosition;
    ofd->currentPosition = 0;

    if (currentPosition == 0)
	currentPosition = ofd->os->tellp();

    ofd->lineOffsets[(ofd->currentScanLine - ofd->minY) / ofd->linesInBuffer] =
	currentPosition;

    #ifdef DEBUG

	assert (ofd->os->tellp() == currentPosition);

    #endif

    Xdr::write <StreamIO> (*ofd->os, lineBufferMinY);
    Xdr::write <StreamIO> (*ofd->os, pixelDataSize);
    ofd->os->write (pixelData, pixelDataSize);

    ofd->currentPosition = currentPosition +
			   Xdr::size<int>() +
			   Xdr::size<int>() +
			   pixelDataSize;
}


inline void
writePixelData (OutputFile::Data *ofd, const LineBuffer *lineBuffer)
{
    writePixelData (ofd,
		    lineBuffer->minY,
                    lineBuffer->dataPtr,
		    lineBuffer->dataSize);
}


void
convertToXdr (OutputFile::Data *ofd,
              Array<char> &lineBuffer,
              int lineBufferMinY,
              int lineBufferMaxY,
              int inSize)
{
    //
    // Convert the contents of a lineBuffer from the machine's native
    // representation to Xdr format.  This function is called by
    // CompressLineBuffer::execute(), below, if the compressor wanted
    // its input pixel data in the machine's native format, but then
    // failed to compress the data (most compressors will expand rather
    // than compress random input data).
    //
    // Note that this routine assumes that the machine's native
    // representation of the pixel data has the same size as the
    // Xdr representation.  This makes it possible to convert the
    // pixel data in place, without an intermediate temporary buffer.
    //
   
    int startY, endY;		// The first and last scanlines in
    				// the file that are in the lineBuffer.
    int step;
    
    if (ofd->lineOrder == INCREASING_Y)
    {
	startY = max (lineBufferMinY, ofd->minY);
	endY = min (lineBufferMaxY, ofd->maxY) + 1;
        step = 1;
    }
    else
    {
	startY = min (lineBufferMaxY, ofd->maxY);
	endY = max (lineBufferMinY, ofd->minY) - 1;
        step = -1;
    }

    //
    // Iterate over all scanlines in the lineBuffer to convert.
    //

    for (int y = startY; y != endY; y += step)
    {
	//
        // Set these to point to the start of line y.
        // We will write to writePtr from readPtr.
	//
	
        char *writePtr = lineBuffer + ofd->offsetInLineBuffer[y - ofd->minY];
        const char *readPtr = writePtr;
        
	//
        // Iterate over all slices in the file.
	//
	
        for (unsigned int i = 0; i < ofd->slices.size(); ++i)
        {
            //
            // Test if scan line y of this channel is
            // contains any data (the scan line contains
            // data only if y % ySampling == 0).
            //

            const OutSliceInfo &slice = ofd->slices[i];

            if (modp (y, slice.ySampling) != 0)
                continue;

            //
            // Find the number of sampled pixels, dMaxX-dMinX+1, for
	    // slice i in scan line y (i.e. pixels within the data window
            // for which x % xSampling == 0).
            //

            int dMinX = divp (ofd->minX, slice.xSampling);
            int dMaxX = divp (ofd->maxX, slice.xSampling);
            
	    //
            // Convert the samples in place.
	    //
            
            convertInPlace (writePtr, readPtr, slice.type, dMaxX - dMinX + 1);
        }
    }
}


//
// A LineBufferTask encapsulates the task of copying a set of scanlines
// from the user's frame buffer into a LineBuffer object, compressing
// the data if necessary.
//

class LineBufferTask: public Task
{
  public:

    LineBufferTask (TaskGroup *group,
                    OutputFile::Data *ofd,
		    int number,
                    int scanLineMin,
		    int scanLineMax);

    virtual ~LineBufferTask (); 

    virtual void	execute ();

  private:

    OutputFile::Data *	_ofd;
    LineBuffer *	_lineBuffer;
};


LineBufferTask::LineBufferTask
    (TaskGroup *group,
     OutputFile::Data *ofd,
     int number,
     int scanLineMin,
     int scanLineMax)
:
    Task (group),
    _ofd (ofd),
    _lineBuffer (_ofd->getLineBuffer(number))
{
    //
    // Wait for the lineBuffer to become available
    //

    _lineBuffer->wait ();
    
    //
    // Initialize the lineBuffer data if necessary
    //

    if (!_lineBuffer->partiallyFull)
    {
        _lineBuffer->endOfLineBufferData = _lineBuffer->buffer;

        _lineBuffer->minY = _ofd->minY + number * _ofd->linesInBuffer;

        _lineBuffer->maxY = min (_lineBuffer->minY + _ofd->linesInBuffer - 1,
				 _ofd->maxY);

        _lineBuffer->partiallyFull = true;
    }
    
    _lineBuffer->scanLineMin = max (_lineBuffer->minY, scanLineMin);
    _lineBuffer->scanLineMax = min (_lineBuffer->maxY, scanLineMax);
}


LineBufferTask::~LineBufferTask ()
{
    //
    // Signal that the line buffer is now free
    //

    _lineBuffer->post ();
}


void
LineBufferTask::execute ()
{
    try
    {
        //
        // First copy the pixel data from the
	// frame buffer into the line buffer
        //
        
        int yStart, yStop, dy;

        if (_ofd->lineOrder == INCREASING_Y)
        {
            yStart = _lineBuffer->scanLineMin;
            yStop = _lineBuffer->scanLineMax + 1;
            dy = 1;
        }
        else
        {
            yStart = _lineBuffer->scanLineMax;
            yStop = _lineBuffer->scanLineMin - 1;
            dy = -1;
        }
    
	int y;

        for (y = yStart; y != yStop; y += dy)
        {
            //
            // Gather one scan line's worth of pixel data and store
            // them in _ofd->lineBuffer.
            //
        
            char *writePtr = _lineBuffer->buffer +
                             _ofd->offsetInLineBuffer[y - _ofd->minY];
            //
            // Iterate over all image channels.
            //
        
            for (unsigned int i = 0; i < _ofd->slices.size(); ++i)
            {
                //
                // Test if scan line y of this channel contains any data
		// (the scan line contains data only if y % ySampling == 0).
                //
        
                const OutSliceInfo &slice = _ofd->slices[i];
        
                if (modp (y, slice.ySampling) != 0)
                    continue;
        
                //
                // Find the x coordinates of the leftmost and rightmost
                // sampled pixels (i.e. pixels within the data window
                // for which x % xSampling == 0).
                //
        
                int dMinX = divp (_ofd->minX, slice.xSampling);
                int dMaxX = divp (_ofd->maxX, slice.xSampling);
        
                //
		// Fill the line buffer with with pixel data.
                //
        
                if (slice.zero)
                {
                    //
                    // The frame buffer contains no data for this channel.
                    // Store zeroes in _lineBuffer->buffer.
                    //
                    
                    fillChannelWithZeroes (writePtr, _ofd->format, slice.type,
                                           dMaxX - dMinX + 1);
                }
                else
                {
                    //
                    // If necessary, convert the pixel data to Xdr format.
		    // Then store the pixel data in _ofd->lineBuffer.
                    //
        
                    const char *linePtr = slice.base +
                                          divp (y, slice.ySampling) *
                                          slice.yStride;
        
                    const char *readPtr = linePtr + dMinX * slice.xStride;
                    const char *endPtr  = linePtr + dMaxX * slice.xStride;
    
                    copyFromFrameBuffer (writePtr, readPtr, endPtr,
                                         slice.xStride, _ofd->format,
                                         slice.type);
                }
            }
        
            if (_lineBuffer->endOfLineBufferData < writePtr)
                _lineBuffer->endOfLineBufferData = writePtr;
        
            #ifdef DEBUG
        
                assert (writePtr - (_lineBuffer->buffer +
                        _ofd->offsetInLineBuffer[y - _ofd->minY]) ==
                        (int) _ofd->bytesPerLine[y - _ofd->minY]);
        
            #endif
        
        }
    
        //
        // If the next scanline isn't past the bounds of the lineBuffer
        // then we are done, otherwise compress the linebuffer
        //
    
        if (y >= _lineBuffer->minY && y <= _lineBuffer->maxY)
            return;
    
        _lineBuffer->dataPtr = _lineBuffer->buffer;

        _lineBuffer->dataSize = _lineBuffer->endOfLineBufferData -
                                _lineBuffer->buffer;
    
	//
        // Compress the data
	//

        Compressor *compressor = _lineBuffer->compressor;

        if (compressor)
        {
            const char *compPtr;

            int compSize = compressor->compress (_lineBuffer->dataPtr,
                                                 _lineBuffer->dataSize,
                                                 _lineBuffer->minY, compPtr);
    
            if (compSize < _lineBuffer->dataSize)
            {
                _lineBuffer->dataSize = compSize;
                _lineBuffer->dataPtr = compPtr;
            }
            else if (_ofd->format == Compressor::NATIVE)
            {
                //
                // The data did not shrink during compression, but
                // we cannot write to the file using the machine's
                // native format, so we need to convert the lineBuffer
                // to Xdr.
                //
    
                convertToXdr (_ofd, _lineBuffer->buffer, _lineBuffer->minY,
                              _lineBuffer->maxY, _lineBuffer->dataSize);
            }
        }

        _lineBuffer->partiallyFull = false;
    }
    catch (std::exception &e)
    {
        if (!_lineBuffer->hasException)
        {
            _lineBuffer->exception = e.what ();
            _lineBuffer->hasException = true;
        }
    }
    catch (...)
    {
        if (!_lineBuffer->hasException)
        {
            _lineBuffer->exception = "unrecognized exception";
            _lineBuffer->hasException = true;
        }
    }
}

} // namespace


OutputFile::OutputFile
    (const char fileName[],
     const Header &header,
     int numThreads)
:
    _data (new Data (true, numThreads))
{
    try
    {
	header.sanityCheck();
	_data->os = new StdOFStream (fileName);
	initialize (header);
    }
    catch (Iex::BaseExc &e)
    {
	delete _data;

	REPLACE_EXC (e, "Cannot open image file "
			"\"" << fileName << "\". " << e);
	throw;
    }
    catch (...)
    {
	delete _data;
        throw;
    }
}


OutputFile::OutputFile
    (OStream &os,
     const Header &header,
     int numThreads)
:
    _data (new Data (false, numThreads))
{
    try
    {
	header.sanityCheck();
	_data->os = &os;
	initialize (header);
    }
    catch (Iex::BaseExc &e)
    {
	delete _data;

	REPLACE_EXC (e, "Cannot open image file "
			"\"" << os.fileName() << "\". " << e);
	throw;
    }
    catch (...)
    {
	delete _data;
        throw;
    }
}


void
OutputFile::initialize (const Header &header)
{
    _data->header = header;

    const Box2i &dataWindow = header.dataWindow();

    _data->currentScanLine = (header.lineOrder() == INCREASING_Y)?
				 dataWindow.min.y: dataWindow.max.y;

    _data->missingScanLines = dataWindow.max.y - dataWindow.min.y + 1;
    _data->lineOrder = header.lineOrder();
    _data->minX = dataWindow.min.x;
    _data->maxX = dataWindow.max.x;
    _data->minY = dataWindow.min.y;
    _data->maxY = dataWindow.max.y;

    size_t maxBytesPerLine = bytesPerLineTable (_data->header,
						_data->bytesPerLine);

    for (size_t i = 0; i < _data->lineBuffers.size(); ++i)
    {
        _data->lineBuffers[i] =
	    new LineBuffer (newCompressor (_data->header.compression(),
					   maxBytesPerLine,
					   _data->header));
    }

    LineBuffer *lineBuffer = _data->lineBuffers[0];
    _data->format = defaultFormat (lineBuffer->compressor);
    _data->linesInBuffer = numLinesInBuffer (lineBuffer->compressor);
    _data->lineBufferSize = maxBytesPerLine * _data->linesInBuffer;

    for (size_t i = 0; i < _data->lineBuffers.size(); i++)
        _data->lineBuffers[i]->buffer.resizeErase(_data->lineBufferSize);

    int lineOffsetSize = (dataWindow.max.y - dataWindow.min.y +
			  _data->linesInBuffer) / _data->linesInBuffer;

    _data->lineOffsets.resize (lineOffsetSize);

    offsetInLineBufferTable (_data->bytesPerLine,
			     _data->linesInBuffer,
			     _data->offsetInLineBuffer);

    _data->previewPosition =
	_data->header.writeTo (*_data->os);

    _data->lineOffsetsPosition =
	writeLineOffsets (*_data->os, _data->lineOffsets);

    _data->currentPosition = _data->os->tellp();
}


OutputFile::~OutputFile ()
{
    if (_data)
    {
        {
            if (_data->lineOffsetsPosition > 0)
            {
                try
                {
                    _data->os->seekp (_data->lineOffsetsPosition);
                    writeLineOffsets (*_data->os, _data->lineOffsets);
                }
                catch (...)
                {
                    //
                    // We cannot safely throw any exceptions from here.
                    // This destructor may have been called because the
                    // stack is currently being unwound for another
                    // exception.
                    //
                }
            }
        }

	delete _data;
    }
}


const char *
OutputFile::fileName () const
{
    return _data->os->fileName();
}


const Header &
OutputFile::header () const
{
    return _data->header;
}


void	
OutputFile::setFrameBuffer (const FrameBuffer &frameBuffer)
{
    Lock lock (*_data);
    
    //
    // Check if the new frame buffer descriptor
    // is compatible with the image file header.
    //

    const ChannelList &channels = _data->header.channels();

    for (ChannelList::ConstIterator i = channels.begin();
	 i != channels.end();
	 ++i)
    {
	FrameBuffer::ConstIterator j = frameBuffer.find (i.name());

	if (j == frameBuffer.end())
	    continue;

	if (i.channel().type != j.slice().type)
	{
	    THROW (Iex::ArgExc, "Pixel type of \"" << i.name() << "\" channel "
			        "of output file \"" << fileName() << "\" is "
			        "not compatible with the frame buffer's "
			        "pixel type.");
	}

	if (i.channel().xSampling != j.slice().xSampling ||
	    i.channel().ySampling != j.slice().ySampling)
	{
	    THROW (Iex::ArgExc, "X and/or y subsampling factors "
				"of \"" << i.name() << "\" channel "
				"of output file \"" << fileName() << "\" are "
				"not compatible with the frame buffer's "
				"subsampling factors.");
	}
    }
    
    //
    // Initialize slice table for writePixels().
    //

    vector<OutSliceInfo> slices;

    for (ChannelList::ConstIterator i = channels.begin();
	 i != channels.end();
	 ++i)
    {
	FrameBuffer::ConstIterator j = frameBuffer.find (i.name());

	if (j == frameBuffer.end())
	{
	    //
	    // Channel i is not present in the frame buffer.
	    // In the file, channel i will contain only zeroes.
	    //

	    slices.push_back (OutSliceInfo (i.channel().type,
					    0, // base
					    0, // xStride,
					    0, // yStride,
					    i.channel().xSampling,
					    i.channel().ySampling,
					    true)); // zero
	}
	else
	{
	    //
	    // Channel i is present in the frame buffer.
	    //

	    slices.push_back (OutSliceInfo (j.slice().type,
					    j.slice().base,
					    j.slice().xStride,
					    j.slice().yStride,
					    j.slice().xSampling,
					    j.slice().ySampling,
					    false)); // zero
	}
    }

    //
    // Store the new frame buffer.
    //

    _data->frameBuffer = frameBuffer;
    _data->slices = slices;
}


const FrameBuffer &
OutputFile::frameBuffer () const
{
    Lock lock (*_data);
    return _data->frameBuffer;
}


void	
OutputFile::writePixels (int numScanLines)
{
    try
    {
        Lock lock (*_data);

	if (_data->slices.size() == 0)
	    throw Iex::ArgExc ("No frame buffer specified "
			       "as pixel data source.");

        //
        // Maintain two iterators:
        //     nextWriteBuffer: next linebuffer to be written to the file
        //     nextCompressBuffer: next linebuffer to compress
        //

        int first = (_data->currentScanLine - _data->minY) /
                         _data->linesInBuffer;

        int nextWriteBuffer = first;
        int nextCompressBuffer;
        int stop;
        int step;
        int scanLineMin;
        int scanLineMax;

        {
            //
            // Create a task group for all line buffer tasks. When the
            // taskgroup goes out of scope, the destructor waits until
	    // all tasks are complete.
            //
            
            TaskGroup taskGroup;
            
            //
            // Determine the range of lineBuffers that intersect the scan
	    // line range.  Then add the initial compression tasks to the
	    // thread pool.  We always add in at least one task but the
	    // individual task might not do anything if numScanLines == 0.
            //
    
            if (_data->lineOrder == INCREASING_Y)
            {
                int last = (_data->currentScanLine + (numScanLines - 1) -
                            _data->minY) / _data->linesInBuffer;
    
                scanLineMin = _data->currentScanLine;
                scanLineMax = _data->currentScanLine + numScanLines - 1;
    
                int numTasks = max (min ((int)_data->lineBuffers.size(),
                                         last - first + 1),
				    1);

                for (int i = 0; i < numTasks; i++)
		{
                    ThreadPool::addGlobalTask
                        (new LineBufferTask (&taskGroup, _data, first + i,
                                             scanLineMin, scanLineMax));
		}
    
                nextCompressBuffer = first + numTasks;
                stop = last + 1;
                step = 1;
            }
            else
            {
                int last = (_data->currentScanLine - (numScanLines - 1) -
                            _data->minY) / _data->linesInBuffer;
    
                scanLineMax = _data->currentScanLine;
                scanLineMin = _data->currentScanLine - numScanLines + 1;
    
                int numTasks = max (min ((int)_data->lineBuffers.size(),
                                         first - last + 1),
				    1);

                for (int i = 0; i < numTasks; i++)
		{
                    ThreadPool::addGlobalTask
                        (new LineBufferTask (&taskGroup, _data, first - i,
                                             scanLineMin, scanLineMax));
		}
    
                nextCompressBuffer = first - numTasks;
                stop = last - 1;
                step = -1;
            }
            
            while (true)
            {
                if (_data->missingScanLines <= 0)
                {
                    throw Iex::ArgExc ("Tried to write more scan lines "
                                       "than specified by the data window.");
                }
    
		//
                // Wait until the next line buffer is ready to be written
		//

                LineBuffer *writeBuffer =
		    _data->getLineBuffer (nextWriteBuffer);

                writeBuffer->wait();
                
                int numLines = writeBuffer->scanLineMax - 
                               writeBuffer->scanLineMin + 1;

                _data->missingScanLines -= numLines;
    
		//
                // If the line buffer is only partially full, then it is
		// not complete and we cannot write it to disk yet.
		//

                if (writeBuffer->partiallyFull)
                {
                    _data->currentScanLine = _data->currentScanLine +
                                             step * numLines;
                    writeBuffer->post();
    
                    return;
                }
    
		//
                // Write the line buffer
		//

                writePixelData (_data, writeBuffer);
                nextWriteBuffer += step;

                _data->currentScanLine = _data->currentScanLine +
                                         step * numLines;
    
                #ifdef DEBUG
    
                    assert (_data->currentScanLine ==
                            ((_data->lineOrder == INCREASING_Y) ?
                             writeBuffer->scanLineMax + 1:
                             writeBuffer->scanLineMin - 1));
    
                #endif
                
		//
                // Release the lock on the line buffer
		//

                writeBuffer->post();
                
		//
                // If this was the last line buffer in the scanline range
		//

                if (nextWriteBuffer == stop)
                    break;
    
		//
                // If there are no more line buffers to compress,
                // then only continue to write out remaining lineBuffers
		//

                if (nextCompressBuffer == stop)
                    continue;
    
		//
                // Add nextCompressBuffer as a compression task
		//

                ThreadPool::addGlobalTask
                    (new LineBufferTask (&taskGroup, _data, nextCompressBuffer,
                                         scanLineMin, scanLineMax));
                
		//
                // Update the next line buffer we need to compress
		//

                nextCompressBuffer += step;
            }
        
	    //
            // Finish all tasks
	    //
        }
        
	//
	// Exeption handling:
	//
	// LineBufferTask::execute() may have encountered exceptions, but
	// those exceptions occurred in another thread, not in the thread
	// that is executing this call to OutputFile::writePixels().
	// LineBufferTask::execute() has caught all exceptions and stored
	// the exceptions' what() strings in the line buffers.
	// Now we check if any line buffer contains a stored exception; if
	// this is the case then we re-throw the exception in this thread.
	// (It is possible that multiple line buffers contain stored
	// exceptions.  We re-throw the first exception we find and
	// ignore all others.)
	//

	const string *exception = 0;

        for (int i = 0; i < _data->lineBuffers.size(); ++i)
	{
            LineBuffer *lineBuffer = _data->lineBuffers[i];

	    if (lineBuffer->hasException && !exception)
		exception = &lineBuffer->exception;

	    lineBuffer->hasException = false;
	}

	if (exception)
	    throw Iex::IoExc (*exception);
    }
    catch (Iex::BaseExc &e)
    {
	REPLACE_EXC (e, "Failed to write pixel data to image "
		        "file \"" << fileName() << "\". " << e);
	throw;
    }
}


int	
OutputFile::currentScanLine () const
{
    Lock lock (*_data);
    return _data->currentScanLine;
}


void	
OutputFile::copyPixels (InputFile &in)
{
    Lock lock (*_data);

    //
    // Check if this file's and and the InputFile's
    // headers are compatible.
    //

    const Header &hdr = _data->header;
    const Header &inHdr = in.header();

    if (inHdr.find("tiles") != inHdr.end())
	THROW (Iex::ArgExc, "Cannot copy pixels from image "
			    "file \"" << in.fileName() << "\" to image "
			    "file \"" << fileName() << "\". "
                            "The input file is tiled, but the output file is "
                            "not. Try using TiledOutputFile::copyPixels "
                            "instead.");

    if (!(hdr.dataWindow() == inHdr.dataWindow()))
	THROW (Iex::ArgExc, "Cannot copy pixels from image "
			    "file \"" << in.fileName() << "\" to image "
			    "file \"" << fileName() << "\". "
                            "The files have different data windows.");

    if (!(hdr.lineOrder() == inHdr.lineOrder()))
	THROW (Iex::ArgExc, "Quick pixel copy from image "
			    "file \"" << in.fileName() << "\" to image "
			    "file \"" << fileName() << "\" failed. "
			    "The files have different line orders.");

    if (!(hdr.compression() == inHdr.compression()))
	THROW (Iex::ArgExc, "Quick pixel copy from image "
			    "file \"" << in.fileName() << "\" to image "
			    "file \"" << fileName() << "\" failed. "
			    "The files use different compression methods.");

    if (!(hdr.channels() == inHdr.channels()))
	THROW (Iex::ArgExc, "Quick pixel copy from image "
			    "file \"" << in.fileName() << "\" to image "
			    "file \"" << fileName() << "\" failed.  "
			    "The files have different channel lists.");

    //
    // Verify that no pixel data have been written to this file yet.
    //

    const Box2i &dataWindow = hdr.dataWindow();

    if (_data->missingScanLines != dataWindow.max.y - dataWindow.min.y + 1)
	THROW (Iex::LogicExc, "Quick pixel copy from image "
			      "file \"" << in.fileName() << "\" to image "
			      "file \"" << fileName() << "\" failed. "
			      "\"" << fileName() << "\" already contains "
			      "pixel data.");

    //
    // Copy the pixel data.
    //

    while (_data->missingScanLines > 0)
    {
	const char *pixelData;
	int pixelDataSize;

	in.rawPixelData (_data->currentScanLine, pixelData, pixelDataSize);

	writePixelData (_data, lineBufferMinY (_data->currentScanLine,
				               _data->minY,
				               _data->linesInBuffer),
                        pixelData, pixelDataSize);

	_data->currentScanLine += (_data->lineOrder == INCREASING_Y)?
				   _data->linesInBuffer: -_data->linesInBuffer;

	_data->missingScanLines -= _data->linesInBuffer;
    }
}


void
OutputFile::updatePreviewImage (const PreviewRgba newPixels[])
{
    Lock lock (*_data);

    if (_data->previewPosition <= 0)
	THROW (Iex::LogicExc, "Cannot update preview image pixels. "
			      "File \"" << fileName() << "\" does not "
			      "contain a preview image.");

    //
    // Store the new pixels in the header's preview image attribute.
    //

    PreviewImageAttribute &pia =
	_data->header.typedAttribute <PreviewImageAttribute> ("preview");

    PreviewImage &pi = pia.value();
    PreviewRgba *pixels = pi.pixels();
    int numPixels = pi.width() * pi.height();

    for (int i = 0; i < numPixels; ++i)
	pixels[i] = newPixels[i];

    //
    // Save the current file position, jump to the position in
    // the file where the preview image starts, store the new
    // preview image, and jump back to the saved file position.
    //

    Int64 savedPosition = _data->os->tellp();

    try
    {
	_data->os->seekp (_data->previewPosition);
	pia.writeValueTo (*_data->os, _data->version);
	_data->os->seekp (savedPosition);
    }
    catch (Iex::BaseExc &e)
    {
	REPLACE_EXC (e, "Cannot update preview image pixels for "
			"file \"" << fileName() << "\". " << e);
	throw;
    }
}


void	
OutputFile::breakScanLine  (int y, int offset, int length, char c)
{
    Lock lock (*_data);

    Int64 position = 
	_data->lineOffsets[(y - _data->minY) / _data->linesInBuffer];

    if (!position)
	THROW (Iex::ArgExc, "Cannot overwrite scan line " << y << ". "
			    "The scan line has not yet been stored in "
			    "file \"" << fileName() << "\".");

    _data->currentPosition = 0;
    _data->os->seekp (position + offset);

    for (int i = 0; i < length; ++i)
	_data->os->write (&c, 1);
}


} // namespace Imf
