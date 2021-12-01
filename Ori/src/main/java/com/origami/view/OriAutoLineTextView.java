package com.origami.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * @by: origami
 * @date: {2021-10-15}
 * @info:   暂时不支持 emoji 和 {@link android.text.SpannableString}
 **/
public class OriAutoLineTextView extends AppCompatTextView {

    private static final int pd = 10;
    private float startX, startY;
    private String d_txt;

    public OriAutoLineTextView(Context context) {
        super(context);
    }

    public OriAutoLineTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OriAutoLineTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        Drawable background = getBackground();
        if(background != null){
            canvas.save();
            background.draw(canvas);
            canvas.restore();
        }
        if(getPaddingBottom() != 0) {
            canvas.clipRect(new Rect(0, getHeight() - getPaddingBottom(), getWidth(), getHeight()), Region.Op.DIFFERENCE);
        }
        TextPaint mPaint = getPaint();
        Paint.FontMetrics fm = mPaint.getFontMetrics();

        float baseline = fm.descent - fm.ascent;
        d_txt = getText().toString();

        startX = getPaddingStart() + getPaddingLeft() + pd;
        startY = baseline + getPaddingTop();
        int showW = getWidth() - getPaddingLeft() - getPaddingRight() - getPaddingStart() - getPaddingEnd() - pd * 2;
        do {
            drawMyText(canvas, mPaint, showW);
            startY += baseline + fm.leading; //添加字体行间距
        }while (!TextUtils.isEmpty(d_txt));
    }

    private void drawMyText(Canvas canvas, TextPaint paint, int maxW){
        int breakText = paint.breakText(d_txt, true, maxW, null);
        String drawText = d_txt.substring(0, breakText);
        if(drawText.contains("\n")){
            String[] split = d_txt.split("\n", 2);
            drawText = split[0];
            d_txt = split[1];
            canvas.drawText(drawText, startX, startY, paint);
            return;
        }
        float v = Math.max(maxW - paint.measureText(drawText), 0) / (float) (drawText.length() - 1);
        if(breakText == d_txt.length()){
            d_txt = "";
            canvas.drawText(drawText, startX, startY, paint);
        }else {
            float sX = startX;
            for (int i = 0; i < drawText.length(); i++) {
                String charAt = String.valueOf(drawText.charAt(i));
                canvas.drawText(charAt, sX, startY, paint);
                sX += paint.measureText(charAt) + v;
            }
            d_txt = d_txt.substring(breakText);
        }
    }

    /**
     * 自动分割文本
     *
     * @param content 需要分割的文本
     * @param p       画笔，用来根据字体测量文本的宽度
     * @param width   最大的可显示像素（一般为控件的宽度）
     * @return 一个字符串数组，保存每行的文本
     */
    private String[] autoSplit(String content, Paint p, float width) {
        int length = content.length();
        float textWidth = p.measureText(content);
        if (textWidth <= width) {
            return new String[]{content};
        }

        int start = 0, end = 1, i = 0;
        int lines = (int) Math.ceil(textWidth / width); //计算行数
        String[] lineTexts = new String[lines];
        while (start < length) {
            if (p.measureText(content, start, end) > width) { //文本宽度超出控件宽度时
                lineTexts[i++] = (String) content.subSequence(start, end);
                start = end;
            }
            if (end == length) { //不足一行的文本
                lineTexts[i] = (String) content.subSequence(start, end);
                break;
            }
            end += 1;
        }
        return lineTexts;
    }
}
