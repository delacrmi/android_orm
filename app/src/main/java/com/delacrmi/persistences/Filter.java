package com.delacrmi.persistences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 17/02/16.
 */
abstract class Filter<Column, Value, Comparison, Condition> {
    private List<Column> nameList = new ArrayList<>();
    private Map<Column,Value> columnMap = new HashMap();
    private Map<Column, Condition> conditionMap = new HashMap();
    private Map<Column, Comparison> comparisonMap = new HashMap();

    public Filter<Column, Value, Comparison, Condition>
        addArgument(Column column, Value value, Comparison comparison, Condition condition){
        nameList.add(column);
        columnMap.put(column,value);
        comparisonMap.put(column, comparison);
        conditionMap.put(column, condition);
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

    public Map<Column, Condition> getConditionMap() {
        return conditionMap;
    }

    public abstract String getWhereValue();
    public abstract String[] getArgumentValue();
}
