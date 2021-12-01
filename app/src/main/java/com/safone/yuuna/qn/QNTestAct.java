package com.safone.yuuna.qn;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;


import com.origami.origami.base.act.AnnotationActivity;
import com.origami.origami.base.annotation.BContentView;
import com.origami.origami.base.annotation.BView;
import com.qiniu.droid.rtc.QNCustomMessage;
import com.qiniu.droid.rtc.QNRTCEngine;
import com.qiniu.droid.rtc.QNRTCEngineEventListener;
import com.qiniu.droid.rtc.QNRoomState;
import com.qiniu.droid.rtc.QNSourceType;
import com.qiniu.droid.rtc.QNStatisticsReport;
import com.qiniu.droid.rtc.QNSurfaceView;
import com.qiniu.droid.rtc.QNTrackInfo;
import com.qiniu.droid.rtc.QNVideoFormat;
import com.qiniu.droid.rtc.model.QNAudioDevice;
import com.safone.yuuna.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @by: origami
 * @date: {2021-09-13}
 * @info:       7Wd1px9tlb0oGwLpK73tFltdjWL4I7Yd9S0qbSGW:uk1iiKmohd_aR_sas5VjkV1qXdI=:eyJhcHBJZCI6ImZ3ZzF3ODNuYiIsImV4cGlyZUF0IjoxNjMxNjAwNDY2LCJwZXJtaXNzaW9uIjoidXNlciIsInJvb21OYW1lIjoidGVzdCIsInVzZXJJZCI6IjEyZGMifQ==
 **/
@SuppressLint("NonConstantResourceId")
@BContentView(R.layout.activity_qn_test)
public class QNTestAct extends AnnotationActivity implements QNRTCEngineEventListener {
    QNRTCEngine engine;

    String room0 = "7Wd1px9tlb0oGwLpK73tFltdjWL4I7Yd9S0qbSGW:uk1iiKmohd_aR_sas5VjkV1qXdI=:eyJhcHBJZCI6ImZ3ZzF3ODNuYiIsImV4cGlyZUF0IjoxNjMxNjAwNDY2LCJwZXJtaXNzaW9uIjoidXNlciIsInJvb21OYW1lIjoidGVzdCIsInVzZXJJZCI6IjEyZGMifQ==";
    String room1 = "7Wd1px9tlb0oGwLpK73tFltdjWL4I7Yd9S0qbSGW:fctK2v7PxPj5zD3Tc-txOcIknSQ=:eyJhcHBJZCI6ImZ3ZzF3ODNuYiIsImV4cGlyZUF0IjoxNjMxNjAwNDY2LCJwZXJtaXNzaW9uIjoidXNlciIsInJvb21OYW1lIjoidGVzdCIsInVzZXJJZCI6IjEyZCJ9";

    @BView(R.id.local_surface_view)
    QNSurfaceView localView;

