package com.origami.activity.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.origami.activity.OriImageActivity;
import com.origami.activity.OriImageSelect;
import com.origami.origami.R;
import com.origami.origami.base.toast.OriToast;

import java.util.ArrayList;
import java.util.List;

/**
 * @by: origami
 * @date: {2021-08-06}
 * @info:
 **/
public class SelectImageAdapter extends RecyclerView.Adapter<SelectImageAdapter.ViewHolder> {

    final OriImageSelect context;
    final List<String> dates;
    final int itemBackColor;
    //最大选择数
    final int maxSelect;
    //是否可以预览
    final boolean canPre;
    final List<String> selectPaths = new ArrayList<>();

    public SelectImageAdapter(OriImageSelect context, List<String> dates, int selectNum, boolean canPre,@ColorInt int itemBackColor) {
        this.context = context;
        this.dates = dates;
        this.maxSelect = Math.max(1, selectNum);
        this.canPre = canPre;
        this.itemBackColor = itemBackColor;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout._adapter_ori_select_image, parent, false);
        view.setBackgroundColor(itemBackColor);
        ViewHolder viewHolder = new ViewHolder(view);
        if(!canPre && maxSelect <= 1){
            viewHolder.textView.setVisibility(View.GONE);
        }
        View.OnClickListener listener = v -> {
            int position = viewHolder.getAdapterPosition();
            String path = dates.get(position);
            Log.e("SELECT","path->" + path);
            if(!canPre && maxSelect <= 1){
                selectPaths.add(path);
                context.selectOk();
                return;
            }
            if (selectPaths.contains(path)) {
                selectPaths.remove(path);
                notifyDataSetChanged();
            } else if (selectPaths.size() >= maxSelect) {
                OriToast.show(String.format("最多只能选择%s张图片", maxSelect), false);
            } else {
                selectPaths.add(path);
                notifyItemChanged(position);
            }
        };
        if(canPre) {
            viewHolder.imageView.setOnClickListener(v -> OriImageActivity.startThisAct(context,
                    dates.get(viewHolder.getAdapterPosition()),
                    false,
                    viewHolder.imageView));
            viewHolder.textView.setOnClickListener(listener);
        }else {
            viewHolder.imageView.setOnClickListener(listener);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String path = dates.get(position);
        if(selectPaths.contains(path)){
            holder.textView.setBackground(context.getResources().getDrawable(R.mipmap.ori_select_ok));
            if(maxSelect > 1){ holder.textView.setText(String.valueOf(selectPaths.indexOf(path) + 1)); }
        }else {
            holder.textView.setBackground(context.getResources().getDrawable(R.mipmap.ori_select_press));
            holder.textView.setText("");
        }
        Glide.with(context).load(path).thumbnail(0.3f).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    /**
     * @return 选择的图片集合
     */
    public List<String> getSelectPaths() {
        return selectPaths;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id._ori__select_image);
            textView = itemView.findViewById(R.id._ori__select_index);
        }
    }

}
