package com.origami.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author origami
 * @date 2020/12/18 0018
 * @description:
 **/
public class ProviderImageUtils {

    public static class ResultData{
        public List<String> keys = new ArrayList<>();
        public Map<String, List<String>> dates;
    }


    public static int ONE_PAGE_NUM = 20;

    public static ResultData getImagesPathList(Context context, String[] supportType){
        String selection = null;
        if(supportType != null && supportType.length != 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < supportType.length; i++) {
                builder.append(MediaStore.Images.Media.MIME_TYPE)
                        .append("=? ");
                if(i != supportType.length - 1){ builder.append("or "); }
            }
            selection = builder.toString();
        }
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                supportType,
                MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        );
        ResultData resultData = new ResultData();
        Map<String, List<String>> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        map.put("",list);
        resultData.keys.add("");
        while (cursor.moveToNext()){
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            list.add(path);
            String name = Objects.requireNonNull(new File(path).getParentFile()).getName();
            if(!map.containsKey(name)){
                List<String> listChild = new ArrayList<>();
                resultData.keys.add(name);
                map.put(name,listChild);
            }
            Objects.requireNonNull(map.get(name)).add(path);
        }
        cursor.close();
        resultData.dates = map;
        return resultData;
    }


    public static List<String> getImagesPathByPage(Context context, int page){
        int start = (page - 1) * ONE_PAGE_NUM;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Images.Media.DATE_MODIFIED + " DESC LIMIT " + start + "," + ONE_PAGE_NUM
        );
        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()){
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            list.add(path);
        }
        cursor.close();
        return list;
    }

}
