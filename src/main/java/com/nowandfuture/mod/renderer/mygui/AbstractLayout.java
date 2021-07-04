package com.nowandfuture.mod.renderer.mygui;

import com.mojang.blaze3d.matrix.MatrixStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

//not finished
public abstract class AbstractLayout<T extends LayoutParameter> extends ViewGroup {

    private T layoutParameter;
    private Color color;

    private final Map<Class<? extends LayoutParameter>, LayoutParameter> cache =
            new HashMap<>();

    public AbstractLayout(@Nonnull RootView rootView) {
        super(rootView);
        color = new Color(0, 0, 0, 0);
    }

    public AbstractLayout(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        color = new Color(0, 0, 0, 0);
    }

    public AbstractLayout(@Nonnull RootView rootView, ViewGroup parent, @Nonnull T layoutParameter) {
        super(rootView, parent);
        this.layoutParameter = layoutParameter;
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        drawBackground(stack);
    }

    protected void drawBackground(MatrixStack stack) {
        drawRect(stack, 0, 0, getWidth(), getHeight(), colorInt(color));
    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return true;
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {

    }

    @Nonnull
    protected <S extends LayoutParameter> S getLayoutParameter(Class<S> clazz) {
        if (layoutParameter != null && clazz.equals(layoutParameter.getClass()))
            return (S) layoutParameter;

        LayoutParameter q = cache.get(clazz);

        if (q == null) {
            try {
                q = clazz.newInstance().createDefaultParameter();
                cache.put(clazz, q);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();

            }

        } else {
            return (S) q;
        }

        return (S) q;
    }

    public void pushLayoutParameter(@Nonnull LayoutParameter layoutParameter) {
        cache.put(layoutParameter.getClass(), layoutParameter);
    }

    public void setLayoutParameter(@Nonnull T layoutParameter) {
        this.layoutParameter = layoutParameter;
    }

    @Nullable
    public T getLayoutParameter() {
        return layoutParameter;
    }

    @Override
    public boolean onKeyType(char typedChar, int mod) {
        return false;
    }

    @Override
    protected void onChildrenLayout() {
        super.onChildrenLayout();
    }

    public void setBackgroundColor(Color color) {
        this.color = color;
    }

    public Color getBackgroundColor() {
        return color;
    }
}
