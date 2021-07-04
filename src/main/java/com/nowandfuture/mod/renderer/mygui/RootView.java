package com.nowandfuture.mod.renderer.mygui;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.nowandfuture.mod.renderer.mygui.api.IEvents;
import com.nowandfuture.mod.renderer.mygui.api.MyGui;
import com.nowandfuture.mod.renderer.mygui.layouts.FrameLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.shader.Framebuffer;

public class RootView implements MyGui {
    public Minecraft context = Minecraft.getInstance();
    private Screen guiContainer;

    protected long longClickThreshold = 1000;//ms

    private int x, y, w, h;

    private final ViewGroup topView;
    private ViewGroup focusedView;
    private ViewGroup hoverView;

    private Dialog dialogView;
    private final ViewGroup notifyView;

    ViewGroup getTopView() {
        return topView;
    }

    public static boolean isInside(MyGui gui, int mouseX, int mouseY) {
        return mouseX >= 0 && mouseY >= 0 && mouseX <= gui.getWidth() && mouseY <= gui.getHeight();
    }

    public static boolean isInside2(MyGui gui, int mouseXAtParent, int mouseYAtParent) {
        mouseXAtParent -= gui.getX();
        mouseYAtParent -= gui.getY();
        return mouseXAtParent >= 0 && mouseYAtParent >= 0 && mouseXAtParent <= gui.getWidth() && mouseYAtParent <= gui.getHeight();
    }

    public static boolean isInside(MyGui parent, MyGui gui, int mouseX, int mouseY) {
        mouseX += gui.getX();
        mouseY += gui.getY();
        return mouseX >= 0 && mouseY >= 0 && mouseX <= parent.getWidth() && mouseY <= parent.getHeight();
    }

    public FontRenderer getFontRenderer() {
        return context.fontRenderer;
    }

    final public ViewGroup getFocusedView() {
        return focusedView;
    }

    final void setFocusedView(ViewGroup viewGroup) {
        if (focusedView != null) {
            focusedView.setFocused(false);

            if (viewGroup != null && viewGroup.isFocusable()) {
                focusedView = viewGroup;
                viewGroup.setFocused(true);
            } else {
                focusedView = null;
            }

        } else {
            if (viewGroup != null && viewGroup.isFocusable()) {
                focusedView = viewGroup;
                focusedView.setFocused(true);
            }
        }
    }

    public RootView(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        topView = new TopView(this);
        topView.setClipping(false);

        topView.setX(x);
        topView.setY(y);
        topView.setWidth(w);
        topView.setHeight(h);

        dialogView = new Dialog();

        //not finished
        notifyView = new FrameLayout(this);
        notifyView.setX(x);
        notifyView.setY(y);
        notifyView.setWidth(w);
        notifyView.setHeight(h);
    }

    public static class DialogBuilder {
        RootView rootView;
        ViewGroup content;

        private DialogBuilder(RootView rootView, ViewGroup content) {
            this.rootView = rootView;
            this.content = content;
        }

        static DialogBuilder newDialogBuilder(RootView rootView, ViewGroup content) {
            return new DialogBuilder(rootView, content);
        }

        public DialogBuilder buildDialog(ViewGroup view) {
            content = view;
            content.setX(rootView.x);
            content.setY(rootView.y);
            content.setWidth(rootView.w);
            content.setHeight(rootView.h);

            content.setVisible(false);
            return this;
        }


        public DialogBuilder showDialog() {
            if (content != null) {
                content.setVisible(true);
            }
            return this;
        }

        public DialogBuilder hideDialog() {
            if (content != null) {
                content.setVisible(false);
            }
            return this;
        }

        public Dialog build() {
            Dialog dialog = new Dialog(content);
            dialog.setSize(content.getWidth(), content.getHeight());
            rootView.setDialogView(dialog);
            return dialog;
        }
    }

    public DialogBuilder createDialogBuilder(ViewGroup content) {
        return new DialogBuilder(this, content);
    }

    void setDialogView(Dialog view) {
        this.dialogView = view;
    }

