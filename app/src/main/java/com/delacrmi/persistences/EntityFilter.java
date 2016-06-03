package com.delacrmi.persistences;

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

        for (String key : getNameList()){
            String comp;
            if(getComparisonMap().get(key) == null)
                comp = " = ";
            else
                comp = getComparisonMap().get(key)
                        .replace(" ","").equals("") ? " = " : " "+ getComparisonMap().get(key)+" ";

            value += key + comp + parameterInjector + " " +
                    (getConditionMap().get(key) == null  ? "" : getConditionMap().get(key)) +" ";
        }

        return value;
    }

    @Override
    public String[] getArgumentValue() {
        String[] value = new String[getConditionMap().size()];
        int count = 0;
        for (String key : getNameList()){
            value[count] = getColumnMap().get(key);
            count++;
        }
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
