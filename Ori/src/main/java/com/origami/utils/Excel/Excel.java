package com.origami.utils.Excel;


import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * @by: origami
 * @date: {2021-09-02}
 * @info:   excel 工具, 如果是读取外部非此类生成的 excel , 只支持xsl 2003版本, 需手动另存为xsl2003再进行读取
 **/
public class Excel<B extends XlsBean> {

    /**
     * 创建新的 xls     {@link #saveAndClose()}
     * @param bean      映射的对象
     * @param savePath  保存xls的全路径
     * @param <Bean>    对象
     * @return  Excel<Bean>
     */
    public static <Bean extends XlsBean> Excel<Bean> build_CreateNewFile(Class<Bean> bean, String savePath) throws IOException, BiffException {
        return new Excel<>(bean, true, savePath);
    }

    /**
     * 打开一个 xls 不存在会创建
     *  一旦成功读取会复写原来的，不save会导致源数据被置空
     *  ******->  {@link #saveAndClose()}必须执行
     * @param bean      映射的对象
     * @param path      要打开的xls路径
     * @param <Bean>    对象
     * @return  Excel<Bean>
     */
    public static <Bean extends XlsBean> Excel<Bean> build_OpenFile(Class<Bean> bean, String path) throws IOException, BiffException {
        return new Excel<>(bean, false, path);
    }

    /**
     * 打开一个 xls {@link #saveAndClose()}
     * @param bean      映射的对象
     * @param path      要打开的xls路径
     * @param <Bean>    对象
     * @return  Excel<Bean>
     */
    public static <Bean extends XlsBean> Excel<Bean> build_ReadFile(Class<Bean> bean, String path) throws IOException, BiffException {
        return new Excel<>(bean, path);
    }

    public static <Bean extends XlsBean> Excel<Bean> build_ReadFile(Class<Bean> bean, InputStream stream) throws IOException, BiffException {
        return new Excel<>(bean, stream);
    }

    private Workbook workbook = null;
    private WritableWorkbook writeWorkbook = null;
    private final Class<B> bClass;

    WritableCellFormat format_white = null, format_gray = null;
    private WritableSheet writeSheet = null;
    private Sheet readSheet = null;
    private final String mPath;

    private final Map<Integer , Field> fieldIndexMap = new HashMap<>();


    private Excel(Class<B> bean, boolean create, String path) throws IOException {
        this.mPath = path;
        File file = new File(path);
        createParentIfNotExists(file);
        if(create || !file.exists()){
            writeWorkbook = Workbook.createWorkbook(file);
        }else {
            try {
                workbook = Workbook.getWorkbook(file);
                readSheet = workbook.getSheet(0);
                writeWorkbook = Workbook.createWorkbook(file, workbook);
            }catch (NullPointerException | BiffException e){
                writeWorkbook = Workbook.createWorkbook(file);
            }
        }
        if(writeWorkbook.getNumberOfSheets() != 0){
            writeSheet = writeWorkbook.getSheet(0);
        }else {
            writeSheet = writeWorkbook.createSheet("sheet0", 0);
        }
        this.bClass = bean;
        initClassFieldsAndFormats();
    }

    private Excel(Class<B> bean, String path) throws IOException, BiffException {
        this.mPath = path;
        File file = new File(path);
        createParentIfNotExists(file);
        workbook = Workbook.getWorkbook(file);
        readSheet = workbook.getSheet(0);
        this.bClass = bean;
        initClassFieldsAndFormats();
    }

    private Excel(Class<B> bean, InputStream stream) throws IOException, BiffException {
        this.mPath = "";
        workbook = Workbook.getWorkbook(stream);
        readSheet = workbook.getSheet(0);
        this.bClass = bean;
        initClassFieldsAndFormats();
    }


