package me.jellysquid.stitcher.annotations;

public enum At {
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
