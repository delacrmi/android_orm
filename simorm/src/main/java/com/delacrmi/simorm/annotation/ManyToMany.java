package com.delacrmi.simorm.annotation;

/**
 * Created by miguel on 01/02/16.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ManyToMany {
    String RelationshipTable();
    String[] ForeingKey() default {};
}
