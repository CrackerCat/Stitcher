package me.jellysquid.stitcher.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Inject {
    Target target();

    Where where();

    Slice[] slice() default {};

    int priority() default 0;
}
