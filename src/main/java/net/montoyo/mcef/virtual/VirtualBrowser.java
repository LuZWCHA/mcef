package net.montoyo.mcef.virtual;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.IStringVisitor;

public class VirtualBrowser implements IBrowser {
    boolean activate = true;

    @Override
    public void close() {
        activate = false;
    }

    @Override
    public boolean isActivate() {
        return activate;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void draw(MatrixStack stack, double x1, double y1, double x2, double y2) {
    }

    @Override
    public int getTextureID() {
        return 0;
    }

    @Override
    public void injectMouseMove(int x, int y, int mods, boolean left) {
    }

    @Override
    public void injectMouseDrag(int x, int y, int btn, int dragX, int dragY) {

    }

    @Override
    public void injectMouseButton(int x, int y, int mods, int btn, boolean pressed, int ccnt) {
    }

    @Override
    public void injectKeyTyped(char c, int key_code, int mods) {
    }

    @Override
    public void injectKeyPressedByKeyCode(int keyCode, char c, int mods) {
    }

    @Override
    public void injectKeyReleasedByKeyCode(int keyCode, char c, int mods) {
    }

    @Override
    public void injectMouseWheel(int x, int y, int mods, int amount, int rot) {
    }

    @Override
    public void runJS(String script, String frame) {
    }

    @Override
    public void loadURL(String url) {
    }

    @Override
    public void goBack() {
    }

    @Override
    public void goForward() {
    }

    @Override
    public String getURL() {
        return "about:blank";
    }

    @Override
    public void visitSource(IStringVisitor isv) {
        isv.visit("https://www.youtube.com/watch?v=VX5gXHcbJAk");
    }

    @Override
    public boolean isPageLoading() {
        return true;
    }

}
