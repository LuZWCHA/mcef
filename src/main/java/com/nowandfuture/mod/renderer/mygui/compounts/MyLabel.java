package com.nowandfuture.mod.renderer.mygui.compounts;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nowandfuture.mod.renderer.mygui.ViewGroup;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

class MyLabel extends Screen {

    protected int width;
    protected int height;
    public int x;
    public int y;
    private final List<String> labels;
    private boolean horizontalCenter;
    public boolean visible = true;
    private boolean labelBgEnabled;
    private int textColor;
    private int backColor;
    private int ulColor;
    private int brColor;
    private final FontRenderer fontRenderer;
    private int border;
    private boolean drawShadow = true;
    private int maxLines = 1;

    public MyLabel(FontRenderer fontRenderer, int x, int y, int width, int height, int textColor) {
        super(new StringTextComponent(""));
        this.fontRenderer = fontRenderer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.labels = Lists.<String>newArrayList();
        this.horizontalCenter = false;
        this.labelBgEnabled = true;
        this.textColor = textColor;
        this.backColor = -1;
        this.ulColor = -1;
        this.brColor = -1;
        this.border = 0;
    }

    public MyLabel addLine(String line) {
        this.labels.add(I18n.format(line));
        return this;
    }

    public MyLabel setLine(int index, String line) {
        while (labels.size() <= index) {
            labels.add(Strings.EMPTY);
        }
        this.labels.set(index, I18n.format(line));
        return this;
    }

    public MyLabel empty() {
        labels.clear();
        labels.add("");
        return this;
    }

    public MyLabel setFirst(String line) {
        if (labels.isEmpty()) labels.add("");
        setLine(0, I18n.format(line));
        return this;
    }

    public void removeLine(int line) {
        this.labels.remove(line);
    }

    public void removeAllLines(int line) {
        this.labels.clear();
    }

    /**
     * Sets the Label to be centered
     */
    public MyLabel setHorizontalCenter(boolean horizontalCenter) {
        this.horizontalCenter = horizontalCenter;
        return this;
    }

    public MyLabel setBackColor(int color) {
        this.backColor = color;
        return this;
    }

    public MyLabel setBorderColor(int color) {
        this.brColor = color;
        this.ulColor = color;
        return this;
    }

    public MyLabel setBorderWidth(int width) {
        border = width;
        return this;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public MyLabel enableBackDraw(boolean labelBgEnabled) {
        this.labelBgEnabled = labelBgEnabled;
        return this;
    }

    public void drawLabel(MatrixStack stack, int mouseX, int mouseY) {
        if (this.visible) {
            GlStateManager.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            this.drawLabelBackground(stack, mouseX, mouseY);
            int i = this.y + this.height / 2 + this.border / 2;

            int textHeight = 10;
            int rowNum = getHeight() / textHeight;
            int size = Math.min(rowNum, labels.size());
            size = Math.min(maxLines, size);

            int j = i - size * 10 / 2;

            for (int k = 0; k < size; ++k) {
                if (this.horizontalCenter) {
                    if (drawShadow)
                        drawCenteredString(stack, this.fontRenderer, this.labels.get(k), this.x + this.width / 2, j + k * textHeight, this.textColor);
                    else
                        ViewGroup.drawCenteredStringWithoutShadow(this.fontRenderer, stack, this.labels.get(k), this.x + this.width / 2, j + k * textHeight, this.textColor);
                } else {
                    if (drawShadow)
                        drawString(stack, this.fontRenderer, this.labels.get(k), this.x, j + k * textHeight, this.textColor);
                    else
                        ViewGroup.drawStringWithoutShadow(this.fontRenderer, stack, this.labels.get(k), this.x, j + k * textHeight, this.textColor);
                }
            }
        }
    }

    protected void drawLabelBackground(MatrixStack stack, int mouseX, int mouseY) {

        if (this.labelBgEnabled) {
            int i = this.width + this.border * 2;
            int j = this.height + this.border * 2;
            int k = this.x - this.border;
            int l = this.y - this.border;
            fill(stack, k, l, k + i, l + j, this.backColor);
            hLine(stack, k, k + i, l, this.ulColor);
            hLine(stack, k, k + i, l + j, this.brColor);
            hLine(stack, k, l, l + j, this.ulColor);
            hLine(stack, k + i, l, l + j, this.brColor);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void draw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        drawLabel(stack, mouseX, mouseY);
    }

    public void draw2(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {

    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public void setDrawTextShadow(boolean drawTextShadow) {
        this.drawShadow = drawTextShadow;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    public boolean mousePressed(int mouseX, int mouseY, int state) {
        return this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    public boolean handleMouseInput(int mouseX, int mouseY) {
        return false;
    }
}
