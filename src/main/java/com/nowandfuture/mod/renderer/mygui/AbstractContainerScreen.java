package com.nowandfuture.mod.renderer.mygui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.nowandfuture.mod.renderer.mygui.api.IEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractContainerScreen<T extends Container> extends ContainerScreen<T> {

    private final MyScreen screen;

    public AbstractContainerScreen(T screenContainer, PlayerInventory inv, @Nonnull MyScreen screen) {
        super(screenContainer, inv, screen.getTitle());

        //reset the size and pos  for screen
        screen.setGuiLeft(this.guiLeft);
        screen.setGuiTop(this.guiTop);
        screen.setWidth(this.width);
        screen.setHeight(this.height);

        this.screen = screen;
    }

    public void onDrawBackgroundLayer(MatrixStack stack, int vOffset) {
        screen.onDrawBackgroundLayer(stack, vOffset);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        screen.renderTop(matrixStack, x, y, 0);
    }

    public void post(IEvents.GuiEvent event) {
        screen.post(event);
    }

    public void clearAll() {
        screen.clearAll();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        screen.renderBottom(matrixStack, x, y, partialTicks);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        screen.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return screen.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return screen.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return screen.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return screen.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return screen.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return screen.charTyped(codePoint, modifiers);
    }

    @Override
    public void closeScreen() {
        screen.closeScreen();
    }

    @Override
    public void init() {
        screen.init();
    }

    public void onLoad() {
        screen.onLoad();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        screen.resize(minecraft, width, height);
    }

    @Override
    public void tick() {
        screen.tick();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return screen.isMouseOver(mouseX, mouseY);
    }

    public RootView getRootView() {
        return screen.getRootView();
    }

    @Override
    public void setFocusedDefault(@Nullable IGuiEventListener eventListener) {
        screen.setFocusedDefault(eventListener);
    }

    @Override
    public ITextComponent getTitle() {
        return screen.getTitle();
    }

    @Override
    public String getNarrationMessage() {
        return screen.getNarrationMessage();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return screen.shouldCloseOnEsc();
    }

    @Override
    public <T extends Widget> T addButton(T button) {
        return screen.addButton(button);
    }

    @Override
    public <T extends IGuiEventListener> T addListener(T listener) {
        return screen.addListener(listener);
    }

    @Override
    public List<ITextComponent> getTooltipFromItem(ItemStack itemStack) {
        return screen.getTooltipFromItem(itemStack);
    }

    @Override
    public void renderTooltip(MatrixStack matrixStack, ITextComponent text, int mouseX, int mouseY) {
        screen.renderTooltip(matrixStack, text, mouseX, mouseY);
    }

    @Override
    public void func_243308_b(MatrixStack p_243308_1_, List<ITextComponent> p_243308_2_, int p_243308_3_, int p_243308_4_) {
        screen.func_243308_b(p_243308_1_, p_243308_2_, p_243308_3_, p_243308_4_);
    }

    @Override
    public void renderWrappedToolTip(MatrixStack matrixStack, List<? extends ITextProperties> tooltips, int mouseX, int mouseY, FontRenderer font) {
        screen.renderWrappedToolTip(matrixStack, tooltips, mouseX, mouseY, font);
    }

    @Override
    public void renderTooltip(MatrixStack matrixStack, List<? extends IReorderingProcessor> tooltips, int mouseX, int mouseY) {
        screen.renderTooltip(matrixStack, tooltips, mouseX, mouseY);
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, List<? extends IReorderingProcessor> tooltips, int mouseX, int mouseY, FontRenderer font) {
        screen.renderToolTip(matrixStack, tooltips, mouseX, mouseY, font);
    }

    @Override
    public boolean handleComponentClicked(@Nullable Style style) {
        return screen.handleComponentClicked(style);
    }

    @Override
    public void sendMessage(String text) {
        screen.sendMessage(text);
    }

    @Override
    public void sendMessage(String text, boolean addToChat) {
        screen.sendMessage(text, addToChat);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        screen.init(minecraft, width, height);
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return screen.getEventListeners();
    }

    @Override
    public void onClose() {
        screen.onClose();
    }

    @Override
    public void renderBackground(MatrixStack matrixStack) {
        screen.renderBackground(matrixStack);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int vOffset) {
        screen.renderBackground(matrixStack, vOffset);
    }

    @Override
    public void renderDirtBackground(int vOffset) {
        screen.renderDirtBackground(vOffset);
    }

    @Override
    public boolean isPauseScreen() {
        return screen.isPauseScreen();
    }

    @Override
    public void addPacks(List<Path> packs) {
        screen.addPacks(packs);
    }

    @Override
    public Minecraft getMinecraft() {
        return screen.getMinecraft();
    }

    @Override
    @Nullable
    public IGuiEventListener getListener() {
        return screen.getListener();
    }

    @Override
    public void setListener(@Nullable IGuiEventListener listener) {
        screen.setListener(listener);
    }

    @Override
    public void blitBlackOutline(int width, int height, BiConsumer<Integer, Integer> boxXYConsumer) {
        screen.blitBlackOutline(width, height, boxXYConsumer);
    }
}
