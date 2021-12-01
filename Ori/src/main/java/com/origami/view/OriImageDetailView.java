package com.origami.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Environment;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntRange;

import com.origami.utils.UiThreadUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

/**
 * @by: origami
 * @date: {2021-05-27}
 * @info:
 **/
public class OriImageDetailView extends View {

    private final int handler_taken = 1216;
    private float tanH_sc = 50f;

    private final static String TAG = "ImageDetailView";

    private float mScale = 1f;//放缩结果参数
    private final float[] mTranslation = new float[]{0f,0f};//偏移参数

    private float centerTW = 0;//初始横向偏移 -> 横向居中
    private float centerTH = 0;//初始竖向偏移 -> 竖向居中

    private final float[] mTRange = new float[]{0f,0f};//最大偏移

    private Bitmap mBitmap;//原始位图

    private boolean canMove = true;//长按事件后不允许移动
    private boolean simPoint = true;//是否全程单点触控, 判断触发单点事件(长按 or 点击)

    private final float[] mR = new float[]{0, 0};//{x, y}显示有效矩形 屏幕中心 x*2, y*2 大小矩形
    private float minScale = 1f;//最小放缩, 根据位图大小确定

    private final int mustPx = 5;//偏移放缩保留像素,预防紧贴情况下强转int可能产生的裁剪时数组越界

    private final SparseArray<Point> pointSparseArray = new SparseArray<>();
    private int markX = 0;//双指放缩参数

    private OnLongClickListener mLongClickListener;

    public OriImageDetailView(Context context) {
        this(context,null);
    }

