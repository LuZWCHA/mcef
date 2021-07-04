package com.nowandfuture.mod.renderer.mygui.api;

public interface IAction {
    void clicked(MyGui gui, int button);

    void press(MyGui gui, int button);

    void release(MyGui gui, int button);

    void longClick(MyGui gui, int button, long lastTime);

    abstract class ActionClick implements IAction {
        public final void press(MyGui gui, int button) {
        }

        public final void release(MyGui gui, int button) {
        }

        public void longClick(MyGui gui, int button, long lastTime) {
        }
    }

    abstract class ActionPress implements IAction {
        public final void clicked(MyGui gui, int button) {
        }

        public final void release(MyGui gui, int button) {
        }
    }

    abstract class ActionRelease implements IAction {
        public final void clicked(MyGui gui, int button) {
        }

        public final void press(MyGui gui, int button) {
        }
    }
}