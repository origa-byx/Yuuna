package com.origami.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.origami.origami.R;
import com.origami.utils.Dp2px;

/**
 * @by: origami
 * @date: {2021-08-03}
 * @info:
 **/
public class YuunaTextView extends View {

    private String text;
    private int offset_b = 20;
    private int radius = Dp2px.dp2px(50);
    private int yuuna_text_size = 16;

    private Path mPath, mBackPath;
    private Paint mPaint;

    public YuunaTextView(Context context) {
        this(context, null);
    }

    public YuunaTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YuunaTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.YuunaTextView);
        text = typedArray.getString(R.styleable.YuunaTextView_yuuna_text);
        radius = typedArray.getDimensionPixelSize(R.styleable.YuunaTextView_yuuna_radius, radius);
        yuuna_text_size = typedArray.getDimensionPixelSize(R.styleable.YuunaTextView_yuuna_text_size, yuuna_text_size);
        typedArray.recycle();
        initPath();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(yuuna_text_size);
        mPaint.setColor(Color.parseColor("#ffbc32"));
//        mPaint.setTextAlign(Paint.Align.RIGHT);
    }

    private void initPath(){
        if(mPath == null){ mPath = new Path(); }else { mPath.reset(); }
        if(mBackPath == null){ mBackPath = new Path(); }else { mBackPath.reset(); }
        int text_radius = radius + yuuna_text_size;
        mPath.addArc(-text_radius , -text_radius , text_radius, text_radius, 90, -90);
        mBackPath.addCircle(0, 0, radius, Path.Direction.CCW);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        int radius_b = radius + offset_b + yuuna_text_size;
        mPaint.setColor(Color.parseColor("#ffbc32"));
        canvas.clipPath(mBackPath, Region.Op.DIFFERENCE);
        canvas.drawCircle(0, 0, radius_b, mPaint);
        mPaint.setColor(Color.parseColor("#f4433c"));
        canvas.drawTextOnPath(text, mPath, 0, 0, mPaint);
//        mPath.reset();
//        int text_radius = radius + yuuna_text_size + yuuna_text_size;
//        mPath.addArc(-text_radius , -text_radius , text_radius, text_radius, 90, -90);
//        canvas.drawTextOnPath(text, mPath, 0, 0, mPaint);
    }

}
