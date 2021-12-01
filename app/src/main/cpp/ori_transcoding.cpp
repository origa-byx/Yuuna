//
// Created by Administrator on 2021-11-23.
//

#include "include/ori_transcoding.h"

jfieldID objAtJava_ptr;

extern "C" {
    //jni 的第一个加载函数
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
        jclass jClazz = env->FindClass("com/ori/origami/jni/NativeOriTranscoding");
        objAtJava_ptr = env->GetFieldID(jClazz, "cplusplus_obj_ptr", "J");
        return jni_version;
    }

    JNIEXPORT void JNICALL
    Java_com_ori_origami_jni_NativeOriTranscoding_pcm2mp3(JNIEnv *env, jobject
        thiz, jstring pcm_path, jstring out_path) {
        auto* m_transcoding = new Transcoding(jString2str(*env, pcm_path), jString2str(*env, out_path));
        env->SetLongField(thiz, objAtJava_ptr, reinterpret_cast<jlong>(m_transcoding));
    }

    JNIEXPORT void JNICALL
    Java_com_ori_origami_jni_NativeOriTranscoding_initTranscoding(JNIEnv *env, jobject thiz) {
        auto* m_transcoding = getTranscodingByJavaPtr(*env, thiz);
        m_transcoding->init();
    }

    JNIEXPORT void JNICALL
    Java_com_ori_origami_jni_NativeOriTranscoding_release(JNIEnv *env, jobject thiz) {
        auto* m_transcoding = getTranscodingByJavaPtr(*env, thiz);
        m_transcoding->release();
    }

    JNIEXPORT void JNICALL
    Java_com_ori_origami_jni_NativeOriTranscoding_transcoding(JNIEnv *env, jobject thiz, jint type) {
        auto* m_transcoding = getTranscodingByJavaPtr(*env, thiz);
        m_transcoding->transcoding(AVCodecID::AV_CODEC_ID_MP3);
    }

}

/**
 * 构建编码器
 */
void Transcoding::init() {
    av_register_all();
    avcodec_register_all();
    LOG_E("ffmpeg -v: %s", av_version_info());

//    AVCodecID avCodecId = AV_CODEC_ID_MP3;
    infile = fopen(srcPath.data(), "rb");
    m_AudioCodec = avcodec_find_encoder(AVCodecID::AV_CODEC_ID_PCM_ALAW);
    if(!m_AudioCodec){
        LOG_E("编码器查找失败");
        return;
    }
    m_AudioCodecContext = avcodec_alloc_context3(m_AudioCodec);
    if(!m_AudioCodecContext){
        LOG_E("编码器上下文创建失败");
        return;
    }
    m_AudioCodecContext->codec_type = AVMEDIA_TYPE_AUDIO;
    //平均比特率 每秒
    m_AudioCodecContext->bit_rate = 16000;
    //采样率 每秒
    m_AudioCodecContext->sample_rate = 44100;
    //音频样本格式 - 编码
    m_AudioCodecContext->sample_fmt = AV_SAMPLE_FMT_S16P;
    //音频通道数
    m_AudioCodecContext->channels = 2;
    //音频通道布局
    m_AudioCodecContext->channel_layout = av_get_default_channel_layout(2);
    //AV_CODEC_FLAG_GLOBAL_HEADER: 将全局标题放置在 extradata 中，而不是每个关键帧中。
    m_AudioCodecContext->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    if(int ret = avcodec_open2(m_AudioCodecContext, m_AudioCodec, nullptr) < 0){
        LOG_E("打开编码器失败-> %d , %s", ret, av_err2str(ret));
        avcodec_free_context(&m_AudioCodecContext);
        return;
    }
    if(!makeFormat(outPath)){ release(); return; }
    if(avformat_write_header(m_FormatContext, nullptr) < 0){
        LOG_E("写入封装文件头部数据失败");
        avcodec_free_context(&m_AudioCodecContext);
        return;
    }
    frame = av_frame_alloc();
    if(!frame){
        LOG_E("创建音频帧失败");
        release();
        return;
    }
    int32_t nb_samples;
    //AV_CODEC_CAP_VARIABLE_FRAME_SIZE: 音频编码器支持在每次调用中接收不同数量的样本
    if(m_AudioCodecContext->codec->capabilities & AV_CODEC_CAP_VARIABLE_FRAME_SIZE){
        nb_samples = 10000;
    }else nb_samples = m_AudioCodecContext->frame_size;//音频帧中每个通道的样本数 由avcodec_open2构建填充
    frame->nb_samples = nb_samples;
    frame->format = m_AudioCodecContext->sample_fmt;
    frame->channel_layout = m_AudioCodecContext->channel_layout;
    if(av_frame_get_buffer(frame, 0) < 0){
        LOG_E("分配音频帧内存失败");
        av_frame_free(&frame);
        release();
        return;
    }
    if(av_frame_make_writable(frame) < 0){
        LOG_E("设置frame可写失败");
        av_frame_free(&frame);
        release();
        return;
    }
    int size = av_samples_get_buffer_size(nullptr,
                                          m_AudioCodecContext->channels,
                                          m_AudioCodecContext->frame_size,
                                          m_AudioCodecContext->sample_fmt, 1);

    if(size < 0){
        LOG_E("获取单帧缓冲区大小失败");
        release();
        return;
    }
//    avcodec_fill_audio_frame()
    auto* frame_buf = (uint8_t*) av_malloc(size);
    //音频重采样 上下文初始化
    SwrContext *asc = nullptr;
    asc = swr_alloc_set_opts(asc, m_AudioCodecContext->channel_layout,
                             AV_SAMPLE_FMT_FLTP,
                             m_AudioCodecContext->sample_rate,//输出格式
                             m_AudioCodecContext->channel_layout,
                             AV_SAMPLE_FMT_S16,
                             m_AudioCodecContext->sample_rate, 0,
                                         nullptr);//输入格式
    if (!asc) {
        LOG_E("音频重采样构建失败");
        return;
    }
    if (swr_init(asc) < 0) {
        LOG_E("音频重采样初始化失败");
        return;
    }

    int32_t frameCount = 0;
    while(!feof(infile)){
        AVPacket* pkt = av_packet_alloc();
        av_init_packet(pkt);
        if(fread(frame_buf, 1, size, infile) <= 0){
            LOG_E("读文件失败");
            av_packet_free(&pkt);
            release();
            return;
        }
        swr_convert(asc, frame->data, frame->nb_samples,
                    (const uint8_t **) frame_buf, frame->nb_samples);
        frame->pts = frameCount++;
        if(avcodec_send_frame(m_AudioCodecContext, frame) < 0){
            av_packet_free(&pkt);
            continue;
        }
        do{
            if(avcodec_receive_packet(m_AudioCodecContext, pkt) < 0){
                av_packet_free(&pkt);
                break;
            }
            av_packet_rescale_ts(pkt, m_AudioCodecContext->time_base, m_FormatContext->streams[0]->time_base);
            pkt->stream_index = 0;
            if(av_interleaved_write_frame(m_FormatContext, pkt) < 0){ av_packet_free(&pkt);break; }
        } while (true);
        av_packet_free(&pkt);
    }
    //写文件尾
    av_write_trailer(m_FormatContext);
    if(m_AVStream){ av_free(frame_buf); }
    //关闭IO
    avio_closep(&m_FormatContext->pb);
    av_frame_free(&frame);
    release();
    fclose(infile);
}
/**
 * 构建输出封装格式
 */
