package com.ori.origami;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;

import com.ori.origami.recoder.OriAudio;
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

    Timer timer;

    @Override
    public void init(@Nullable Bundle savedInstanceState) { }

    boolean click = false;
    int timeIndex = 0;
    String fileName = "";

    @BClick(R.id.cliAudio)
    public void doClick_0(){
        if(click){
            OriAudio.instance().stopRecord();
            OriAudio.instance().release();
            click = false;
            stopTime();
            mViews.cliAudio.setText("录制");
            mViews.proAudio.setVisibility(View.INVISIBLE);
            OriToast.show(String.format("文件保存至：%s", fileName), true, false);
        }else {
            checkPermissionAndThen(new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, new RequestPermissionNext() {
                @Override
                public void next() {
                    OriAudio.instance().init();
                    OriAudio.instance().initMp3encode(8000, 2, 16000, 7);
                    OriAudio.instance().mp3encode()
                            .setId3V1Tag("测试title", "ori", "ori专辑", "2021");
                    fileName = Ori.getSaveFilePath(DemoAct.this, Environment.DIRECTORY_MUSIC)
                            + Ori.getRandomString(6) + "-test.mp3";
                    OriAudio.instance().startRecord(
                            fileName);
                    click = true;
                    mViews.cliAudio.setText("停止");
                    mViews.proAudio.setVisibility(View.VISIBLE);
                    startTime();
                }

                @Override
                public void failed() {
                    OriToast.show("你拒绝了权限申请");
                }
            });
        }
    }

    private void startTime(){
        if(timer != null){ stopTime(); }
        timeIndex = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(()->{ mViews.timeIndex.setText(String.valueOf(++timeIndex)); });
            }
        }, 1000, 1000);
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
