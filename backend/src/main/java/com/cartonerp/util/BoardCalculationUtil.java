package com.cartonerp.util;

public final class BoardCalculationUtil {
    private BoardCalculationUtil() {}

    public record Result(Double boardArea, Double totalArea, Double boardUnitPrice, Double profitRate,
                         Double boardAmount, Double actualAmount) {}

    public static Result calculate(Double boardLength, Double boardWidth, Integer boardQty,
                                   Double materialBasePrice, Double discountRate, Double boardUnitPriceInput,
                                   Double unitPrice, Integer actualQty) {
        double boardArea = safeDouble(boardLength) > 0 && safeDouble(boardWidth) > 0
            ? round6(safeDouble(boardLength) * safeDouble(boardWidth) / 10000.0)
            : 0.0;
        double totalArea = round6(boardArea * safeInt(boardQty));
        double boardUnitPrice = resolveBoardUnitPrice(materialBasePrice, discountRate, boardUnitPriceInput);
        double profitRate = safeDouble(unitPrice) > 0
            ? round2((safeDouble(unitPrice) - boardUnitPrice) / safeDouble(unitPrice) * 100.0)
            : 0.0;
        double boardAmount = round2(totalArea * boardUnitPrice);
        double actualAmount = round2(safeInt(actualQty) * boardArea * boardUnitPrice);

        return new Result(boardArea, totalArea, boardUnitPrice, profitRate, boardAmount, actualAmount);
    }

    private static int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private static double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private static double resolveBoardUnitPrice(Double materialBasePrice, Double discountRate, Double boardUnitPriceInput) {
        if (safeDouble(boardUnitPriceInput) > 0.0) {
            return round4(safeDouble(boardUnitPriceInput));
        }
        if (materialBasePrice != null && discountRate != null) {
            return round4(safeDouble(materialBasePrice) * safeDouble(discountRate) / 100.0);
        }
        return 0.0;
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
