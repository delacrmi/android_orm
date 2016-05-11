package com.delacrmi.persistences;

import java.lang.reflect.Field;

/**
 * Created by delacrmi on 14/4/2016.
 */
class ColumnClass {
    public String name;
    public int type;
    public String value;
    
    public int index;
    public int length;
    
    //this is the representation attribute in the entity class
    public Field field;
    
    //know if the column is a entity
    public boolean isEntity = false;
    
    //relationship name
    public String relationshipType;
    
    //relationship columns
    public String[] relationshipColumns;
    
    //if this attribute is true the column is part of the DB table
    public boolean writable;
    
    //if this attribute is false the column can be null
    public boolean notNull;
    
    //these attirbutes represent the table key
    public boolean primaryKey;
    public boolean autoIncrement;
    
    public String dateFormat;
}
