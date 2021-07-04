package com.nowandfuture.mod.renderer.mygui;

public class Color {
    private int red;
    private int green;
    private int blue;
    private int alpha;

    public Color(int r, int g, int b, int a) {
        red = r;
        green = g;
        blue = b;
        alpha = a;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getAlpha() {
        return alpha;
    }
}
