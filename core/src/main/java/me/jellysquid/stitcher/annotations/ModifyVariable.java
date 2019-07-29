package me.jellysquid.stitcher.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface ModifyVariable {
    MethodTarget target();

    Where where();

    Slice[] slice() default {};

    int[] only() default {};
}
