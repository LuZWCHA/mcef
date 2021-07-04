package com.nowandfuture.mod.renderer.mygui.api;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface MyGui extends IEvents.IInputEvent {
    int getX();

    int getY();

    void setX(int x);

    void setY(int y);

    int getWidth();

    int getHeight();

    void setWidth(int width);

    void setHeight(int height);

    void draw(MatrixStack stack, int mouseX, int mouseY, float partialTicks);

    void draw2(MatrixStack stack, int mouseX, int mouseY, float partialTicks);
}
