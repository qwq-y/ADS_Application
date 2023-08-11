package com.example.objectelimination.utils.models;

public class Point {
    private int x;
    private int y;
    private int isLongPress;    // 1: long, 2: short

    public Point() {
    }

    public Point(int x, int y, int isLongPress) {
        this.x = x;
        this.y = y;
        this.isLongPress = isLongPress;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(int isLongPress) {
        this.isLongPress = isLongPress;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getIsLongPress() {
        return isLongPress;
    }

    public void setIsLongPress(int isLongPress) {
        this.isLongPress = isLongPress;
    }
}