    private void createParentIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            if(file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
        }
    }

    private void initClassFieldsAndFormats(){
        Field[] declaredFields = bClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            XlsName annotation = declaredField.getAnnotation(XlsName.class);
            if(annotation != null){ fieldIndexMap.put(annotation.index() ,declaredField); }
        }
        format();
    }

    public void addOrChangeItem(B obj) {
        if(writeSheet.getRows() == 0){ addTitle(); }
        int row;
        if(obj.row != Integer.MIN_VALUE){
            row = obj.row;
        }else {
            row = writeSheet.getRows();
        }
        for (int index : fieldIndexMap.keySet()) {
            Field field = fieldIndexMap.get(index);
            if(field == null) { continue; }
            try {
                Label label = new Label(index, row, String.valueOf(field.get(obj)));
                if(row % 2 == 0){
                    if(format_gray != null){ label.setCellFormat(format_gray); }
                }else {
                    if(format_white != null){ label.setCellFormat(format_white); }
                }
                writeSheet.setRowView(row, 450);
                writeSheet.addCell(label);
                obj.row = row;
            } catch (IllegalAccessException | WriteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成标题
     */
    private void addTitle(){
        WritableCellFormat format_title = null;
        try {
            WritableFont writableFont = new WritableFont(WritableFont.COURIER, 14, WritableFont.BOLD);
            writableFont.setColour(Colour.YELLOW2);
            format_title = new WritableCellFormat(writableFont);
            format_title.setAlignment(Alignment.CENTRE);
            format_title.setShrinkToFit(true);
            format_title.setVerticalAlignment(VerticalAlignment.CENTRE);
            format_title.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.WHITE);
            format_title.setBackground(Colour.GRAY_50);
        } catch (WriteException e) {
            e.printStackTrace();
        }
        for (int index : fieldIndexMap.keySet()) {
            XlsName annotation = Objects.requireNonNull(fieldIndexMap.get(index)).getAnnotation(XlsName.class);
            if(annotation != null){
                try {
                    Label cell = new Label(index, 0, annotation.name());
                    if(format_title != null){ cell.setCellFormat(format_title); }
                    writeSheet.addCell(cell);
                    writeSheet.setColumnView(index, annotation.width());
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化 Excel 单元格格式
     */
    private void format() {
        try {
            WritableFont arial14font = new WritableFont(WritableFont.COURIER, 12, WritableFont.NO_BOLD);
            arial14font.setColour(Colour.BLACK);
            format_white = new WritableCellFormat(arial14font);
            format_white.setShrinkToFit(true);
            format_white.setIndentation(1);
            format_white.setAlignment(Alignment.LEFT);
            format_white.setVerticalAlignment(VerticalAlignment.CENTRE);
            format_white.setBorder(Border.ALL, BorderLineStyle.DOUBLE, Colour.WHITE);
            format_white.setBackground(Colour.WHITE);

            format_gray = new WritableCellFormat(arial14font);
            format_gray.setShrinkToFit(true);
            format_gray.setIndentation(1);
            format_gray.setAlignment(Alignment.LEFT);
            format_gray.setVerticalAlignment(VerticalAlignment.CENTRE);
            format_gray.setBorder(Border.ALL, BorderLineStyle.DOUBLE, Colour.WHITE);
            format_gray.setBackground(Colour.GRAY_25);

        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存并释放资源
     */
    public boolean saveAndClose(){
        boolean ok = true;
        boolean noData = false;
        if(writeSheet != null) { noData = writeSheet.getRows() == 0; }
        try {
            if(workbook != null){
                workbook.close();
            }
            if(writeWorkbook != null){
                writeWorkbook.write();
                writeWorkbook.close();
            }
        } catch (IOException | WriteException e) {
            ok = false;
            e.printStackTrace();
        }finally {
            if(noData && !TextUtils.isEmpty(mPath)){
                ok = false;
                File file = new File(mPath);
                if(file.exists()){ file.delete(); }
            }
        }
        return ok;
    }

    /**
     * 第 0 行一般是 title 过滤掉
     * @param objects 将传递至 {@link XlsBean#init_other_field(int, Object...)} 中
     * @return  List<B>
     */
    public List<B> readAll(Object... objects){ return readAll(1, objects); }

    /**
     * 读数据
     * @param from  开始行
     * @return  List<B>
     */
    public List<B> readAll(int from, Object... objects) {
        List<B> dates = new ArrayList<>();
        if(readSheet == null){ return dates; }
        for (int row = from; row < readSheet.getRows(); row++){
            try {
                B b = bClass.getConstructor().newInstance();
                boolean isNull = true;
                b.row = row;
                for (int index : fieldIndexMap.keySet()) {
                    Cell cell = readSheet.getCell(index, row);
                    String value = cell.getContents();
                    Field field = fieldIndexMap.get(index);
                    if(TextUtils.isEmpty(value) || field == null){ continue; }
                    Class<?> type = field.getType();
                    isNull = false;
                    if(type == String.class){
                        field.set(b, value);
                    }else if(type == int.class){
                        field.set(b, Integer.parseInt(value));
                    }else if(type == float.class){
                        field.set(b, Float.parseFloat(value));
                    }else if(type == double.class){
                        field.set(b, Double.parseDouble(value));
                    }else if(type == boolean.class){
                        field.set(b, Boolean.parseBoolean(value));
                    }else {
                        isNull = true;
                    }
                }
                if(!isNull) {
                    b.init_other_field(row - from, objects);
                    dates.add(b);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return dates;
    }

}