    @BView(R.id.remote_surface_view)
    QNSurfaceView remoteView;

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        engine = QNRTCEngine.createEngine(this);
        engine.setEventListener(this);
        engine.joinRoom(room0);
    }

    @Override
    public void onRoomStateChanged(QNRoomState qnRoomState) {
        if (qnRoomState == QNRoomState.CONNECTED) {//自己加入房间本地用户回调
            Log.e("ORI","CONNECTED;//连接成功");
            QNTrackInfo localVideoTrack = engine.createTrackInfoBuilder()
                    .setVideoEncodeFormat(new QNVideoFormat(640,480,20))
                    .setSourceType(QNSourceType.VIDEO_CAMERA)
                    .setBitrate(600 * 1000)// 设置视频码率
                    .setMaster(true)
                    .create();
            QNTrackInfo localAudioTrack = engine.createTrackInfoBuilder()
                    .setSourceType(QNSourceType.AUDIO)
                    .setBitrate(64 * 1000)// 设置音频码率
                    .setMaster(true)
                    .create();
            engine.setRenderWindow(localVideoTrack, remoteView);
            engine.publishTracks(new ArrayList<QNTrackInfo>(){
                {
                    add(localVideoTrack);
                    add(localAudioTrack);
                }
            });
        }else if(qnRoomState == QNRoomState.IDLE){
            Log.e("ORI","IDLE;//初始化");
        }else if(qnRoomState == QNRoomState.RECONNECTED){
            Log.e("ORI","RECONNECTED;//重连成功");
        }else if(qnRoomState == QNRoomState.CONNECTING){//connecting
            Log.e("ORI","CONNECTING;//正在连接");
        }else if(qnRoomState == QNRoomState.RECONNECTING){
            Log.e("ORI","RECONNECTING;//正在重连");
        }
//        QNRoomState.IDLE;//初始化
//        QNRoomState.CONNECTED;//连接成功
//        QNRoomState.RECONNECTED;//重连成功
//        QNRoomState.CONNECTING;//正在连接
//        QNRoomState.RECONNECTING;//正在重连
    }

    @Override
    public void onRoomLeft() {
        //本地调用 QNRTCEngine.leaveRoom() 退出房间
        Log.e("ORI","退出房间");
    }

    /**
     * @param s     remoteUserId
     * @param s1    userData
     */
    @Override
    public void onRemoteUserJoined(String s, String s1) {//其他用户加入房间
        Log.e("ORI","用户 " + s + " 加入房间");
    }

    @Override
    public void onRemoteUserReconnecting(String s) {

    }

    @Override
    public void onRemoteUserReconnected(String s) {

    }

    /**
     * @param s remoteUserId
     */
    @Override
    public void onRemoteUserLeft(String s) {
//        远端用户调用 QNRTCEngine.leaveRoom() 退出房间
    }

    /**
     * 简单发布与取消
     *
     * public void publish();
     * public void unPublish();
     *
     * public void publishAudio();
     * public void unPublishAudio();
     *
     * public void publishVideo();
     * public void unPublishVideo();
     *  在 v2.x.x 版本后提供了默认自动订阅的功能，
     *      用户可以通过调用 QNRTCEngine#setAutoSubscribe
     *          进行对自动订阅功能的开启与关闭，默认为开启状态。
     *
     * 在自动订阅功能关闭时，用户可以通过调用 QNRTCManager#subscribeTracks 接口订阅远端 Tracks。
     */

    //QNRTCEngine#publicTracks
    //发布 Track 成功后，会触发如下回调：
    @Override
    public void onLocalPublished(List<QNTrackInfo> list) {
        Log.e("ORI","轨道发布成功");
    }

    //QNRTCEngine#publicTracks
    //发布 Track 成功后，会触发如下回调：
    @Override
    public void onRemotePublished(String s, List<QNTrackInfo> list) {
        Log.e("ORI","远端发布轨道成功");
        engine.subscribeTracks(list);
        for (QNTrackInfo qnTrackInfo : list) {
            if(qnTrackInfo.isVideo()){
                engine.setRenderWindow(qnTrackInfo, localView);
                break;
            }
        }
    }

    //调用 QNRTCEngine#unPublishTracks 取消发布本地媒体流。
    //取消发布成功后，会触发如下回调：
    @Override
    public void onRemoteUnpublished(String s, List<QNTrackInfo> list) {

    }

    @Override
    public void onRemoteUserMuted(String s, List<QNTrackInfo> list) {

    }

    //本地订阅远端用户媒体流成功后，会触如下回调：
    @Override
    public void onSubscribed(String s, List<QNTrackInfo> list) {
        Log.e("ORI","订阅成功");
    }

    @Override
    public void onSubscribedProfileChanged(String s, List<QNTrackInfo> list) {

    }

    /**
     * @param s userId
     */
    @Override
    public void onKickedOut(String s) {
//        本地调用 QNRTCEngine.kickOutUser(String userId) 即可将 userId 对应的用户踢出房间。
    }

    @Override
    public void onStatisticsUpdated(QNStatisticsReport qnStatisticsReport) {

    }

    @Override
    public void onRemoteStatisticsUpdated(List<QNStatisticsReport> list) {

    }

    @Override
    public void onAudioRouteChanged(QNAudioDevice qnAudioDevice) {

    }

    @Override
    public void onCreateMergeJobSuccess(String s) {

    }

    @Override
    public void onCreateForwardJobSuccess(String s) {

    }

    @Override
    public void onError(int i, String s) {

    }

    @Override
    public void onMessageReceived(QNCustomMessage qnCustomMessage) {

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        engine.destroy();
    }
}
