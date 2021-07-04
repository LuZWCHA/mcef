package com.nowandfuture.mod.renderer.mygui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.nowandfuture.mod.renderer.mygui.layouts.FrameLayout;

class TopView extends FrameLayout {

    TopView(RootView rootView) {
        super(rootView, null);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int state) {
        return super.mousePressed(mouseX, mouseY, state);
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }
}
