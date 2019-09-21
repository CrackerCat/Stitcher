package me.jellysquid.stitcher.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface Where {
    At at();

    Target[] method() default {};

    String varName() default "";

    int varIndex() default -1;

    int offset() default 0;

    String constant() default "";

    int[] only() default { };

    int priority() default 0;

    enum At {
        /**
         * See {@link me.jellysquid.stitcher.matchers.at.AtTail}.
         */
        TAIL,

        /**
         * See {@link me.jellysquid.stitcher.matchers.at.AtHead}
         */
        HEAD,

        /**
         * See {@link me.jellysquid.stitcher.matchers.at.AtConstant}
         */
        CONSTANT,

        /**
         * See {@link me.jellysquid.stitcher.matchers.at.AtInvoke}
         */
        INVOKE,

        /**
         * See {@link me.jellysquid.stitcher.matchers.at.AtVariable}
         */
        LOAD,

        /**
         * See {@link me.jellysquid.stitcher.matchers.at.AtVariable}
         */
        STORE
    }
}
