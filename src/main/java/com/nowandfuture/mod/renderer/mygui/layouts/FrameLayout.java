package com.nowandfuture.mod.renderer.mygui.layouts;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.nowandfuture.mod.renderer.mygui.AbstractLayout;
import com.nowandfuture.mod.renderer.mygui.LayoutParameter;
import com.nowandfuture.mod.renderer.mygui.RootView;
import com.nowandfuture.mod.renderer.mygui.ViewGroup;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class FrameLayout extends AbstractLayout<FrameLayout.FrameLayoutParameter> {

    public FrameLayout(@Nonnull RootView rootView) {
        super(rootView);
    }

    public FrameLayout(@Nonnull RootView rootView, ViewGroup parent, @Nonnull FrameLayoutParameter layoutParameter) {
        super(rootView, parent);
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {
        boolean fpw = getLayoutParameter(FrameLayoutParameter.class).isFlowParentWidth();
        boolean fph = getLayoutParameter(FrameLayoutParameter.class).isFlowParentHeight();

        if (fpw) {
            setWidthWithoutLayout(parentWidth);
        }

        if (fph) {
            setHeightWithoutLayout(parentHeight);
        }
    }

    public FrameLayout(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.onDraw(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return true;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }

    public static class FrameLayoutParameter extends LayoutParameter {

        private boolean flowParentWidth = true;
        private boolean flowParentHeight = true;

        public FrameLayoutParameter() {

        }

        public FrameLayoutParameter(boolean flowParentWidth, boolean flowParentHeight) {
            this.flowParentWidth = flowParentWidth;
            this.flowParentHeight = flowParentHeight;
        }

        @NotNull
        @Override
        protected LayoutParameter createDefaultParameter() {
            return new FrameLayoutParameter();
        }

        public boolean isFlowParentHeight() {
            return flowParentHeight;
        }

        public void setFlowParentHeight(boolean flowParentHeight) {
            this.flowParentHeight = flowParentHeight;
        }

        public boolean isFlowParentWidth() {
            return flowParentWidth;
        }

        public void setFlowParentWidth(boolean flowParentWidth) {
            this.flowParentWidth = flowParentWidth;
        }
    }
}
