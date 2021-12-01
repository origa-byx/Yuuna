package com.safone.yuuna.p2p;

import android.util.Log;

import com.origami.origami.base.event.OriEventBus;
import com.origami.origami.base.toast.ToastMsg;
import com.safone.yuuna.adapter.bean.MainAdapterBean;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @by: origami
 * @date: {2021-08-18}
 * @info:
 **/
public final class UdpP2P implements Runnable {

    public static String myNodeName = "node0";
    final Object lock = new Object();

    private static final String ServiceIp = "47.106.97.211";
    private static final int ServicePort = 56612;

    private static final int localPort = 51662;

    private static DatagramSocket udpSocket;

    private static UdpP2P instance;

    final List<MainAdapterBean> linkList = new ArrayList<>();

    public static synchronized UdpP2P getInstance(){
        if(instance == null){ instance = new UdpP2P(); }
        return instance;
    }

    private UdpP2P(){ }

    public void initAndRun(){
        if(udpSocket != null){ return; }
        try {
            udpSocket = new DatagramSocket(localPort);
            udpSocket.setSoTimeout(15000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        new Thread(this).start();

        //保证 NAT 记录不被清理定期发送数据包
        new Thread(()->{
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (linkList.isEmpty()) {
                            return;
                        }
                        for (MainAdapterBean bean : linkList) {
                            sendUdpData(bean.ip, bean.port, "heart");
                        }
                    }
                }
            },1000, 3000);
        }).start();

        sendUdpData("add " + UdpP2P.myNodeName + " tag");
    }

    @Override
    public void run() {
        while (udpSocket != null){
            try {
                byte[] pack = new byte[1024];
                DatagramPacket packet = new DatagramPacket(pack, pack.length);
                udpSocket.receive(packet);
                String msg = new String(pack, 0, packet.getLength());
                Log.e("UDP-MSG", msg);
                if(packet.getAddress().getHostAddress().contains(ServiceIp)){
                    if(msg.startsWith("list")){ OriEventBus.triggerEvent("list", msg.split("->")[1]); continue; }
                    if(msg.startsWith("link_to")){//link 到其他客户端节点 NAT 公网地址上
                        String[] split = msg.split(" +");//"link_to " + IpAndPort_me.ip + " " + IpAndPort_me.port + " " + name;
                        sendUdpData(split[1], Integer.parseInt(split[2]), "heart");
                        MainAdapterBean bean = new MainAdapterBean();
                        bean.ip = split[1];
                        bean.port = Integer.parseInt(split[2]);
                        bean.name = split[3];
                        addLinkNode(bean);
                        ToastMsg.show_msg("来自其他节点的尝试链接...", 2000);
                        OriEventBus.triggerEvent("link_me", split[3]);
                        continue;
                    }
                    if(msg.startsWith("add_ok")){ ToastMsg.show_msg("注册成功", 2000); continue; }
                    if(msg.startsWith("link_ok")){ ToastMsg.show_msg("链接指令中转成功", 2000); continue; }
                }else if(!msg.contains("heart")) { ToastMsg.show_msg(msg, 3000); }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("UDP","wait...");
            }
        }
        Log.e("ORI","END!");
    }

    public void addLinkNode(MainAdapterBean bean){
        synchronized (lock){ linkList.add(bean); }
    }

    public void clearLinkNode(){
        synchronized (lock){ linkList.clear(); }
    }

    /**
     * 向服务器发送 UDP 数据包
     * @param data 待发送数据
     * @return
     */
    public int sendUdpData(final String data){
        return sendUdpData(ServiceIp, ServicePort, data);
    }

    /**
     * 向指定地址端口发送 UDP 数据包
     * @param data 待发送数据
     * @return 0 成功，1 失败，2 线程同步失败
     */
    public int sendUdpData(final String ip, final int port, final String data){
        Log.e("ORI", ip + ":" + port + "->" + data);
        final AtomicInteger ret = new AtomicInteger(0);
        Thread thread_send = new Thread(() -> {
            try {
                DatagramPacket packet = new DatagramPacket(
                        data.getBytes(),
                        data.getBytes().length,
                        InetAddress.getByName(ip),
                        port);
                udpSocket.send(packet);
            }catch (IOException e){
                e.printStackTrace();
                ret.set(1);
            }
        });
        thread_send.start();
        try {
            thread_send.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            ret.set(2);
        }
        return ret.get();
    }


}
