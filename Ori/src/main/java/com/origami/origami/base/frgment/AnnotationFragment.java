package com.origami.origami.base.frgment;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.origami.origami.base.callback.RequestPermissionNext;
import com.origami.origami.base.act.AnnotationActivity;
import com.origami.origami.base.annotation.BClick;
import com.origami.origami.base.annotation.BContentView;
import com.origami.origami.base.annotation.BView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author xiao gan
 * @date 2020/12/3
 * @description:
 **/
public abstract class AnnotationFragment<T extends AnnotationActivity> extends Fragment implements View.OnClickListener {

    protected final SparseArray<Method> methodSparseArray = new SparseArray<>();
    protected WeakReference<T> mAct;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mAct = new WeakReference<>((T) context);
    }

    protected T getAct(){
        return mAct.get();
    }

    protected void initBindView(View view){
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            BView bindMyView = field.getAnnotation(BView.class);
            if(bindMyView != null){
                boolean accessible = field.isAccessible();
                try {
                    if(field.getModifiers() != Modifier.PUBLIC && !accessible){
                        field.setAccessible(true);
                        field.set(this,view.findViewById(bindMyView.value()));
                        field.setAccessible(false);
                    }else {
                        field.set(this,view.findViewById(bindMyView.value()));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BContentView annotation = getClass().getAnnotation(BContentView.class);
        if(annotation != null){
            View view = inflater.inflate(annotation.value(), container, false);
            initBindView(view);
            Method[] methods = getClass().getDeclaredMethods();
            for (Method method : methods) {
                BClick bindClickListener = method.getAnnotation(BClick.class);
                if(bindClickListener != null){
                    int[] value = bindClickListener.value();
                    for (int i : value) {
                        methodSparseArray.put(i,method);
                        view.findViewById(i).setOnClickListener(this);
                    }
                }
            }
            init(inflater,container,savedInstanceState);
            return view;
        }else{
            init(inflater,container,savedInstanceState);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public abstract void init(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);


    public void checkPermissionAndThen(String[] permissions, RequestPermissionNext permissionNext){
        T t = mAct.get();
        if(t != null){ t.checkPermissionAndThen(permissions, permissionNext); }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAct = null;
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
}
