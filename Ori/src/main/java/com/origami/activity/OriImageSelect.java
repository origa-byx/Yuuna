package com.origami.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.origami.activity.adapter.SelectImageAdapter;
import com.origami.activity.adapter.SelectPlaceAdapter;
import com.origami.origami.R;
import com.origami.origami.base.act.AnnotationActivity;
import com.origami.origami.base.utils.OriTransfer;
import com.origami.utils.Dp2px;
import com.origami.utils.Ori;
import com.origami.utils.ProviderImageUtils;
import com.origami.utils.StatusUtils;
import com.origami.view.OriRecyclerView;
import com.origami.view.OriRelativeLayout;
import com.origami.view.inter.TouchHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @by: origami
 * @date: {2021-08-04}
 * @info:  图片选择器  return: String[]  key : paths
 **/
public class OriImageSelect extends AnnotationActivity implements TouchHandler {

    public static final String RESULT_KEY = "paths";

    TextView _ori__cancel, _ori__select_place, _ori__complete;
    RecyclerView _ori__recyclerView_select_place;
    OriRecyclerView _ori__recyclerView;
    OriRelativeLayout _ori__top_bar;

    public static Builder builder(){ return new Builder(); }

    public static class Builder {
        Set<String> supportType = new HashSet<>();
        //默认选一张
        int selectNum = 1;
        //默认请求码200
        int requestCode = 200;
        //默认不能预览
        boolean canPre = false;
        //默认3张一行
        int rowShowNum = 3;
        //背景
        private int background = 0;
        //背景颜色 （优先应用背景，如果有）
        private int color = Color.parseColor("#212121");
        private final int[] text_colors = new int[]{Color.parseColor("#212121"), Color.parseColor("#eeeeee")};

        private Builder(){ }

        /**
         * 设置请求code
         * @param requestCode
         * @return
         */
        public Builder setRequestCode(int requestCode){
            this.requestCode = requestCode;
            return this;
        }

        /**
         * 设置一行显示个数
         * @param rowShowNum
         */
        public Builder setRowShowNum(@IntRange(from = 1, to = 6) int rowShowNum) {
            this.rowShowNum = rowShowNum;
            return this;
        }

        /**
         * 是否可以预览
         * @param canPre
         * @return
         */
        public Builder setCanPre(boolean canPre) {
            this.canPre = canPre;
            return this;
        }

        /**
         * 设置能够选择多少张图片
         * @param selectNum
         * @return
         */
        public Builder setSelectNum(int selectNum){
            this.selectNum = selectNum;
            return this;
        }

        /**
         * 设置背景
         * @param background
         * @return
         */
        public Builder setBackgroundDrawable(@DrawableRes int background){
            this.background = background;
            return this;
        }

        /**
         * 设置背景颜色
         * @param color
         * @return
         */
        public Builder setBackgroundColor(@ColorRes int color){
            this.color = color;
            return this;
        }

        /**
         * 设置界面按钮颜色
         * @param color_in
         * @param color_out
         * @return
         */
        public Builder setTextColors(int color_in, int color_out) {
            text_colors[0] = color_in;
            text_colors[1] = color_out;
            return this;
        }

        public Builder addSupportGIF() {
            supportType.add("image/gif");
            return this;
        }

        public Builder addSupportType(String mimeType){
            supportType.add(mimeType);
            return this;
        }

        public void build(Activity activity){
            supportType.add("image/jpeg");
            supportType.add("image/png");
//            supportType.add("image/webp");
            Intent intent = new Intent(activity, OriImageSelect.class);
            OriTransfer.registerTransfer("oriImageSelect", new OriTransfer.Transfer<Builder>(OriTransfer.Simple) {
                @Override
                public Builder getT() {
                    return Builder.this;
                }
            });
            activity.startActivityForResult(intent, requestCode);
        }

