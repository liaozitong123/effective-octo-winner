package com.cartonerp.util;

public final class BoardCalculationUtil {
    private BoardCalculationUtil() {}

    public record Result(Double boardArea, Double totalArea, Double boardUnitPrice, Double boardAmount) {}

    public static Result calculate(Double boardLength, Double boardWidth, Integer boardQty,
                                   Double materialBasePrice, Double discountRate) {
        double boardArea = safeDouble(boardLength) > 0 && safeDouble(boardWidth) > 0
            ? round6(safeDouble(boardLength) * safeDouble(boardWidth) / 10000.0)
            : 0.0;
        double totalArea = round6(boardArea * safeInt(boardQty));
        double boardUnitPrice = round4(safeDouble(materialBasePrice) * safeDiscount(discountRate));
        double boardAmount = round2(totalArea * boardUnitPrice);

        return new Result(boardArea, totalArea, boardUnitPrice, boardAmount);
    }

    private static int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private static double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private static double safeDiscount(Double value) {
        return value != null ? value : 1.0;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static double round6(double value) {
        return Math.round(value * 1000000.0) / 1000000.0;
    }
}
