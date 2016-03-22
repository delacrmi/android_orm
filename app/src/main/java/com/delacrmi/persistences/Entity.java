package com.delacrmi.persistences;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.delacrmi.persistences.annotation.Column;
import com.delacrmi.persistences.annotation.ManyToMany;
import com.delacrmi.persistences.annotation.ManyToOne;
import com.delacrmi.persistences.annotation.OneToMany;
import com.delacrmi.persistences.annotation.OneToOne;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

/**
 * Created by miguel on 09/10/15.
 */
public class Entity implements Serializable {

    private EntityProperty property;
    private EntityFilter entityFilter;
    private static EntityManager manager = null;
    private SimpleDateFormat dateFormat;
    private Map<String,Object> entityRelationMap = new HashMap<>();

    public String getNickName() {
        setPropertyTable();
        return property.getTableAnnotation().NickName();
    }

    public boolean isSynchronizable() {
        setPropertyTable();
        return property.getTableAnnotation().Synchronazable();
    }

    public EntityFilter getEntityFilter() {
        return entityFilter;
    }

    public void setEntityFilter(EntityFilter entityFilter) {
        this.entityFilter = entityFilter;
    }

    @Deprecated
    public void configureEntityFilter(Context context){}

    public static void setEntityManager(EntityManager manager){
        Entity.manager = manager;
    }

    public EntityManager getEntityManager(){
        return Entity.manager;
    }

    @Deprecated
    public List<Entity> getDefaultInsert(){
        return null;
    }

    public String getCreateString() throws Exception{
        setPropertyTable();
        int count = 1;
        String create = "create table "+property.getTableName()+"(";

        for(String key : property.getFieldMap().keySet()){
            Field field = property.getFieldMap().get(key);

            try {
                Column column = property.getColumnMap().get(key);
                String t  = " " + convertToDBType(field.getType().getSimpleName());
                create += key + t;

                if(column.PrimaryKey() &&
                        (column.AutoIncrement() || column.WritePrimaryKey())) create += " PRIMARY KEY";
                //if(column.AutoIncrement()) create += " autoincrement";
                if(column.NotNull() ||
                        (column.PrimaryKey() && !column.AutoIncrement() && !column.WritePrimaryKey())) create += " not null";

            } catch (Exception e) {

                if(property.getRelationships().containsKey(key)){
                    Annotation annotation = property.getRelationships().get(key);
                    if( annotation instanceof OneToMany
                            || (annotation instanceof OneToOne && !((OneToOne)annotation).Create())){
                        count++;
                        continue;
                    }

                    create += getColumnByRelation(this,key);

                }else throw new Exception("The column " + key + "need a relationship annotation type");
            }

            if(count < property.getFieldMap().size()){
                create  += ",";
                count++;
            }
        }

        create += ")";
        return create;
    }

    private String getColumnByRelation(Entity entity, String key) throws Exception{
        String columns = "";
        int ct = 1;
        Field field = entity.getProperty().getFieldMap().get(key);
        Annotation annotation = entity.getProperty().getRelationships().get(key);
        String[] ann = convertRelationshipToString(annotation);
        Entity ent = getEntityFromType(field);
        ent.setPropertyTable();

        if(annotation.annotationType().getSimpleName().equals("OneToOne") ||
             annotation.annotationType().getSimpleName().equals("ManyToOne")){
            if(ann.length > 0) {
                for (String one : ann){
                    try{
                        columns += ent.getName() + "_" + one + " " + convertToDBType(ent.getFieldsPrimariesKey()
                                .get(one).getType().getSimpleName());
                    }catch (NullPointerException n){
                        throw new Exception("The column " + one + " isn't a Primary key");
                    }

                    if (entity.getProperty().getColumnMap().get(key).NotNull()) columns += " not null";
                    if(ct < ann.length){
                        columns  += ",";
                        ct++;
                    }
                }
            }else {
                for (String one : ent.getFieldsPrimariesKey().keySet()) {
                    columns += ent.getName() + "_" + one + " " + convertToDBType(ent.getFieldsPrimariesKey()
                            .get(one).getType().getSimpleName());
                    if (entity.getProperty().getColumnMap().get(key).NotNull()) columns += " not null";
                    if(ct < ent.getFieldsPrimariesKey().size()){
                        columns  += ",";
                        ct++;
                    }
                }
            }
        }

        return columns;
    }

/*============================================================================*/
    public EntityProperty getProperty(){
        setPropertyTable();
        return property;
    }
    public List<String> getPrimariesKeys(){
        List<String> list = new ArrayList<String>();
        for(String pk : property.getPrimaryKeyMap().keySet())
            list.add(pk);
        return  list;
    }

