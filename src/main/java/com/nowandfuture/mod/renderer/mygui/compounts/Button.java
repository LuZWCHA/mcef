package com.nowandfuture.mod.renderer.mygui.compounts;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.nowandfuture.mod.renderer.mygui.Color;
import com.nowandfuture.mod.renderer.mygui.RootView;
import com.nowandfuture.mod.renderer.mygui.View;
import com.nowandfuture.mod.renderer.mygui.ViewGroup;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;

import javax.annotation.Nonnull;

public class Button extends View {
    private final MyButton btn;

    private ResourceLocation location;
    private boolean vanillaStyle = true;

    private Color buttonColor;
    private Color buttonHoverColor;
    private Color disableColor;
    private Color textColor;
    private Color textHoverColor;
    private Color disableTextColor;

    private ActionListener actionListener;
    private int imagePadding = 2;

    public Button(@NotNull RootView rootView) {
        super(rootView);
        btn = new MyButton(0, 0, getWidth(), getHeight(), ITextComponent.getTextComponentOrEmpty(""), new net.minecraft.client.gui.widget.button.Button.IPressable() {
            @Override
            public void onPress(net.minecraft.client.gui.widget.button.Button b) {

            }
        });
        init();
    }

    public Button(@NotNull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        btn = new MyButton(0, 0, getWidth(), getHeight(), ITextComponent.getTextComponentOrEmpty(""), new net.minecraft.client.gui.widget.button.Button.IPressable() {
            @Override
            public void onPress(net.minecraft.client.gui.widget.button.Button b) {

            }
        });
        init();
    }

    private void init() {
        buttonColor = new Color(128, 128, 128);
        buttonHoverColor = new Color(200, 200, 200);
        disableColor = new Color(80, 80, 80);
        textColor = new Color(225, 225, 225);
        textHoverColor = new Color(255, 255, 255);
        disableTextColor = new Color(128, 128, 128);
    }

    public Button setText(String text) {
        btn.setMessage(ITextComponent.getTextComponentOrEmpty(text));
        return this;
    }

    public String getText() {
        return btn.getMessage().getString();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        btn.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        btn.setHeight(height);
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {

        if (vanillaStyle) {
            btn.setHovered(isHovering());
            btn.render(stack, mouseX, mouseY, partialTicks);
        } else {
            Color buttonColor, textColor;
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            if (isEnabled()) {
                buttonColor = isHovering() ? buttonHoverColor : this.buttonColor;
                textColor = isHovering() ? textHoverColor : this.textColor;
            } else {
                buttonColor = disableColor;
                textColor = disableTextColor;
            }
            drawRect(stack, 0, 0, getWidth(), getHeight(), colorInt(buttonColor));

            FontRenderer renderer = getRoot().context.fontRenderer;
            int strWidth = renderer.getStringWidth(getText());
            int ellipsisWidth = renderer.getStringWidth("...");
            String text = getText();

            if (strWidth > getWidth() - 6 && strWidth > ellipsisWidth)
                text = renderer.trimStringToWidth(text, getWidth() - 6 - ellipsisWidth).trim() + "...";

            GlStateManager.color4f(1F, 1F, 1F, 1F);
            this.drawForeground(stack);
            drawCenteredStringWithoutShadow(renderer, stack, text, getWidth() / 2, (getHeight() - 8) / 2, colorInt(textColor));
        }
    }


    private void drawForeground(MatrixStack stack) {
        if (location != null) {
            GlStateManager.enableAlphaTest();
            getRoot().context.getRenderManager().textureManager.bindTexture(location);

            int size = Math.min(getHeight(), getWidth()) - imagePadding * 2;
            if (size > 0) {
                int offsetX = (getWidth() - size) / 2;
                int offsetY = (getHeight() - size) / 2;
                Screen.blit(stack, offsetX, offsetY, 0, 0, size, size, size, size);
            }
            GlStateManager.disableAlphaTest();
        }
    }

    //fixed minecraft button absolute location drawable
    @Override
    public void setX(int x) {
        super.setX(x);
        btn.x = 0;
    }

    //fixed minecraft button absolute location drawable
    @Override
    public void setY(int y) {
        super.setY(y);
        btn.y = 0;
    }

    public void setEnable(boolean enable) {
        btn.active = enable;
    }

    public boolean isEnabled() {
        return btn.active;
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        boolean clicked = btn.mouseClicked(mouseX, mouseY, mouseButton);
        if (clicked && actionListener != null)
            actionListener.onClicked(this, mouseX, mouseY, mouseButton);
        return clicked;
    }

    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        boolean f = btn.keyPressed(keyCode, scanCode, modifiers);
        if (f) {
            //perform button click
            onClicked(0, 0, 0);
        }
        return f;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        if (actionListener != null)
            actionListener.onLongClicked(this, mouseX, mouseY, mouseButton);
        return true;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    public Button setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
        return this;
    }

    public Button setVanillaStyle(boolean vanillaStyle) {
        this.vanillaStyle = vanillaStyle;
        return this;
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public Button setButtonColor(@Nonnull Color buttonColor) {
        this.buttonColor = buttonColor;
        return this;
    }

    public Color getButtonHoverColor() {
        return buttonHoverColor;
    }

    public Button setButtonHoverColor(@Nonnull Color buttonHoverColor) {
        this.buttonHoverColor = buttonHoverColor;
        return this;
    }

    public Color getDisableColor() {
        return disableColor;
    }

    public Button setDisableColor(@Nonnull Color disableColor) {
        this.disableColor = disableColor;
        return this;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Button setTextColor(@Nonnull Color textColor) {
        this.textColor = textColor;
        return this;
    }

    public Color getTextHoverColor() {
        return textHoverColor;
    }

    public Button setTextHoverColor(@Nonnull Color textHoverColor) {
        this.textHoverColor = textHoverColor;
        return this;
    }

    public Color getDisableTextColor() {
        return disableTextColor;
    }

    public Button setDisableTextColor(@Nonnull Color disableTextColor) {
        this.disableTextColor = disableTextColor;
        return this;
    }

}
