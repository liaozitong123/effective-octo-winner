package com.cartonerp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class OrderNumberUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNumberUtil() {}

    public static String next(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(FORMATTER);
    }
}