    public Map<String, Field> getFieldsPrimariesKey(){
        Map<String, Field> p = new HashMap<String, Field>();
        setPropertyTable();

        for(String pk : property.getPrimaryKeyMap().keySet())
            p.put(pk, property.getFieldMap().get(pk));
        return p;
    }

    public Map<String, Column> getColumnPrimaryKey(){
        setPropertyTable();
        return property.getPrimaryKeyMap();
    }

    public String getColumnsNameAsString(boolean primaryKey){
        int count = 1;
        String columns = "";
        setPropertyTable();
        int length = property.getColumnMap().size();

        for (String column: property.getColumnMap().keySet()){
            if(!property.getPrimaryKeyMap().containsKey(column)){
                columns += column;
                if(count < length){
                    columns += ",";
                }
            }else if(primaryKey){
                columns += column;
                if(count < length){
                    columns += ",";
                }
            }

            count++;
        }

        return columns;
    }

    public String getColumnsNameAsWithout(String [] withoutNames){
        int count = 1;
        String columns = "";
        setPropertyTable();
        int length = property.getColumnMap().size();

        for (String column: property.getColumnMap().keySet()){
            if(!arrayContains(column, withoutNames)){
                columns += column;
                if(count < length-withoutNames.length){
                    columns += ",";
                }
            }
            count++;
        }

        return columns;
    }

    private boolean arrayContains(String arg1, String [] arg2){
        for (int a =0; a < arg2.length; a++){
            if (arg1.equals(arg2[a]))
                return true;
        }
        return false;
    }

