//
// Created by Administrator on 2021-11-29.
//

#include "include/mp3encode.h"

jfieldID objAtJava_ptr;

extern "C" {
    JNIEXPORT jint JNICALL
    JNI_OnLoad(JavaVM *vm, void *reserved) {
        LOG_E("fun JNI_OnLoad is loading...");
        if (vm == nullptr) {
            return JNI_ERR;
        }
        JNIEnv *env;
        int32_t jni_version = JNI_ERR;
        if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {
            jni_version = JNI_VERSION_1_6;
        } else if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) == JNI_OK) {
            jni_version = JNI_VERSION_1_4;
        } else if (vm->GetEnv((void **) &env, JNI_VERSION_1_2) == JNI_OK) {
            jni_version = JNI_VERSION_1_2;
        } else if (vm->GetEnv((void **) &env, JNI_VERSION_1_1) == JNI_OK) {
            jni_version = JNI_VERSION_1_1;
        }
        LOG_D("当前JNI版本：%d", jni_version);
        jclass jClazz = env->FindClass("com/ori/origami/jni/Mp3encode");
        objAtJava_ptr = env->GetFieldID(jClazz, "obj_ptr", "J");
        return jni_version;
    }

    JNIEXPORT void JNICALL
    Java_com_ori_origami_jni_Mp3encode_init(JNIEnv *env, jobject thiz, jint pcm_sample_rate,
                                            jint mp3_sample_rate, jint mp3_channel, jint mp3_bit_rate,
                                            jint quality) {
        auto* mp3Encode = new Mp3encode();
        mp3Encode->init(pcm_sample_rate, mp3_sample_rate, mp3_channel, mp3_bit_rate, quality);
        env->SetLongField(thiz, objAtJava_ptr, reinterpret_cast<jlong>(mp3Encode));
    }

    JNIEXPORT jint JNICALL
    Java_com_ori_origami_jni_Mp3encode_encode(JNIEnv *env, jobject thiz, jshortArray buffer_l,
                                              jshortArray buffer_r, jint size, jbyteArray mp3buf) {
        auto * mp3Encode = reinterpret_cast<Mp3encode*>(env->GetLongField(thiz, objAtJava_ptr));
        int16_t * pcm_l = env->GetShortArrayElements(buffer_l, nullptr);
        int16_t * pcm_r = env->GetShortArrayElements(buffer_r, nullptr);
        int8_t * mp3buff = env->GetByteArrayElements(mp3buf, nullptr);
        int32_t ret = mp3Encode->encode_mp3(pcm_l, pcm_r, size, reinterpret_cast<uint8_t *>(mp3buff), env->GetArrayLength(mp3buf));
        env->ReleaseShortArrayElements(buffer_r, pcm_r, 0);
        env->ReleaseShortArrayElements(buffer_l, pcm_l, 0);
        env->ReleaseByteArrayElements(mp3buf, mp3buff, 0);
        return ret;
    }

    JNIEXPORT jint JNICALL
    Java_com_ori_origami_jni_Mp3encode_end(JNIEnv *env, jobject thiz, jbyteArray mp3buffer,
                                           jint mp3buffer_size) {
        auto * mp3Encode = reinterpret_cast<Mp3encode*>(env->GetLongField(thiz, objAtJava_ptr));
        int8_t * mp3buff = env->GetByteArrayElements(mp3buffer, nullptr);
        int32_t ret = mp3Encode->end_write(reinterpret_cast<uint8_t *>(mp3buff), mp3buffer_size);
        env->ReleaseByteArrayElements(mp3buffer, mp3buff, 0);
        return ret;
    }

    JNIEXPORT void JNICALL
    Java_com_ori_origami_jni_Mp3encode_release(JNIEnv *env, jobject thiz) {
        delete reinterpret_cast<Mp3encode*>(env->GetLongField(thiz, objAtJava_ptr));
        env->SetLongField(thiz, objAtJava_ptr, 0ll);
    }

    JNIEXPORT void JNICALL
    Java_com_ori_origami_jni_Mp3encode_setId3V1Tag(JNIEnv *env, jobject thiz, jstring title,
                                                   jstring artist, jstring album, jstring year) {
        auto * mp3Encode = reinterpret_cast<Mp3encode*>(env->GetLongField(thiz, objAtJava_ptr));
        mp3Encode->setIdTag(jString2str(*env, title), jString2str(*env, artist), jString2str(*env, album), jString2str(*env, year));
    }

    JNIEXPORT void JNICALL
    Java_com_ori_origami_jni_Mp3encode_setImage(JNIEnv *env, jobject thiz, jbyteArray image_src, jint size) {
        auto* mp3Encode = reinterpret_cast<Mp3encode*>(env->GetLongField(thiz, objAtJava_ptr));
        jbyte* jbytes = env->GetByteArrayElements(image_src, nullptr);
        LOG_E("setImage");
        mp3Encode->setImage(reinterpret_cast<const char *>(jbytes), size);
        env->ReleaseByteArrayElements(image_src, jbytes, 0);
    }

}

