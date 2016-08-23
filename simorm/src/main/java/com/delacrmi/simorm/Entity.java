package com.delacrmi.simorm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.delacrmi.simorm.annotation.Column;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Created by miguel on 09/10/15.
 */
public class Entity implements Serializable {

    private EntityProperty property;
    private EntityFilter entityFilter;
    private static EntityManager manager = null;
    private SimpleDateFormat dateFormat;
    private Map<String,Object> entityRelationMap = new HashMap<>();
    private int pagination = 0;

    private boolean validateSaving = true;
    private boolean isSaved = false;

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
        String create = "";
        ColumnClass column;

        for(String key : property.getColumnsMap().keySet()){
            column = property.getColumn(key);

            try {

                String t  = " " + convertToDBType(column.field.getType().getSimpleName());

                if(column.primaryKey && column.autoIncrement)
                    create = key + t + " primary key autoincrement, " +create;
                else
                    create += key + t;

                if((column.notNull || column.primaryKey) &&
                        !column.autoIncrement) create += " not null";

            } catch (Exception e) {

                if(column.relationshipType != null){

                    if(!column.writable){
                        count++;
                        continue;
                    }

                    create += getColumnByRelation(column);

                }else throw new Exception("The column " + key + "need a relationship annotation type");
            }

            count ++;
            if(count <= property.getColumnsMap().size()){
                if(!column.autoIncrement)
                    create  += ", ";
            }else if(column.autoIncrement)
                create = create.substring(0,create.length()-2);
        }

        if(create.substring(create.length()-2,create.length()).equals(", "))
            create = create.substring(0,create.length()-2);

