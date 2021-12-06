package com.ori.origami;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.ori.origami.recoder.OriAudio;
import com.origami.activity.OriImageSelect;
import com.origami.origami.base.act.OriBaseActivity;
import com.origami.origami.base.annotation.BClick;
import com.origami.origami.base.annotation.BContentView;
import com.origami.origami.base.callback.RequestPermissionNext;
import com.origami.origami.base.toast.OriToast;
import com.origami.utils.Ori;
import com.origami.utils.StatusUtils;
import com.safone.yuuna.R;
import com.safone.yuuna.databinding.ActDemoBinding;

import java.util.Timer;
import java.util.TimerTask;


/**
 * @by: origami
 * @date: {2021-11-30}
 * @info:
 **/
@SuppressLint("NonConstantResourceId")
@BContentView(R.layout.act_demo)
public class DemoAct extends OriBaseActivity<ActDemoBinding> {

    private final Object taken = new Object();
    Timer timer;

    int autoSaveTime = 5 * 60 * 1000;

    @Override
    public void init(@Nullable Bundle savedInstanceState) { }

    boolean click = false;
    long currentClickTime = 0;

    int timeIndex = 0;
    String path;
    String fileName = "";

    @BClick(R.id.cliAudio)
    public void click_recorder(){
        if(System.currentTimeMillis() - currentClickTime < 1000){
            OriToast.show("两次操作间隔至少需要1秒", false);
            return;
        }
        currentClickTime = System.currentTimeMillis();
        click = !click;
        if(click){
            checkPermissionAndThen(new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, new RequestPermissionNext() {
                @Override
                public void next() {
                    start();
                }

                @Override
                public void failed() {
                    click = !click;
                    OriToast.show("你拒绝了权限申请", false);
                }

            });
        }else {
            stop();
        }
    }

    private void start(){
        OriAudio.instance().init(true);
        OriAudio.instance().initMp3encode(8000, 2, 16000, 7);
        OriAudio.instance().mp3encode().setId3V1Tag("yuuna", "ori", "origami", "2021");
        mViews.cliAudio.setText("停止");
        mViews.proAudio.setVisibility(View.VISIBLE);
        goRun();
    }

    private void goRun(){
        fileName = Ori.getSaveFilePath(DemoAct.this, Environment.DIRECTORY_MUSIC, false)
                + Ori.getRandomString(6) + ".mp3";
        OriAudio.instance().startRecord(fileName);
        Handler handler = getWindow().getDecorView().getHandler();
        if(handler != null){
            Message obtain = Message.obtain(handler, () -> {
                Log.e("ORI-DE", "dodo");
                OriAudio.instance().stopRecord();
                OriToast.show(String.format("文件保存至：%s", fileName), true, false);
                goRun();
            });
            obtain.obj = taken;
            handler.sendMessageDelayed(obtain, autoSaveTime);
        }
        startTime();
    }

    private void stop(){
        Handler handler = getWindow().getDecorView().getHandler();
        if(handler != null) {
            handler.removeCallbacksAndMessages(taken);
        }
        OriAudio.instance().stopRecord();
        OriAudio.instance().release();
        stopTime();
        mViews.cliAudio.setText("录制");
        mViews.proAudio.setVisibility(View.INVISIBLE);
        OriToast.show(String.format("文件保存至：%s", fileName), true, false);
    }

//    @BClick(R.id.artImage)
//    public void clickArt(){
//        OriImageSelect.builder()
//                .setRequestCode(123)
//                .setRowShowNum(3)
//                .setCanPre(true)
//                .setSelectNum(1)
//                .build(this);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == Activity.RESULT_OK && data != null) {
            String[] stringExtra = data.getStringArrayExtra(OriImageSelect.RESULT_KEY);
            Log.e("ORI-SELECT", stringExtra[0]);
            path = stringExtra[0];
        }
    }

    private void startTime(){
        timeIndex = 0;
        if(timer == null){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        mViews.timeIndex.setText(String.valueOf(++timeIndex));
                    });
                }
            }, 1000, 1000);
        }
    }

    private void stopTime(){
        mViews.timeIndex.setText("");
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Override
    protected void setStatusBar() {
        StatusUtils.setStatusBarResource(this, R.drawable._ori_status_color);
    }
}
