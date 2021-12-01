package com.safone.yuuna.adapter.bean;

/**
 * @by: origami
 * @date: {2021-08-18}
 * @info:
 **/
public class MainAdapterBean implements Cloneable {

    public boolean linkFlag = false;

    public String name;
    public String ip;
    public int port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public MainAdapterBean getClone(){
        try {
            return (MainAdapterBean) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
