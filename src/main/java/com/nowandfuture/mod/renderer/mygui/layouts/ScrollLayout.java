package com.nowandfuture.mod.renderer.mygui.layouts;

import com.nowandfuture.mod.renderer.mygui.RootView;
import com.nowandfuture.mod.renderer.mygui.ViewGroup;
import org.jetbrains.annotations.NotNull;

public class ScrollLayout extends FrameLayout {

    public ScrollLayout(@NotNull RootView rootView) {
        super(rootView);
    }

    public ScrollLayout(@NotNull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }
}
