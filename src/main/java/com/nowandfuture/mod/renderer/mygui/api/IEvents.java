package com.nowandfuture.mod.renderer.mygui.api;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.nowandfuture.mod.renderer.mygui.RootView;

public interface IEvents {

    interface GuiEvent extends Comparable<GuiEvent> {
        void draw(MatrixStack stack, int mouseX, int mouseY, float partialTicks);

        boolean isDied(int mouseX, int mouseY, float partialTicks);

        void destroy(int mouseX, int mouseY, float partialTicks);

        int getPriority();

        void create(RootView rootView);

        abstract class AbstractGuiEvent implements GuiEvent {

            @Override
            public int compareTo(GuiEvent o) {
                return this.getPriority() - o.getPriority();
            }

            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public boolean isDied(int mouseX, int mouseY, float partialTicks) {
                return true;
            }
        }
    }

    interface IInputEvent {
        boolean mouseClicked(int mouseX, int mouseY, int mouseButton);

        boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton);

        void mouseReleased(int mouseX, int mouseY, int state);

        boolean mousePressed(int mouseX, int mouseY, int state);

        boolean mouseMoved(int mouseX, int mouseY);

        boolean mouseScrolled(int mouseX, int mouseY, float delta);

        boolean keyPressed(int keyCode, int scanCode, int modifiers);

        boolean keyReleased(int keyCode, int scanCode, int modifiers);
    }
}
