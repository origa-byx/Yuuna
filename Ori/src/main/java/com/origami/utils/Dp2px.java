package com.origami.utils;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * @author xiao gan
 * @date 2020/12/4
 * @description:
 **/
public class Dp2px {

    public static int dp2px(float dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
