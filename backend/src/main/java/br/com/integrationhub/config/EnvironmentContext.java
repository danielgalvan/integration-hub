package br.com.integrationhub.config;

public final class EnvironmentContext {

    private static final ThreadLocal<String> CONTEXT =
            new ThreadLocal<>();

    private EnvironmentContext() {
    }

    public static void set(String environment) {
        CONTEXT.set(environment);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
