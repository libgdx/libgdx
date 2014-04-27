#include "VideoDecoder.h"

#include <cstring>
#include <stdexcept>

#include <pthread.h>

static int readFunction(void* opaque, uint8_t* buffer, int bufferSize)
{
    VideoDecoder* decoder = (VideoDecoder*) opaque;

    //Call implemented function
    return decoder->getFillFileBufferFunc()(decoder->getCustomFileBufferFuncData(), buffer, bufferSize);
}

VideoDecoder::VideoDecoder() : videoBufferMutex(true), videoBufferConditional(videoBufferMutex), listMutex(true){
    fileLoaded = false;

    formatContext = NULL;
    videoCodecContext = NULL;
    audioCodecContext = NULL;
    avioContext = NULL;
    avioBuffer = NULL;

    videoCodec = NULL;
    audioCodec = NULL;
    swsContext = NULL;
    videoBuffer = NULL;
    frame = av_frame_alloc();
    for(int i = 0; i < VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES; i++) {
        rgbFrames[i] = av_frame_alloc();
    }
    audioFrame = av_frame_alloc();
    audioDecodedSize = 0;
    audioDecodedUsed = 0;

    videoCurrentBufferIndex = 0;
    videoNumFrameBuffered = 0;
    timestampOffset = -1;
}

VideoDecoder::~VideoDecoder() {



    //Take care of cleanup
    if(swrContext !=  NULL) {
        swr_free(&swrContext);
    }

    if(swsContext != NULL) {
        sws_freeContext(swsContext);
    }

    if(videoBuffer != NULL) {
        delete[] videoBuffer;
    }

    if(audioCodecContext != NULL) {
        avcodec_close(audioCodecContext);
    }
    if(videoCodecContext != NULL) {
        avcodec_close(videoCodecContext);
    }

    av_frame_free(&audioFrame);
    for(int i = 0; i < VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES; i++) {
        av_frame_free(rgbFrames + i);
    }
    av_frame_free(&frame);
    avformat_close_input(&formatContext);

    if(avioContext != NULL) {
        av_free(avioContext);
    }
}

void VideoDecoder::loadFile(char* filename, VideoBufferInfo *bufferInfo) {
    if(fileLoaded) {
        logError("[VideoPlayer::loadFile] Tried to load a new file. Ignoring...\n");
        return;
    }
    if(filename == NULL || strcmp(filename, "")==0) {
        logError("[VideoPlayer::loadFile] Invalid arguments supplied!\n");
        throw std::invalid_argument("Filename should not be empty!");
    }

    if(debugLoggingActive) {
        //Print all available information about the streams inside of the file.
        av_dump_format(formatContext, 0, filename, 0);
    }

    //Try to open the file
    int err = avformat_open_input(&formatContext, filename, NULL, NULL);
    if(err < 0) {
        char error[1024];
        av_strerror(err, error, 1024);
        logError("[VideoPlayer::loadFile] Error opening file (%s): %s\n", filename, error);
        throw std::runtime_error("Could not open file!");
    }
    loadContainer(bufferInfo);
}

void VideoDecoder::loadFile(FillFileBufferFunc func, void* funcData, CleanupFunc cleanupFunc, VideoBufferInfo* bufferInfo) {
    if(fileLoaded) {
        logError("[VideoPlayer::loadFile] Tried to load a new file. Ignoring...\n");
        return;
    }

    if(func == NULL) {
        logError("[VideoPlayer::loadFile] Invalid arguments supplied!\n");
        throw std::invalid_argument("FillFileBufferFunc should be a valid function");
    }

    fillFileBufferFunc = func;
    customFileBufferFuncData = funcData;
    this->cleanupFunc = cleanupFunc;

    avioBuffer = (u_int8_t*)av_malloc(CUSTOMIO_BUFFER_SIZE);
    avioContext = avio_alloc_context(avioBuffer, CUSTOMIO_BUFFER_SIZE, 0, (void*)this, &readFunction, NULL, NULL);

    formatContext = avformat_alloc_context();
    formatContext->pb = avioContext;

    int err = avformat_open_input(&formatContext, "dummyFileName", NULL, NULL);if(err < 0) {
        char error[1024];
        av_strerror(err, error, 1024);
        logError("[VideoPlayer::loadFile] Error opening file: %s\n", error);
        throw std::runtime_error("Could not open file!");
    }
    loadContainer(bufferInfo);
}

