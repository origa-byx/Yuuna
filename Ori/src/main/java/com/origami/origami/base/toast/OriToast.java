package com.origami.origami.base.toast;

import android.view.Gravity;

import com.origami.origami.base.event.EventConfig;
import com.origami.origami.base.event.OriEventBus;
import com.origami.utils.Dp2px;

/**
 * @by: origami
 * @date: {2021-10-27}
 * @info:
 **/
public class OriToast {

    public final boolean shortDur;

    public final String msg;

    public final Boolean icon;

    public final int gravity;

    public int dy = Dp2px.dp2px(50);

    public OriToast(boolean shortDur, String msg, Boolean icon, int gravity) {
        this.shortDur = shortDur;
        this.msg = msg;
        this.icon = icon;
        this.gravity = gravity;
    }

    public static void show(String msg){
        OriEventBus.triggerEvent(EventConfig.SHOW_TOAST,new OriToast(true, msg, null, Gravity.TOP));
    }
    public static void show(String msg, boolean icon){
        OriEventBus.triggerEvent(EventConfig.SHOW_TOAST,new OriToast(true, msg, icon, Gravity.TOP));
    }
    public static void show(String msg, boolean icon, boolean shortDur){
        OriEventBus.triggerEvent(EventConfig.SHOW_TOAST,new OriToast(shortDur, msg, icon, Gravity.TOP));
    }
    public static void show(String msg, Boolean icon, boolean shortDur, int gravity, int... dy){
        OriToast oriToast = new OriToast(shortDur, msg, icon, gravity);
        if(dy != null && dy.length > 0) {
            oriToast.dy = dy[0];
        }
        OriEventBus.triggerEvent(EventConfig.SHOW_TOAST, oriToast);
    }

}
