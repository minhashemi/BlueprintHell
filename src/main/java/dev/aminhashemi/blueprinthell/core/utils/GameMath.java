package dev.aminhashemi.blueprinthell.core.utils;

import java.awt.Point;

/**
 * Utility class for mathematical operations used in the game.
 * Follows Single Responsibility Principle by focusing only on math operations.
 */
public final class GameMath {
    
    // Private constructor to prevent instantiation
    private GameMath() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Calculates the distance between two points
     * @param p1 First point
     * @param p2 Second point
     * @return Distance between the points
     */
    public static double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }
    
    /**
     * Calculates the distance between two points
     * @param x1 X coordinate of first point
     * @param y1 Y coordinate of first point
     * @param x2 X coordinate of second point
     * @param y2 Y coordinate of second point
     * @return Distance between the points
     */
    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    /**
     * Calculates the distance between two points
     * @param x1 X coordinate of first point
     * @param y1 Y coordinate of first point
     * @param x2 X coordinate of second point
     * @param y2 Y coordinate of second point
     * @return Distance between the points
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    /**
     * Clamps a value between min and max
     * @param value Value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clamps a value between min and max
     * @param value Value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clamps a value between min and max
     * @param value Value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Linear interpolation between two values
     * @param start Start value
     * @param end End value
     * @param t Interpolation factor (0.0 to 1.0)
     * @return Interpolated value
     */
    public static double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }
    
    /**
     * Linear interpolation between two points
     * @param start Start point
     * @param end End point
     * @param t Interpolation factor (0.0 to 1.0)
     * @return Interpolated point
     */
    public static Point lerp(Point start, Point end, double t) {
        int x = (int) lerp(start.x, end.x, t);
        int y = (int) lerp(start.y, end.y, t);
        return new Point(x, y);
    }
    
    /**
     * Calculates the angle between two points in radians
     * @param p1 First point
     * @param p2 Second point
     * @return Angle in radians
     */
    public static double angle(Point p1, Point p2) {
        return Math.atan2(p2.y - p1.y, p2.x - p1.x);
    }
    
    /**
     * Calculates the angle between two points in degrees
     * @param p1 First point
     * @param p2 Second point
     * @return Angle in degrees
     */
    public static double angleDegrees(Point p1, Point p2) {
        return Math.toDegrees(angle(p1, p2));
    }
    
    /**
     * Converts degrees to radians
     * @param degrees Angle in degrees
     * @return Angle in radians
     */
    public static double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }
    
    /**
     * Converts radians to degrees
     * @param radians Angle in radians
     * @return Angle in degrees
     */
    public static double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }
    
    /**
     * Normalizes an angle to be between 0 and 2π
     * @param angle Angle in radians
     * @return Normalized angle
     */
    public static double normalizeAngle(double angle) {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }
    
    /**
     * Calculates the midpoint between two points
     * @param p1 First point
     * @param p2 Second point
     * @return Midpoint
     */
    public static Point midpoint(Point p1, Point p2) {
        return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }
    
    /**
     * Checks if a point is within a circle
     * @param point Point to check
     * @param center Circle center
     * @param radius Circle radius
     * @return True if point is within circle
     */
    public static boolean isPointInCircle(Point point, Point center, double radius) {
        return distance(point, center) <= radius;
    }
    
    /**
     * Checks if a point is within a rectangle
     * @param point Point to check
     * @param x Rectangle X coordinate
     * @param y Rectangle Y coordinate
     * @param width Rectangle width
     * @param height Rectangle height
     * @return True if point is within rectangle
     */
    public static boolean isPointInRectangle(Point point, int x, int y, int width, int height) {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height;
    }
    
    /**
     * Calculates the percentage of a value relative to a total
     * @param value Value to calculate percentage for
     * @param total Total value
     * @return Percentage (0.0 to 100.0)
     */
    public static double percentage(double value, double total) {
        if (total == 0) return 0;
        return (value / total) * 100.0;
    }
    
    /**
     * Rounds a double to a specified number of decimal places
     * @param value Value to round
     * @param decimalPlaces Number of decimal places
     * @return Rounded value
     */
    public static double round(double value, int decimalPlaces) {
        double factor = Math.pow(10, decimalPlaces);
        return Math.round(value * factor) / factor;
    }
}