    public JSONObject getJSON(){
        setPropertyTable();
        JSONObject json = new JSONObject();

        for(String column : property.getFieldMap().keySet())
            try {
                Field field = property.getFieldMap().get(column);
                if(field.getType().getSimpleName().equals("Date")){
                    json.put(column, new SimpleDateFormat(property.getColumnMap()
                            .get(column).DateFormat())
                            .format((Date)field.get(this)));
                }else
                    json.put(column,field.get(this));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        return json;
    }

    public JSONArray getColumnstoJSONArray(){
        setPropertyTable();
        JSONArray array = new JSONArray();

        Set<String> sets = property.getColumnMap().keySet();
        for(String set: sets)
            array.put(set);

        return array;
    }

    public int getColumnsCount(){
        setPropertyTable();
        return property.getFieldMap().size();
    }

    public String getName(){
        setPropertyTable();
        return property.getTableName();
    }

    public void setColumn(String column, Object value)
            throws NoSuchFieldException, InstantiationException{

        setPropertyTable();

        if(property.getFieldMap().containsKey(column))
            try {
                Field field = property.getFieldMap().get(column);
                field.setAccessible(true);

                /*try{
                    if (property.getRelationships().get(column) != null){

                        String n = null;
                        int count = 1;
                        String[] v = null;
                        int length = property.getPrimaryKeyMap().size();

                        if (length > 0) {
                            n = "";
                            v = new String[length];
                        }

                        for (String key : property.getPrimaryKeyMap().keySet()) {
                            if(convertRelationshipToString(property.getRelationships().get(column))
                                    .equals(""))
                                n += property.getTableName() + "_" + key;
                            else
                                n += property.getTableName() + "_" +
                                        convertRelationshipToString(property.getRelationships().get(column));

                            Field ele = property.getFieldMap().get(key);
                            if (ele.getType().getSimpleName().equals("Date")) {
                                if (v != null)
                                    v[count - 1] = ((Date) ele.get(this)).getTime() + "";

                            } else if (v != null) v[count - 1] = ele.get(this) + "";

                            if(count < length) n += ",";
                        }

                        if(field.getType().getSimpleName().equals("List")){
                            value = getList(field);
                        }else if (field.getType().getSuperclass().isInstance(new Entity())) {
                            value = ((Entity)field.getType()
                                    .newInstance()).findOnce(null);
                        }
                    }
                } catch (InstantiationException e) {
                    throw new InstantiationException("The type of " + column +
                            " column isn't instance of Entity class");
                }*/

                //TODO Check the Date and others conversions type
                field.set(this, value);

            }catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        else throw new NoSuchFieldException("The column " + column + " not exist in the Entity " +
                getClass().getSimpleName());
    }

    private void setEntityColumn(Entity entity){
        Field field;
        Object value = null;
        for (String column : property.getRelationshipNames()){
            field = property.getFieldMap().get(column);
            Annotation annotation = property.getRelationships().get(column);
            String [] primaryArray = convertRelationshipToString(annotation);
            EntityFilter filter = new EntityFilter("?");
            Entity merry;

            if(annotation instanceof OneToOne && ((OneToOne)annotation).Create()){
                try{
                    //Preparing the filter parameter
                    merry = ((Entity)field.getType()
                            .newInstance());
                    for(int i = 0; i < primaryArray.length;i++){
                        String cn = merry.getName()+"_"+primaryArray[i];
                        if((i+1) < primaryArray.length)
                            filter.addArgument(primaryArray[i],entityRelationMap.get(cn)+"",null,"and");
                        else
                            filter.addArgument(primaryArray[i],entityRelationMap.get(cn)+"",null);
                    }

                    merry.findOnce(filter);
                    try {
                        setColumn(column,merry);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }


                }catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                /*try {
                    if(field.getType().getSimpleName().equals(entity.getClass().getSimpleName())){
                        setColumn(column, entity);
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }*/

            } else if(annotation instanceof OneToMany){
                int count = 1;
                for(String key: property.getPrimaryKeyMap().keySet()){
                    if(count < property.getPrimaryKeyMap().size())
                        filter.addArgument(key,getColumnValue(key)+"",null,"and");
                    else
                        filter.addArgument(key,getColumnValue(key)+"",null);
                }
                merry = getEntityFromType(field);

                merry.find(filter,entity);

            }

            /*if(field.getType().getSimpleName().equals("List")){
                value = getList(field,entity);
            }else if (field.getType().getSuperclass().isInstance(new Entity())) {
                try {
                    value = ((Entity)field.getType()
                            .newInstance()).findOnce(null);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }*/
        }
    }

    private Object getColumnValue(String columnName){
        Object o = null;
        setPropertyTable();
        Field field = property.getFieldMap().get(columnName);

        try {
            o = field.get(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return o;
    }

    public ContentValues getContentValues(){
        setPropertyTable();
        ContentValues cv = new ContentValues();

        for(String key: property.getFieldMap().keySet())
            setContentValue(cv, key, getColumnValue(key), true);
        return cv;
    }

    public void setContentValue(ContentValues content, String key, Object value, boolean check) throws NullPointerException{
        setPropertyTable();

        if(check){
            Column column = property.getColumnMap().get(key);
            if((column.NotNull() || (column.PrimaryKey() && !column.AutoIncrement()))
                    && value == null) throw new NullPointerException("The column " + key + " can't be null");
        }


        if(value != null) Log.i("Class "+key, value.getClass().getSimpleName());
        if(value != null && value.getClass().getSimpleName().equals("Text"))
            Log.d("Super", value.getClass().getSuperclass().getSimpleName());

        if(value == null){}
        else if(value.getClass().getSimpleName().equals("Integer") ||
                value.getClass().getSimpleName().equals("int")) content.put(key, (Integer)value);
        else if(value.getClass().getSimpleName().equals("String")) content.put(key,(String)value);
        else if(value.getClass().getSimpleName().equals("Long")) content.put(key,(Long)value);
        else if(value.getClass().getSimpleName().equals("BigDecimal")) content.put(key,((BigDecimal)value).longValue());
        else if(value.getClass().getSimpleName().equals("Date")) content.put(key,((Date)value).getTime());
        else if (value.getClass().getSimpleName().equals("List")){
            Entity entity = getEntityFromType(property.getFieldMap().get(key));
            Log.i("table name", entity.getName());
            for(String keys : entity.getProperty().getPrimaryKeyMap().keySet())
                setContentValue(content,entity.getName()+"_"+keys,entity.getColumnValue(keys), false);
        }else if(value.getClass().getSuperclass().getSimpleName().equals("Entity")){
            Entity entity = (Entity)value;
            Annotation annotation = property.getRelationships().get(key);
            if(annotation instanceof OneToOne
                    && !((OneToOne) annotation).Create())
                return;

            Log.i("table name", entity.getName());
            for(String keys : entity.getProperty().getPrimaryKeyMap().keySet()) {
                Log.i("Add", keys+" "+entity.getColumnValue(keys));
                setContentValue(content, entity.getName()+"_"+keys, entity.getColumnValue(keys),false);
            }
        }
    }

    public Entity getEntityFromType(Field field){
        Class value = null;
        if(field.getType().getSimpleName().equals("List")){
            Type genericFieldType = field.getGenericType();
            if(genericFieldType instanceof ParameterizedType){
                ParameterizedType aType = (ParameterizedType) genericFieldType;
                Type[] fieldArgTypes = aType.getActualTypeArguments();
                for(Type fieldArgType : fieldArgTypes){
                    value = (Class) fieldArgType;
                    break;
                }
            }
        }else if (field.getType().getSuperclass().isInstance(new Entity())) {
            value = field.getType();
        }

        try {
            return (Entity)value.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<? extends Entity> getList(Field field, Entity ob){
        Type genericFieldType = field.getGenericType();
        if(genericFieldType instanceof ParameterizedType){
            ParameterizedType aType = (ParameterizedType) genericFieldType;
            Type[] fieldArgTypes = aType.getActualTypeArguments();
            for(Type fieldArgType : fieldArgTypes){
                @SuppressWarnings("unchecked")
                Class<? extends Entity> fieldArgClass = (Class<? extends Entity>) fieldArgType;
                try {
                    return ((Entity) fieldArgClass.newInstance()).find(null);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void setPropertyTable(){
        if(!Entity.manager.getTablesProperties()
                .containsKey(getClass().getName())){
            property = new EntityProperty(getClass());
            Entity.manager.getTablesProperties().put(property.getTableName(),property);

        }else if(property == null)
            property = Entity.manager.getTablesProperties().get(getClass().getName());
    }

    //region converts methods
    private String convertToDBType(String type) throws Exception {
        String value;
        switch (type){
            case "String": value = "text";
                break;
            case "int": value = "integer";
                break;
            case "BigDecimal": value = "real";
                break;
            case "Long": value = "numeric";
                break;
            case "Date": value = "numeric";
                break;
            default:
                throw new Exception("Error converting field type: "+type);
        }

        return value;
    }

    private String[] convertRelationshipToString(Annotation annotation){
        String [] value = new String[0];
            if(annotation.annotationType().getSimpleName().equals("OneToOne"))
                value =((OneToOne)annotation).ForeingKey();
            else if(annotation.annotationType().getSimpleName().equals("OneToMany"))
                value = ((OneToMany)annotation).ForeingKey();
            else if(annotation.annotationType().getSimpleName().equals("ManyToOne"))
                value = ((ManyToOne)annotation).ForeingKey();
            else if(annotation.annotationType().getSimpleName().equals("ManyToMany"))
                value = ((ManyToMany)annotation).ForeingKey();

        return value;
    }

    public Object convertToFieldType(String fieldName, String value)
            throws NoSuchFieldException, TypeNotPresentException {
        setPropertyTable();
        Object result;
        if(property.getFieldMap().containsKey(fieldName)){
            Field field = property.getFieldMap().get(fieldName);

            switch (field.getType().getSimpleName()){
                case "String": result = value;
                    break;
                case "int": result = Integer.parseInt(value);
                    break;
                case "BigDecimal": result = new BigDecimal(value);
                    break;
                case "Long": result = Long.parseLong(value);
                    break;
                case "Date":
                    dateFormat = new SimpleDateFormat(property.getColumnMap()
                            .get(fieldName).DateFormat());
                    Date l = new Date();
                    l.setTime(Long.parseLong(value));

                    String d = dateFormat.format(l);
                    try{
                        l = dateFormat.parse(d);
                        result = l;
                    }catch (ParseException e){
                        e.printStackTrace();
                        return null;
                    }

                    break;
                default:
                    throw new TypeNotPresentException("Error converting field type: " +
                            field.getType().getSimpleName(), null);
            }

        }else throw new NoSuchFieldException("The column " + fieldName + " not exist in the Entity " +
                getClass().getSimpleName());

        return result;
    }
    //endregion

    private void addValues(Cursor cursor, Entity ob){
        if(cursor != null){
            for (int index = 0; index < cursor.getColumnNames().length; index++){
                String columnName = cursor.getColumnName(index);
                int col = cursor.getColumnIndex(columnName);
                try {
                    //Log.d("Add Values",columnName+" value "+cursor.getString(col));
                    Object o = convertToFieldType(columnName, cursor.getString(col));
                    if(!property.getRelationships().containsKey(columnName))
                        setColumn(columnName, o);
                    else if (ob != null)
                        setColumn(columnName, ob);
                    /*else {
                        Log.d("Entity",columnName);
                        entityRelationMap.put(columnName, o);
                    }*/
                } catch (NoSuchFieldException e) {
                    //Log.d("Entity",columnName);
                    entityRelationMap.put(columnName, cursor.getString(col));
                } catch (InstantiationException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //region Persistences Methods
    public synchronized Entity findOnce(EntityFilter filter){
        setPropertyTable();
        String[] arg = null;

        String sql = "select * from "+getName();
        if(filter != null){
            sql += " where " + filter.getWhereValue() +" ";
            arg = filter.getArgumentValue();
        }

        Log.d("SQL", sql);
        for(String value : arg)
            Log.d("value",value);

        try{
            Cursor cursor = manager.read().rawQuery(sql, arg);

            if(cursor != null && cursor.moveToFirst()) addValues(cursor,null);
            setEntityColumn(this);
            return this;
        }finally {
            manager.close();
        }
    }

    private void findOnce(EntityFilter filter, Entity obj){
        String[] arg = null;

        String sql = "select * from "+getName();
        if(filter != null){
            sql += " where " + filter.getWhereValue() +" ";
            arg = filter.getArgumentValue();
        }

        Cursor cursor = manager.read().rawQuery(sql, arg);

        if(cursor != null && cursor.moveToFirst()) addValues(cursor, obj);
    }

    public List<Entity> find(EntityFilter filter){
        //TODO create the code to find a list entity object
        setPropertyTable();

        List<Entity> entities = null;

        try{
            Log.i("Where",filter.getWhereValue());
            for(String i : filter.getArgumentValue())
                Log.i("Value",i);

            Entity entity;
            String sql = "select "+getColumnsNameAsString(true)+" from "+getName();
            String[] arg = null;

            if(filter != null){
                sql += " where " + filter.getWhereValue() +" ";
                arg = filter.getArgumentValue();
            }

            Cursor cursor = manager.read().rawQuery(sql,arg);
            if(cursor != null && cursor.moveToFirst()){
                entities = new ArrayList();
                try {
                    do{
                        entity = getClass().newInstance();
                        entity.addValues(cursor,null);
                        entities.add(entity);
                    }while (cursor.moveToNext());

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }finally {
            manager.close();
        }

        return entities;
    }

    private List<Entity> find(EntityFilter filter, Entity master){
        //TODO create the code to find a list entity object
        setPropertyTable();

        List<Entity> entities = null;

        try{
            Log.i("Where",filter.getWhereValue());
            for(String i : filter.getArgumentValue())
                Log.i("Value",i);

            Entity entity;
            String sql = "select "+getColumnsNameAsString(true)+" from "+getName();
            String[] arg = null;

            if(filter != null){
                sql += " where " + filter.getWhereValue() +" ";
                arg = filter.getArgumentValue();
            }

            Cursor cursor = manager.read().rawQuery(sql,arg);
            if(cursor != null && cursor.moveToFirst()){
                entities = new ArrayList();
                try {
                    do{
                        entity = getClass().newInstance();
                        entity.addValues(cursor,master);
                        entities.add(entity);
                    }while (cursor.moveToNext());

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }finally {
            manager.close();
        }

        return entities;
    }

    public synchronized long save(){
        return save(getContentValues());
    }

    public synchronized long save(ContentValues content){
        try{
            Long id = manager.write().insert(getName(), null, content);
            Map<String,Column> pks = property.getPrimaryKeyMap();
            for(String pk : pks.keySet())
                if(pks.get(pk).AutoIncrement())
                    try {
                        setColumn(pk,id.intValue());
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }

            return id;
        }finally {
            manager.write().close();
        }
    }

    public synchronized long save(SQLiteDatabase db){
        Long id = db.insert(getName(), null, getContentValues());
        Map<String,Column> pks = property.getPrimaryKeyMap();
        for(String pk : pks.keySet())
            if(pks.get(pk).AutoIncrement())
                try {
                    setColumn(pk,id.intValue());
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

        return id;
    }
    //endregion

    //region Override Methods

    @Override
    public boolean equals(Object o) {
        return toString().equals(o.toString());
    }

    @Override
    public String toString() {
        setPropertyTable();
        String values = "";
        for (String key : property.getPrimaryKeyMap().keySet()){
            Object v = getColumnValue(key);
            if(v != null)
                values += key+":"+v;
        }
        return getName()+"{"+values+"}";
    }

    //endregion

}