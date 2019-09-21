package me.jellysquid.stitcher.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@java.lang.annotation.Target({ElementType.FIELD, ElementType.METHOD})
public @interface Shadow {
    String value();
}
