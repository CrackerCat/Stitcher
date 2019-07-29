package me.jellysquid.stitcher.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface MethodTarget {
    Class<?> owner() default void.class;

    String value();

    String desc();
}
