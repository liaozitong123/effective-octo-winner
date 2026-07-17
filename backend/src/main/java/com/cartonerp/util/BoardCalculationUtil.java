package com.cartonerp.util;

public final class BoardCalculationUtil {
    private BoardCalculationUtil() {}

    public record Result(Double boardLength, Double boardWidth, Double boardArea,
                         Double totalArea, Double boardUnitPrice, Double boardAmount) {}

    public static Result calculate(String spec, String boxType, String stitchType, Integer cutCount,
                                   Integer boardQty, Double materialBasePrice, Double discountRate) {
        double[] parts = parseSpec(spec);
        if (parts.length < 2 || isBlank(boxType) || isBlank(stitchType)) return null;

        double length = parts[0];
        double width = parts[1];
        double height = parts.length >= 3 ? parts[2] : 0.0;
        Double boardLength = calculateLength(length, width, boxType, stitchType);
        Double boardWidth = calculateWidth(width, height, boxType, cutCount);
        if (boardLength == null || boardWidth == null) return null;

        double boardArea = round6(boardLength * boardWidth / 10000.0);
        double totalArea = round6(boardArea * safeInt(boardQty));
        double boardUnitPrice = round4(safeDouble(materialBasePrice) * safeDiscount(discountRate));
        double boardAmount = round2(totalArea * boardUnitPrice);

        return new Result(round4(boardLength), round4(boardWidth), boardArea, totalArea, boardUnitPrice, boardAmount);
    }

    private static Double calculateLength(double length, double width, String boxType, String stitchType) {
        if ("纸板".equals(boxType)) return length;
        return switch (stitchType) {
            case "一片成" -> (length + width) * 2 + 3;
            case "两片成" -> length + width + 3;
            case "四片成" -> (length + width + 7) / 2;
            default -> null;
        };
    }

    private static Double calculateWidth(double width, double height, String boxType, Integer cutCount) {
        return switch (boxType) {
            case "纸板" -> width;
            case "中封箱" -> 2 + height + 2 * width + 1.8;
            case "全盖箱" -> safeInt(cutCount) > 0 ? (2 * width + height) * safeInt(cutCount) - 1 : null;
            case "平口箱" -> safeInt(cutCount) > 0 ? (width + height) * safeInt(cutCount) + 1.8 : null;
            default -> null;
        };
    }

    private static double[] parseSpec(String spec) {
        if (isBlank(spec)) return new double[0];
        String[] raw = spec.trim().split("[*xX×]");
        double[] values = new double[raw.length];
        for (int i = 0; i < raw.length; i++) {
            try {
                values[i] = Double.parseDouble(raw[i].trim());
                if (values[i] <= 0) return new double[0];
            } catch (NumberFormatException e) {
                return new double[0];
            }
        }
        return values;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
