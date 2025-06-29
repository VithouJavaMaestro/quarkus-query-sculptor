package io.github.querysculptor;

public final class Assert {
    private Assert() {}

    public static void notNull(Object val, String errorMessage) {
        if (val  == null) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public static void notNull(Object val) {
        if (val == null) {
            throw new NullPointerException();
        }
    }
}