bool Transcoding::makeFormat(const std::string& fileName) {
    if(avformat_alloc_output_context2(&m_FormatContext, nullptr, nullptr, fileName.data()) < 0){
        LOG_E("创建封装格式输出上下文失败");
        return false;
    }
    m_OutputFormat = m_FormatContext->oformat;
    m_AVStream = avformat_new_stream(m_FormatContext, m_AudioCodec);
    if(!m_AVStream){
        LOG_E("构建输出音频流失败");
        avformat_free_context(m_FormatContext);
        return false;
    }
    //特定于格式的流 ID   这里是音频通道数 - 1
    m_AVStream->id = (int32_t) m_FormatContext->nb_streams - 1;
    //时间戳的基本时间单位  （秒）  num->分子 den->分母  采用ffmpeg更高精度的有理数计算的结构体
    m_AVStream->time_base = {1, 44100};
    //根据提供的编解码器上下文中的值填充参数结构。 par 中任何已分配的字段都将被释放并替换为编解码器中相应字段的副本。
    if(avcodec_parameters_from_context(m_AVStream->codecpar, m_AudioCodecContext) < 0){
        LOG_E("同步编码器参数到输出音频流参数失败");
        avformat_free_context(m_FormatContext);
        return false;
    }
    //打印有关输入或输出格式的详细信息，例如持续时间、比特率、流、容器、程序、元数据、侧面数据、编解码器和时基。
    av_dump_format(m_FormatContext, 0, fileName.data(), 1);
    //打开文件IO流
    if(!(m_OutputFormat->flags & AVFMT_NOFILE)){
        if(avio_open(&m_FormatContext->pb, fileName.data(), AVIO_FLAG_READ_WRITE) < 0){
            LOG_E("打开IO流失败");
            avformat_free_context(m_FormatContext);
            return false;
        }
        return true;
    }
    LOG_E("AVFMT_NOFILE -> 文件不存在");
    return false;

}

void Transcoding::release() {
    if(frame){
        av_frame_free(&frame);
    }
    if(m_FormatContext){
        avformat_free_context(m_FormatContext);
    }
    if(m_AudioCodecContext){
        avcodec_free_context(&m_AudioCodecContext);
    }
}

void Transcoding::transcoding(AVCodecID avCodecId) {

}


Transcoding* getTranscodingByJavaPtr(JNIEnv& env, jobject thiz){
    auto* m_transcoding = reinterpret_cast<Transcoding*>(env.GetLongField(thiz, objAtJava_ptr));
    return m_transcoding;
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