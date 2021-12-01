package com.safone.yuuna;


import com.origami.App;
import com.qiniu.droid.rtc.QNLogLevel;
import com.qiniu.droid.rtc.QNRTCEnv;
import com.safone.yuuna.qn.Utils;

/**
 * @by: origami
 * @date: {2021-09-13}
 * @info:
 **/
public class Yuuna extends App {

    @Override
    public void onCreate() {
        super.onCreate();

        //QN
//        QNRTCEnv.setLogLevel(QNLogLevel.INFO);
        /**
         * init must be called before any other func
         */
//        QNRTCEnv.init(getApplicationContext());
//        QNRTCEnv.setLogFileEnabled(true, "ori的会议");
        // 设置自定义 DNS manager，不设置则使用 SDK 默认 DNS 服务
//        new Thread(() -> QNRTCEnv.setDnsManager(Utils.getDefaultDnsManager(getApplicationContext()))).start();
    }
}
