package com.origami.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.origami.origami.R;
import com.origami.origami.base.act.AnnotationActivity;
import com.origami.origami.base.utils.OriTransfer;
import com.origami.origami.base.toast.OriToast;
import com.origami.view.OriImageDetailView;
import com.origami.window.WindowUtil2;

import java.io.File;

/**
 * {@link OriImageActivity#startThisAct(Activity, Object, boolean)}
 */
public class OriImageActivity extends AnnotationActivity {

    private OriImageDetailView imageView;

    /**
     * 启动
     * @param activity
     * @param bitmapOrPath 路径 或者 bitmap
     */
    public static void startThisAct(Activity activity, final Object bitmapOrPath, boolean canSave){
        Intent intent = new Intent(activity, OriImageActivity.class);
        intent.putExtra("saveFlag", canSave);
        OriTransfer.registerTransfer("ori_getBitmap", new OriTransfer.Transfer<Object>(OriTransfer.Simple) {
            @Override
            public Object getT() {
                return bitmapOrPath;
            }
        });
        activity.startActivity(intent);
    }

    /**
     * 启动带转场动画
     * @param activity
     * @param view      目标view ：一般为点击的imageView
     * @param bitmapOrPath    路径 或者 bitmap
     */
    public static void startThisAct(Activity activity,final Object bitmapOrPath, boolean canSave, ImageView view){
        Intent intent = new Intent(activity, OriImageActivity.class);
        OriTransfer.registerTransfer("ori_getBitmap", new OriTransfer.Transfer<Object>(OriTransfer.Simple) {
            @Override
            public Object getT() {
                return bitmapOrPath;
            }
        });
        intent.putExtra("saveFlag", canSave);
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, "image");
        activity.startActivity(intent, optionsCompat.toBundle());
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_ori_image;
    }

    /**
     *
     * CustomViewTarget<OriImageDetailView, Bitmap> customViewTarget = new CustomViewTarget<OriImageDetailView, Bitmap>(imageView) {
     *
     *                 @Override
     *                 public void onLoadFailed(@Nullable Drawable errorDrawable) {
     *                 }
     *
     *                 @Override
     *                 public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
     *
     *                 }
     *
     *                 @Override
     *                 protected void onResourceCleared(@Nullable Drawable placeholder) {
     *                 }
     *             };
     * @param savedInstanceState
     */
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        imageView = findViewById(R.id.ori_image);
        boolean save = getIntent().getBooleanExtra("saveFlag", true);
        Object bitmap = OriTransfer.getTransferValue("ori_getBitmap");
        if(bitmap instanceof  Bitmap){
            imageView.setImageBitmap((Bitmap) bitmap);
        }else {
            Glide.with(this).asBitmap().load((String) bitmap).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    imageView.setImageBitmap(resource);
                }
            });
        }
        if(save) {
            imageView.setOnLongClickListener(v -> {
                WindowUtil2.showSelect(OriImageActivity.this, new String[]{"保存"}, (txt, index) -> {
                    int labelRes = getApplication().getApplicationInfo().labelRes;
                    String path;
                    if (labelRes == 0) {
                        path = "ori" + File.separator + "image";
                    } else {
                        path = getResources().getString(labelRes) + File.separator + "image";
                    }
                    String savePath = imageView.saveBitmap(path, true);
                    if (savePath != null) {
                        OriToast.show("保存成功：" + savePath, true);
                    } else {
                        OriToast.show("保存失败", false);
                    }
                }, true);
                return true;
            });
        }

    }
}