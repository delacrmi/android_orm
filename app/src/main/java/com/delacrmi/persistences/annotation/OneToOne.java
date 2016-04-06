package com.delacrmi.persistences.annotation;

/**
 * Created by miguel on 01/02/16.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {
    String[] ForeingKey() default {};
    boolean Create() default true;
    String FieldName() default "";
}