void VideoDecoder::loadContainer(VideoBufferInfo* bufferInfo) {
    if (av_find_stream_info(formatContext) < 0) {
        logError("[VideoPlayer::loadFile] Could not find stream info!\n");
        throw std::runtime_error("Could not find stream info!");
    }

    videoStreamIndex = -1;
    audioStreamIndex = -1;

    //Loop through the streams and see if there is a video stream.
    for(int i = 0; i < formatContext->nb_streams; i++) {
        if(formatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO && videoStreamIndex < 0) {
            //Found videostream, save index
            videoStreamIndex = i;
        }
        if(formatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO && audioStreamIndex < 0) {
            //Found audiostream, save index
            audioStreamIndex = i;
        }
        if(videoStreamIndex >= 0 && audioStreamIndex >= 0) {
            break;
        }
    }

    if(videoStreamIndex < 0) {
        logError("[VideoPlayer::loadFile] Could not find video stream!\n");
        throw std::runtime_error("Could not find any video stream");
    } else {
        logDebug("[VideoPlayer::loadFile] video stream found [index=%d]\n", videoStreamIndex);
    }

    if(audioStreamIndex < 0) {
        logError("[VideoPlayer::loadFile] Could not find audio stream!\n");
    } else {
        logDebug("[VideoPlayer::loadFile] audio stream found [index=%d]\n", audioStreamIndex);
    }

    //Initialize video decoder
    videoCodecContext = formatContext->streams[videoStreamIndex]->codec;

    videoCodec = avcodec_find_decoder(videoCodecContext->codec_id);
    if(videoCodec == NULL) {
        logError("[VideoPlayer::loadFile] Could not find a suitable video decoder!\n");
        throw std::runtime_error("Could not find a suitable video decoder!");
    }

    AVRational streamTimeBase = formatContext->streams[videoStreamIndex]->time_base;
    timeBase = ((double)streamTimeBase.num / (double)streamTimeBase.den);

    AVDictionary* codecOptions = NULL;

    if(avcodec_open2(videoCodecContext, videoCodec, &codecOptions) < 0) {
        logError("[VideoPlayer::loadFile] Could not open video decoder!\n");
        throw std::runtime_error("Could not open video decoder!");
    }

    //Initialize audio decoder
    if(audioStreamIndex >= 0) {
        audioCodecContext = formatContext->streams[audioStreamIndex]->codec;

        bufferInfo->audioChannels = audioCodecContext->channels;
        bufferInfo->audioSampleRate = audioCodecContext->sample_rate;

        audioCodec = avcodec_find_decoder((audioCodecContext->codec_id));
        if(audioCodec == NULL) {
            logError("[VideoPlayer::loadFile] Could not find a suitable audio decoder!");
            throw std::runtime_error("Could not find a suitable audio decoder!");
        }

        if(avcodec_open2(audioCodecContext, audioCodec, &codecOptions) < 0) {
            logError("[VideoPlayer::loadFile] Could not open audio decoder!\n");
            throw std::runtime_error("Could not open audio decoder!");
        }
        bufferInfo->audioBuffer = this->audioBuffer;
        bufferInfo->audioBufferSize = VIDEOPLAYER_AUDIO_BUFFER_SIZE;

        int channelLayout = 0;
        switch(bufferInfo->audioChannels) {
        case 1:
            channelLayout = AV_CH_LAYOUT_MONO;
            break;
        case 2:
            channelLayout = AV_CH_LAYOUT_STEREO;
            break;
        default:
            logError("[VideoPlayer::loadFile] InputFile has unsupported number of audiochannels!");
            throw std::runtime_error("InputFile has unsupported number of audiochannels!");
        }

        //Setup conversion context, which will convert our audio to the right output format
        swrContext = swr_alloc_set_opts(NULL, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_S16, bufferInfo->audioSampleRate, channelLayout, AV_SAMPLE_FMT_FLTP, bufferInfo->audioSampleRate, 0, NULL);
        swr_init(swrContext);

        //Calculate how much seconds a single kb block is (1024 bytes): blockSize / bytesPerSample / channels / sampleRate
        secPerKbBlock = 1024.0 / 1 / (double)audioCodecContext->channels / (double)audioCodecContext->sample_rate;
    }

    videoFrameSize = avpicture_get_size(PIX_FMT_RGB24, videoCodecContext->width, videoCodecContext->height);
    logDebug("[VideoPlayer::loadFile] buffer for single frame is of size: %d\n", videoFrameSize);

    videoBuffer = new u_int8_t[videoFrameSize * VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES];
    swsContext = sws_getContext(videoCodecContext->width,
                                videoCodecContext->height,
                                videoCodecContext->pix_fmt,
                                videoCodecContext->width,
                                videoCodecContext->height,
                                PIX_FMT_RGB24,
                                SWS_BILINEAR,
                                NULL,
                                NULL,
                                NULL);

    for(int i = 0; i < VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES; i++) {
        avpicture_fill((AVPicture *)rgbFrames[i], videoBuffer + (i * videoFrameSize), PIX_FMT_RGB24, videoCodecContext->width, videoCodecContext->height);
    }
    bufferInfo->videoBuffer = rgbFrames[0]->data[0];
    bufferInfo->videoBufferSize = videoFrameSize;
    bufferInfo->videoWidth = videoCodecContext->width;
    bufferInfo->videoHeight = videoCodecContext->height;

    fileLoaded = true;
    firstVideoPacket = true;

    //start filling up the buffer (Start seperate thread)
    this->start();
    videoBufferMutex.lock();
    while(videoNumFrameBuffered < (VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES - 1)) {
        videoBufferConditional.wait();
        logDebug("[VideoPlayer::loadFile] Waiting for buffer to fill: %d\n", videoNumFrameBuffered);
    }
    videoBufferMutex.unlock();
}

