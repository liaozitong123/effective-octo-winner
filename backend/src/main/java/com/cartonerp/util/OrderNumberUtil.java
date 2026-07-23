package com.cartonerp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class OrderNumberUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private OrderNumberUtil() {}

    public static String next(String prefix) {
        int suffix = COUNTER.updateAndGet(current -> current >= 999 ? 0 : current + 1);
        return prefix + "-" + LocalDateTime.now().format(FORMATTER) + String.format("%03d", suffix);
    }
}