    public void onLoad() {
        Framebuffer framebuffer = Minecraft.getInstance().getFramebuffer();
        if (!framebuffer.isStencilEnabled()) {
            framebuffer.enableStencil();
        }
        topView.load();
    }

    public void onSizeChanged(int oldW, int oldH, int w, int h) {
        topView.layout(this.getWidth(), this.getHeight());
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
        topView.setX(x);
    }

    @Override
    public void setY(int y) {
        this.y = y;
        topView.setY(y);
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getHeight() {
        return h;
    }

    @Override
    public void setWidth(int width) {
        w = width;
        topView.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        h = height;
        topView.setHeight(height);
    }

    public void init() {
        //update dialog's position
        if (dialogView.isShowing() && dialogView.isInCenter())
            dialogView.setCenter();
    }

    @Override
    public final void draw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        topView.setReachable(!dialogView.isShowing());

        GlStateManager.pushMatrix();
        GlStateManager.translatef(topView.getX(), topView.getY(), 0);
        updateHoveringView(topView, mouseX, mouseY);
        topView.draw(stack, mouseX - topView.getX(), mouseY - topView.getY(), partialTicks);
        GlStateManager.popMatrix();
    }

    private boolean updateHoveringView(ViewGroup root, int mouseX, int mouseY) {
        ViewGroup hover = root.checkHover(mouseX - root.getX(), mouseY - root.getY());
        if (hoverView != null) hoverView.setHovering(false);
        if (hover != null) hover.setHovering(true);
        hoverView = hover;
        return hoverView != null;
    }

    public final void drawDialog(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        if (!dialogView.isShowing()) return;
        ViewGroup content = dialogView.getView();
        content.layout(this.getWidth(), this.getHeight());

        GlStateManager.pushMatrix();
        GlStateManager.translatef(content.getX(), content.getY(), 0);
        ViewGroup hover = content.checkHover(mouseX - content.getX(), mouseY - content.getY());
        if (hoverView != null) hoverView.setHovering(false);
        if (hover != null) hover.setHovering(true);
        hoverView = hover;
        content.draw(stack, mouseX - content.getX(), mouseY - content.getY(), partialTicks);
        GlStateManager.popMatrix();
    }

    public boolean isDialogShowing() {
        return dialogView.isShowing();
    }

    @Override
    public void draw2(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        topView.draw2(stack, mouseX, mouseY, partialTicks);
    }

