//
// Created by Administrator on 2021-11-23.
//

#ifndef ORIGAMI_ORI_TRANSCODING_H
#define ORIGAMI_ORI_TRANSCODING_H

#include <jni.h>
#include <string>
#include <android/log.h>
#include <fstream>
#ifdef __cplusplus
extern "C"
{
#endif
#include <libswscale/swscale.h>
#include <libavformat/avformat.h>
#include <libavutil/imgutils.h>
#include <libswresample/swresample.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h> // ���߿�
#include <libavutil/opt.h>
#ifdef __cplusplus
}
#endif

#define TAG "JNI-ori_transcoding" // 这个是自定义的LOG的标识
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOG_W(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
class Transcoding;
Transcoding* getTranscodingByJavaPtr(JNIEnv& env, jobject thiz);
std::string jString2str(JNIEnv& env, jstring j_str);
class Transcoding{

    private:
        AVCodec* m_AudioCodec = nullptr;
        AVCodecContext* m_AudioCodecContext = nullptr;
        AVFormatContext* m_FormatContext = nullptr;
        AVOutputFormat* m_OutputFormat = nullptr;
        AVStream* m_AVStream = nullptr;
        AVFrame* frame = nullptr;
        FILE* infile = nullptr;
        //源路径
        const std::string srcPath;
        //输出路径
        const std::string outPath;

        bool makeFormat(const std::string& fileName);
    public:
        Transcoding(std::string srcPath, std::string outPath) : srcPath(), outPath(){}
        enum AudioFormatType{
            AudioFormat_MP3
        };
        void init();
        void transcoding(AVCodecID avCodecId);
        void release();

};


#endif //ORIGAMI_ORI_TRANSCODING_H
