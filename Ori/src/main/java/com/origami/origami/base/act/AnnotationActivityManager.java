package com.origami.origami.base.act;

import android.os.Process;

import com.origami.origami.base.act.AnnotationActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * @by: origami
 * @date: {2021-05-21}
 * @info:
 **/
public class AnnotationActivityManager {

    private final static List<AnnotationActivity> mActivityList = new LinkedList<>();

    public static void addActivity(AnnotationActivity activity){
        mActivityList.add(activity);
    }

    public static void removeActivity(AnnotationActivity activity){
        mActivityList.remove(activity);
    }

    public static boolean contains(AnnotationActivity activity){
        return mActivityList.contains(activity);
    }

    public static AnnotationActivity getActivity(Class<AnnotationActivity> activity_clazz){
        for (AnnotationActivity annotationActivity : mActivityList) {
            if(annotationActivity.getClass() == activity_clazz){
                return annotationActivity;
            }
        }
        return null;
    }

    public static List<AnnotationActivity> getActivityList() {
        return mActivityList;
    }

    public static void killAll(boolean killProcess){
        for (AnnotationActivity activity : mActivityList) {
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
        if (killProcess) {
            Process.killProcess(Process.myPid());
        }
    }

}
