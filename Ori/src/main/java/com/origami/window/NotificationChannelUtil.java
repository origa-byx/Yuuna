package com.origami.window;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.os.Build;

public class NotificationChannelUtil {

    private NotificationChannelUtil(){};
    /**
     * @param channelId ---> 唯一标识的字符串 eg："chat_msg"
     * @param channelName ---> 会话组名字 eg："聊天消息"
     * @param importance ---> 重要程度 eg：int importance = NotificationManager.IMPORTANCE_HIGH;
     */
    public static void createNotificationChannel(NotificationManager manager,String channelId, String channelName, int importance) {
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            if(manager.getNotificationChannel(channelId) != null){return;}
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            manager.createNotificationChannel(channel);
        }
    }
}
