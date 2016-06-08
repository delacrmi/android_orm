package com.delacrmi.persistences;

/**
 * Created by miguel on 01/02/16.
 */

import android.support.annotation.Nullable;

import com.delacrmi.persistences.annotation.Column;
import com.delacrmi.persistences.annotation.ManyToOne;
import com.delacrmi.persistences.annotation.OneToMany;
import com.delacrmi.persistences.annotation.OneToOne;
import com.delacrmi.persistences.annotation.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EntityProperty {

    private Class<? extends Entity> tableClass;
    private String tableName;
    private String nickName;
    private Map<String,Field> fieldMap = new HashMap();
    private Map<String,Column> primaryKey = new HashMap();
    private Map<String, String[]> tableTriggers = new HashMap();
    
    private Map<String,ColumnClass> columns = new HashMap();
    private List<String> relationship = new ArrayList();
    private int columnCount = 0;
    
    public EntityProperty(Class<? extends Entity> tableClass){

        this.tableClass = tableClass;
        Table table = tableClass.getAnnotation(Table.class);

        tableName =  setTableName(this.tableClass);
        setNickName(table);
        setMethods(table);

        setElementsProperties();

    }

    private String setTableName(Class<? extends Entity> tableClass){

        Table table = tableClass.getAnnotation(Table.class);

        if(table.Name().equals("")) return tableClass.getSimpleName().toUpperCase();
        else return table.Name().toUpperCase();

    }

    private void setNickName(Table table){
        if(table.NickName().equals("")) nickName = getClass().getSimpleName();
        else nickName = table.NickName();
    }

    private void setMethods(Table table){
        tableTriggers.put("before", table.BeforeToCreate());
        tableTriggers.put("after", table.AfterToCreated());
    }

    public Table getTableAnnotation(){
        return tableClass.getAnnotation(Table.class);
    }

    private void setElementsProperties(){
        Field[] fields = tableClass.getDeclaredFields();
        ColumnClass columnClass;
        for(Field field : fields){
            columnCount ++;
            field.setAccessible(true);
            Annotation column = field.getAnnotation(Column.class);
            
            if(column != null){
                Column c = (Column) column;
                
                String name;
                if(c.Name().equals("")) name = field.getName().toUpperCase();
                else name = c.Name().toUpperCase();

                if(columns.containsKey(name))
                    throw new Error("The column name <" + name + "> is duplicated");

                columnClass = new ColumnClass();
                columnClass.name = name;
                columnClass.field = field;
                columnClass.notNull = c.NotNull();
                columnClass.primaryKey = c.PrimaryKey();
                columnClass.autoIncrement = c.AutoIncrement();
                columnClass.dateFormat = c.DateFormat();
                columnClass.length = c.Length();
                
                fieldMap.put(name, field);

                if(c.PrimaryKey()) primaryKey.put(name, c);

                //Adding the annotations properties for field
                Annotation[] annotations = field.getAnnotations();
                for(Annotation annotation : annotations)
                    if (annotation instanceof OneToMany ||
                            annotation instanceof ManyToOne ||
                            annotation instanceof OneToOne) {
                        columnClass.relationshipType = annotation.annotationType().getSimpleName();
                        columnClass.relationshipColumns = convertRelationshipToString(columnClass.relationshipType,annotation);

                        Class cClass = getEntityClassFromType(field);
                        if(cClass != null && cClass.isInstance(new Entity()))
                            columnClass.isEntity = true;
                        
                        columnClass.writable = isWritable(annotation);
                        relationship.add(name);
                    }
                columns.put(name,columnClass);
            }
        }
    }
    
    public ColumnClass getColumn(String name) {
        name = name.toUpperCase();
        return columns.get(name);
    }
    
    public Map<String, ColumnClass> getColumnsMap() {
        return columns;
    }

    public int getColumnCount(){return  columnCount;}

    public Map<String, Column> getPrimaryKeyMap(){ return  primaryKey; }
    public Map<String, String[]> getTableTriggers(){ return tableTriggers; }

    public List<String> getRelationshipNames() {
        return relationship;
    }

    public String getTableName() {
        return tableName;
    }
    public String getNickName() {
        return nickName;
    }
    
    public Class<? extends Entity> getTableClass(){ return tableClass; }

    @Nullable
    public Class<? extends Entity> getEntityClassFromType(Field field){
        Class value = null;
        
        if(field.getType().getSimpleName().equals("List")){
            Type genericFieldType = field.getGenericType();
            if(genericFieldType instanceof ParameterizedType){
                ParameterizedType aType = (ParameterizedType) genericFieldType;
                Type[] fieldArgTypes = aType.getActualTypeArguments();
                for(Type fieldArgType : fieldArgTypes) {
                    Class c = getLastClass((Class)fieldArgType);
                    if (c != null) {
                        value = c;
                        break;
                    }
                }
            }
        }else if (getLastClass(field.getType()) != null) {
            value = field.getType();
        }
        return value;
    }

    @Nullable
    private Class getLastClass(Class cl){
        try{
            if(cl.getSuperclass() != null)
                return getLastClass(cl.getSuperclass());
        }catch (NullPointerException e){}

        if(cl.isInstance(new Entity()))
            return cl;
        else
            return null;
    }

    private String[] convertRelationshipToString(String type,Annotation annotation){
        String [] value = new String[0];
            if(type.equals("OneToOne"))
                value =((OneToOne)annotation).ForeingKey();
            else if(type.equals("OneToMany"))
                value = ((OneToMany)annotation).ForeingKey();
            else if(type.equals("ManyToOne"))
                value = ((ManyToOne)annotation).ForeingKey();

        return value;
    }
    
    private boolean isWritable(Annotation annotation){
        boolean value = false;
        if(annotation instanceof OneToOne)
            value = ((OneToOne)annotation).Create();
        else if(annotation instanceof ManyToOne)
            value = true;
        return value;
    }

}
