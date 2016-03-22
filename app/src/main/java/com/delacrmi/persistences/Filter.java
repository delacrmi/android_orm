package com.delacrmi.persistences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 17/02/16.
 */
public abstract class Filter<Column, Value, Comparison, Where> {
    private List<Column> nameList = new ArrayList<>();
    private Map<Column,Value> columnMap = new HashMap();
    private Map<Column, Where> conditionMap = new HashMap();
    private Map<Column, Comparison> comparisonMap = new HashMap();

    public Filter<Column, Value, Comparison, Where>
        addArgument(Column column, Value value, Comparison comparison, Where where){
        nameList.add(column);
        columnMap.put(column,value);
        comparisonMap.put(column, comparison);
        conditionMap.put(column,where);
        return this;
    }

    public Filter<Column, Value, Comparison, Where>
    addArgument(Column column, Value value, Comparison comparison){
        nameList.add(column);
        columnMap.put(column,value);
        comparisonMap.put(column, comparison);
        conditionMap.put(column,null);
        return this;
    }

    public List<Column> getNameList() {
        return nameList;
    }

    public Map<Column, Value> getColumnMap() {
        return columnMap;
    }

    public Map<Column, Comparison> getComparisonMap() {
        return comparisonMap;
    }

    public Map<Column, Where> getConditionMap() {
        return conditionMap;
    }

    public abstract String getWhereValue();
    public abstract String[] getArgumentValue();
}
