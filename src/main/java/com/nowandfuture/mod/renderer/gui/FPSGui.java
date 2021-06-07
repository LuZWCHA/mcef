package com.nowandfuture.mod.renderer.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
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

    public FPSGui(){
        this(new StringTextComponent(""));
    }

    @Override
    protected void init() {
        super.init();
        slider = new Slider(width / 2 - 100, height / 2 - 10, 200, 20, new StringTextComponent("FPS proportion: "), new StringTextComponent(""), 0, 100, MCEF.FPS_TAKE_ON, true, true, new Button.IPressable() {
            @Override
            public void onPress(Button p_onPress_1_) {
                //do nothing
            }
        }
        , new Slider.ISlider() {
            @Override
            public void onChangeSliderValue(Slider slider) {
                MCEF.FPS_TAKE_ON = slider.getValueInt();
            }
        });

        addWidget(slider);
    }

    @Override
    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        renderBackground(p_230430_1_);

        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);

        slider.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        drawString(p_230430_1_, font, minecraft.fpsString, 10,10, 14737632);
        int fps = Integer.parseInt(minecraft.fpsString.split("fps")[0].trim());
        int browserFPS = (int) (fps * slider.getValue() / 100);
        drawString(p_230430_1_, font, "Browser FPS limit: " + browserFPS, 10,10 + font.lineHeight + 5, 14737632);
    }

    @Override
    public void onClose() {
        // save to config
        Config.FPS_TAKE_ON.set(slider.getValueInt());
        Config.CLIENT_CONFIG.save();

        super.onClose();
    }
}