void Mp3encode::setIdTag(const std::string& title, const std::string& artist, const std::string& album, const std::string& year) {
    if(!m_lame){ return; }
    id3tag_init(m_lame);
    id3tag_set_year(m_lame, year.c_str());
    id3tag_set_artist(m_lame, artist.c_str());
    id3tag_set_title(m_lame, artist.c_str());
    id3tag_set_album(m_lame, album.c_str());
}

void Mp3encode::setImage(const char *image, size_t size) {
    if(!m_lame){ return; }
    id3tag_set_albumart(m_lame, image, size);
    lame_init_params(m_lame);
}

void Mp3encode::init(int32_t sample_rate_in, int32_t sample_rate_out, int32_t channel_out, int32_t bit_rate_out,
                     int32_t quality) {
    if(m_lame){ return; }
    m_lame = lame_init();
    lame_set_in_samplerate(m_lame, sample_rate_in);
    lame_set_out_samplerate(m_lame, sample_rate_out);
    lame_set_num_channels(m_lame, channel_out);
    lame_set_brate(m_lame, bit_rate_out);
    lame_set_quality(m_lame, quality);
    lame_init_params(m_lame);
}

int32_t Mp3encode::encode_mp3(const int16_t *pcm_l, const int16_t *pcm_r,
                          const int32_t nsamples, uint8_t *mp3buf, const int32_t mp3buf_size) {
    LOG_D("编码......");
    return lame_encode_buffer(m_lame, pcm_l, pcm_r, nsamples, mp3buf, mp3buf_size);
}

int32_t Mp3encode::end_write(uint8_t *mp3buffer, int32_t mp3buffer_size) {
    LOG_W("写入缓冲区文件尾部字节");
    int ret = lame_encode_flush(m_lame, mp3buffer, mp3buffer_size);
    return ret;
}

void Mp3encode::release() {
    lame_close(m_lame);
}

std::string jString2str(JNIEnv& env, jstring j_str){
    char* rtn = nullptr;
    jclass cls_string = env.FindClass("java/lang/String");
    jstring str_encode = env.NewStringUTF("GB2312");
    jmethodID mid = env.GetMethodID(cls_string,   "getBytes",   "(Ljava/lang/String;)[B");
    auto barr = (jbyteArray) env.CallObjectMethod(j_str, mid, str_encode);
    jsize alen = env.GetArrayLength(barr);
    jbyte* ba = env.GetByteArrayElements(barr,JNI_FALSE);
    if(alen > 0){
        rtn = (char*) malloc(alen+1);
        memcpy(rtn,ba,alen);
        rtn[alen]=0;
    }
    env.ReleaseByteArrayElements(barr,ba,0);
    std::string out_string = std::string(rtn);
    free(rtn);
    return out_string;
}