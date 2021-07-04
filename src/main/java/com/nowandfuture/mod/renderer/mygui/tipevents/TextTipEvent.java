package com.nowandfuture.mod.renderer.mygui.tipevents;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.nowandfuture.mod.renderer.mygui.RootView;
import net.minecraft.util.text.ITextComponent;

public class TextTipEvent extends RootView.AbstractDataTipEvent<String> {
    public TextTipEvent(String text) {
        super(text);
    }

    public TextTipEvent(String text, long lifeTime) {
        super(text, lifeTime);
    }

    public TextTipEvent(String text, long lifeTime, Position position) {
        super(text, lifeTime, position);
    }

    @Override
    protected void onDrawData(MatrixStack stack, int posX, int posY, int mouseX, int mouseY, float partialTicks) {
        rootView.getGuiContainer().renderTooltip(stack, ITextComponent.getTextComponentOrEmpty(data), posX, posY);
    }
}