u_int8_t* VideoDecoder::nextVideoFrame() {
    videoBufferMutex.lock();

    while(videoNumFrameBuffered < 1 && !videoOutputEnded) {
        logDebug("[VideoPlayer::nextVideoFrame] Waiting for frame\n");
        videoBufferConditional.wait();
    }

    if(!videoOutputEnded) {
        int readingIndex = videoCurrentBufferIndex;
        logDebug("[VideoPlayer::nextVideoFrame] last returned buffer points to index %d\n", readingIndex);
        videoCurrentBufferIndex = (readingIndex + 1) % VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES;
        videoNumFrameBuffered--;
        videoBufferConditional.signal();
        videoBufferMutex.unlock();
        return rgbFrames[readingIndex]->data[0];
    }

    videoBufferMutex.unlock();
    return NULL;
}

void VideoDecoder::updateAudioBuffer() {
    int sizeLeft = VIDEOPLAYER_AUDIO_BUFFER_SIZE;

    // Try getting enough data, to fill the buffer.
    while(sizeLeft > 0) {
        //If there is no decoded data left, decode new frame
        if(audioDecodedUsed >= audioDecodedSize) {
            audioDecodedUsed = 0;

            int size = decodeAudio(audioDecodingBuffer);
            if(size < 0) {
                logDebug("[VideoPlayer::updateAudioBuffer] Could not decode more frames!\n");

                //Play silence
                audioDecodedSize = 1024;
                memset(audioDecodingBuffer, 0, audioDecodedSize);

                //Set an offset for the video, so that audio won't be behind
                timestampOffset += secPerKbBlock;
            } else {
                audioDecodedSize = size;
            }
        }

        //Copy data into the buffer
        int lengthToCopy = audioDecodedSize - audioDecodedUsed;
        //Make sure we never copy more than the buffer
        if(lengthToCopy > sizeLeft) {
            lengthToCopy = sizeLeft;
        }

        memcpy(audioBuffer + (VIDEOPLAYER_AUDIO_BUFFER_SIZE - sizeLeft), audioDecodingBuffer + audioDecodedUsed, lengthToCopy);
        sizeLeft -= lengthToCopy;
        audioDecodedUsed += lengthToCopy;
    }
}

int VideoDecoder::decodeAudio(void* audioBuffer) {
    while(true) {
        listMutex.lock();
        if(audioPackets.empty()) {
            //Keep reading packets until no more can be read, or an audio packet is found.
            while(audioPackets.empty() && readPacket());
            if(audioPackets.empty()) {
                listMutex.unlock();
                return -1;
            }
        }

        AVPacket packet = audioPackets.back();
        audioPackets.pop_back();
        listMutex.unlock();
        int audioPacketSize = packet.size;

        //While we did not read the full packet size (can be split over multiple frames)
        while(audioPacketSize > 0) {
            int gotFrame = 0;
            int decodedSize = avcodec_decode_audio4(audioCodecContext, audioFrame, &gotFrame, &packet);
            if(decodedSize < 0) {
                //Something bad happened, skip frame...
                break;
            }

            audioPacketSize -= decodedSize;

            if(gotFrame) {
                int size = av_samples_get_buffer_size(NULL, audioCodecContext->channels, audioFrame->nb_samples, audioCodecContext->sample_fmt, 0);
                int ret = 0;
                if(size > 0) {
                    ret = swr_convert(swrContext, (u_int8_t**)&audioBuffer, audioFrame->nb_samples, (uint8_t const **)audioFrame->extended_data, audioFrame->nb_samples);
                }

                size = av_samples_get_buffer_size(NULL, 2, ret, AV_SAMPLE_FMT_S16, 0);
                //Free the packet's data since we won't ever reach out of the loop
                av_free_packet(&packet);
                //Return copied size
                return size;
            }
        }

        av_free_packet(&packet);
    }
}

