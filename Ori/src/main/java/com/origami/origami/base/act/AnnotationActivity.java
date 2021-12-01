package com.origami.origami.base.act;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.origami.origami.R;
import com.origami.origami.base.event.EventConfig;
import com.origami.origami.base.event.OriEventBus;
import com.origami.origami.base.callback.RequestPermissionNext;
import com.origami.origami.base.annotation.BClick;
import com.origami.origami.base.annotation.BContentView;
import com.origami.origami.base.annotation.BView;
import com.origami.origami.base.toast.OriToast;
import com.origami.origami.base.toast.ToastMsg;
import com.origami.utils.Dp2px;
import com.origami.utils.StatusUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author xiao gan
 * @date 2020/12/2
 * @description:
 * @see BContentView
 * @see BView
 * @see BClick
 **/
public abstract class AnnotationActivity extends AppCompatActivity implements View.OnClickListener {

    protected final static int per_requestCode = 1028;
    //点击事件集合
    protected final SparseArray<Method> methodSparseArray = new SparseArray<>();

    private final int anTime = 1000;

    private ValueAnimator sMsgAnimator;
    private Toast sToast;
    private View sToastView;
    private final OriEventBus.Event2 showToastEvent = new OriEventBus.Event2(this, OriEventBus.RunThread.MAIN_UI) {
        @Override
        public void postEvent(Object... args) {
            if (args.length > 0) {
                if(args[0] instanceof OriToast){ showSysToastMsg(((OriToast) args[0])); }
                else if (args[0] instanceof ToastMsg) { showToastMsg((ToastMsg) args[0]); }
                else if (args[0] instanceof String) { showToastMsg(new ToastMsg((String) args[0])); }
            }
        }
    };

    private RequestPermissionNext next;

    public abstract void init(@Nullable Bundle savedInstanceState);

    protected void initContentView(@LayoutRes int resId){
        setContentView(resId);
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            BView bindMyView = field.getAnnotation(BView.class);
            if(bindMyView != null){
                try {
                    boolean accessible = field.isAccessible();
                    if(field.getModifiers() != Modifier.PUBLIC && !accessible){
                        field.setAccessible(true);
                        field.set(this,findViewById(bindMyView.value()));
                        field.setAccessible(false);
                    }else{
                        field.set(this,findViewById(bindMyView.value()));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.e("ORI",String.format("init -> %s", this.getClass().getSimpleName()));
        onCreateBefore(savedInstanceState);
        BContentView contentView = getClass().getAnnotation(BContentView.class);
        if(contentView != null){
            initContentView(contentView.value());
        }else {
            setContentView(getLayout());
            setStatusBar();
            init(savedInstanceState);
            AnnotationActivityManager.addActivity(this);
            return;
        }
        OriEventBus.bindOriEvent(this);
        Method[] methods = getClass().getDeclaredMethods();
        for (Method method : methods) {
            BClick bindClickListener = method.getAnnotation(BClick.class);
            if(bindClickListener != null){
                int[] value = bindClickListener.value();
                for (int i : value) {
                    methodSparseArray.put(i, method);
                    findViewById(i).setOnClickListener(this);
                }
            }

        }
        AnnotationActivityManager.addActivity(this);
        OriEventBus.registerEvent(EventConfig.SHOW_TOAST, showToastEvent);
        setStatusBar();
        init(savedInstanceState);
    }

    protected void onCreateBefore(Bundle savedInstanceState){ }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("ORI",String.format("onNewIntent -> %s", this.getClass().getSimpleName()));
        setIntent(intent);
        initNewIntent(intent);
    }

    /**
     * 特殊情况下 singleTop 与 singleTask 模式需要重新处理数据用
     */
    protected void initNewIntent(Intent intent){ }

    /**
     * 动态权限申请
     * @param permissions
     * @param permissionNext
     */
    public void checkPermissionAndThen(String[] permissions, RequestPermissionNext permissionNext){
        if(!checkPermissionsAllGranted(permissions)){
            this.next = permissionNext;
            ActivityCompat.requestPermissions(this, permissions, per_requestCode);
        }else if(permissionNext != null) {
            permissionNext.next();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == per_requestCode) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                if(next != null){ next.next(); }
            }else if(next != null) { next.failed(); }
            next = null;
        }
    }

