package com.origami.window;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.origami.activity.OriImageSelect;
import com.origami.origami.R;

/**
 * @by: origami
 * @date: {2021-09-07}
 * @info:   api26以上 需要 弹出 横幅 通知的话需要指引用户到setting界面开启通知权限设置
 *              {@link android.provider.Settings#ACTION_APP_NOTIFICATION_SETTINGS}
 **/
public class NotificationUtil {

    //通知管理器
    NotificationManager mNotificationManager;
    //通知类
    Notification notification;

    Builder builder;

    public static Builder builder(Activity activity, String channel_id ,String channel_name){
        return new Builder(activity).setId_Name(channel_id, channel_name);
    }

    public static class Builder{

        Activity activity;
        public Builder(Activity activity) { this.activity = activity; }

        //标题
        String title;
        //内容
        String contentText;
        //跳转意图
        PendingIntent pendingIntent;
        //图标
        @DrawableRes
        int iconRes;
        //通知通道标识id  比如 com.origami.notify.chat
        String id;
        //通知通道名 （一般叙述会显示在设置界面） 比如 聊天消息通知
        String name;
        //通知重要程度
        int importance = 0;

        /**
         * 创建活动意图
         * @param actClass
         * @return  this
         */
        public Builder setIntent(Class<? extends Activity> actClass){
            Intent intent = new Intent(activity, actClass)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getActivity(
                    activity, 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT
            );
            return this;
        }

        /**
         * 创建意图     可能想启动的不是 act 而是 活动这些呢
         * @param intent
         * @return  this
         */
        public Builder setIntent(PendingIntent intent){
            this.pendingIntent = intent;
            return this;
        }

        /**
         * 标题
         * @param title
         * @return  this
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * 内容
         * @param contentText
         * @return  this
         */
        public Builder setContentText(String contentText) {
            this.contentText = contentText;
            return this;
        }

        /**
         * 图标
         * @param iconRes
         * @return  this
         */
        public Builder setIconRes(@DrawableRes int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        /**
         * 通道 id  name
         * @param id        通知通道标识id  比如 com.origami.notify.chat
         * @param name      通知通道名 （一般叙述会显示在设置界面） 比如 聊天消息通知
         * @return  this
         */
        private Builder setId_Name(String id ,String name) {
            this.id = id;
            this.name = name;
            return this;
        }

        /**
         * 通知重要程度
         * @param importance  eg.  {@link NotificationManager#IMPORTANCE_HIGH}
         * @return
         */
        public Builder setImportance(int importance) {
            this.importance = importance;
            return this;
        }

        /**
         * @return  构建
         */
        public NotificationUtil build(){
            return new NotificationUtil(this);
        }
    }

    private NotificationUtil(Builder builder) {
        this.builder = builder;
        mNotificationManager = (NotificationManager) builder.activity.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * @param args 最大三个
     *   依次为 震动 - 声音 - 闪光灯 但是 api 26以上没处理（builder 内置的弃用了，如果需要自己加上）
     *             官方操作是  在注册 通知通道{@link NotificationManager#createNotificationChannel(NotificationChannel)}
     *                  之前对 {@link NotificationChannel} 进行设置 比如：
     *                              {@link NotificationChannel#enableVibration(boolean)} 为开启震动
     */
    public void show(boolean... args) { show(1, args); }

    public void show(int id, boolean... args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(builder.importance == 0){ builder.importance = NotificationManager.IMPORTANCE_DEFAULT; }
            NotificationChannelUtil.createNotificationChannel(mNotificationManager, builder.id, builder.name, builder.importance);
            Notification.Builder builder = new Notification.Builder(this.builder.activity, this.builder.id)
                    .setContentTitle(this.builder.title)
                    .setContentIntent(this.builder.pendingIntent)
                    .setContentText(this.builder.contentText)
                    .setSmallIcon(this.builder.iconRes);
            notification = builder.build();
        } else {
            final boolean[] flag = new boolean[]{ false, false, false };
            if(args != null){ System.arraycopy(args, 0, flag, 0, args.length); }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this.builder.activity)
                    .setSmallIcon(this.builder.iconRes)
                    .setContentTitle(this.builder.title)
                    .setContentText(this.builder.contentText)
                    .setContentIntent(this.builder.pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            if(flag[0]){ builder.setDefaults(Notification.DEFAULT_VIBRATE); }
            if(flag[1]){ builder.setDefaults(Notification.DEFAULT_SOUND); }
            if(flag[2]){ builder.setDefaults(Notification.DEFAULT_LIGHTS); }
            notification = builder.build();
        }
        mNotificationManager.notify(id, notification);
    }

}