        create = "create table "+property.getTableName()+"(" + create + ")";
        return create.toUpperCase();
    }

    private String getColumnByRelation(ColumnClass column) throws Exception{
        String columns = "";
        int ct = 1;
        ColumnClass ent;

        for (String one : column.relationshipColumns){
            try{
                ent = getEntityFromType(column.field).getProperty().getColumn(one.toUpperCase());
                columns += column.name + "_" + one + " " + convertToDBType(ent.field.getType().getSimpleName());
            }catch (NullPointerException n){
                throw new Exception("The column " + one + " don't exist in  the relationship table");
            }

            if (column.notNull) columns += " not null";
            if(ct < column.relationshipColumns.length){
                columns  += ",";
                ct++;
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
        setPropertyTable();
        List<String> list = new ArrayList<String>();
        for(String pk : property.getPrimaryKeyMap().keySet())
            list.add(pk);
        return  list;
    }

    public String getColumnsNameAsString(boolean primaryKey){
        String columns = "";
        setPropertyTable();

        for (String name: property.getColumnsMap().keySet()){
            ColumnClass column = property.getColumn(name);
            if(column.relationshipType != null){
                if(!column.writable){
                   // count++;
                    continue;
                }

                for(String postfix :  convertRelationshipToString(column,false))
                    columns += column.name+"_"+postfix.toUpperCase()+",";

            }else if(!column.primaryKey){
                columns += column.name+",";
            }else if(primaryKey){
                columns += column.name+",";
            }

            //count++;
        }

        if(columns.substring(columns.length()-1,columns.length()).equals(","))
            columns = columns.substring(0,columns.length()-1);

        return columns;
    }

    public String getColumnsNameAsWithout(String [] withoutNames){
        int count = 1;
        String columns = "";
        setPropertyTable();
        int length = property.getColumnsMap().size();

        for (String column: property.getColumnsMap().keySet()){
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

        for(String key : property.getColumnsMap().keySet())
            try {
                ColumnClass column = property.getColumn(key);
                if(column.field.getType().getSimpleName().equals("Date")){
                    json.put(key, new SimpleDateFormat(column.dateFormat)
                            .format((Date)column.field.get(this)));
                }else
                    json.put(key,column.field.get(this));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        return json;
    }

    public boolean setColumnsFromJSON(@NonNull JSONObject json){
        try{
            Iterator iterator = json.keys();

            while(iterator.hasNext()){
                String name = iterator.next().toString();

                try {
                    addValues(name.toUpperCase(),json.get(name));
                    //setEntityColumn(this);
                } catch (JSONException e) {
                    resetEntity();
                    return false;
                }

            }
            return true;
        }catch (NullPointerException e){
            resetEntity();
            return false;
        }
    }

    public JSONArray getColumnstoJSONArray(){
        setPropertyTable();
        JSONArray array = new JSONArray();

        Set<String> sets = property.getColumnsMap().keySet();
        for(String set: sets)
            array.put(set);

        return array;
    }

    public int getColumnsCount(){
        setPropertyTable();
        return property.getColumnsMap().size();
    }

    public String getName(){
        setPropertyTable();
        return property.getTableName();
    }

    public void setColumn(String column, Object value)
            throws NoSuchFieldException, InstantiationException{

        setPropertyTable();

        if(property.getColumnsMap().containsKey(column))
            try {
                Field field = property.getColumn(column).field;
                field.setAccessible(true);

                field.set(this,value);
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

    public EntityFilter createSimpleFilter(){
        EntityFilter filter = new EntityFilter("?");

        Log.e("setEntityColumn","Writable ");
        List<String> columns = getPrimariesKeys();
        for(int i = 0; i < columns.size();i++){
            String cn = columns.get(i);
            if((i+1) < columns.size())
                filter.addArgument(cn,getColumnValue(cn)+"",null,"and");
            else
                filter.addArgument(cn,getColumnValue(cn)+"",null);
        }
        return filter;
    }

    private void setEntityColumn(Entity entity){
        Field field;
        ColumnClass column;

        for (String name : property.getRelationshipNames()){
            field = property.getColumn(name).field;
            column = property.getColumn(name);
            //Annotation annotation = property.getRelationships().get(name);
            String [] relationshipArray = convertRelationshipToString(column,true);
            EntityFilter filter = new EntityFilter("?");
            Entity merry;
            Object o = null;

            if(column.writable){

                Log.e("setEntityColumn","Writable ");
                for(int i = 0; i < relationshipArray.length;i++){
                    String cn = name+"_"+relationshipArray[i].toUpperCase();
                    if((i+1) < relationshipArray.length)
                        filter.addArgument(relationshipArray[i],entityRelationMap.get(cn)+"",null,"and");
                    else
                        filter.addArgument(relationshipArray[i],entityRelationMap.get(cn)+"",null);
                }


                merry = getEntityFromType(field);
                o = merry.findOnce(filter,false);

            } else if(column.relationshipType.equals("OneToMany")/*annotation instanceof OneToMany*/){
                Log.e("setEntityColumn","OneToMany");

                merry = getEntityFromType(field);

                for (String cr : relationshipArray) {
                    Log.e("relationship", cr);

                    String[] ra = convertRelationshipToString(
                            merry.getProperty().getColumn(cr.toUpperCase()),false);
                    for (int i = 0; i < ra.length; i++) {
                        String cn = ra[i].toUpperCase();
                        Log.e("columns", "that: " + cr + "_" + cn + " this: " + cn);
                        if ((i + 1) < ra.length)
                            filter.addArgument(cr + "_" + cn, getColumnValue(cn) + "", null, "and");
                        else
                            filter.addArgument(cr + "_" + cn, getColumnValue(cn) + "", null);
                    }
                    o = merry.find(filter);
                }

            }

            try {
                if(o != null) setColumn(name,o);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    public Object getColumnValue(String columnName){
        Object o = null;
        setPropertyTable();
        Field field = property.getColumn(columnName.toUpperCase()).field;

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

        for(String key: property.getColumnsMap().keySet())
            setContentValue(cv, key, getColumnValue(key));
        return cv;
    }

    public void setContentValue(ContentValues content, String key, Object value) throws NullPointerException{
        setPropertyTable();
        ColumnClass column = property.getColumn(key);
        Log.e("Column Name",key+" !! "+(column == null));
         if(column != null && (column.notNull || (column.primaryKey && !column.autoIncrement))
                 && value == null) throw new NullPointerException("The column " + key + " can't be null");



        if(value != null) Log.i("Class " + key, value+"");
        if(value != null && value.getClass().getSimpleName().equals("Text"))
            Log.d("Super", value.getClass().getSuperclass().getSimpleName());

        if(value == null ||
                (value instanceof Integer && (Integer)value == 0 && column.primaryKey)) {
            if (column.autoIncrement) content.putNull(key);
        }else if(value.getClass().getSimpleName().equals("Integer") ||
                value.getClass().getSimpleName().equals("int")) content.put(key, (Integer)value);
        else if(value.getClass().getSimpleName().equals("String")) content.put(key,(String)value);
        else if(value.getClass().getSimpleName().equals("Long")) content.put(key,(Long)value);
        else if(value.getClass().getSimpleName().equals("BigDecimal")) content.put(key,((BigDecimal)value).toString());
        else if(value.getClass().getSimpleName().equals("Date")) content.put(key,((Date)value).getTime());
        else{

            ColumnClass annotation = property.getColumn(key);
            if(!annotation.writable)
                return;

            Entity entity = (Entity) getColumnValue(annotation.name);

            Log.i("table name", entity.getName());
            for(String keys : annotation.relationshipColumns) {
                Log.i("Add", keys+" "+entity.getColumnValue(keys.toUpperCase()));
                setContentValue(content, key+"_"+keys.toUpperCase(), entity.getColumnValue(keys.toUpperCase()));
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
            case "int": value = "INTEGER";
                break;
            case "Integer": value = "INTEGER";
                break;
            case "BigDecimal": value = "text";
                break;
            case "Long": value = "numeric";
                break;
            case "Date": value = "numeric";
                break;
            case "Double": value = "real";
                break;
            case "double": value = "real";
                break;
            case "Float": value = "real";
                break;
            case "float": value = "real";
                break;
            default:
                throw new Exception("Error converting field type: "+type);
        }

        return value;
    }

    private String[] convertRelationshipToString(ColumnClass column, boolean multi){
        String [] value = new String[0];
        if(!column.relationshipType.equals("OneToMany") || multi)
            value = column.relationshipColumns;
            /*if(annotation.annotationType().getSimpleName().equals("OneToOne"))
                value =((OneToOne)annotation).ForeingKey();
            else if(annotation.annotationType().getSimpleName().equals("OneToMany")
                    && multi)
                value = ((OneToMany)annotation).ForeingKey();
            else if(annotation.annotationType().getSimpleName().equals("ManyToOne"))
                value = ((ManyToOne)annotation).ForeingKey();
            else if(annotation.annotationType().getSimpleName().equals("ManyToMany"))
                value = ((ManyToMany)annotation).ForeingKey();*/

        return value;
    }

    public Object convertToFieldType(String fieldName, Object value)
            throws NoSuchFieldException, TypeNotPresentException {
        setPropertyTable();
        Object result;
        if(property.getColumnsMap().containsKey(fieldName)){
            ColumnClass column = property.getColumn(fieldName);

            switch (column.field.getType().getSimpleName()){
                case "String": result = value;
                    break;
                case "int": result = Integer.parseInt(value.toString());
                    break;
                case "BigDecimal": result = new BigDecimal(value.toString());
                    break;
                case "Long": result = Long.parseLong(value.toString());
                    break;
                case "Date":
                    dateFormat = new SimpleDateFormat(column.dateFormat);
                    Date l = new Date();
                    Log.e("Empty",value.toString().isEmpty()+"");
                    l.setTime(Long.parseLong(value.toString()));

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
                    if(column.isEntity)
                        result = value;
                    else
                        throw new TypeNotPresentException("Error converting field type: " +
                                column.field.getType().getSimpleName(), null);
            }

        }else throw new NoSuchFieldException("The column " + fieldName + " not exist in the Entity " +
                getClass().getSimpleName());

        return result;
    }
    //endregion

    private void addValues(List<ColumnClass> temporary, boolean find){
        for (ColumnClass column : temporary){
            String columnName = column.name.toUpperCase();
            try {
                Log.d("Add Values",columnName+" value "+column.value);
                Object o = convertToFieldType(columnName, column.value);
                if(!property.getRelationshipNames().contains(columnName))
                    setColumn(columnName, o);
                /*else if (ob != null)
                    setColumn(columnName, ob);*/
            } catch (NoSuchFieldException e) {
                Log.d("entityRelationMap",columnName+" "+column.value);
                entityRelationMap.put(columnName, column.value);
            } catch (InstantiationException e){
                e.printStackTrace();
            }
        }
        if(find)
            setEntityColumn(this);
    }

    private void addValues(String columnName,Object o){
        try {
            Log.d("Add Values",columnName+" value "+o.toString());
            o = convertToFieldType(columnName, o.toString());
            if(!property.getRelationshipNames().contains(columnName))
                setColumn(columnName, o);
        } catch (NoSuchFieldException e) {
            entityRelationMap.put(columnName, o.toString());
        } catch (InstantiationException e){
            e.printStackTrace();
        }
    }

    public void resetEntity() {
        try {
            isSaved = false;
            for(String key: property.getColumnsMap().keySet()){
                Field field = property.getColumn(key).field;
                if(field.getType().getSimpleName().equals("int") ||
                        field.getType().getSimpleName().equals("Integer"))
                    setColumn(key, new Integer(0));
                else
                    setColumn(key, null);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    //region Persistences Methods
    public synchronized Entity findOnce(EntityFilter filter){
        Temporary t = getTemporaryStructure(filter,getName(),getColumnsNameAsString(true),1);
        if(t.next()){
            addValues(t.getRowAt(),true);
        }
        return this;
    }

    private Entity findOnce(EntityFilter filter, boolean find){

        Temporary t = getTemporaryStructure(filter,getName(),getColumnsNameAsString(true),1);

        if(t.next()){
            addValues(t.getRowAt(),find);
        }

        return this;
    }

    public List<Entity> find(EntityFilter filter){
        setPropertyTable();

        List<Entity> entities =  new ArrayList();
        Temporary t = getTemporaryStructure(filter,getName(),getColumnsNameAsString(true),0);

        while (t.next()){
            try {
                Entity entity = getClass().newInstance();
                entity.addValues(t.getRowAt(),true);
                entities.add(entity);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        return entities;
    }

    private Temporary getTemporaryStructure(EntityFilter filter, String name, String columns, int limit){
        setPropertyTable();

        if(limit == 1)
            resetEntity();

        Temporary t = new Temporary();

        try{
            /*Log.i("Where",filter.getWhereValue());
            for(String i : filter.getArgumentValue())
                Log.i("Value",i);*/

            String sql = ("select "+columns+" from "+name).toUpperCase();
            String[] arg = null;

            if(filter != null){
                sql += (" where " + filter.getWhereValue() +" ").toUpperCase();
                arg = filter.getArgumentValue();
            }

            Log.i("SQL",sql+" "+arg.length);

            Cursor cursor = manager.read().rawQuery(sql,arg);
            if(cursor != null && cursor.moveToFirst()){
                do{
                    List<ColumnClass> row = new Vector<>();
                    for (int index = 0; index < cursor.getColumnNames().length; index++){
                        String columnName = cursor.getColumnName(index).toUpperCase();
                        int col = cursor.getColumnIndex(columnName);

                        String v = cursor.getString(cursor.getColumnIndex(columnName));

                        if(v != null){
                            ColumnClass c = new ColumnClass();
                            c.name = columnName;
                            c.type = cursor.getType(col);
                            c.value = cursor.getString(col);

                            row.add( c);
//                            Log.i(columnName,cursor.getString(cursor.getColumnIndex(columnName)));
                        }/*else{
                            Log.i(columnName,"null");
                        }*/

                    }

                    t.add(row);

                    if(t.getCountRows() == limit && limit > 0)
                        break;

                }while (cursor.moveToNext());

            }
        }finally {
            manager.close();
        }

        return t;
    }

    public synchronized long save(){
        validateSaving = true;
        return save(getContentValues());
    }

    public synchronized long save(ContentValues content){
        /*TODO Validate the relationship tables
            Ex.:
                user.text(Text){ManyToOne} <=> text.users(User){OneToMany}
                user.text(Text){OneToOne} <=> text.user(User){OneToOne}

                user.text(Text){OneToOne} !<=> text.users(User){OneToMany or ManyToOne}
                user.text(Text){OneToMany} !<=> text.users(User){OneToMany or OneToOne}
                user.text(Text){ManyToOne} !<=> text.users(User){ManyToOne or OneToOne}
        */

        try{

            if(validateSaving)
                catchNullValuesFromContent(content);
            if(!isSaved) {
                Long id = manager.write().insert(getName(), null, content);
                Map<String, Column> pks = property.getPrimaryKeyMap();
                for (String pk : pks.keySet())
                    if (pks.get(pk).AutoIncrement())
                        try {
                            setColumn(pk, id.intValue());
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        }
                isSaved = true;
                return id;
            }
            return 0l;

        }finally {
            validateSaving = true;
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

    public synchronized int  update(EntityFilter filter){

        String sql = null;
        String[] arg = null;

        if(filter != null){
            sql = filter.getWhereValue().toUpperCase();
            arg = filter.getArgumentValue();
        }

        try {
            return manager.read().update(getName(),getContentValues(),sql,arg);
        }finally {
            manager.write().close();
        }
    }

    public synchronized int update(){
        return update(getEntityFilter());
    }

    public synchronized int  delete(EntityFilter filter){

        String sql = null;
        String[] arg = null;

        if(filter != null){
            sql = filter.getWhereValue().toUpperCase();
            arg = filter.getArgumentValue();
        }

        try {
            return manager.read().delete(getName(),sql,arg);
        }finally {
            manager.write().close();
        }
    }

    public synchronized int delete(){return delete(getEntityFilter());}

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
            if(!values.equals(""))
                values += ", ";
            if(v != null)
                values += key+":"+v;
        }
        return getName()+"{"+values+"}";
    }

    //endregion

    //throw an exception if the value could't be null
    private boolean catchNullValuesFromContent(ContentValues content){
        setPropertyTable();

        for(String key : property.getColumnsMap().keySet()){
            ColumnClass column = property.getColumn(key);

            if(column.relationshipType == null && !content.containsKey(key) && column.notNull)
                throw new NullPointerException("Table : "+getName()+" The column "+key+" can't be null");
            else if(column.relationshipType != null){
                Log.e("Null array",key);
                for (String key_s : column.relationshipColumns)
                    if (!content.containsKey(key + "_" + key_s.toUpperCase()) && column.notNull)
                        throw new NullPointerException("Table : " + getName() + " The virtual column " + key_s.toUpperCase() + " can't be null");
            }
        }
        return true;
    }

    public Entity refresh(){
        setEntityColumn(this);
        return this;

    }
}