package com.nowandfuture.mod.renderer.mygui;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.jetbrains.annotations.NotNull;

public class Button extends View {


    public Button(@NotNull RootView rootView) {
        super(rootView);
    }

    public Button(@NotNull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
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
}
