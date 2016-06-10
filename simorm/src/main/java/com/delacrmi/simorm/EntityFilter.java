package com.delacrmi.simorm;

/**
 * Created by atorres on 12/11/2015.
 */
public class EntityFilter extends Filter<String,String,String,String>{

    private String parameterInjector = "?";

    public EntityFilter(){}
    public EntityFilter(String parameterInjector){
        this.parameterInjector = parameterInjector;
    }

    public void setParameterInjector(String parameterInjector){
        this.parameterInjector = parameterInjector;
    }
    public String getParameterInjector(){
        return parameterInjector;
    }

    @Override
    public String getWhereValue() {
        String value = "";

        for (WhereCondition condition : getConditionList()){
            if(value != "") value += " ";

            String comp;
            if(condition.comparison == null)
                comp = " =";
            else
                comp = condition.comparison
                        .replace(" ","").equals("") ? " =" : " "+condition.comparison;

            value += condition.name + comp +
                    (condition.value == null ? "" : " "+parameterInjector) +
                    (condition.condition == null  ? "" : " "+condition.condition);
        }

        return value;
    }

    @Override
    public String[] getArgumentValue() {
        String[] value = new String[countValues()];
        int count = 0;
        for (WhereCondition condition : getConditionList())
            if(condition.value != null){
                value[count] = condition.value;
                count++;
            }

        if(count == 0)
            return null;

        return value;
    }

    @Override
    public EntityFilter addArgument(String column, String value, String comparison, String condition) {
        return (EntityFilter)super.addArgument(column, value, comparison, condition);
    }

    public EntityFilter addArgument(String column, String value, String comparison) {
        return addArgument(column, value,comparison,null);
    }

    public EntityFilter addArgument(String column, String value){
        return addArgument(column, value,null,null);
    }
}
