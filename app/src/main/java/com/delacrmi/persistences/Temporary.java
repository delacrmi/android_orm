package com.delacrmi.persistences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by delacrmi on 13/4/2016.
 */

class Temporary extends ArrayList<List<ColumnClass>> {

    private final int indexStart = 0;
    private int index;

    public Temporary(){
        restart();
    }

    //to know if have more rows
    public boolean next(){
        if(!isEmpty() && size() > indexStart){
            index++;
            if(index < size()){
                return true;
            }
        }

        index = indexStart-1;
        return false;
    }

    //restart the index
    public void restart(){
        index = indexStart-1;
    }

    //move to first row
    public void moveToFirst(){
        if(!isEmpty() && size() > indexStart){
            index = indexStart;
        }
    }

    //to know if i'm in the first row
    public boolean isFirst(){
        if(index == indexStart)
            return true;
        return false;
    }

    //return the row number
    public int getRowNumber(){
        if(index < indexStart)
            return 0;
        else
            return index+1;
    }

    public int getCountColumns(){
        if(size() > index )
            return get(index).size();
        return 0;
    }

    //evaluate if we are below of the start
    private boolean startOut(){
        if(!isEmpty() && size() > indexStart && size() > index){
            if (index < indexStart)
                moveToFirst();
            return true;
        }else return false;
    }

    //return String
    /*public String getString(int arg0){
        if(!startOut())
            throw new IndexOutOfBoundsException();

        String result = get(index).get(arg0-1).value;

        if(result.equals("null"))
            return null;

        return result;

    }
    //return the column name
    public String getColumnName(int arg0){
        return get(index).get(arg0).name;
    }
    //return the column type
    public int getColumnType(int arg0){
        return get(index).get(arg0).type;
    }*/

    public ColumnClass getColumnClass(int row, int col){
        return get(row).get(col);
    }

    public int getCountRows() {
        if(size() > indexStart)
            return size();
        return 0;
    }

    public List<ColumnClass> getRowAt() {
        return get(index);
    }

    /*//return integer
    public int getInt(int arg0){
        if(!startOut())
            throw new IndexOutOfBoundsException();
        return Integer.parseInt((String)get(index).get(arg0-1));
    }

    //return Long
    public BigInteger getBigInteger(int arg0){
        if(!startOut())
            throw new IndexOutOfBoundsException();
        return new BigInteger((String)get(index).get(arg0-1));
    }

    //return Long
    public Long getLong(int arg0){
        if(!startOut())
            throw new IndexOutOfBoundsException();
        return Long.parseLong((String)get(index).get(arg0-1));
    }

    //return BigDecimal
    public BigDecimal getBigDecimal(int arg0){
        if(!startOut())
            throw new IndexOutOfBoundsException();
        return new BigDecimal((String)get(index).get(arg0-1));
    }*/


}
