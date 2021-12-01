package com.origami.origami.base.event;


import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.origami.App;
import com.origami.utils.UiThreadUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @by: origami
 * @date: {2021/4/29}
 * @info:
 **/
public class OriEventBus {

    public static final int ui_thread = RunThread.MAIN_UI;
    public static final int current_thread = RunThread.CURRENT;
    public static final int new_thread = RunThread.NEW_THREAD;

    private final static String TAG = "OriEventBus";

    private final static Map<String, Set<BaseEvent>> EVENT_MAP = new ConcurrentHashMap<>();
    private final static Map<String, Set<ObjBEvent>> objEventMap = new ConcurrentHashMap<>();

    public static void registerEvent(String tag, BaseEvent event){
        if(tag == null || event == null){ return; }
        Set<BaseEvent> events = EVENT_MAP.get(tag);
        if(events == null){ events = new HashSet<>(); }
        if(!events.contains(event)) {
            event.tag = tag;
            events.add(event);
            EVENT_MAP.put(tag, events);
        }
    }

    public static void triggerEvent(String tag, Object... args){
        if(tag == null){ return; }
        Set<BaseEvent> events = EVENT_MAP.get(tag);
        if(events != null){
            for (BaseEvent event : events) {
                event.triggerEvent(args);
            }
        }
    }

    public static void removeEventsByTag(String tag){
        if(tag == null){ return; }
        Set<BaseEvent> remove = EVENT_MAP.remove(tag);
        if(remove != null && !remove.isEmpty()){
            for (BaseEvent baseEvent : remove) {
                if(baseEvent instanceof ObjBEvent){
                    removeObjEventMap(((ObjBEvent) baseEvent));
                }
            }
        }
    }

    public static void removeEvent(BaseEvent event){
        if(event == null){ return; }
        Set<BaseEvent> baseEvents = EVENT_MAP.get(event.tag);
        if(baseEvents != null){
            baseEvents.remove(event);
            if(event instanceof ObjBEvent){
                removeObjEventMap(((ObjBEvent) event));
            }
        }
    }

    public static void triggerEventAndRemove(String tag, Object... args) {
        if(tag == null){ return; }
        Set<BaseEvent> events = EVENT_MAP.remove(tag);
        if(events != null){
            for (BaseEvent event : events) {
                event.triggerEvent(args);
                if(event instanceof ObjBEvent){
                    removeObjEventMap(((ObjBEvent) event));
                }
            }
        }
    }

    public static void bindOriEvent(Object obj){
        Set<ObjBEvent> objBEventSet = new HashSet<>();
        for (Method method : obj.getClass().getDeclaredMethods()) {
            BEvent bEvent = method.getAnnotation(BEvent.class);
            if(bEvent != null){
                ObjBEvent event = new ObjBEvent(obj, method, bEvent.runThread());
                event.tag = bEvent.value();
                objBEventSet.add(event);
                registerEvent(bEvent.value(), event);
            }
        }
        if(!objBEventSet.isEmpty()){
            objEventMap.put(obj.getClass().getName(), objBEventSet);
        }
    }

    public static void removeOriEvent(Object obj){
        if(obj == null){
            for (Set<ObjBEvent> value : objEventMap.values()) {
                for (ObjBEvent objBEvent : value) {
                    removeEvent(objBEvent);
                }
            }
            objEventMap.clear();
            return;
        }
        Set<ObjBEvent> objBEventSet = objEventMap.remove(obj.getClass().getName());
        if(objBEventSet != null && !objBEventSet.isEmpty()){
            for (ObjBEvent objBEvent : objBEventSet) {
                removeEvent(objBEvent);
            }
        }
    }

    private static void removeObjEventMap(ObjBEvent event){
        Set<ObjBEvent> objBEventSet = objEventMap.get(event.keyName);
        if(objBEventSet != null) {
            objBEventSet.remove(event);
        }
    }

    public @interface RunThread{
        int MAIN_UI = 0;
        int CURRENT = 1;
        int NEW_THREAD = 2;
    }

    public static abstract class BaseEvent{
        protected String tag;
        public abstract void triggerEvent(Object... args);
    }

    private static final class ObjBEvent extends BaseEvent{

        private final String keyName;
        private final int run_on_thread;
        private final WeakReference<Object> obj;
        private final Method method;

