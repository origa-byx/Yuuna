package com.origami.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @by: origami
 * @date: {2021-06-25}
 * @info:
 **/
public class SqlUtil<T extends DbBean> {

    private final SQLiteOpenHelper db;
    private final String db_name;
    private final List<Field> fields;
    private final Constructor<T> constructor;
    private Field key;

    public static <S extends DbBean> SqlUtil<S> newInstance(SQLiteOpenHelper db, Class<S> sClass) {
        return new SqlUtil<>(db, sClass);
    }

    /**
     * 查询语句,传入查询条件
     * @param
     * @return
     */
    private SqlUtil(SQLiteOpenHelper db, Class<T> aClass){
        this.db = db;
        DbName dbName = aClass.getAnnotation(DbName.class);
        if(dbName == null || TextUtils.isEmpty(dbName.value())){ throw new RuntimeException("miss Dbname"); }
        db_name = dbName.value();
        fields = new ArrayList<>();
        for (Field field : aClass.getDeclaredFields()) {
            if(field.getAnnotation(Item.class) != null){
                fields.add(field);
            }else if(field.getAnnotation(Key.class) != null){
                fields.add(field);
                key = field;
            }
        }
        try {
            constructor = aClass.getDeclaredConstructor();
            if(!constructor.isAccessible()){ constructor.setAccessible(true); }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        if(key == null){ throw new RuntimeException("not found key -- key and Item Can not be used together"); }
    }

    public List<T> selectDataBase(){
        return selectDataBase(null, 0, 0);
    }

    public List<T> selectDataBase(T data){
        return selectDataBase(data, 0, 0);
    }

    public List<T> selectDataBase(T data, int page, int pageSize, String... ext) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(db_name);
        boolean firstAdd = true;
        List<String> obj_args = new ArrayList<>();
        if(data != null) {
            for (Field field : fields) {
                try {
                    Object o = field.get(data);
                    if (o != null) {
                        if (firstAdd) {
                            sql.append(" WHERE ");
                            firstAdd = false;
                        } else {
                            sql.append(" AND ");
                        }
                        sql
                                .append(" ")
                                .append(field.getName())
                                .append("=");
                        Class<?> type = field.getType();
                        if (type == String.class) {//string
                            sql.append("?");
                            obj_args.add((String) o);
                        } else {
                            sql.append(o);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if(pageSize != 0){
            if(page < 1){ page = 1; }
            int start = (page - 1) * pageSize;
            sql.append(" LIMIT ")
                    .append(start)
                    .append(",")
                    .append(pageSize);
        }
        String[] args = new String[obj_args.size()];
        for (int i = 0; i < obj_args.size(); i++) {
            args[i] = obj_args.get(i);
        }
        Log.e("SQL","selectDataBase:\n" + sql.toString());
        Cursor cursor = Db.doReadSql(db, sql.toString(), args);
        List<T> faceDates = new ArrayList<>();
        while (cursor.moveToNext()){
            T bean = null;
            try {
                bean = constructor.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            for (Field field : fields) {
                String fieldName = field.getName();
                boolean go = true;
                if (ext != null && ext.length != 0) {
                    for (String s : ext) {
                        if (s.equals(fieldName)) {
                            go = false;
                        }
                    }
                }
                if (go) {
                    int columnIndex = cursor.getColumnIndex(fieldName);
                    Class<?> type = field.getType();
                    try {
                        if (type == String.class) {
                            field.set(bean, cursor.getString(columnIndex));
                        } else if (type == Integer.class || type == int.class) {
                            field.set(bean, cursor.getInt(columnIndex));
                        } else if (type == Long.class) {
                            field.set(bean, cursor.getLong(columnIndex));
                        } else if (type == Double.class) {
                            field.set(bean, cursor.getDouble(columnIndex));
                        } else if (type == Float.class) {
                            field.set(bean, cursor.getString(columnIndex));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            bean.readAndThen();
            faceDates.add(bean);
        }
        cursor.close();
        return faceDates;
    }

    public void deleteDateBaseByKey(String key, int id){
        String sql = "DELETE FROM " + db_name + " WHERE " + key + "=" + id;
        Db.doWriteSql(db, sql);
    }

    public void deleteDataBase(T data){
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(db_name);
        boolean firstAdd = true;
        List<Object> obj_args = new ArrayList<>();
        for (Field field : fields) {
            try {
                Object o = field.get(data);
                if(o != null){
                    if(firstAdd){
                        sql.append(" WHERE ");
                        firstAdd = false;
                    }else {
                        sql.append(" AND ");
                    }
                    sql
                            .append(" ")
                            .append(field.getName())
                            .append("=?");
                    obj_args.add(o);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Object[] args = new Object[obj_args.size()];
        for (int i = 0; i < obj_args.size(); i++) {
            args[i] = obj_args.get(i);
        }
        Log.e("SQL","deleteDataBase:\n" + sql.toString());
        Db.doWriteSql(db, sql.toString(), args);
    }

    public void saveDataBase(T data) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(db_name).append(" (");
        boolean add = false;
        for (Field field : fields) {
            if(field == key){ continue;}
            if(add){ sql.append(" , "); }else {add = true;}
            sql.append(field.getName());
        }
        add = false;
        sql.append(") VALUES (");
        List<Object> obj_args = new ArrayList<>();
        for (Field field : fields) {
            if(field == key){ continue; }
            field.setAccessible(true);
            if(add){ sql.append(","); }else {add = true;}
            sql.append("?");
            try {
                obj_args.add(field.get(data));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        sql.append(")");
        Log.e("SQL","saveDataBase:\n" + sql.toString());
        Object[] args = new Object[obj_args.size()];
        for (int i = 0; i < obj_args.size(); i++) {
            args[i] = obj_args.get(i);
        }
        Db.doWriteSql(db, sql.toString(), args);
        String sqlId = "SELECT last_insert_rowid() FROM " + db_name;
        Cursor cursor = Db.doReadSql(db, sqlId, null);
        if(cursor.moveToFirst()){
            try {
                key.set(data, cursor.getInt(0));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新一条数据,根据 where 定位数据
     * @param data
     */
    public void updateDataBase(T data, String[] where, Object... args){
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(db_name)
                .append(" SET ");
        boolean firstAdd = true;
        List<Object> obj_args = new ArrayList<>();
        for (Field field : fields) {
            if(field == key){ continue; }
            try {
                Object o = field.get(data);
                if(o != null){
                    if(firstAdd){
                        firstAdd = false;
                    }else {
                        sql.append(",");
                    }
                    sql.append(" ").append(field.getName()).append("=?");
                    obj_args.add(o);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        for (String s : where) {
            if(firstAdd){sql.append(" AND ");}else { firstAdd = true;sql.append(" WHERE "); }
            sql.append(s).append(" = ?");
        }
        Object[] sql_args = new Object[obj_args.size() + args.length];
        for (int i = 0; i < obj_args.size(); i++) {
            sql_args[i] = obj_args.get(i);
        }
        if(args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                sql_args[obj_args.size() + i] = args[i];
            }
        }
        Log.e("SQL","updateDataBase:\n" + sql.toString());
        Db.doWriteSql(db, sql.toString(), sql_args);
    }


    public int getTotal(T data){
        StringBuilder sql = new StringBuilder("SELECT count(").append(key.getName()).append(") FROM ");
        sql.append(db_name);
        boolean firstAdd = true;
        for (Field field : fields) {
            try {
                Object o = field.get(data);
                if(o != null){
                    if(firstAdd){
                        sql.append(" WHERE ");
                        firstAdd = false;
                    }else {
                        sql.append(" AND ");
                    }
                    sql
                            .append(" ")
                            .append(field.getName())
                            .append("=");
                    if(field.getType() == Integer.class
                            || field.getType() == int.class
                            || field.getType() == Long.class
                            || field.getType() == Float.class
                            || field.getType() == Double.class){
                        sql.append(o);
                    }else {//string
                        sql.append("\"").append(o).append("\"");
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Cursor cursor = Db.doReadSql(db, sql.toString(), null);
        int c = 0;
        if(cursor.moveToFirst()){
            c = cursor.getInt(0);
        }
        Log.e("SQL","getTotal:\n" + sql.toString() + " -> " + c);
        cursor.close();
        return c;
    }

    /**
     * 检查是否存在该对象对应的查询条件下的数据
     * @param data
     * @return
     */
    public boolean checkHasBean(T data){
        StringBuilder sql = new StringBuilder("SELECT count(").append(key.getName()).append(") FROM ");
        sql.append(db_name);
        boolean firstAdd = true;
        for (Field field : fields) {
            try {
                Object o = field.get(data);
                if(o != null){
                    if(firstAdd){
                        sql.append(" WHERE ");
                        firstAdd = false;
                    }else {
                        sql.append(" AND ");
                    }
                    sql
                            .append(" ")
                            .append(field.getName())
                            .append("=");
                    if(field.getType() == Integer.class
                            || field.getType() == int.class
                            || field.getType() == Long.class
                            || field.getType() == Float.class
                            || field.getType() == Double.class){
                        sql.append(o);
                    }else {//string
                        sql.append("\"").append(o).append("\"");
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Log.e("SQL","checkHasBean:\n" + sql.toString());
        Cursor cursor = Db.doReadSql(db, sql.toString(), null);
        boolean b = cursor.moveToNext();
        cursor.close();
        return b;
    }


}
