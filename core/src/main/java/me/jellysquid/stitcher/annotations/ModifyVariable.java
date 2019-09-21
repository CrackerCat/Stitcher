package me.jellysquid.stitcher.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface ModifyVariable {
    Target target();

    Where where();

    Slice[] slice() default {};

    int[] only() default {};

    int priority() default 0;
}