double VideoDecoder::getCurrentFrameTimestamp() {
    //Since the nextVideoFrame function already upped the variable, undo the effect (ugly, but effective, may be refactored later).
    int index = videoCurrentBufferIndex - 1;
    if(index < 0) {
        index = VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES - 1;
    }
    logDebug("[VideoPlayer::nextVideoFrame] last returned timestamp is of index %d\n", index);
    return videoTimestamps[index];// + ((timestampOffset) > 0 ? timestampOffset : 0);
}

bool VideoDecoder::readPacket() {
    AVPacket packet;
    if(av_read_frame(formatContext, &packet) >= 0) {
        if(packet.stream_index==videoStreamIndex) {
            //queue video packet for handling later
            videoBufferMutex.lock();
            videoPackets.push_front(packet);
            videoBufferMutex.unlock();
        } else if(packet.stream_index == audioStreamIndex) {
            //queue audio packet for handling later
            listMutex.lock();
            audioPackets.push_front(packet);
            listMutex.unlock();
        } else {
            av_free_packet(&packet);
        }
        return true;
    } else {
        return false;
    }
}

int numFramesDecoded = 0;

void VideoDecoder::run() {
    videoBufferMutex.lock();
    while(!videoOutputEnded) {
        while(videoNumFrameBuffered < (VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES - 1) && !videoOutputEnded) {
            int indexToWrite = (videoCurrentBufferIndex + videoNumFrameBuffered) % VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES;

            int frameFinished = false;
            while(!frameFinished) {
                //Keep reading until a video packet is read, or stream is ended.
                while(videoPackets.empty() && readPacket());

                if(videoPackets.empty()) {
                    //End of stream reached, stop filling buffer
                    videoOutputEnded = true;
                    break;
                }

                AVPacket videoPacket = videoPackets.back();
                videoPackets.pop_back();
                videoBufferMutex.unlock();

                if(firstVideoPacket) {
                    firstVideoPacket = false;
                    videoTimestamps[indexToWrite] = timeBase*(double)videoPacket.pts;
                }

                //Decode video
                avcodec_decode_video2(videoCodecContext, frame, &frameFinished, &videoPacket);
                if(frameFinished) {
                    //The frame is finished, so scale it into an RGB frame
                    sws_scale(swsContext, (uint8_t const * const *)frame->data, frame->linesize, 0, videoCodecContext->height, rgbFrames[indexToWrite]->data, rgbFrames[indexToWrite]->linesize);
                    logDebug("[VideoPlayer::run] Filled buffer on position %d with frame %d\n", indexToWrite, numFramesDecoded++);
                    //We are done with the packet, free it, then return status.
                    av_free_packet(&videoPacket);
                    firstVideoPacket = true;
                    //Atomic increment of videoNumFrameBuffered
                    __sync_add_and_fetch(&videoNumFrameBuffered, 1);
                    if(videoBufferMutex.trylock() == 0) {
                        videoBufferConditional.signal();
                        videoBufferMutex.unlock();
                    }
                }
                videoBufferMutex.lock();
            }
        }
        videoBufferConditional.wait();
    }
    videoBufferMutex.unlock();

    if(cleanupFunc != NULL) {
        cleanupFunc(this->customFileBufferFuncData);
    }
}

int VideoDecoder::getVideoFrameSize()
{
    return videoFrameSize;
}

bool VideoDecoder::isBuffered() {
    return videoNumFrameBuffered == (VIDEOPLAYER_VIDEO_NUM_BUFFERED_FRAMES - 1);
}


FillFileBufferFunc VideoDecoder::getFillFileBufferFunc() const
{
    return fillFileBufferFunc;
}

void *VideoDecoder::getCustomFileBufferFuncData() const
{
    return customFileBufferFuncData;
}
