package com.platform;

public class Weapon {
    private int active;
    private int[] icon;
    private int color; // 0 = red, 1 = yellow, 2 = blue, 3 = purple

    public int getActive() {
        return active;
    }

    public int getColor() {
        return color;
    }

    public int[] getIcon() {
        return icon;
    }
}