        public void buildWithNotInitSupportJpegAndPng(Activity activity){
            Intent intent = new Intent(activity, OriImageSelect.class);
            OriTransfer.registerTransfer("oriImageSelect", new OriTransfer.Transfer<Builder>(OriTransfer.Simple) {
                @Override
                public Builder getT() {
                    return Builder.this;
                }
            });
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private Map<String, List<String>> pathList;

    private final List<SelectPlaceAdapter.AdapterData> adapter_SelectData = new ArrayList<>();
    private final List<String> adapter_ImageData = new ArrayList<>();
    Builder builder;

    private SelectPlaceAdapter selectPlaceAdapter;
    private SelectImageAdapter selectImageAdapter;

    private final ValueAnimator animation = new ValueAnimator();
    private boolean isShowSelectPlace = false;
    private final float defTY = -Dp2px.dp2px(100);
    private final Point screenP = new Point();
    int dp5 = Dp2px.dp2px(1);

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        getWindowManager().getDefaultDisplay().getSize(screenP);
        bindViewAndEvent();
        initRecyclerView();
        animation.setDuration(800);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                _ori__recyclerView_select_place.setTranslationY(animatedValue);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProviderImageUtils.ResultData resultData = ProviderImageUtils.getImagesPathList(OriImageSelect.this, builder.supportType.toArray(new String[0]));
                pathList = resultData.dates;
                for (String s : resultData.keys) {
                    SelectPlaceAdapter.AdapterData data = new SelectPlaceAdapter.AdapterData();
                    data.text = s;
                    data.image = Objects.requireNonNull(pathList.get(s)).get(0);
                    data.num = Objects.requireNonNull(pathList.get(s)).size();
                    adapter_SelectData.add(data);
                }
                adapter_ImageData.clear();
                adapter_ImageData.addAll(Objects.requireNonNull(pathList.get("")));
                runOnUiThread(() -> {
                    selectPlaceAdapter.notifyDataSetChanged();
                    selectImageAdapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private void initRecyclerView() {
        int width = (screenP.x - dp5 * builder.rowShowNum * 2 + builder.rowShowNum - 1) / builder.rowShowNum;
        ListPreloader.PreloadSizeProvider<String> sizeProvider = new FixedPreloadSizeProvider<>(width, width);
        ListPreloader.PreloadModelProvider<String> provider = new ListPreloader.PreloadModelProvider<String>(){
            @NonNull
            @Override
            public List<String> getPreloadItems(int position) {//作用是返回地址
                //imagesList是你的图片地址列表
                if(position < adapter_ImageData.size()){
                    //告诉RecyclerViewPreloader每个item项需要加载的图片url集合
                    return adapter_ImageData.subList(position, position + 1);
                }else {
                    return adapter_ImageData.subList(adapter_ImageData.size() - 1, adapter_ImageData.size());
                }
            }
            @Nullable
            @Override
            public RequestBuilder<Drawable> getPreloadRequestBuilder(@NonNull String url) {
                //返回一个加载图片的RequestBuilder
                return Glide.with(OriImageSelect.this).load(url).thumbnail(0.3f);
            }
        };
        RecyclerViewPreloader<String> preloader = new RecyclerViewPreloader<>(
                        Glide.with(this), provider, sizeProvider, builder.rowShowNum * 3 /*maxPreload*/);
        selectPlaceAdapter = new SelectPlaceAdapter(this, adapter_SelectData);
        _ori__recyclerView_select_place.setLayoutManager(new LinearLayoutManager(this));
        _ori__recyclerView_select_place.setAdapter(selectPlaceAdapter);
        _ori__recyclerView_select_place.setTranslationY(-screenP.y);
        _ori__recyclerView_select_place.setVisibility(View.VISIBLE);
        selectImageAdapter = new SelectImageAdapter(this, adapter_ImageData, builder.selectNum, builder.canPre, builder.text_colors[0]);
        _ori__recyclerView.setLayoutManager(new GridLayoutManager(this, builder.rowShowNum));
        _ori__recyclerView.setAdapter(selectImageAdapter);
        _ori__recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = dp5;
                outRect.right = dp5;
                outRect.left = dp5;
                outRect.bottom = dp5;
            }
        });
        _ori__recyclerView.addOnScrollListener(preloader);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindViewAndEvent() {
        _ori__cancel = findViewById(R.id._ori__cancel);
        _ori__select_place = findViewById(R.id._ori__select_place);
        _ori__complete = findViewById(R.id._ori__complete);
        _ori__top_bar = findViewById(R.id._ori__top_bar);
        _ori__select_place.setBackground(Ori.getDefSelectorBackgroundButtonDrawable(
                this, builder.text_colors[0], builder.text_colors[1], true));
        _ori__select_place.setTextColor(Ori.getSelectorColorStateList(builder.text_colors[0], builder.text_colors[1], true));
        _ori__complete.setBackground(Ori.getDefSelectorBackgroundButtonDrawable(
                this, builder.text_colors[0], builder.text_colors[1], true));
        _ori__complete.setTextColor(Ori.getSelectorColorStateList(builder.text_colors[0], builder.text_colors[1], true));
        _ori__recyclerView = findViewById(R.id._ori__recyclerView);
        _ori__recyclerView_select_place = findViewById(R.id._ori__recyclerView_select_place);
        _ori__recyclerView_select_place.setBackground(
                Ori.getGradientDrawable(new int[]{Color.WHITE,Color.parseColor("#525252")})
        );
        _ori__cancel.setOnClickListener(this);
        _ori__select_place.setOnClickListener(this);
        _ori__complete.setOnClickListener(this);
        _ori__recyclerView.setDispatchTouchHandler(this);
        _ori__top_bar.setDispatchTouchHandler(this);
    }

    @Override
    public Boolean handlerTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN && isShowSelectPlace){
            closeSelect();
            return false;
        }
        return null;
    }