    private boolean checkPermissionsAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /**
     * 处理状态栏
     */
    protected void setStatusBar(){
        StatusUtils.setImmerseStatus(this);
    }

    @Override
    public void onClick(View v) {
        Method method = methodSparseArray.get(v.getId());
        if(method != null){
            try {
                boolean accessible = method.isAccessible();
                if(method.getModifiers() != Modifier.PUBLIC && !accessible){
                    method.setAccessible(true);
                    method.invoke(this);
                    method.setAccessible(false);
                }else{
                    method.invoke(this);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        OriEventBus.registerEvent(EventConfig.SHOW_TOAST, showToastEvent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        OriEventBus.removeEvent(showToastEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OriEventBus.removeOriEvent(this);
        AnnotationActivityManager.removeActivity(this);
    }

    @Deprecated
    private void showSysToastMsg(OriToast msg){
        if(TextUtils.isEmpty(msg.msg)){ return; }
        if(sToastView == null || sToast == null){
            ViewGroup contView = getWindow().getDecorView().findViewById(android.R.id.content);
            sToastView = LayoutInflater.from(this)
                    .inflate(R.layout._base_sys_toast, contView,false);
            ViewGroup.LayoutParams params = sToastView.getLayoutParams();
            if(params == null){ params = new ViewGroup.LayoutParams(
                    contView.getWidth() - Dp2px.dp2px(60),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            }else {
                params.width = contView.getWidth() - Dp2px.dp2px(60);
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            sToastView.setLayoutParams(params);
        }
        ImageView iconView = sToastView.findViewById(R.id._base_show_toast_icon);
        if(msg.icon == null){
            iconView.setVisibility(View.INVISIBLE);
        }else {
            iconView.setVisibility(View.VISIBLE);
            if (msg.icon) {
                iconView.setImageResource(R.mipmap._toast_ok);
            } else {
                iconView.setImageResource(R.mipmap._toast_no);
            }
        }
        ((TextView) sToastView.findViewById(R.id._base_show_toast_msg)).setText(msg.msg);
        if(sToast == null){ sToast = new Toast(this); sToast.setView(sToastView); }
        sToast.setDuration(msg.shortDur? Toast.LENGTH_SHORT: Toast.LENGTH_LONG);
        sToast.setGravity(msg.gravity, 0, msg.dy);
        sToast.show();
    }

    public void showToastMsg(ToastMsg msg){
        if(TextUtils.isEmpty(msg.msg)){ return; }
        if(sToastView == null || sToast != null){
            sToast = null;
            ViewGroup contView = getWindow().getDecorView().findViewById(android.R.id.content);
            sToastView = LayoutInflater.from(this)
                    .inflate(R.layout._base_show_toast, contView,false);
            sToastView.setVisibility(View.GONE);
            contView.addView(sToastView);
        }
        ImageView iconView = sToastView.findViewById(R.id._base_show_toast_icon);
        if(msg.showIcon == null){
            iconView.setVisibility(View.INVISIBLE);
        }else {
            iconView.setVisibility(View.VISIBLE);
            if (msg.showIcon) {
                iconView.setImageResource(R.mipmap._toast_ok);
            } else {
                iconView.setImageResource(R.mipmap._toast_no);
            }
        }
        ((TextView) sToastView.findViewById(R.id._base_show_toast_msg)).setText(msg.msg);
//        sToastView.measure(0,0);
//        sToastView_H = sToastView.getMeasuredHeight();
        if(sMsgAnimator == null){
            sMsgAnimator = ObjectAnimator.ofFloat(0,1);
            sMsgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = Math.min((float) animation.getAnimatedValue() / anTime * (anTime + msg.showTime),1f);
                    if(value != 1f && sToastView.getTranslationY() != 0){
                        sToastView.setTranslationY(sToastView.getHeight() * (value - 1));
                    }
                }
            });
            sMsgAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sToastView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    sToastView.setVisibility(View.GONE);
                }
            });
        }
        if(sMsgAnimator.isRunning()){
            sMsgAnimator.cancel();
        }
        sMsgAnimator.setDuration(anTime + msg.showTime);
        sToastView.setVisibility(View.VISIBLE);
        sMsgAnimator.start();
    }

    /**
     * @see BContentView
     * @return
     * @deprecated use {@link BContentView}
     */
    protected int getLayout(){ return 0; }

}
