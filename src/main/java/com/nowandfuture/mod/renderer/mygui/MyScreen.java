package com.nowandfuture.mod.renderer.mygui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.nowandfuture.mod.renderer.mygui.api.IEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public abstract class MyScreen extends Screen {
    private boolean isFirstInit = true;

    private final RootView rootView;

    private int guiLeft = 0, guiTop = 0;

    public void setGuiLeft(int guiLeft) {
        this.guiLeft = guiLeft;
        rootView.setX(guiLeft);
    }

    public void setGuiTop(int guiTop) {
        this.guiTop = guiTop;
        rootView.setY(guiTop);
    }

    protected MyScreen(ITextComponent titleIn) {
        super(titleIn);

        rootView = new RootView(0, 0, this.width, this.height);
        rootView.setGuiContainer(this);
    }

    @Override
    public void render(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBottom(stack, mouseX, mouseY, partialTicks);
        renderTop(stack, mouseX, mouseY, partialTicks);
    }


    public void renderBottom(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();

        rootView.draw(stack, mouseX, mouseY, partialTicks);
        rootView.draw2(stack, mouseX, mouseY, partialTicks);

        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();

    }

    public void renderTop(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        //render other views
        drawDialog(stack, mouseX, mouseY, partialTicks);
        drawTopTip(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public final void renderBackground(MatrixStack matrixStack) {
        this.renderBackground(matrixStack, 0);
    }

    @Override
    public final void renderBackground(MatrixStack matrixStack, int vOffset) {
        //super.renderBackground(matrixStack, vOffset);
        onDrawBackgroundLayer(matrixStack, vOffset);
    }

    protected void onDrawBackgroundLayer(MatrixStack stack, int vOffset) {
        super.renderBackground(stack, vOffset);
    }

    private final PriorityQueue<IEvents.GuiEvent> tipList = new PriorityQueue<>();

    public void post(IEvents.GuiEvent event) {
        synchronized (tipList) {
            tipList.add(event);
        }
    }

    public void clearAll() {
        synchronized (tipList) {
            tipList.clear();
        }
    }

    private void drawTopTip(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        synchronized (tipList) {
            Iterator<IEvents.GuiEvent> iterator = tipList.iterator();

            while (iterator.hasNext()) {
                IEvents.GuiEvent event = iterator.next();

                event.create(rootView);
                event.draw(stack, mouseX, mouseY, partialTicks);

                if (event.isDied(mouseX, mouseY, partialTicks)) {
                    event.destroy(mouseX, mouseY, partialTicks);
                    iterator.remove();
                }
            }
        }
    }

    private void drawDialog(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        rootView.drawDialog(stack, mouseX, mouseY, partialTicks);
        rootView.drawDialog2(stack, mouseX, mouseY, partialTicks);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
        GlStateManager.enableDepthTest();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        rootView.mouseMoved(((int) mouseX), ((int) mouseY));
        if (!rootView.isDialogShowing())
            super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        //in ViewGroup clicked means btn's down-up(it only happened after release button)
        //pressed means button's down
        //released means button's up
        boolean c = rootView.mousePressed(((int) mouseX), ((int) mouseY), button);
        if (!rootView.isDialogShowing())
            super.mouseClicked(mouseX, mouseY, button);

        return c;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        rootView.mouseReleased(((int) mouseX), ((int) mouseY), button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return rootView.mouseScrolled(((int) mouseX), ((int) mouseY), ((float) delta));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) ||
                rootView.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return rootView.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return rootView.handleKeyType(codePoint, modifiers);
    }

    @Override
    public void closeScreen() {
        onDestroy();
        super.closeScreen();
        isFirstInit = true;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public void onDestroy() {
        rootView.cleanUp();
        tipList.clear();
    }

    @Override
    protected void init() {
        super.init();
        int oldW = rootView.getWidth(), oldH = rootView.getHeight();
        rootView.setX(this.guiLeft);
        rootView.setY(this.guiTop);
        rootView.setWidth(this.width);
        rootView.setHeight(this.height);
        rootView.init();

        if (isFirstInit) {
            onLoad();
            rootView.onLoad();
            isFirstInit = false;
            MinecraftForge.EVENT_BUS.register(this);
        }

        onLayout(oldW, oldH, rootView.getWidth(), rootView.getHeight());
    }

    protected abstract void onLoad();

    protected void onLayout(int oldW, int oldH, int width, int height) {
        rootView.onSizeChanged(oldW, oldH, width, height);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        this.minecraft = minecraft;
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.fontRenderer;
        int oldW = this.width, oldH = this.height;
        this.width = width;
        this.height = height;

        this.buttons.clear();

        //init GUIs for minecraft's
        this.init();
    }

    @Override
    public void tick() {
        rootView.update();
    }

    private boolean isInside(List<ViewGroup.GuiRegion> regions, int x, int y) {
        if (regions == null) return false;
        for (ViewGroup.GuiRegion v :
                regions) {
            if (x > v.left && x < v.right && y < v.bottom && y > v.top) {
                return true;
            }
        }
        return false;
    }


    public RootView getRootView() {
        return rootView;
    }

    public void setWidth(int width) {
        if (!isFirstInit) {
            throw new RuntimeException("Error: It is illegal to set width after init!");
        }
        this.width = width;
    }

    public void setHeight(int height) {
        if (!isFirstInit) {
            throw new RuntimeException("Error: It is illegal to set height after init!");
        }
        this.height = height;
    }

    ////////////////////////////////Disable Screen Method////////////////////////////////////////////////


    @Override
    public final void setListener(@Nullable IGuiEventListener listener) {
        //do nothing
    }

    @Override
    public final List<? extends IGuiEventListener> getEventListeners() {
        //do noting
        return Lists.newArrayListWithExpectedSize(0);
    }

    @Override
    public final void setFocusedDefault(@Nullable IGuiEventListener eventListener) {
        //do nothing
    }

    @Override
    protected final <T extends Widget> T addButton(T button) {
        return super.addButton(null);
    }

    @Override
    protected final <T extends IGuiEventListener> T addListener(T listener) {
        return super.addListener(null);
    }
}
