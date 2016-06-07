package com.delacrmi.persistences;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 09/10/15.
 */
public class EntityManager  {

    private ConnectSQLite conn = null;

    //private List<Class> tables;
    private Map<String,String> entitiesNickName = new HashMap();
    private Map<String,EntityProperty> propertyMap = new HashMap();
    public Map<String,Entity> temporyEntity = new HashMap();

    /*==================================================*/
    private Map<String,Class<? extends Entity>> name_class = new HashMap();
    private List<Entity> entities = new ArrayList<>();
    private Context context;
    private String dbName;
    private int dbVersion;
    private SQLiteDatabase.CursorFactory factory = null;

    public List<String> getTablesNames() {
        List<String> list = new ArrayList<String>();
        for(String key : propertyMap.keySet())
            list.add(key);

        return list;
    }

    public List<Class<? extends Entity>> getTables() {
        List<Class<? extends Entity>> list = new ArrayList<Class<? extends Entity>>();
        for(String key : propertyMap.keySet())
            list.add(propertyMap.get(key).getTableClass());
        //return tables;
        return list;
    }

    public EntityManager setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
        return this;
    }

    public EntityManager setDbName(String dbName) {
        this.dbName = dbName;
        return  this;
    }

    public EntityManager setContext(Context context) {
        this.context = context;
        return this;
    }

    public EntityManager setFactory(SQLiteDatabase.CursorFactory factory) {
        this.factory = factory;
        return this;
    }

    public EntityManager(){}
    public EntityManager(Context context, String dbName,
                         SQLiteDatabase.CursorFactory factory, int dbVersion){
        this.context = context;
        this.dbName = dbName;
        this.factory = factory;
        this.dbVersion = dbVersion;
    }

    public EntityManager init(){
        Entity.setEntityManager(this);
        ConnectSQLite.validate = false;
        conn = new ConnectSQLite(context,dbName,factory,dbVersion){

            @Override
            protected void beforeToCreate(SQLiteDatabase db){
                setCreateString();
                onCreateDataBase(this, db);
            }
            @Override
            protected void afterToCreate(SQLiteDatabase db) {
                executeAfterTrigger(db);
                onDataBaseCreated(this, db);
            }

            @Override
            protected void beforeToUpdate(SQLiteDatabase db) {
                setCreateString();
                onDatabaseUpdate(this, db);
            }

            @Override
            protected void afterToUpdate(SQLiteDatabase db) {
                onUpdatedDataBase(this, db);
            }
        };
        read();
        return this;
    }

    public void onCreateDataBase(ConnectSQLite conn, SQLiteDatabase db){}
    public void onDataBaseCreated(ConnectSQLite conn, SQLiteDatabase db){}

    public void onDatabaseUpdate(ConnectSQLite conn, SQLiteDatabase db){}
    public void onUpdatedDataBase(ConnectSQLite conn, SQLiteDatabase db){}

    protected SQLiteDatabase write(){
        if(conn == null)
            init();

        return conn.getWritableDatabase();
    }

    public SQLiteDatabase read(){
        if(conn == null)
            init();

        return conn.getReadableDatabase();
    }

    public void close() {
        if(write().isOpen()) write().close();
        if(read().isOpen()) read().close();
    }

    public EntityManager addEntity(Class<? extends Entity> entityClass){
        name_class.put(entityClass.getSimpleName(),entityClass);
        return this;
    }

    private void setCreateString(){
        if(ConnectSQLite.tablesCreater == null) ConnectSQLite.tablesCreater = new ArrayList<>();
        if(ConnectSQLite.tablesNames == null) ConnectSQLite.tablesNames = new ArrayList<>();

        for (String name : name_class.keySet()){
            Entity entity = initInstance(name_class.get(name));
            if(entity.getEntityManager() == null)
                Entity.setEntityManager(this);

            entities.add(entity);

            try {
                ConnectSQLite.tablesCreater.add(entity.getCreateString());
                ConnectSQLite.tablesNames.add(entity.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void executeAfterTrigger(SQLiteDatabase db){
        for(Entity entity : entities)
            for (String trigger : entity.getProperty().getTableTriggers().get("after")){
                try {
                    Method method =  entity.getClass().getDeclaredMethod(trigger, db.getClass());
                    method.setAccessible(true);
                    method.invoke(entity,db);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
    }

    @Deprecated
    public Class getClassByName(String name){
        return name_class.get(name);
    }

    public Map<String, String> getEntitiesNickName() {
        return entitiesNickName;
    }

    @Deprecated
    public void setEntitiesNickName(HashMap<String, String> entitiesNickName) {
        this.entitiesNickName = entitiesNickName;
    }

    public String getEntityNicName(String entity){
        return entitiesNickName.get(entity);
    }

    //<editor-fold desc="Saving the Entities class">
    public Entity save(Class entity,ContentValues args){
        Entity ent = initInstance(entity);
        ent.save(args);
        return ent;
    }

    @Deprecated
    public synchronized Entity save(Entity entity){
        //TODO write the code logical to the save method 2
        if(entity != null){
            long insert = 0/*write().insert(entity.getName(), null, entity.getContentValues())*/;
            write().close();
            //Log.e("Save", "" + insert);
            if(insert > 0) {
                //entity.getColumnValueList().put(entity.getPrimaryKey(),insert);
                List<String> pk = entity.getPrimariesKeys();
                /*if(pk.size() == 1)
                    pk.get(0).setValue(insert);*/
                return entity;
            }else
                return null;
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Updating the Entities class">
    public Entity update(Class entity,ContentValues columnsValue,String where, String[] whereValues,boolean save){
        //TODO write the code logical to the update method 1
        Entity ent= findOnce(entity, "*", where, whereValues);
        /*ent.setValues(columnsValue);*/
        return update(ent, where, whereValues, save);
    }

    public synchronized Entity update(Entity entity,String where,String[] whereValues,boolean save){
        //TODO write the code logical to the update method 2
        if(entity != null){
            long insert = 0/*write().update(entity.getName(), entity.getColumnValueList(), where, whereValues)*/;
            write().close();
            if(insert > 0)
                return entity;
            else if(save)
                return save(entity);
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Finding the Entities class">
    public synchronized Entity findOnce(Class entity,String[] columns,String where,
                           String[] whereValues, String groupBy, String having, String orderBy){
        Entity ent= initInstance(entity);
        Cursor cursor = read().query(ent.getName(),columns,where,whereValues,groupBy,
                having,orderBy,"1");

        if(cursor != null && cursor.moveToFirst())
            addEntityValues(cursor,ent);

        read().close();
        return ent;
    }

    public synchronized Entity findOnce(Class entity,String columns,String conditions,String[] args){
        Entity ent= initInstance(entity);

        String sql = "select "+columns+" from "+ent.getName();
        if(conditions != null)
            sql += " where "+conditions;

        Cursor cursor = read().rawQuery(sql, args);

        if(cursor != null && cursor.moveToFirst()) addEntityValues(cursor, ent);

        read().close();
        return ent;
    }

    public synchronized List<Entity> find(Class entity, boolean distinct, String[] columns, String where,
                                          String[] whereValues, String groupBy, String having, String orderBy,
                           String limit) {
        Cursor cursor = read().query(distinct, initInstance(entity).getName(),
                columns, where, whereValues, groupBy, having, orderBy, limit);


        if(cursor != null && cursor.moveToFirst()){
            List<Entity> list = new ArrayList<Entity>();
            do {
                Entity ent= initInstance(entity);
                addEntityValues(cursor,ent);
                list.add(ent);
            }while(cursor.moveToNext());

            read().close();

            return  list;
        }
        read().close();
        return new ArrayList<Entity>();
    }

    public synchronized List<Entity> find(Class entity,String columns,String conditions,String[] args){
        Entity ent= initInstance(entity);

        String sql = "select "+columns+" from "+ent.getName();
        if(conditions != null)
            sql += " where "+conditions;

        Cursor cursor = read().rawQuery(sql, args);
        List<Entity> list = new ArrayList<Entity>();
        setListFromCursor(cursor,list,entity);

        read().close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="deleting the Entities class">
    public synchronized boolean delete(Class entity,String where,String[] whereValues){
        Entity ent = initInstance(entity);

        int deleted = write().delete(ent.getName(),where,whereValues);
        write().close();

        if(deleted > 0)
            return true;
        else
            return false;
    }

    public synchronized boolean delete(Entity entity){
        //TODO write the code logical to the delete method
        int deleted =0 /*write().delete(entity.getName(),entity.getPrimaryKey()+" = ?",
                new String[]{entity.getColumnValueList().getAsString(entity.getPrimaryKey())})*/;
        write().close();

        if(deleted > 0)
            return true;
        else
            return false;
    }
    //</editor-fold>

    //adding the columns value by entity
    public void addEntityValues(Cursor cursor,Entity entity){
        if(cursor != null){
            for (int index = 0; index < cursor.getColumnNames().length; index++){
                String columnName = cursor.getColumnName(index);
                int col = cursor.getColumnIndex(columnName);
                try {
                    Object o = entity.convertToFieldType(columnName, cursor.getString(col));
                    if(o != null)
                        entity.setColumn(columnName, o);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InstantiationException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void setListFromCursor(Cursor cursor, List<Entity> entities,Class entity){
        Entity ent;
        if(cursor != null && cursor.moveToFirst()){
            do {
                ent= initInstance(entity);
                addEntityValues(cursor,ent);
                entities.add(ent);
            }while(cursor.moveToNext());
        }
    }

    /*
    private void createList(Class<? extends Entity> entityClass){
        //List<String> value= new ArrayList<String>();
        //tablesNames = new ArrayList<String>();

        Entity entity = initInstance(entityClass);
        entity.setEntityManager(this);
        entity.setPropertyTable();
        ConnectSQLite.tablesCreater.add(entity.getCreateString());

        //tablesNames.add(entity.getName());
        //entitiesNickName.put(entity.getName(),entity.getNickName());
        //name_class.put(entity.getName(),entityClass);
        //value.add(entity.getCreateString());


        //return value;
    }*/

    public Entity initInstance(Class entity){
        try {
            return  (Entity)entity.newInstance();
        } catch (InstantiationException e1) {
        } catch (IllegalAccessException e1) {}

        return null;
    }

    public Map<String,EntityProperty> getTablesProperties(){
        return propertyMap;
    }
}
