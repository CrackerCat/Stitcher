package me.jellysquid.stitcher.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface Redirect {
    MethodTarget[] targets();

    MethodTarget site();

    int priority() default 0;
}
