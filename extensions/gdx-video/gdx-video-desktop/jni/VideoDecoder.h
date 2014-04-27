#pragma once

extern "C"
{
//This makes certain C libraries usable for ffmpeg
#define __STDC_CONSTANT_MACROS
//Include ffmpeg headers
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
}

#include "Utilities.h"
#include "Thread.hpp"
#include "Mutex.hpp"
#include "CondVar.hpp"

#include <list>

//Should always be bigger then 1! If not, the buffer will never be filled, because the buffer will never be completely full.
//It will always have 1 single empty element, which is used as protection for faster synchronization.
#define VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES 10
#define VIDEOPLAYER_AUDIO_BUFFER_SIZE 1024
#define MAX_AUDIO_FRAME_SIZE 192000
#define CUSTOMIO_BUFFER_SIZE 4096

struct VideoBufferInfo {
    void* videoBuffer;
    void* audioBuffer;
    int videoBufferSize;
    int videoWidth;
    int videoHeight;
    int audioBufferSize;
    int audioChannels;
    int audioSampleRate;
};

/**
 *  The FillFileByfferFunc function will give a pointer to some data you gave to it, a buffer, and an integer representing
 *  the buffer's size. The function needs to return the amount of data that is filled into the buffer.
 */
typedef int (*FillFileBufferFunc)(void*, uint8_t*, int);
typedef void (*CleanupFunc)(void*);

/**
 * @brief The VideoPlayer class is the base class which will handle everything needed to play a videofile.
 */
class VideoDecoder : private Thread{
public:
    /**
     * @brief VideoPlayer Default constructor
     */
    VideoDecoder();
    /**
     * @brief ~VideoPlayer Destructor
     */
    virtual ~VideoDecoder();

    /**
     * @brief loadFile This function will load the given file, and creates a buffer which will be put in the pointer.
     * @param filename The filename of the file to load
     * @param bufferPointer A reference to a void* which will then be filled with the buffer address.
     * @return The size of the buffer
     */
    void loadFile(char* filename, VideoBufferInfo* bufferInfo);
    void loadFile(FillFileBufferFunc func, void* funcData, CleanupFunc cleanupFunc, VideoBufferInfo* bufferInfo);
    /**
     * @brief fillBufferWithNextFrame This function will fill the buffers with the data of the next available frame
     * @return Whether a new frame was available
     */
    u_int8_t* nextVideoFrame();
    /**
     * @brief updateAudioBuffer This function will fill the audio buffers with the next amount of data.
     * @return
     */
    void updateAudioBuffer();

    /**
     * @brief getCurrentFrameTimestamp    This function will return the latest available Presentation TimeStamp. This is the
     *                              timestamp that the current frame should be shown.
     * @return The pts of the currently buffered frame (The one that can be read from the buffer at THIS moment)
     */
    double getCurrentFrameTimestamp();
    /**
     * @brief getVideoFrameSize This method returns the size in bytes of each aquired videoFrame.
     * @return The size in bytes of a single video frame
     */
    int getVideoFrameSize();

    /**
     * @brief isBuffered Returns whether the frame buffer is full
     * @return Whether the framebuffer is full.
     */
    bool isBuffered();
    void *getCustomFileBufferFuncData() const;
    FillFileBufferFunc getFillFileBufferFunc() const;
private:
    int decodeAudio(void* audioBuffer);

    bool readPacket();

    /**
     * @brief run Implements the Threads run method, and will fill videobuffers
     */
    virtual void run();

    /**
     * @brief loadContainer Used by the loadFile functions, which setup a single input method, and then call
     * this to load the shared info.
     */
    void loadContainer(VideoBufferInfo* bufferInfo);
private:
    AVFormatContext* formatContext;
    AVCodecContext* videoCodecContext;
    SwrContext* swrContext;
    AVCodecContext* audioCodecContext;
    AVCodec* videoCodec;
    AVCodec* audioCodec;
    AVFrame* frame;
    AVFrame* audioFrame;
    struct SwsContext* swsContext;
    AVIOContext* avioContext;

    //Owned by ffmpeg, don't clean
    u_int8_t* avioBuffer;
    //Don't do any cleanup for this. It does not own the data that is pointed to!
    void* customFileBufferFuncData;
    FillFileBufferFunc fillFileBufferFunc;
    CleanupFunc cleanupFunc;

    u_int8_t* videoBuffer;
    int videoFrameSize;
    double videoTimestamps[VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES];
    AVFrame* rgbFrames[VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES];
    int videoCurrentBufferIndex;
    int videoNumFrameBuffered;
    Mutex videoBufferMutex;
    CondVar videoBufferConditional;
    bool videoOutputEnded;
    std::list<AVPacket> videoPackets;

    char audioBuffer[VIDEOPLAYER_AUDIO_BUFFER_SIZE];
    char audioDecodingBuffer[(MAX_AUDIO_FRAME_SIZE * 3) / 2];
    int audioDecodedSize;
    int audioDecodedUsed;
    std::list<AVPacket> audioPackets;
    Mutex listMutex;
    int videoStreamIndex;
    int audioStreamIndex;
    double secPerKbBlock;

    bool fileLoaded;
    double timeBase;
    double timestampOffset;
    bool firstVideoPacket;
};
