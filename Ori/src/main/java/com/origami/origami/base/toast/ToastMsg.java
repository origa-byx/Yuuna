package com.origami.origami.base.toast;


import com.origami.origami.base.event.EventConfig;
import com.origami.origami.base.event.OriEventBus;

/**
 * @by: origami
 * @date: {2021/4/29}
 * @info:
 *
 * @deprecated
 * @see OriToast
 **/
public class ToastMsg {

    /**
     * 队列模式
     */
    public final static int DEF = 0;

    /**
     * 抢断模式
     */
    public final static int STEAL = 1;


    public String msg;

    public Boolean showIcon;

    public int showType;

    public long showTime;

    public ToastMsg(String msg){
        this(msg,DEF);
    }

    public ToastMsg(String msg, int showType){
        this(msg,showType,2000);
    }

    public ToastMsg(String msg, int showType, long showTime){
        this(msg,showType,showTime,true);
    }

    public ToastMsg(String msg, int showType, long showTime, boolean showIcon) {
        this.msg = msg;
        this.showIcon = showIcon;
        this.showType = showType;
        this.showTime = showTime;
    }

    public static void show_msg(String msg){
        new ToastMsg(msg).show();
    }

    public static void show_msg(String msg, long time){
        new ToastMsg(msg, DEF, time).show();
    }

    public static void show_msg(String msg, Boolean boo){ new ToastMsg(msg, DEF, 2000 , boo).show(); }

    public static void show_msg(String msg, Boolean boo, long time){ new ToastMsg(msg, DEF, time , boo).show(); }

    public void show(){
            OriEventBus.triggerEvent(EventConfig.SHOW_TOAST,this);
    }
}