    public OriImageDetailView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public OriImageDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param event
     * @return
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        switch (actionMasked){
            case MotionEvent.ACTION_POINTER_DOWN:
                setSimPointFalse();
                int index_pointer = event.getActionIndex();
                pointSparseArray.put(event.getPointerId(index_pointer), new Point(
                        (int) event.getX(index_pointer),
                        (int) event.getY(index_pointer) ));
                return true;
            case MotionEvent.ACTION_DOWN:
                simPoint = true;
                canMove = true;
                int index = event.getActionIndex();
                pointSparseArray.put(event.getPointerId(index), new Point(
                        (int) event.getX(index),
                        (int) event.getY(index) ));
                if(mLongClickListener != null && getHandler() != null) {
                    Message obtain = Message.obtain(getHandler(), () -> {
                        if (simPoint) {
                            canMove = false;
                            simPoint = false;// 这里是为了防止单击和长按一起触发了
//                            UiThreadUtil.getInstance().runOnUiThread(()->{
                            mLongClickListener.onLongClick(OriImageDetailView.this);
//                            });
                        }
                    });
                    obtain.obj = handler_taken;
                    getHandler().sendMessageDelayed(obtain, 1200);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if(!canMove){ return true; }
                int size = event.getPointerCount();
                if(size == 1) {//单指移动
                    int key_1 = event.getPointerId(0);
                    Point point = pointSparseArray.get(key_1);
                    float moveX = event.getX(0) - point.x;
                    float moveY = event.getY(0) - point.y;
                    if(Math.abs(moveX) > 3 || Math.abs(moveY) > 3){
                        setSimPointFalse();
                        mTranslation[0] += moveX / mScale;
                        mTranslation[1] += moveY / mScale;
                        point.x = (int) event.getX(0);
                        point.y = (int) event.getY(0);
                        pointSparseArray.put(key_1,point);
//                        Log.e("ORI","mTranslation -> " + mTranslation[0] + "  |  " + mTranslation[1]);
                        invalidate();
                        return true;
                    }
                }else {
                    setSimPointFalse();
                    if (size == 2) {//双指缩放
                        int key_0 = event.getPointerId(0);
                        int key_1 = event.getPointerId(1);
                        Point point_0 = pointSparseArray.get(key_0);
                        Point point_1 = pointSparseArray.get(key_1);
                        Point point_now_0 = new Point((int) event.getX(0), (int) event.getY(0));
                        Point point_now_1 = new Point((int) event.getX(1), (int) event.getY(1));
                        float distance = getDistance(point_now_0, point_now_1) - getDistance(point_0, point_1);
                        if (Math.abs(distance) > 5) {
                            if (distance > 0) {//放大
                                markX++;
                            } else {//缩小
                                if (mScale == minScale) {
                                    pointSparseArray.put(key_0, point_now_0);
                                    pointSparseArray.put(key_1, point_now_1);
                                    return true;
                                }
                                markX--;
                            }
                            mScale = (float) Math.tanh((double) markX / tanH_sc) + 1;
                            pointSparseArray.put(key_0, point_now_0);
                            pointSparseArray.put(key_1, point_now_1);
                            if (mScale < minScale) {
                                mScale = minScale;
                                markX++;
                            }
                            invalidate();
                            return true;
                        }
                    } else if (size >= 3) {//TODO 三指旋转 (斜率的变化 反应到 旋转角度上) --> 只关心前三根手指
                        int key_0 = event.getPointerId(0);
                        int key_1 = event.getPointerId(1);
                        int key_2 = event.getPointerId(2);
                        pointSparseArray.put(key_0, new Point((int) event.getX(0), (int) event.getY(0)));
                        pointSparseArray.put(key_1, new Point((int) event.getX(1), (int) event.getY(1)));
                        pointSparseArray.put(key_2, new Point((int) event.getX(2), (int) event.getY(2)));
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                canMove = true;
                if(simPoint) { setSimPointFalse(); performClick(); }
                pointSparseArray.clear();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                pointSparseArray.remove(event.getPointerId(event.getActionIndex()));
                return true;
        }
        return true;
//        return super.onTouchEvent(event);
    }


    private void setSimPointFalse(){
        simPoint = false;
        if(getHandler() != null){
            getHandler().removeCallbacksAndMessages(handler_taken);
        }
    }

    /**
     * 设置此参数
     * @param tanH_sc 放缩的tanH函数中的斜率控制参数，影响每次放缩的实际大小，越小放缩越快，参考值 90
     */
    public void setTanH_sc(float tanH_sc) {
        this.tanH_sc = tanH_sc;
    }

    /**
     * 强制不能{@link ViewGroup.LayoutParams#WRAP_CONTENT}
     *      替换成 MATCH_PARENT
     **/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeW = MeasureSpec.getMode(widthMeasureSpec);
        int modeH = MeasureSpec.getMode(heightMeasureSpec);
        if(modeW != MeasureSpec.EXACTLY){ widthMeasureSpec
                = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.EXACTLY); }
        if(modeH != MeasureSpec.EXACTLY){ heightMeasureSpec
                = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec),MeasureSpec.EXACTLY); }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mR[0] = 0;
        mR[1] = 0;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener mLongClickListener) {
        this.mLongClickListener = mLongClickListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mBitmap != null) {
            canvas.save();
            if(Math.abs(mTranslation[0] * mScale) > (mTRange[0] * mScale - mR[0] - mustPx)) {
                if(mTranslation[0] < 0) {
                    mTranslation[0] = - ( mTRange[0] - (mR[0] + mustPx) / mScale );
                }else {
                    mTranslation[0] = mTRange[0] - (mR[0] + mustPx) / mScale;
                }
            }
            if(Math.abs(mTranslation[1] * mScale) > (mTRange[1] * mScale - mR[1] - mustPx)) {
                if(mTranslation[1] < 0) {
                    mTranslation[1] = - ( mTRange[1] - (mR[1] + mustPx) / mScale );
                }else {
                    mTranslation[1] = mTRange[1] - (mR[1] + mustPx) / mScale;
                }
            }
            canvas.scale(mScale, mScale,
                    (float) getWidth() / 2 + (mTranslation[0] + centerTW) * mScale,
                    (float) getHeight() / 2 + (mTranslation[1] + centerTH) * mScale);
            canvas.translate((mTranslation[0] + centerTW) * mScale, (mTranslation[1] + centerTH) * mScale);
            canvas.drawBitmap(mBitmap, 0,0, null);
            canvas.restore();
        }
    }

    /**
     * 设置待剪切图片
     */
    public void setImagePath(String localPath){
        setImageBitmap(BitmapFactory.decodeFile(localPath));
    }

    public void setImageBitmap(Bitmap bm) {
        post(new Runnable() {
            @Override
            public void run() {
                Matrix matrix = new Matrix();
                float[] scale = new float[]{1f, 1f};
                scale[0] = (float) getW() / (float) bm.getWidth();
                scale[1] = (float) getH() / (float) bm.getHeight();
                float s = Math.min(scale[0],scale[1]);
                matrix.postScale(s,s);
                mBitmap = Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight(),matrix,true);
                centerTW = (float) (getW() - mBitmap.getWidth()) / 2;
                centerTH = (float) (getH() - mBitmap.getHeight()) / 2;
                mTRange[0] = (float) mBitmap.getWidth() / 2;
                mTRange[1] = (float) mBitmap.getHeight() / 2;
                minScale = 0.8f;
                postInvalidate();
            }
        });
    }

    /**
     * 保存位图
     * @param path 例如: "test/image/head"
     * @param isRandom 是否随机命名图片
     * @return null ：保存失败
     */
    public String saveBitmap(String path, boolean isRandom) {
        String savePath;
        File filePic;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            savePath = Environment.getExternalStorageDirectory().getPath() + File.separator + path + File.separator;
        } else {
            Log.e("ORI", "saveBitmap : sdcard not mounted");
            return null;
        }
        if(isRandom){
            savePath += (getRandomString(14) + ".jpg");
        }else {
            savePath += "_headImage.jpg";
        }
        try {
            filePic = new File(savePath);
            if (!filePic.exists()) {
                if(filePic.getParentFile() != null) {
                    filePic.getParentFile().mkdirs();
                }
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("ORI", "saveBitmap : " + e.getMessage());
            return null;
        }
        return savePath;
    }


    public String getRandomString(@IntRange(from = 8) int length){
        String ran = "abcdefghijkmlnopqrstuvwxyz123456789";
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        Calendar date = Calendar.getInstance();
        builder.append(date.get(Calendar.YEAR))
                .append(date.get(Calendar.MONTH) < 10 ? "0" + date.get(Calendar.MONTH) : date.get(Calendar.MONTH))
                .append(date.get(Calendar.DATE) < 10 ? "0" + date.get(Calendar.DATE) : date.get(Calendar.DATE));
        for (int i = 0; i < (length - 8); i++) {
            builder.append(ran.charAt(random.nextInt(ran.length())));
        }
        return builder.toString();
    }

    private int getW(){
        return getWidth() == 0 ? getMeasuredWidth() : getWidth();
    }

    private int getH(){
        return getHeight() == 0 ? getMeasuredHeight() : getHeight();
    }

    private float getDistance(Point p1, Point p2){
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

}
