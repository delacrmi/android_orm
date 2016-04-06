package com.delacrmi.persistences;

/**
 * Created by miguel on 01/02/16.
 */

import com.delacrmi.persistences.annotation.Column;
import com.delacrmi.persistences.annotation.ManyToMany;
import com.delacrmi.persistences.annotation.ManyToOne;
import com.delacrmi.persistences.annotation.OneToMany;
import com.delacrmi.persistences.annotation.OneToOne;
import com.delacrmi.persistences.annotation.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityProperty {

    private Class<? extends Entity> tableClass;
    private String tableName;
    private String nickName;
    private Map<String,Field> fieldMap = new HashMap();
    private Map<String,Column> columnMap = new HashMap();
    private Map<String,Column> primaryKey = new HashMap();
    private AnnotationType<String, Annotation, Boolean> relationship = new AnnotationType();
    private Map<String, String[]> tableTriggers = new HashMap();
    //private List<String> relationshipName = new ArrayList<>();


    public EntityProperty(Class<? extends Entity> tableClass){

        this.tableClass = tableClass;
        setTableName();
        setElementsProperties();

    }

    private void setTableName(){
        Table table = tableClass.getAnnotation(Table.class);
        if(table.Name().equals("")) tableName = getClass().getSimpleName().toUpperCase();
        else tableName = table.Name().toUpperCase();

        if(table.NickName().equals("")) nickName = getClass().getSimpleName();
        else nickName = table.NickName();

        tableTriggers.put("before", table.BeforeToCreate());
        tableTriggers.put("after", table.AfterToCreated());

    }

    public Table getTableAnnotation(){
        return tableClass.getAnnotation(Table.class);
    }

    private void setElementsProperties(){
        Field[] fields = tableClass.getDeclaredFields();
        for(Field field : fields){
            Boolean isEntity = false;
            field.setAccessible(true);
            Annotation column = field.getAnnotation(Column.class);
            if(column != null){
                Column c = (Column) column;
                String name;
                if(c.Name().equals("")) name = field.getName().toUpperCase();
                else name = c.Name().toUpperCase();

                if(columnMap.containsKey(name))
                    throw new Error("The column name <" + name + "> is duplicated");

                fieldMap.put(name, field);
                columnMap.put(name, c);
                if(c.PrimaryKey()) primaryKey.put(name, c);

                //Adding the annotations properties for field
                Annotation[] annotations = field.getAnnotations();
                for(Annotation annotation : annotations)
                    if (annotation instanceof ManyToMany ||
                            annotation instanceof OneToMany ||
                            annotation instanceof ManyToOne ||
                            annotation instanceof OneToOne) {
                        if(field.getType().isInstance(new Entity()) ||
                                field.getType().getSimpleName().equals("List")){
                            isEntity = true;
                        }

                        relationship.put(name, annotation, isEntity);
                        //relationshipName.add(name);
                    }

            }
        }
    }

    public Map<String, Field> getFieldMap() {
        return fieldMap;
    }
    public Map<String, Annotation> getRelationships() {
        return relationship.getValuesMap();
    }
    public Map<String, Column> getColumnMap() {
        return columnMap;
    }
    public Map<String, Column> getPrimaryKeyMap(){ return  primaryKey; }
    public Map<String, String[]> getTableTriggers(){ return tableTriggers; }

    public List<String> getRelationshipNames() {
        return relationship.getKeysList();
    }
    public Map<String, Boolean>isEntityMap(){
        return relationship.isEntityType;
    }

    public String getTableName() {
        return tableName;
    }
    public String getNickName() {
        return nickName;
    }
    public Class<? extends Entity> getTableClass(){ return tableClass; }

    public class AnnotationType<Key, Value, IsEntity> {
       private Map<Key, Value> valueMap = new HashMap<>();
       private Map<Key, IsEntity> isEntityType = new HashMap<>();
       private List<Key> keys = new ArrayList<>();

        public void put(Key key, Value value, IsEntity isEntity){
            keys.add(key);
            valueMap.put(key, value);
            isEntityType.put(key, isEntity);
        }

        public Map<Key, Value> getValuesMap(){
            return valueMap;
        }

        public Map<Key, IsEntity> getIsEntityTypeMap(){
            return isEntityType;
        }

        public List<Key> getKeysList(){
            return keys;
        }

    }

}
