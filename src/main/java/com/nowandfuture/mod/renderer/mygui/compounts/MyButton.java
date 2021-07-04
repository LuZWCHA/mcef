package com.nowandfuture.mod.renderer.mygui.compounts;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

public class MyButton extends ExtendedButton {

    public MyButton(int xPos, int yPos, int width, int height, ITextComponent displayString, IPressable handler) {
        super(xPos, yPos, width, height, displayString, handler);
    }

    public void setHovered(boolean isHovered) {
        this.isHovered = isHovered;
    }
}
