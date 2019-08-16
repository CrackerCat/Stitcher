package me.jellysquid.stitcher.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface Where {
    At at();

    MethodTarget[] method() default {};

    String varName() default "";

    int varIndex() default -1;

    int offset() default 0;

    String constant() default "";

    int[] only() default { };

    int priority() default 0;
}
