package com.origami.utils;

import android.text.TextUtils;

/**
 * @by: origami
 * @date: {2021/5/6}
 * @info:
 **/
public class XmlUtil {

    private final String rootPoint;

    private final StringBuilder builder;

    private XmlUtil(String rootPoint){
        if(TextUtils.isEmpty(rootPoint)) {
            builder = new StringBuilder("");
        }else {
            builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<").append(rootPoint).append(">\n");
        }
        this.rootPoint = rootPoint;
    }

    public static XmlUtil getInstance(){
        return new XmlUtil(null);
    }

    public static XmlUtil getInstance(String rootPoint){
        return new XmlUtil(rootPoint);
    }

    public XmlUtil add_key_value(String key, Object value){
        builder
                .append("<").append(key).append(">")
                .append(value)
                .append("</").append(key).append(">\n");
        return this;
    }

    public XmlUtil add_key_xmlValue(String key, XmlUtil value){
        builder
                .append("<").append(key).append(">\n")
                .append(value.getXML())
                .append("</").append(key).append(">\n");
        return this;
    }

    public String getXML(){
        if(!TextUtils.isEmpty(rootPoint)){
            builder.append("</").append(rootPoint).append(">");
        }
        return builder.toString();
    }

}
