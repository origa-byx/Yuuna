//
// Created by Administrator on 2021-11-29.
//

#ifndef YUUNA_MP3ENCODE_H
#define YUUNA_MP3ENCODE_H

#include <jni.h>
#include <string>
#include <android/log.h>
#include "../libmp3lame/lame.h"

#define TAG "JNI-Mp3encode" // 这个是自定义的LOG的标识
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOG_W(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)

std::string jString2str(JNIEnv& env, jstring j_str);
class Mp3encode{
    private:
        lame_global_flags* m_lame;
        int32_t index = 0;
    public:
        void setImage(const char *image, size_t size);

        void setIdTag(const std::string& title, const std::string& artist, const std::string& album, const std::string& year);

        void init(int32_t sample_rate_in, int32_t sample_rate_out,
                  int32_t channel_out, int32_t bit_rate_out, int32_t quality);

        int32_t encode_mp3(const int16_t pcm_l[], const int16_t pcm_r[],
                        const int32_t nsamples, uint8_t *mp3buf, const int32_t mp3buf_size);

        int32_t end_write(uint8_t *mp3buffer, int32_t mp3buffer_size);
        void release();

        ~Mp3encode(){ release(); }
};


#endif //YUUNA_MP3ENCODE_H
