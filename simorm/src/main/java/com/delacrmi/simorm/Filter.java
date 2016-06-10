package com.delacrmi.simorm;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miguel on 17/02/16.
 */
abstract class Filter<Column, Value, Comparison, Condition> {
    private List<WhereCondition> whereConditions = new ArrayList<>();
    private int values = 0;

    public Filter<Column, Value, Comparison, Condition>
        addArgument(Column column, @Nullable Value value, @Nullable Comparison comparison,
                    @Nullable Condition condition){
        WhereCondition cond = new WhereCondition();

        cond.name = column;
        cond.value = value;
        cond.comparison = comparison;
        cond.condition = condition;

        whereConditions.add(cond);

        if(value != null)
            values ++;


        return this;
    }

    public List<WhereCondition> getConditionList() {
        return whereConditions;
    }

    public int countValues(){
        return values;
    }

    public abstract String getWhereValue();
    public abstract String[] getArgumentValue();

    class WhereCondition{
        Column name;
        Value value;
        Condition condition;
        Comparison comparison;
    }
}