    public void drawDialog2(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        if (dialogView.isShowing())
            dialogView.getView().draw2(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public final boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        //nothing to do
        return false;
    }

    @Override
    public final boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) {
        //nothing to do
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            content.mouseReleased(mouseX - content.getX(), mouseY - content.getY(), state);
        } else
            topView.mouseReleased(mouseX - getX(), mouseY - getY(), state);
    }

    public void mouseDragged(int mouseX, int mouseY, int state) {
        if (dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            content.mouseDragged(mouseX - content.getX(), mouseY - content.getY(), state);
        } else
            topView.mouseDragged(mouseX - getX(), mouseY - getY(), state);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int state) {

        boolean flag;
        if (dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            mouseX -= content.getX();
            mouseY -= content.getY();
            if (isInside(content, mouseX, mouseY))
                content.mousePressed(mouseX, mouseY, state);
            else {
                setFocusedView(null);
                dialogView.dispose();
            }
            flag = true;
        } else {
            mouseX -= getX();
            mouseY -= getY();
            flag = topView.mousePressed(mouseX, mouseY, state);
        }

        if (!flag) setFocusedView(null);

        return flag;
    }

    @Override
    public boolean mouseMoved(int mouseX, int mouseY) {
        if (dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            return content.mouseMoved(mouseX - content.getX(), mouseY - content.getY());
        } else
            return topView.mouseMoved(mouseX - getX(), mouseY - getY());
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, float delta) {
        if (dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            return content.mouseScrolled(mouseX - content.getX(), mouseY - content.getY(), delta);
        } else
            return topView.mouseScrolled(mouseX - getX(), mouseY - getY(), delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            return content.keyPressed(keyCode, scanCode, modifiers);
        } else
            return topView.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            return content.keyReleased(keyCode, scanCode, modifiers);
        } else
            return topView.keyReleased(keyCode, scanCode, modifiers);
    }

    public boolean handleKeyType(char typedChar, int keyCode) {
        if (dialogView.isShowing()) {
            return dialogView.getView().handleKeyType(typedChar, keyCode);
        }
        return topView.handleKeyType(typedChar, keyCode);
    }

    //gametick update
    public void update() {
        topView.onUpdate();
        if (dialogView.isShowing())
            dialogView.getView().onUpdate();
    }

    public void add(ViewGroup viewGroup) {
        this.topView.addChild(viewGroup);
    }

    public void add(int index, ViewGroup viewGroup) {
        this.topView.addChild(index, viewGroup);
    }

    public void remove(ViewGroup viewGroup) {
        this.topView.removeChild(viewGroup);
    }

    public void remove(int index) {
        this.topView.removeChild(index);
    }

    public void cleanUp() {
        topView.destroy();
    }

    public void setVisible(boolean v) {
        if (topView != null)
            topView.setVisible(v);
    }

    public boolean isShowDebugInfo() {
        return context.gameSettings.showDebugInfo;
    }

    public Screen getGuiContainer() {
        return guiContainer;
    }

    public void setGuiContainer(Screen guiContainer) {
        this.guiContainer = guiContainer;
    }

    public ViewGroup getHoverView() {
        return hoverView;
    }

    public static abstract class AbstractDataTipEvent<T> extends IEvents.GuiEvent.AbstractGuiEvent {

        protected T data;
        protected long lifeTime;
        protected long totalLifeTime;
        protected RootView rootView;
        protected Position position;
        protected int fixedX = 0, fixedY = 0;

        public enum Position {
            LEFT_TOP,
            RIGHT_TOP,
            LEFT_BOTTOM,
            RIGHT_BOTTOM,
            FIXED
        }

        public AbstractDataTipEvent(T text) {
            this(text, 0);
        }

        public AbstractDataTipEvent(T text, long lifeTime) {
            this(text, lifeTime, Position.RIGHT_TOP);
        }

        public AbstractDataTipEvent(T text, long lifeTime, Position position) {
            this.data = text;
            this.lifeTime = lifeTime;
            this.totalLifeTime = this.lifeTime;
            this.position = position;
        }

        public void setFixedX(int fixedX) {
            this.fixedX = fixedX;
        }

        public void setFixedY(int fixedY) {
            this.fixedY = fixedY;
        }

        @Override
        public void create(RootView rootView) {
            this.rootView = rootView;
        }

        private long lastFrameTime = -1;

        @Override
        public void draw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
            int posX, posY;
            if (position == Position.LEFT_TOP) {
                posX = 0;
                posY = 0;
            } else if (position == Position.RIGHT_TOP) {
                posX = rootView.getWidth();
                posY = 0;
            } else if (position == Position.LEFT_BOTTOM) {
                posX = 0;
                posY = rootView.getHeight();
            } else if (position == Position.RIGHT_BOTTOM) {
                posX = rootView.getWidth();
                posY = rootView.getHeight();
            } else {
                posX = fixedX;
                posY = fixedY;
            }

            onDrawData(stack, posX, posY, mouseX, mouseY, partialTicks);

            long curTime = System.currentTimeMillis();

            if (lastFrameTime > -1)
                lifeTime -= curTime - lastFrameTime;

            lastFrameTime = curTime;
        }

        protected abstract void onDrawData(MatrixStack stack, int posX, int posY, int mouseX, int mouseY, float partialTicks);

        @Override
        public void destroy(int mouseX, int mouseY, float partialTicks) {
            lifeTime = 0;
            lastFrameTime = -1;
        }

        @Override
        public boolean isDied(int mouseX, int mouseY, float partialTicks) {
            return lifeTime < 0;
        }
    }
}
