package com.nowandfuture.mod.renderer.mygui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.nowandfuture.mod.renderer.mygui.api.IAction;
import com.nowandfuture.mod.renderer.mygui.api.ISizeChanged;
import com.nowandfuture.mod.renderer.mygui.api.MyGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL20;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ViewGroup implements MyGui, ISizeChanged {

    private int width;
    private int height;
    private int x, y;

    private ViewGroup parent;
    private RootView root;
    protected List<ViewGroup> children;
    private boolean enableIntercepted = false;

    private ViewGroup lastPressedChild;
    private long lastPressTime;

    private boolean focusable = true;
    private boolean isFocused;
    private boolean visible = true;
    private boolean isHover = false;
    private boolean isClickable = true;

    private IAction.ActionClick actionClick;

    private boolean isLoaded;

    //this scissor can only do not in nested-mode
    protected boolean isClipping = false;
    private static int stencilMaskDepth = -1;

    private boolean isReachable = true;
    private boolean isInside;

    private ViewClipMask viewClipMask;

    public int getPadLeft() {
        return padLeft;
    }

    public void setPadLeft(int padLeft) {
        this.padLeft = padLeft;
    }

    public int getPadRight() {
        return padRight;
    }

    public void setPadRight(int padRight) {
        this.padRight = padRight;
    }

    public int getPadTop() {
        return padTop;
    }

    public void setPadTop(int padTop) {
        this.padTop = padTop;
    }

    public int getPadBottom() {
        return padBottom;
    }

    public void setPadBottom(int padBottom) {
        this.padBottom = padBottom;
    }

    protected int padLeft, padRight, padTop, padBottom;

    protected ViewGroup() {
        isLoaded = false;
    }

    public ViewGroup(@Nonnull RootView rootView) {
        this(rootView, rootView.getTopView());
    }

    public ViewGroup(@Nonnull RootView rootView, @Nullable ViewGroup parent) {
        this.children = new LinkedList<>();
        this.parent = parent;
        this.root = rootView;
        this.isLoaded = false;
        padBottom = padLeft = padRight = padTop = 0;
        viewClipMask = new RectangleClipMask(this);

        if (parent != null) {
            parent.addChild(this);
        }
    }

    public void setReachable(boolean reachable) {
        isReachable = reachable;
    }

    public boolean isReachable() {
        return isReachable;
    }

    public ViewGroup getParent() {
        return parent;
    }

    public RootView getRoot() {
        return root;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    @Override
    public int getX() {
        return x;
    }

    /**
     * @return get absolute location at root view
     */
    public int getAbsoluteX() {
        if (parent != null)
            return parent.getAbsoluteX() + x;
        else
            return x;
    }

    /**
     * @return get absolute location at root view
     */
    public int getAbsoluteY() {
        if (parent != null)
            return parent.getAbsoluteY() + y;
        else
            return y;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    public boolean moveX(int x) {
        setX(this.x + x);
        return RootView.isInside2(this, this.x, this.y);
    }

    public boolean moveY(int y) {
        setY(this.y + y);
        return RootView.isInside2(this, this.x, this.y);
    }

    public boolean moveXY(int x, int y) {
        return moveX(x) & moveY(y);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setWidth(int width) {
        if (width != this.width) {
            int temp = this.width;
            this.width = width;
            onWidthChanged(temp, width);
        }
    }

    @Override
    public void setHeight(int height) {
        if (height != this.height) {
            int temp = this.height;
            this.height = height;
            onHeightChanged(temp, height);
        }
    }

    protected void setWidthWithoutLayout(int width) {
        if (width != this.width) {
            this.width = width;
        }
    }

    protected void setHeightWithoutLayout(int height) {
        if (height != this.height) {
            this.height = height;
        }
    }

    public void layout(int suggestWidth, int suggestHeight) {
        if (isLoaded) {
            onLayout(suggestWidth, suggestHeight);
            onChildrenLayout();
        }
    }

    public void load() {
        onLoad();
        this.isLoaded = true;
        for (ViewGroup view :
                children) {
            view.load();
        }
    }

    /**
     * @see ContainerScreen#init()  this method will be invoked only one time,even gui size changed
     * to re-layout the guis,to see {@link ViewGroup#onLayout(int, int)}
     */
    protected void onLoad() {

    }

    /**
     * @param parentWidth  its parent's width
     * @param parentHeight its parent's height
     *                     this method will be invoked when the gui's size changed: {@link ViewGroup#onWidthChanged(int, int)}
     *                     {@link ViewGroup#onHeightChanged(int, int)} and also be invoked before its children
     *                     to be layout {@link ViewGroup#onChildrenLayout()}
     */
    protected abstract void onLayout(int parentWidth, int parentHeight);

    protected void onChildrenLayout() {
        for (ViewGroup view :
                children) {
            view.layout(this.width, this.height);
        }
    }

    /**
     * @param old the old width
     * @param cur the new width
     */
    @Override
    public void onWidthChanged(int old, int cur) {
        if (!children.isEmpty())
            onChildrenLayout();
    }

    /**
     * @param old the old height
     * @param cur the new height
     */
    @Override
    public void onHeightChanged(int old, int cur) {
        if (!children.isEmpty())
            onChildrenLayout();
    }

    protected boolean checkMouseInside(int mouseX, int mouseY) {
        return isReachable && RootView.isInside(this, mouseX, mouseY);
    }

    /**
     * @param stack
     * @param mouseX       relative location-x at parent view
     * @param mouseY       relative location-y at parent view
     * @param partialTicks
     */
    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        isInside = checkMouseInside(mouseX, mouseY);

        drawDebugHoverBackground(stack);
        if (isClipping) {

            stencilMaskDepth++;
            int mask_layer = (0x01 << stencilMaskDepth);
            int layer = (mask_layer - 1) | mask_layer;

            int currentFunc = GL20.glGetInteger(GL20.GL_STENCIL_FUNC);
            int currentOpFailed = GL20.glGetInteger(GL20.GL_STENCIL_FAIL);
            int currentOpZPass = GL20.glGetInteger(GL20.GL_STENCIL_PASS_DEPTH_PASS);
            int currentOpZFailed = GL20.glGetInteger(GL20.GL_STENCIL_PASS_DEPTH_FAIL);
            int currentRef = GL20.glGetInteger(GL20.GL_STENCIL_REF);
            int currentMask = GL20.glGetInteger(GL20.GL_STENCIL_WRITEMASK);
            int currentValueMask = GL20.glGetInteger(GL20.GL_STENCIL_VALUE_MASK);
            boolean currentEnable = GL20.glIsEnabled(GL20.GL_STENCIL_TEST);

            GL20.glEnable(GL20.GL_STENCIL_TEST);

            GL20.glStencilMask(mask_layer);
            GL20.glClear(GL20.GL_STENCIL_BUFFER_BIT);

            GL20.glStencilFunc(GL20.GL_NEVER, mask_layer, mask_layer);
            GL20.glStencilOp(GL20.GL_REPLACE, GL20.GL_KEEP, GL20.GL_KEEP);

            viewClipMask.drawMask(stack);

            GL20.glStencilFunc(GL20.GL_EQUAL, layer, layer);
            GL20.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

            onDraw(stack, mouseX, mouseY, partialTicks);

            int tempX, tempY;
            for (ViewGroup view :
                    children) {
                if (view.isVisible()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(view.getX(), view.getY(), 0);
                    tempX = mouseX - view.getX();
                    tempY = mouseY - view.getY();
                    view.draw(stack, tempX, tempY, partialTicks);
                    GlStateManager.popMatrix();
                }
            }

//            GL20.glDisable(GL20.GL_SCISSOR_TEST);
            stencilMaskDepth--;
            GL20.glStencilFunc(currentFunc, currentRef, currentValueMask);
            GL20.glStencilOp(currentOpFailed, currentOpZFailed, currentOpZPass);
            GL20.glStencilMask(currentMask);
            if (!currentEnable) {
                GL20.glDisable(GL20.GL_STENCIL_TEST);
            }

        } else {
            onDraw(stack, mouseX, mouseY, partialTicks);
            int tempX, tempY;
            for (ViewGroup view :
                    children) {
                if (view.isVisible()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(view.getX(), view.getY(), 0);
                    tempX = mouseX - view.getX();
                    tempY = mouseY - view.getY();
                    view.draw(stack, tempX, tempY, partialTicks);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    protected ViewGroup checkHover(int mouseX, int mouseY) {
        ViewGroup vg;
        int tempX, tempY;
        if (isVisible() &&
                checkParentHover() && checkMouseInside(mouseX, mouseY) ||
                !checkParentHover()) {
            for (int i = children.size(); i > 0; i--) {
                vg = children.get(i - 1);
                tempX = mouseX - vg.getX();
                tempY = mouseY - vg.getY();
                ViewGroup v = vg.checkHover(tempX, tempY);
                if (v != null) {
                    return v;
                }
            }
            return this;
        }
        return null;
    }


    /**
     * @return true when checking whether it's hovering by mouse and also its parents are hovered
     * false will check itself only.
     */
    protected boolean checkParentHover() {
        return true;
    }

    /**
     * @param stack
     * @param mouseX       absolute location-x at root view
     * @param mouseY       absolute location-y at root view
     * @param partialTicks draw at root-view
     */
    @Override
    public void draw2(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        onDrawAtScreenCoordinate(stack, mouseX, mouseY, partialTicks);

        if (this instanceof TopView)
            drawDebugInfo(stack, mouseX, mouseY);

        for (ViewGroup view :
                children) {
            if (view.isVisible()) {
                view.draw2(stack, mouseX, mouseY, partialTicks);
            }
        }
    }

    /**
     * @param stack
     * @param mouseX       relative location-x at parent view
     * @param mouseY       relative location-y at parent view
     * @param partialTicks draw at root-view
     */
    protected abstract void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks);

    /**
     * @param stack
     * @param mouseX       absolute location-x at root view
     * @param mouseY       absolute location-y at root view
     * @param partialTicks draw at root-view
     * @see ViewGroup#onDraw(MatrixStack, int, int, float) the diffierent between them just the Coordinate where
     * it drawing
     */
    protected void onDrawAtScreenCoordinate(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (actionClick != null && visible) actionClick.clicked(this, mouseButton);

        return visible && onClicked(mouseX, mouseY, mouseButton);
    }

    protected abstract boolean onClicked(int mouseX, int mouseY, int mouseButton);

    @Override
    public boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) {
        return visible && onLongClicked(mouseX, mouseY, mouseButton);
    }

    protected abstract boolean onLongClicked(int mouseX, int mouseY, int mouseButton);

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (!interceptClickAction(mouseX, mouseY, button)
                && lastPressedChild != null) {
            mouseX -= lastPressedChild.getX();
            mouseY -= lastPressedChild.getY();
            lastPressedChild.mouseReleased(mouseX, mouseY, button);
        } else {
            onReleased(mouseX, mouseY, button);
            if (RootView.isInside(this, mouseX, mouseY)) {
                if (getCurrentTime() - lastPressTime < root.longClickThreshold)
                    mouseClicked(mouseX, mouseY, button);
                else {
                    if (actionClick != null) actionClick.longClick(this, button, getCurrentTime() - lastPressTime);
                    mouseLongClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    protected abstract void onReleased(int mouseX, int mouseY, int state);

    /**
     * @return true if it enable its parent intercept its {@link ViewGroup#onClicked(int, int, int)}
     */
    private boolean enableIntercepted() {
        return enableIntercepted;
    }

    protected boolean intercept() {
        return true;
    }

    protected boolean interceptClickAction(int mouseX, int mouseY, int button) {
        boolean flag = visible && RootView.isInside(this, mouseX, mouseY)
                && onPressed(mouseX, mouseY, button);

        if (flag) {
            flag = onInterceptClickAction(mouseX, mouseY, button);
            if (flag && lastPressedChild != null) {
                if (!lastPressedChild.enableIntercepted()) {
                    lastPressedChild.mouseReleased(mouseX - lastPressedChild.getX(),
                            mouseY - lastPressedChild.getY(), button);
                }
            }
        }
        return flag;
    }

    protected boolean onInterceptClickAction(int mouseX, int mouseY, int button) {
        return false;
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int state) {
        boolean flag;

        if (lastPressedChild != null) {
            lastPressedChild.setLastPressTime(0);
            lastPressedChild = null;
        }

        int tempX, tempY;
        ViewGroup vg;
        for (int i = children.size(); i > 0; i--) {
            vg = children.get(i - 1);
            tempX = mouseX - vg.getX();
            tempY = mouseY - vg.getY();
            if (!RootView.isInside(vg, tempX, tempY) || !vg.isClickable()) continue;

            flag = vg.mousePressed(tempX, tempY, state);
            if (flag) {
                lastPressedChild = vg;
                vg.setLastPressTime(getCurrentTime());
                return true;
            }
        }
        flag = visible && onPressed(mouseX, mouseY, state);
        if (flag && this != root.getFocusedView())
            root.setFocusedView(this);
        return flag;
    }

    protected abstract boolean onPressed(int mouseX, int mouseY, int state);

    protected boolean mouseDragged(int mouseX, int mouseY, int state) {
        int tempX, tempY;
        if (lastPressedChild != null)
            for (ViewGroup vg :
                    children) {

                tempX = mouseX - vg.getX();
                tempY = mouseY - vg.getY();
                if (!RootView.isInside(vg, tempX, tempY)) continue;

                if (lastPressedChild == vg && vg.mouseDragged(tempX, tempY, state)) {
                    return true;
                }
            }
        return onMouseDragged(mouseX, mouseY, state);
    }

    protected boolean onMouseDragged(int mouseX, int mouseY, int state) {
        return false;
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, float delta) {
        int tempX, tempY;

        //focused view must be visible
        if (isFocused()) {
            return onMouseScroll(mouseX, mouseY, delta);
        } else {
            for (ViewGroup vg :
                    children) {

                tempX = mouseX - vg.getX();
                tempY = mouseY - vg.getY();
                if (!RootView.isInside(vg, tempX, tempY)) continue;

                if (vg.mouseScrolled(tempX, tempY, delta)) {
                    return true;
                }
            }

            return false;
        }
    }

    protected boolean onMouseScroll(int mouseX, int mouseY, float delta) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //focused view must be visible
        if (isFocused()) {
            return onKeyPressed(keyCode, scanCode, modifiers);
        } else {
            for (ViewGroup vg :
                    children) {
                if (vg.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }

            return false;
        }
    }

    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        //focused view must be visible
        if (isFocused()) {
            return onKeyReleased(keyCode, scanCode, modifiers);
        } else {
            for (ViewGroup vg :
                    children) {
                if (vg.keyReleased(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }

            return false;
        }
    }

    protected boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean onKeyType(char typedChar, int mod) {
        return false;
    }

    /**
     * this method is executed in {@link ContainerScreen#tick()} every tick
     */
    public void onUpdate() {
        for (ViewGroup vg :
                children) {
            if (vg.isVisible()) {
                vg.onUpdate();
            }
        }
    }

    @Override
    public boolean mouseMoved(int mouseX, int mouseY) {
        int tempX, tempY;
        ViewGroup vg;
        boolean flag;
        for (int i = children.size(); i > 0; i--) {
            vg = children.get(i - 1);
            tempX = mouseX - vg.getX();
            tempY = mouseY - vg.getY();

            flag = vg.mouseMoved(tempX, tempY);
            if (flag) {
                return true;
            }
        }
        flag = visible && isMouseover(true) && onMouseMoved(mouseX, mouseY);
        return flag;
    }

    protected boolean onMouseMoved(int mouseX, int mouseY) {
        return false;
    }

    public boolean handleKeyType(char typedChar, int mod) {
        ViewGroup vg;
        for (int i = children.size(); i > 0; i--) {
            vg = children.get(i - 1);
            if (vg.handleKeyType(typedChar, mod)) {
                return true;
            }
        }
        return isFocused && onKeyType(typedChar, mod);
    }

    public void focused() {

    }

    public void loseFocus() {

    }

    //public click event,visible is not affect
    public void performClickAction(int x, int y, int button) {
        onClicked(x, y, button);
    }

    public long getLastPressTime() {
        return lastPressTime;
    }

    public final void setLastPressTime(long lastPressTime) {
        this.lastPressTime = lastPressTime;
    }

    public void addChild(ViewGroup viewGroup) {
        viewGroup.parent = this;
        children.add(viewGroup);
    }

    public void addChildren(ViewGroup... viewGroup) {
        for (ViewGroup v :
                viewGroup) {
            addChild(v);
        }
    }

    public void addChild(int index, ViewGroup viewGroup) {
        viewGroup.parent = this;
        children.add(index, viewGroup);
    }

    public void removeChild(ViewGroup viewGroup) {
        viewGroup.parent = null;
        children.remove(viewGroup);
    }

    public void removeChild(int index) {
        children.remove(index).parent = null;
    }

    public void removeAllChildren() {
        for (ViewGroup v :
                children) {
            v.parent = null;
        }

        children.clear();
    }

    public void addAll(Collection<ViewGroup> viewGroups) {
        for (ViewGroup v :
                viewGroups) {
            addChild(v);
        }
    }

    public void forEach(Consumer<? super ViewGroup> consumer) {
        children.forEach(consumer);
    }

    public ViewGroup getChild(int index) {
        return children.get(index);
    }

    public int getChildrenSize() {
        return children.size();
    }

    /**
     * when GUI close
     */
    public void destroy() {
        for (ViewGroup view :
                children) {
            view.destroy();
        }
        children.clear();
    }

    public void setActionClick(IAction.ActionClick actionClick) {
        this.actionClick = actionClick;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        if (isFocused != focused) {
            isFocused = focused;
            if (focused) {
                this.focused();
            } else {
                this.loseFocus();
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (isFocused && !visible) {
            if (root.getFocusedView() == this) {
                root.setFocusedView(null);
            }
            setFocused(false);
        }
        for (ViewGroup vg :
                children) {
            vg.setVisible(visible);
        }
    }

    public boolean isHovering() {
        return isHover;
    }

    public void setHovering(boolean hover) {
        isHover = hover;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    /**
     * @param isInsideParents same as {@link ViewGroup#checkParentHover()}
     * @return true if it's hovering
     * <p>
     * the difference between this and {@link ViewGroup#isHovering()} is that hovering will only allow one
     * view being hovered,if its children being hovered,{@link ViewGroup#isHovering()} will return false.
     * In anther world, {@link ViewGroup#isHovering()} only check the top view in the rootView.
     */
    public boolean isMouseover(boolean isInsideParents) {
        if (isInsideParents && getParent() != null) {
            return isInside && getParent().isMouseover(true);
        } else {
            return isInside;
        }
    }

    /**
     * @param clipping whether it's children will be scissored by it
     */
    public void setClipping(boolean clipping) {
        isClipping = clipping;
    }

    public void setEnableIntercepted(boolean value) {
        enableIntercepted = value;
    }

    public void childrenEnableIntercepted(boolean value) {
        forEach(viewGroup -> viewGroup.setEnableIntercepted(value));
    }

    public void setViewClipMask(@Nonnull ViewClipMask viewClipMask) {
        this.viewClipMask = viewClipMask;
    }

    public static class Builder {
        ViewGroup viewGroup;

        public Builder setX(int x) {
            viewGroup.setX(x);
            return this;
        }

        public Builder setY(int y) {
            viewGroup.setY(y);
            return this;
        }

        public Builder setWidth(int width) {
            viewGroup.setWidth(width);
            return this;
        }

        public Builder setHeight(int height) {
            viewGroup.setHeight(height);
            return this;
        }

        public Builder setVisible(boolean visible) {
            viewGroup.setVisible(visible);
            return this;
        }

        public Builder setFocused(boolean focused) {
            viewGroup.setFocused(focused);
            return this;
        }

        public ViewGroup build() {
            return viewGroup;
        }
    }

    protected void drawDebugInfo(MatrixStack stack, int x, int y) {
        if (getRoot().isShowDebugInfo()) {
            drawRect(stack, x, y, x + 1, y - 1, colorInt(255, 255, 255, 255));
            drawStringIn(stack, getRoot().getFontRenderer(), "(" + x + "," + y + ")", x, y - getRoot().getFontRenderer().FONT_HEIGHT,
                    getWidth(), getHeight(), colorInt(255, 255, 255, 255));
        }
    }

    protected void drawDebugHoverBackground(MatrixStack stack) {
        if (getRoot().isShowDebugInfo() && isMouseover(true)) {
            drawRect(stack, 0, 0, getWidth(), getHeight(), colorInt(100, 80, 0, 40));
            drawRectOutline(stack, 0, 0, getWidth(), getHeight(), colorInt(255, 235, 59, 255), 1);
        }
    }

    //-----------------------------------------------tools----------------------------------------------------

    public int colorInt(int r, int g, int b, int a) {
        a = (a & 255) << 24;
        r = (r & 255) << 16;
        g = (g & 255) << 8;
        b &= 255;
        return a | r | g | b;
    }

    public int colorInt(Color color) {
        return colorInt(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public void drawString3D(MatrixStack stack, String s, float x, float y, float z, int r, int g, int b, int a) {
        drawString3D(stack, s, x, y, z, r, g, b, a, new Vector3f(0, 0, 1));
    }

    public void drawString3D(MatrixStack stack, String s, float x, float y, float z, int r, int g, int b, int a, Vector3f nomal) {
        drawString3D(stack, s, x, y, z, r, g, b, a, nomal, 0.05f);
    }

    public void drawString3D(MatrixStack stack, String s, float x, float y, float z, int r, int g, int b, int a, Vector3f nomal, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, z);
        GlStateManager.normal3f(nomal.getX(), nomal.getY(), nomal.getZ());
        GlStateManager.scalef(scale, scale, 1);
        GlStateManager.translatef(0, getRoot().context.fontRenderer.FONT_HEIGHT, 0);
        GlStateManager.rotatef(180, 1, 0, 0);
        drawString(stack, getRoot().context.fontRenderer, s, 0, (int) 0, colorInt(r, g, b, a));
        GlStateManager.popMatrix();
    }

    protected static void drawString(MatrixStack stack, FontRenderer fontRenderer, String s, int i, int i1, int colorInt) {
        Screen.drawString(stack, fontRenderer, s, i, i1, colorInt);
    }

    protected static void drawStringIn(MatrixStack stack, FontRenderer fontRenderer, String s, int x, int y, int limW, int limH, int colorInt) {
        int width = fontRenderer.getStringWidth(s);
        if (x + width > limW) {
            x = -width + limW;
        }
        if (y + fontRenderer.FONT_HEIGHT > limH) {
            y = -fontRenderer.FONT_HEIGHT + limH;
        }
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        Screen.drawString(stack, fontRenderer, s, x, y, colorInt);
    }

    public static void drawCenteredStringWithoutShadow(FontRenderer fontRendererIn, MatrixStack stack, String text, int x, int y, int color) {
        fontRendererIn.drawString(stack, text, (x - fontRendererIn.getStringWidth(text) / 2f), y, color);
    }

    public static void drawStringWithoutShadow(FontRenderer fontRendererIn, MatrixStack stack, String text, int x, int y, int color) {
        fontRendererIn.drawString(stack, text, x, y, color);
    }

    public static void drawRect(MatrixStack stack, int x, int y, int i, int i1, int colorInt) {
        Screen.fill(stack, x, y, i, i1, colorInt);
    }

    protected static void hLine(MatrixStack matrixStack, int minX, int maxX, int y, int color, int lineWidth) {
        if (maxX < minX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }

        drawRect(matrixStack, minX, y, maxX + lineWidth, y + lineWidth, color);
    }

    protected static void vLine(MatrixStack matrixStack, int x, int minY, int maxY, int color, int lineWidth) {
        if (maxY < minY) {
            int i = minY;
            minY = maxY;
            maxY = i;
        }

        drawRect(matrixStack, x, minY + lineWidth, x + lineWidth, maxY, color);
    }

    public static void drawTexturedModalRect(MatrixStack stack, int x, int y, float zLevel, int u, int v, int maxU, int maxV, int textureWidth, int textureHeight) {
        Matrix4f matrix4f = stack.getLast().getMatrix();
        float f = 1f / (float) (textureWidth);
        float f1 = 1f / (float) (textureHeight);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL20.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(matrix4f, x, y + maxV, zLevel).tex(u * f, maxV * f1).endVertex();
        bufferBuilder.pos(matrix4f, x + maxU, y + maxV, zLevel).tex(maxU * f, maxV * f1).endVertex();
        bufferBuilder.pos(matrix4f, x + maxU, y, zLevel).tex(maxU * f, v * f1).endVertex();
        bufferBuilder.pos(matrix4f, x, y, zLevel).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    private static void drawRectOutline(MatrixStack stack, int x, int y, int w, int h, int color, int lineWidth) {
        //drawRectOutline(stack.getLast().getMatrix(), x, y, w, h, 0f, color, lineWidth);
        hLine(stack, x, x + w, y - lineWidth, color, lineWidth);
        hLine(stack, x, x + w, y + h, color, lineWidth);
        vLine(stack, x - lineWidth, y - lineWidth, y + h, color, lineWidth);
        vLine(stack, x + w, y - lineWidth, y + h, color, lineWidth);
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public abstract static class ViewClipMask {
        protected ViewGroup viewGroup;

        public ViewClipMask(ViewGroup viewGroup) {
            this.viewGroup = viewGroup;
        }

        public abstract void drawMask(MatrixStack stack);
    }

    public static class RectangleClipMask extends ViewClipMask {

        public RectangleClipMask(ViewGroup viewGroup) {
            super(viewGroup);
        }

        @Override
        public void drawMask(MatrixStack stack) {
            {
                drawRect(stack, 0, 0, viewGroup.getWidth(), viewGroup.getHeight(), viewGroup.colorInt(255, 255, 255, 255));
            }
        }
    }

    public static class GuiRegion {
        public int left, top, right, bottom;

        private GuiRegion() {

        }

        private GuiRegion(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public static GuiRegion of(int left, int top, int right, int bottom) {
            return new GuiRegion(left, top, right, bottom);
        }

        public static GuiRegion of(MyGui gui) {
            return new GuiRegion(gui.getX(), gui.getY(), gui.getX() + gui.getWidth(), gui.getY() + gui.getHeight());
        }

    }
}

