package com.origami.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.origami.origami.R;
import com.origami.App;
import com.origami.utils.Ori;
import com.origami.window.NotificationChannelUtil;

import java.io.File;

import okhttp3.OkHttpClient;

/**
 * @by: origami
 * @date: {2021-09-29}
 * @info:  前台服务  更新进度
 * 下载完成自动执行安装，完成时点击前台服务亦可安装
 * 继承此类  并在 清单文件 中注册 Service
 *    启动服务时 eg：
 *         Intent intent = new Intent({@link Context}, @{@link Class<? extends AppUpdate_service>});
 *         intent.putExtra({@link #PARAM_URL}, "your download_url");
 *         startService(intent);
 **/
public abstract class AppUpdate_service extends Service {

    public static final String PARAM_URL = "param_url";
    private static final String TAG = "AppUpdate_service";

    /**
     * @return app icon
     */
    @DrawableRes
    public abstract int getAppIcon();

    /**
     * @return app name
     */
    public abstract String getAppName(Context context);

    /**
     * @return  你的 FileProvider的 authority 值
     */
    public abstract String getFileProviderAuthority(Context context);

    private final String click_action = App.APP_ID + ".AppUpdate_service.click";
    private final int NOTIFY_ID = 7;

    String downUrl;
    RemoteViews remoteViews;
    NotificationManager manager;
    Notification notification;
    File fileAkp;
    boolean isReally = false;
    ActionHandler actionHandler = new ActionHandler();

    class ActionHandler extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(click_action) && isReally && fileAkp != null){
                install(fileAkp, context);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        remoteViews = new RemoteViews(this.getPackageName(), R.layout._notify_app_update);
        remoteViews.setTextViewText(R.id.notify_a_update_app_name, getAppName(App.appContext));
        remoteViews.setImageViewResource(R.id.notify_a_update_app_icon, getAppIcon());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setCustomContentView(remoteViews);
        }else {
            builder.setContent(remoteViews);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(click_action);
        registerReceiver(actionHandler, intentFilter);
        Intent intent = new Intent(click_action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(,pendingIntent);
        manager = (NotificationManager) this.getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            NotificationChannelUtil.createNotificationChannel(manager, "ori_app_update","APP更新-下载进度",
                    NotificationManager.IMPORTANCE_HIGH);
            builder.setChannelId("ori_app_update");
        }
        builder.setContentIntent(pendingIntent);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(getAppIcon());
        notification = builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){ downUrl = intent.getStringExtra(PARAM_URL); }else { stopSelf(); }
        if(TextUtils.isEmpty(downUrl)){
            Ori.e(TAG, new Exception("缺失下载地址参数-> " + PARAM_URL));
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        startForeground(NOTIFY_ID, notification);
        OkGo.getInstance().setOkHttpClient(new OkHttpClient());
        OkGo.<File>get(downUrl).tag("Download").execute(new FileCallback() {
                    @Override
                    public void onSuccess(Response<File> response) {
                        fileAkp = response.body();
                        isReally = true;
                        remoteViews.setTextViewText(R.id.notify_a_update_text, "下载完成");
                        manager.notify(NOTIFY_ID, notification);
                        install(fileAkp, App.appContext);
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        int endProgress = (int) (progress.fraction * 100);
                        remoteViews.setTextViewText(R.id.notify_a_update_text, endProgress + "%");
                        remoteViews.setInt(R.id.notify_a_update_progress, "setProgress", endProgress);
                        manager.notify(NOTIFY_ID, notification);
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        Ori.e(TAG,"下载出错" ,response.getException());
                    }
                });
        return super.onStartCommand(intent, flags, startId);
    }

    public void install(File apkFile, Context mContext) {
        if (mContext == null) { return; }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//android 版本大于7之后需要增加 FileProvider
            // 这里必须用 addFlags  不然会覆盖之前的Flags
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(mContext, getFileProviderAuthority(App.appContext), apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

}
