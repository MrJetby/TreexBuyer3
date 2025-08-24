package me.jetby.treexBuyer.menus;

import java.util.function.Supplier;

public record Placeholder(String placeholder, Supplier<Object> replacement) {

    public static Placeholder of(String placeholder, Supplier<Object> replacement) {
        return new Placeholder(placeholder, replacement);
    }

    public String value() {
        return String.valueOf(replacement.get());
    }

    public String replace(String string) {
        return string.replace(placeholder,value());
    }
}
