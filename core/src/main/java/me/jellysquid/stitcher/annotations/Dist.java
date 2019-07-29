package me.jellysquid.stitcher.annotations;

public enum Dist {
    CLIENT,
    DEDICATED_SERVER,
    ANY;

    public boolean applies(Dist other) {
        if (other == this) {
            return true;
        }

        return other == Dist.ANY;
    }

    public static Dist fromName(String name) {
        for (Dist dist : Dist.values()) {
            if (name.equals(dist.name())) {
                return dist;
            }
        }

        throw new IllegalArgumentException("No distribution exists with name: '" + name + "'");
    }
}
