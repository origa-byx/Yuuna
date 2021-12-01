package com.origami.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author xiao gan
 * @date 2020/12/3
 * @description:
 **/
public class Time2String {

    private final static String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    public static String longToText(long time,boolean add){
        if(add){ time = time * 1000; }
        return timeToText(new Date(time));
    }

    public static String stringToText(String time,String format){
        if(TextUtils.isEmpty(format)){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            Date date = simpleDateFormat.parse(time);
            return timeToText(date);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static String timeToText(Date date){
        Date date1 = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");
       float mill = (float)(date1.getTime() - date.getTime());
       if(mill>=7*24*60*60*1000){
           return simpleDateFormat.format(date);
       }else if(mill>=48*60*60*1000 || mill/(60*60*1000)>=24){
           return weekDays[getWeekNum(date)];
       }else if(mill/(60*60*1000)<24 && getWeekNum(date)!=getWeekNum(date1)){
           return "昨天";
       }else
//           if(mill/(60*60*1000)>=7)
       {
           String format = simpleDateFormat1.format(date);
           Integer value = Integer.valueOf(format.split(":")[0]);
           String font = "上午";
           if(value > 12){
               font = "下午";
               value -= 12;
               return font + value + ":" + format.split(":")[1];
           }
           return font + format;
       }
//       else if(mill/(60*60*1000)>=1){
//           int h =(int) (mill/(60*60*1000));
//           return h + "小时前";
//       }else if(mill/(60*1000)>=3){
//           int m =(int) (mill/(60*1000));
//           return m + "分钟前";
//       }else{
//           return "刚刚";
//       }
    }

    public static int getWeekNum(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if(w < 0){w = 0;}
        return w;
    }

    public static String getWeekName(Date date){
        return weekDays[getWeekNum(date)];
    }


}
