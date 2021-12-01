package com.origami.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.origami.view.inter.OriViewGroup;

/**
 * @by: origami
 * @date: {2021-08-06}
 * @info:
 * @deprecated {@link OriConstraintLayout} or {@link androidx.constraintlayout.widget.ConstraintLayout}
 **/
public class OriRectRelativeLayout extends OriRelativeLayout {

    public OriRectRelativeLayout(Context context) {
        this(context, null);
    }

    public OriRectRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OriRectRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }


}