    public void refreshBySelect(String key, String name){
        _ori__select_place.setText(name);
        _ori__recyclerView.scrollToPosition(0);
        List<String> list = pathList.get(key);
        if(list != null && !list.isEmpty()){
            adapter_ImageData.clear();
            adapter_ImageData.addAll(list);
            selectImageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id._ori__cancel) {//取消选择
            cancel();
        }else if(id == R.id._ori__select_place){//切换文件夹
            if(isShowSelectPlace){//已经显示
                closeSelect();
            }else {//未显示
                openSelect();
            }
        }else if(id == R.id._ori__complete){//完成选择
            selectOk();
        }
    }

    public void closeSelect(){
        if(!isShowSelectPlace){ return; }
        isShowSelectPlace = false;
        if(animation.isRunning()){ animation.cancel(); }
        float translationY = _ori__recyclerView_select_place.getTranslationY();
        animation.setFloatValues(translationY, defTY - _ori__recyclerView_select_place.getHeight());
        animation.start();
    }

    public void openSelect(){
        if(isShowSelectPlace){ return; }
        isShowSelectPlace = true;
        if(animation.isRunning()){ animation.cancel(); }
        float translationY = _ori__recyclerView_select_place.getTranslationY();
        animation.setFloatValues(translationY, defTY);
        animation.start();
    }


    private void cancel(){
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void selectOk(){
        List<String> selectPaths = selectImageAdapter.getSelectPaths();
        if(selectPaths.isEmpty()){ cancel(); return; }
        String[] paths = new String[selectPaths.size()];
        for (int i = 0; i < selectPaths.size(); i++) {
            paths[i] = selectPaths.get(i);
        }
        Intent intent = new Intent();
        intent.putExtra(RESULT_KEY, paths);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void setStatusBar() {
        super.setStatusBar();
        builder = OriTransfer.getTransferValue("oriImageSelect");
        if(builder == null){ finish(); return; }
        View contentView = this.getWindow().getDecorView()
                .findViewById(android.R.id.content);
        if(builder.background != 0){
            contentView.setBackground(getResources().getDrawable(builder.background));
        }else {
            GradientDrawable gradientDrawable =
                    Ori.getGradientDrawable(new int[]{builder.color, Color.TRANSPARENT, Color.TRANSPARENT});
            contentView.setBackground(gradientDrawable);
        }
        contentView.setPadding(0, StatusUtils.getStatusBarHeight(this), 0, 0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && isShowSelectPlace){
            closeSelect();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_ori_select_image;
    }

}
