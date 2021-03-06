package com.delacrmi.simorm.annotation;

/**
 * Created by miguel on 28/01/16.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    String Name() default "";

    boolean PrimaryKey() default false;

    boolean AutoIncrement() default false;

    boolean NotNull() default false;

    String DateFormat() default "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    
    int Length() default 0;
}
