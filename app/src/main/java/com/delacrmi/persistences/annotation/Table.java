package com.delacrmi.persistences.annotation;

/**
 * Created by miguel on 28/01/16.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Table {
    String Name() default "";
    boolean Synchronazable() default true;
    String NickName() default "";
    String [] BeforeToCreate() default {};
    String [] AfterToCreated() default {};
}
