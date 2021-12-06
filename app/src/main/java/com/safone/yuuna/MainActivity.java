package com.safone.yuuna;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.ori.origami.recoder.OriAudio;

import com.origami.origami.base.act.AnnotationActivity;
import com.origami.origami.base.annotation.BClick;
import com.origami.origami.base.annotation.BContentView;
import com.origami.origami.base.annotation.BView;

import com.origami.origami.base.callback.RequestPermissionNext;
import com.origami.origami.base.event.OriEventBus;
import com.origami.origami.base.toast.ToastMsg;
import com.origami.utils.Dp2px;
import com.origami.utils.Ori;
import com.origami.utils.StatusUtils;
import com.safone.yuuna.adapter.MainAdapter;
import com.safone.yuuna.adapter.bean.MainAdapterBean;
import com.safone.yuuna.p2p.UdpP2P;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("NonConstantResourceId")
@BContentView(R.layout.activity_main)
public class MainActivity extends AnnotationActivity {

    @BView(R.id.act_main_recycler)
    RecyclerView recyclerView;

    MainAdapter adapter;

    private final List<MainAdapterBean> dates = new ArrayList<>();

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        adapter = new MainAdapter(this, dates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = Dp2px.dp2px(8);
            }
        });
        recyclerView.setAdapter(adapter);
        OriEventBus.registerEvent("list", new OriEventBus.Event(this, OriEventBus.RunThread.MAIN_UI) {
            @Override
            public void postEvent(Object... args) {
                if(args != null && args.length > 0){
                    String arg = (String) args[0];
                    try {
                        Map<String, MainAdapterBean> map = new HashMap<>();
                        for (MainAdapterBean date : dates) {
                            map.put(date.name, date);
                        }
                        JSONArray jsonObject = new JSONArray(arg);
                        for(int i = 0; i < jsonObject.length(); i++){
                            JSONObject object = jsonObject.getJSONObject(i);
                            String name = object.getString("name");
                            if(map.containsKey(name)){
                                MainAdapterBean bean = map.get(name);
                                bean.ip = object.getString("ip");
                                bean.port = object.getInt("port");
                                continue;
                            }
                            MainAdapterBean bean = new MainAdapterBean();
                            bean.name = name;
                            bean.ip = object.getString("ip");
                            bean.port = object.getInt("port");
                            dates.add(bean);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastMsg.show_msg("json 错误！", false , 1500);
                    }
                }
            }
        });

        OriEventBus.registerEvent("link_me", new OriEventBus.Event(this, OriEventBus.RunThread.MAIN_UI) {
            @Override
            public void postEvent(Object... args) {
                if(args != null && args.length > 0){
                    int j = -1;
                    for (int i = 0;i < dates.size(); i++) {
                        if(dates.get(i).name.equals(args[0])){
                            j = i;break;
                        }
                    }
                    if(j != -1){
                        dates.get(j).linkFlag = true;
                        adapter.notifyItemChanged(j);
                    }
                }
            }
        });

//        UdpP2P.getInstance().initAndRun();
    }

    @BClick(R.id.b1)
    public void b1(){
        UdpP2P.getInstance().clearLinkNode();
        for (MainAdapterBean date : dates) { date.linkFlag = false; }
        adapter.notifyDataSetChanged();
    }

    @BClick(R.id.b2)
    public void b2(){//7377
        UdpP2P.getInstance().sendUdpData("add " + UdpP2P.myNodeName + " tag");
    }

    @Override
    protected void setStatusBar() {
        StatusUtils.setStatusBarResource(this, R.drawable._ori_status_color);
    }

    boolean click = false;
    @BClick(R.id.testRe)
    public void testRe(){
        if(click){
            OriAudio.instance().stopRecord();
            OriAudio.instance().release();
            click = false;
        }else {
            checkPermissionAndThen(new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, new RequestPermissionNext() {
                @Override
                public void next() {
                    OriAudio.instance().init(false);
                    OriAudio.instance().initMp3encode(8000, 2, 16000, 7);
                    OriAudio.instance().mp3encode()
                            .setId3V1Tag("测试title", "ori", "ori专辑", "2021");
                    OriAudio.instance().startRecord(Ori.getSaveFilePath(MainActivity.this) + "test.mp3");
                    click = true;
                }

                @Override
                public void failed() {

                }
            });
        }
    }

}