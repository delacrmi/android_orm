package com.delacrmi.persistences;

/**
 * Created by delacrmi on 14/4/2016.
 */
class ColumnClass {
    public String name;
    public int type;
    public String value;
    
    private int index;
    private int length;
    private String relationshipType;
    private String [] relationshipColumns;
    
    /*returns true if the column is 
    of relationship type of the 
    otherwise returns false*/
    public boolean isRelationshipColumn(){
        return relationshipType != null ? true : false; 
    }
    
    /*return the String array structure*/
    @Nullable
    public String[] getRelationshipColumns(){
        return relationshipColumns;
    }
    
}
