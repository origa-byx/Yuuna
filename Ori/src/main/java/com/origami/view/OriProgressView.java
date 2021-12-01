package com.origami.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

import com.origami.origami.R;


/**
 * @by: origami
 * @date: {2021-05-17}
 * @info: 动态进度条带显示参数
 **/
public class OriProgressView extends View {

    private float pro_value = 0;
    private final Paint mPaint;
    private int color_back = Color.GRAY, color_pro = Color.BLUE;

    public OriProgressView(Context context) {
        this(context, null);
    }

    public OriProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OriProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OriProgressView);
        color_back = typedArray.getColor(R.styleable.OriProgressView_color_back, color_back);
        color_pro = typedArray.getColor(R.styleable.OriProgressView_color_pro, color_pro);
        int text_size = typedArray.getDimensionPixelSize(R.styleable.OriProgressView_pro_text_size, 0);
        typedArray.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(text_size);
        mPaint.setTextSize(text_size);
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        RectF rectF = new RectF(getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        int r = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        path.addRoundRect(rectF,r,r, Path.Direction.CCW);
        canvas.drawRoundRect(rectF, r, r, mPaint);
        canvas.save();
        canvas.clipPath(path);
        mPaint.setColor(color_back);
        float pro_end = (getWidth() - getPaddingRight()) * pro_value;
        RectF rectF_pro = new RectF(getPaddingLeft(),
                getPaddingTop(),
                Math.max(pro_end, getPaddingLeft()),
                getHeight() - getPaddingBottom());
        mPaint.setColor(color_pro);
        canvas.drawRoundRect(rectF_pro,r,r,mPaint);
        mPaint.setColor(Color.WHITE);
        String pro_txt = (int) (pro_value * 100) + "%";
        float start_x = Math.max(pro_end - mPaint.measureText(pro_txt) - 10, 0);
        canvas.drawText(pro_txt, start_x, (int) (getHeight() / 2) + getBaseline(mPaint), mPaint);
        canvas.restore();
    }

    public void setPro_value(@FloatRange(from = 0,to = 1) float pro_value) {
        this.pro_value = pro_value;
        postInvalidate();
    }

    public static float getBaseline(Paint p) {
        Paint.FontMetrics fontMetrics = p.getFontMetrics();
        return (fontMetrics.descent - fontMetrics.ascent) / 2 -fontMetrics.descent;
    }
}
