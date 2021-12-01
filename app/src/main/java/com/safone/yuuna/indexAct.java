package com.safone.yuuna;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;


import com.origami.origami.base.act.AnnotationActivity;
import com.origami.origami.base.annotation.BContentView;
import com.origami.origami.base.annotation.BView;
import com.origami.origami.base.callback.RequestPermissionNext;
import com.origami.utils.StatusUtils;
import com.safone.yuuna.qn.QNTestAct;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerVideoId;
import com.tencent.liteav.demo.superplayer.SuperPlayerView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.widget.media.AndroidMediaController;
import tv.danmaku.ijk.media.widget.media.IjkVideoView;

@SuppressLint("NonConstantResourceId")
@BContentView(R.layout.activity_index)
public class indexAct extends AnnotationActivity {

//    @BView(R.id.node_name)
//    EditText editText;

    //    SurfaceHolder sh;
//    MediaPlayer player;
    @BView(R.id.play_v)
    IjkVideoView mIjvideo;

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
//        player = new MediaPlayer();
//        try {
//            player.setDataSource(this, Uri.parse("http://192.168.0.80:8082/hls/test.m3u8"));
//            sh =sfv.getHolder();
//            sh.addCallback(new MyCallBack());
//            player.prepare();
//            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    player.start();
//                    player.setLooping(true);
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        SuperPlayerModel model = new SuperPlayerModel();
//        model.title = "RSTP测试";
//        model.url = "http://192.168.0.80:8082/hls/test.m3u8";
//        model.videoId = new SuperPlayerVideoId();
//        mSuperPlayerView.playWithModel(model);
        if(true){
            checkPermissionAndThen(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
            }, new RequestPermissionNext() {
                @Override
                public void next() {
                    Intent intent = new Intent(indexAct.this, MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void failed() {
                    Intent intent = new Intent(indexAct.this, MainActivity.class);
                    startActivity(intent);
                }
            });
        }else {
            mIjvideo.changeAspectRaito(3);

            IjkMediaPlayer.loadLibrariesOnce(null);

            IjkMediaPlayer.native_profileBegin("libijkplayer.so");

            AndroidMediaController controller = new AndroidMediaController(this, false);

            mIjvideo.setMediaController(controller);

            String url = "http://192.168.0.80:8082/hls/test.m3u8";//换成自己的m3u8地址

            mIjvideo.setVideoURI(Uri.parse(url));

            mIjvideo.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    iMediaPlayer.start();
                }
            });

            mIjvideo.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    iMediaPlayer.start();
                }
            });
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
//        mIjvideo.resume();
//        if (!mIjvideo.isPlaying()) { mIjvideo.start(); }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mIjvideo.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        IjkMediaPlayer.native_profileEnd();
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
//        mSuperPlayerView.setVideoURI(Uri.parse("http://192.168.0.80:8082/hls/test.m3u8"));
//        mSuperPlayerView.setMediaController(new MediaController(this));
//        mSuperPlayerView.start();
    }

    //    @BClick(R.id.goNext)
//    public void goNext(){
//        Editable text = editText.getText();
//        if(text == null || TextUtils.isEmpty(text.toString())){
//            ToastMsg.show_msg("节点名称不能为空", false, 2000);
//            return;
//        }
//        UdpP2P.myNodeName = text.toString();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish();
//    }

    @Override
    protected void setStatusBar() {
        StatusUtils.setStatusBarResource(this, R.drawable._ori_status_color);
    }


//    private class MyCallBack implements SurfaceHolder.Callback {
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//            Log.e("ORI","surfaceCreated");
//            player.setDisplay(holder);
//        }
//
//        @Override
//        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//        }
//
//        @Override
//        public void surfaceDestroyed(SurfaceHolder holder) {
//
//        }
//    }

}