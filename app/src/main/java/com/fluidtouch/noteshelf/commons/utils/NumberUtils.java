package com.fluidtouch.noteshelf.commons.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class NumberUtils {

    public static final float FLOAT_EPSILON = 0.001f;
    public static final double DOUBLE_EPSILON = 0.001d;

    private NumberUtils() {
        throw new IllegalStateException("NumberUtils class");
    }

    /**
     * Method for detecting if two floats are almost equal (epsilon {@link NumberUtils#FLOAT_EPSILON})
     * Inconsistencies are to be expected, due to the nature of float values in java
     *
     * @param lhs a float
     * @param rhs another float
     * @return true if equal, else false
     */
    public static boolean isEqual(float lhs, float rhs) {
        return isEqual(lhs, rhs, FLOAT_EPSILON);
    }

    /**
     * Method for detecting if two floats are almost equal
     * Inconsistencies are to be expected, due to the nature of float values in java
     *
     * @param lhs     a float
     * @param rhs     another float
     * @param epsilon The precision of the measurement
     * @return true if equal, else false
     */
    public static boolean isEqual(float lhs, float rhs, float epsilon) {
        return Float.compare(lhs, rhs) == 0 || Math.abs(lhs - rhs) <= epsilon;
    }

    /**
     * Method for detecting if two floats are almost equal (epsilon {@link NumberUtils#DOUBLE_EPSILON})
     * Inconsistencies are to be expected, due to the nature of double values in java
     *
     * @param lhs a float
     * @param rhs another float
     * @return true if equal, else false
     */
    public static boolean isEqual(double lhs, double rhs) {
        return isEqual(lhs, rhs, DOUBLE_EPSILON);
    }

    /**
     * Method for detecting if two floats are almost equal
     * Inconsistencies are to be expected, due to the nature of double values in java
     *
     * @param lhs     a float
     * @param rhs     another float
     * @param epsilon The precision of the measurement
     * @return true if equal, else false
     */
    public static boolean isEqual(double lhs, double rhs, double epsilon) {
        return Double.compare(lhs, rhs) == 0 || Math.abs(lhs - rhs) <= epsilon;
    }

    /**
     * Clamp the current value in between a min and max value.
     *
     * @param min     the lower bound
     * @param current value to check
     * @param max     the upper bound
     * @return {@code min} if {@code current} if less than {@code min},
     * or {@code max} if {@code current} is greater than {@code max},
     * else {@code current}.
     */
    public static float clamp(float min, float current, float max) {
        return Math.max(min, Math.min(current, max));
    }

    /**
     * Clamp the current value in between a min and max value.
     *
     * @param min     the lower bound
     * @param current value to check
     * @param max     the upper bound
     * @return {@code min} if {@code current} if less than {@code min},
     * or {@code max} if {@code current} is greater than {@code max},
     * else {@code current}.
     */
    public static long clamp(long min, long current, long max) {
        return Math.max(min, Math.min(current, max));
    }

    /**
     * Clamp the current value in between a min and max value.
     *
     * @param min     the lower bound
     * @param current value to check
     * @param max     the upper bound
     * @return {@code min} if {@code current} if less than {@code min},
     * or {@code max} if {@code current} is greater than {@code max},
     * else {@code current}.
     */
    public static int clamp(int min, int current, int max) {
        return Math.max(min, Math.min(current, max));
    }

    /**
     * Clamp the current value in between a min and max value.
     *
     * @param min     the lower bound
     * @param current value to check
     * @param max     the upper bound
     * @return {@code min} if {@code current} if less than {@code min},
     * or {@code max} if {@code current} is greater than {@code max},
     * else {@code current}.
     */
    public static double clamp(double min, double current, double max) {
        return Math.max(min, Math.min(current, max));
    }

    public static int gcd(int p, int q) {
        if (q == 0) return p;
        else return gcd(q, p % q);
    }

    public static float calculateDistanceBetweenPoints(float x1, float y1, float x2, float y2) {

        final float side1 = x2 - x1;
        final float side2 = y2 - y1;

        return (float) Math.sqrt(side1 * side1 + side2 * side2);
    }

    public static double getBytesInGB(long bytes) {
        double result = Long.valueOf(bytes).doubleValue();
        result /= (Math.pow(1024, 3));
        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        result = Double.parseDouble(decimalFormat.format(result));
        return result;
    }

    public static int calculatePercentage(long val1, long val2) {
        return (int) (((double) val1 / (double) val2) * 100);
    }

    public static float roundFloatValue(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
