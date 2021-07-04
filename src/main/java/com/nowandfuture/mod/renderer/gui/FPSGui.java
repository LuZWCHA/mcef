package com.nowandfuture.mod.renderer.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.montoyo.mcef.Config;
import net.montoyo.mcef.MCEF;

public class FPSGui extends Screen {
    private Slider slider;

    protected FPSGui(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }

    public FPSGui() {
        this(new StringTextComponent(""));
    }

    @Override
    protected void init() {
        super.init();
        slider = new Slider(width / 2 - 100, height / 2 - 10, 200, 20,
                new StringTextComponent("FPS proportion: "),
                new StringTextComponent(""),
                0, 100, MCEF.FPS_TAKE_ON, true, true,
                onPress -> {
                },
                slider -> MCEF.FPS_TAKE_ON = slider.getValueInt());

        addListener(slider);
    }


    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        slider.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, font, minecraft.debug, 10, 10, 14737632);
        int fps = Integer.parseInt(minecraft.debug.split("fps")[0].trim());
        int browserFPS = (int) (fps * slider.getValue() / 100);
        drawString(matrixStack, font, "Browser FPS limit: " + browserFPS, 10, 10 + font.FONT_HEIGHT + 5, 14737632);
    }


    @Override
    public void onClose() {
        // save to config
        Config.FPS_TAKE_ON.set(slider.getValueInt());
        Config.CLIENT_CONFIG.save();

        super.onClose();
    }
}
