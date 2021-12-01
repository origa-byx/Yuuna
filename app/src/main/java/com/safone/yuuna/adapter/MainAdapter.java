package com.safone.yuuna.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.origami.origami.base.toast.ToastMsg;
import com.origami.utils.Ori;
import com.safone.yuuna.R;
import com.safone.yuuna.adapter.bean.MainAdapterBean;
import com.safone.yuuna.p2p.UdpP2P;

import java.util.List;

/**
 * @by: origami
 * @date: {2021-08-18}
 * @info:
 **/
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private final List<MainAdapterBean> dates;
    private Context context;

    public MainAdapter(Context context,List<MainAdapterBean> dates) {
        this.dates = dates;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_main, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                MainAdapterBean bean = dates.get(position);
                if(((TextView) v).getText().toString().contains("send")){
                    if(bean.name.equals(UdpP2P.myNodeName)){ ToastMsg.show_msg("不能发送到自己", false, 2000); return; }
                    UdpP2P.getInstance().sendUdpData(bean.ip, bean.port,UdpP2P.myNodeName + ":" + Ori.getRandomString(6));
                }else {
                    if(bean.name.equals(UdpP2P.myNodeName)){ ToastMsg.show_msg("不能链接到自己", false, 2000); return; }
                    MainAdapterBean clone = bean.getClone();
                    if(clone != null){
                        UdpP2P.getInstance().sendUdpData("link " + UdpP2P.myNodeName + " " + bean.name + " tag");
                        UdpP2P.getInstance().addLinkNode(clone);
                        bean.linkFlag = true;
                        notifyItemChanged(position);
                    }
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainAdapterBean bean = dates.get(position);
        holder.nodeName.setText(bean.name);
        holder.ipAndPort.setText(String.format("%s:%s", bean.ip, bean.port));
        if(bean.linkFlag){
            holder.adp_main_index.setBackground(context.getResources().getDrawable(R.color._ori_green));
            holder.button.setText("send msg");
        }else {
            holder.adp_main_index.setBackground(context.getResources().getDrawable(R.color._ori_white));
            holder.button.setText("link to him");
        }
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout adp_main_index;
        TextView nodeName, ipAndPort, button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            adp_main_index = itemView.findViewById(R.id.adp_main_index);
            nodeName = itemView.findViewById(R.id.adp_main_nodeName);
            ipAndPort = itemView.findViewById(R.id.adp_main_ipAndPort);
            button = itemView.findViewById(R.id.adp_main_link);
        }

    }

}
