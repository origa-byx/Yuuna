package com.origami.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @author xiao gan
 * @date 2020/12/9 0009
 * @description: android:windowSoftInputMode="adjustResize|stateAlwaysHidden"
 **/
public class SoftInputUtil {

    private int softInputHeight = 0;
    private boolean softInputHeightChanged = false;

    private boolean isNavigationBarShow = false;
    private int navigationHeight = 0;

    private View currentView;

    private View[] anyView;
    private ISoftInputChanged listener;
    private boolean isSoftInputShowing = false;

    private View.OnFocusChangeListener sFocusChangeListener;

    private SoftInputUtil() { }

    public static SoftInputUtil Build(){
        return new SoftInputUtil();
    }

    public interface ISoftInputChanged {
        void onChanged(boolean isSoftInputShow, int softInputHeight, int viewOffset);
    }

    public void attachSoftInput(View moveView,final View... anyView) {
        attachSoftInput(new ISoftInputChanged() {
            @Override
            public void onChanged(boolean isSoftInputShow, int softInputHeight, int viewOffset) {
                if(isSoftInputShow) {
                    moveView.setTranslationY(-viewOffset);
                }else {
                    moveView.setTranslationY(0);
                }
            }
        },anyView);
    }

    public void attachSoftInput( final ISoftInputChanged listener, final View... anyView) {
        if (anyView == null || listener == null || anyView.length == 0)
            return;

        if(anyView.length > 1){
            sFocusChangeListener = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        currentView = v;
                    }
                }
            };
            for (View view : anyView) {
                view.setOnFocusChangeListener(sFocusChangeListener);
            }
        }

        //???View
        final View rootView = anyView[0].getRootView();
        if (rootView == null)
            return;

        navigationHeight = getNavigationBarHeight(anyView[0].getContext());

        //anyView????????????????????????View????????????????????????????????????View
        this.currentView = anyView[0];
        this.anyView = anyView;
        this.listener = listener;

        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //??????Activity????????????????????????????????????
                int rootHeight = rootView.getHeight();
                Rect rect = new Rect();
                //??????????????????????????????????????????????????????????????????????????????????????????
                rootView.getWindowVisibleDisplayFrame(rect);

                if (rootHeight - rect.bottom == navigationHeight) {
                    //?????????????????????????????????????????????????????????????????????????????????????????????
                    isNavigationBarShow = true;
                } else if (rootHeight - rect.bottom == 0) {
                    //?????????????????????????????????????????????????????????????????????
                    isNavigationBarShow = false;
                }

                //cal softInput height
                boolean isSoftInputShow = false;
                int softInputHeight = 0;
                //???????????????????????????????????????????????????
                int mutableHeight = isNavigationBarShow ? navigationHeight : 0;
                if (rootHeight - mutableHeight > rect.bottom) {
                    //??????????????????????????????????????????????????????????????????????????????????????????
                    isSoftInputShow = true;
                    //????????????
                    softInputHeight = rootHeight - mutableHeight - rect.bottom;
                    if (SoftInputUtil.this.softInputHeight != softInputHeight) {
                        softInputHeightChanged = true;
                        SoftInputUtil.this.softInputHeight = softInputHeight;
                    } else {
                        softInputHeightChanged = false;
                    }
                }

                //????????????View???????????????
                int[] location = new int[2];
                currentView.getLocationOnScreen(location);

                //??????1?????????????????????????????????????????????????????????
                //??????2??????????????????????????????????????????????????????????????????
                if (isSoftInputShowing != isSoftInputShow || (isSoftInputShow && softInputHeightChanged)) {
                    if (listener != null) {
                        //?????????????????????View????????????????????????
                        //??????????????????????????????????????????(0,0)????????????
                        listener.onChanged(isSoftInputShow, softInputHeight, location[1] + currentView.getHeight() - rect.bottom);
                    }

                    isSoftInputShowing = isSoftInputShow;
                }
            }
        });
    }

    public boolean isSoftInputShowing() {
        return isSoftInputShowing;
    }

    //***************STATIC METHOD******************

    public static int getNavigationBarHeight(Context context) {
        if (context == null)
            return 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static void showSoftInput(View view) {
        if (view == null)
            return;
        InputMethodManager inputMethodManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(view, 0);
        }
    }

    public static void hideSoftInput(View view) {
        if (view == null)
            return;
        InputMethodManager inputMethodManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
