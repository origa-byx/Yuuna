package com.origami.activity.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.origami.activity.OriImageSelect;
import com.origami.origami.R;

import java.util.List;

/**
 * @by: origami
 * @date: {2021-08-06}
 * @info:
 **/
public class SelectPlaceAdapter extends RecyclerView.Adapter<SelectPlaceAdapter.ViewHolder> {

    public static class AdapterData{
        public String text;
        public String image;
        public int num;
    }
    final OriImageSelect context;
    final List<AdapterData> dates;

    public SelectPlaceAdapter(OriImageSelect context, List<AdapterData> dates) {
        this.context = context;
        this.dates = dates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout._adapter_ori_select_place, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.closeSelect();
                int position = viewHolder.getAdapterPosition();
                context.refreshBySelect(dates.get(position).text, viewHolder.textView.getText().toString());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdapterData adapterData = dates.get(position);
        Glide.with(context).load(adapterData.image).into(holder.imageView);
        String name;
        if(TextUtils.isEmpty(adapterData.text)){
            name = "最近图片";
        }else if(adapterData.text.equalsIgnoreCase("Screenshots")){
            name = "屏幕截图";
        }else if(adapterData.text.equalsIgnoreCase("Camera")){
            name = "相机";
        }else if(adapterData.text.equalsIgnoreCase("QQ_Images")){
            name = "QQ图片";
        }else if(adapterData.text.equalsIgnoreCase("WeiXin")){
            name = "微信图片";
        }else {
            name = adapterData.text;
        }
        holder.numView.setText(String.format("（%s）", adapterData.num));
        holder.textView.setText(name);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView, numView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.__ori_show_image);
            textView = itemView.findViewById(R.id.__ori_show_text);
            numView = itemView.findViewById(R.id.__ori_show_num);
        }
    }

}