        public ObjBEvent(Object ob, Method method, int run_on_thread) {
            keyName = ob.getClass().getName();
            this.obj = new WeakReference<>(ob);
            this.method = method;
            this.run_on_thread = run_on_thread;
        }

        @Override
        public void triggerEvent(Object... args) {
            Object ob = obj.get();
            if(ob == null){ removeEvent(this); return; }
            switch (run_on_thread){
                case RunThread.MAIN_UI:{
                    UiThreadUtil.getInstance().runOnUiThread(()-> postEvent(args));
                }break;
                case RunThread.CURRENT:{ postEvent(args); }break;
                case RunThread.NEW_THREAD:{ new Thread(() -> postEvent(args)).start(); }break;
            }
        }

        private void postEvent(Object ob, Object... args){
            try{
                method.invoke(ob, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, ob.getClass().getSimpleName() + "::" + method.getName() + "-> " + e.getMessage());
            }
        }

    }

    public static abstract class Event2 extends BaseEvent{

        private final int run_on_thread;
        private final WeakReference<Object> weakReference;

        /**
         * @param activity
         * @param run_thread  {@link RunThread}
         */
        public Event2(Activity activity, int run_thread) {
            weakReference = new WeakReference<>(activity);
            run_on_thread = run_thread;
        }

        /**
         * @param fragment
         * @param run_thread  {@link RunThread}
         */
        public Event2(Fragment fragment, int run_thread) {
            weakReference = new WeakReference<>(fragment);
            run_on_thread = run_thread;
        }

        @Override
        public void triggerEvent(Object... args){
            Object ob;
            if((ob = weakReference.get()) != null){
                Activity oa = ob instanceof Activity ? ((Activity) ob) : null;
                Fragment of = ob instanceof Fragment ? ((Fragment) ob) : null;
                if((oa == null || oa.isFinishing()) && (of == null || of.isDetached())){
                    removeEvent(this);
                    log_msg("");  return;
                }
                switch (run_on_thread){
                    case RunThread.MAIN_UI:{
                        UiThreadUtil.getInstance().runOnUiThread(()->{ postEvent(args); });
                    }break;
                    case RunThread.CURRENT:{ postEvent(args); }break;
                    case RunThread.NEW_THREAD:{ new Thread(() -> postEvent(args)).start(); }break;
                }
            }else { removeEvent(this); log_msg(""); }
        }

        public abstract void postEvent(Object... args);

    }

    /**
     * @deprecated use {@link Event2} and {@link android.app.Application} extends {@link App}
     *  or use {@link Event2} and {@link UiThreadUtil#init(Application)} at first
     */
    public static abstract class Event extends BaseEvent{

        private final int run_on_thread;
        private final WeakReference<Object> weakReference;

        /**
         * @param activity
         * @param run_thread  {@link RunThread}
         */
        public Event(Activity activity, int run_thread) {
            weakReference = new WeakReference<>(activity);
            run_on_thread = run_thread;
        }

        /**
         * @param fragment
         * @param run_thread  {@link RunThread}
         */
        public Event(Fragment fragment, int run_thread) {
            weakReference = new WeakReference<>(fragment);
            run_on_thread = run_thread;
        }

        @Override
        public void triggerEvent(Object... args){
            Object ob;
            if((ob = weakReference.get()) != null){
                Activity oa = ob instanceof Activity ? ((Activity) ob) : null;
                Fragment of = ob instanceof Fragment ? ((Fragment) ob) : null;
                if((oa == null || oa.isFinishing()) && (of == null || of.isDetached())){
                    log_msg(""); removeEvent(this);  return;
                }
                switch (run_on_thread){
                    case RunThread.MAIN_UI:{
                        if(oa != null) {
                            oa.runOnUiThread(() -> postEvent(args));
                        }else if(of.getActivity() != null){
                            of.getActivity().runOnUiThread(() -> postEvent(args));
                        }else {
                            removeEvent(this);
                            log_msg("other");
                        }
                    }break;

                    case RunThread.CURRENT:{ postEvent(args); }break;
                    case RunThread.NEW_THREAD:{ new Thread(() -> postEvent(args)).start(); }break;
                }
            }else { removeEvent(this); log_msg("->weakReference get null"); }
        }

        public abstract void postEvent(Object... args);

    }

    private static void log_msg(String msg){
        Log.e(TAG,"当前事件绑定的Activity 或 Fragment 已销毁" + msg);
    }

}
