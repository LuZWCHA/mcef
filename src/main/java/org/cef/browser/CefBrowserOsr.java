// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

// Modified by montoyo for MCEF

package org.cef.browser;

import net.minecraft.client.Minecraft;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.IStringVisitor;
import net.montoyo.mcef.client.StringVisitor;
import net.montoyo.mcef.utilities.Log;
import org.cef.CefClient;
import org.cef.DummyComponent;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents an off-screen rendered browser.
 * The visibility of this class is "package". To create a new
 * CefBrowser instance, please use CefBrowserFactory.
 */
public class CefBrowserOsr extends CefBrowser_N implements CefRenderHandler, IBrowser {
    private CefRenderer renderer_;
    private Rectangle browser_rect_ = new Rectangle(0, 0, 1, 1); // Work around CEF issue #1437.
    private Point screenPoint_ = new Point(0, 0);
    private boolean isTransparent_;
    private boolean justCreated_;
    private final DummyComponent dc_ = new DummyComponent();
    private MouseEvent lastMouseEvent = new MouseEvent(dc_, MouseEvent.MOUSE_MOVED, 0, 0, 0, 0, 0, false);

    public static boolean CLEANUP = true;
    private long window_handle_;
    private double scaleFactor_ = 1.0;
    private int depth = 32;
    private int depth_per_component = 8;

    private boolean activate = true;

    CefBrowserOsr(CefClient client, String url, boolean transparent, CefRequestContext context) {
        this(client, url, transparent, context, null, null);
    }

    private CefBrowserOsr(CefClient client, String url, boolean transparent,
                          CefRequestContext context, CefBrowserOsr parent, Point inspectAt) {
        super(client, url, context, parent, inspectAt);
        isTransparent_ = transparent;
        renderer_ = new CefPBORenderer(transparent);
    }

    @Override
    public void createImmediately() {
        justCreated_ = true;
        // Create the browser immediately.
        createBrowserIfRequired(false);
    }

    @Override
    public Component getUIComponent() {
        return dc_;
    }

    @Override
    public CefRenderHandler getRenderHandler() {
        return this;
    }

    @Override
    public CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution) {
        return null;
    }

    @Override
    protected CefBrowser_N createDevToolsBrowser(CefClient client, String url,
                                                 CefRequestContext context, CefBrowser_N parent, Point inspectAt) {
        return new CefBrowserOsr(
                client, url, isTransparent_, context, (CefBrowserOsr) this, inspectAt);
    }

    @Override
    public Rectangle getViewRect(CefBrowser browser) {
        return browser_rect_;
    }

    @Override
    public boolean getScreenInfo(CefBrowser browser, CefScreenInfo screenInfo) {
        screenInfo.Set(scaleFactor_, depth, depth_per_component, false, browser_rect_.getBounds(),
                browser_rect_.getBounds());
        return false;
    }

    @Override
    public Point getScreenPoint(CefBrowser browser, Point viewPoint) {
        Point screenPoint = new Point(screenPoint_);
        screenPoint.translate(viewPoint.x, viewPoint.y);
        return screenPoint;
    }

    @Override
    public void onPopupShow(CefBrowser browser, boolean show) {
        if (!show) {
            renderer_.clearPopupRects();
            invalidate();
        }
    }

    @Override
    public void onPopupSize(CefBrowser browser, Rectangle size) {
        renderer_.onPopupSize(size);
    }

    //added by montoyo
    private static class PaintData {
        private ByteBuffer buffer;
        private int width;
        private int height;
        private Rectangle[] dirtyRects;
        private boolean hasFrame;
        private boolean fullReRender;
    }

    private final PaintData paintData = new PaintData();

    //modified by montoyo
    @Override
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects,
                        ByteBuffer buffer, int width, int height) {
        if (popup)
            return;

        final int size = (width * height) << 2;

        synchronized (paintData) {
            if (buffer.limit() > size)
                Log.warning("Skipping MCEF browser frame, data is too heavy"); //TODO: Don't spam
            else {
                if (paintData.hasFrame) //The previous frame was not uploaded to GL texture, so we skip it and render this on instead
                    paintData.fullReRender = true;

                if (paintData.buffer == null || size != paintData.buffer.capacity()) //This only happens when the browser gets resized
                    paintData.buffer = BufferUtils.createByteBuffer(size);

                paintData.buffer.position(0);
                paintData.buffer.limit(buffer.limit());
                buffer.position(0);
                paintData.buffer.put(buffer);
                paintData.buffer.flip();

                paintData.width = width;
                paintData.height = height;
                paintData.dirtyRects = dirtyRects;
                paintData.hasFrame = true;
            }
        }
    }

    //added by montoyo
    public void mcefUpdate() {
        synchronized (paintData) {
            if (paintData.hasFrame) {
                renderer_.onPaint(false, paintData.dirtyRects, paintData.buffer, paintData.width, paintData.height, paintData.fullReRender);
                paintData.hasFrame = false;
                paintData.fullReRender = false;
            }
        }
    }

    private long getMCEFWindowsHandler() {
        return Minecraft.getInstance().getMainWindow().getHandle();
    }

    public static int remapCursor(int cursorType) {
        switch (cursorType) {
            case Cursor.CROSSHAIR_CURSOR:
                return GLFW.GLFW_CROSSHAIR_CURSOR;
            case Cursor.HAND_CURSOR:
                return GLFW.GLFW_HAND_CURSOR;
            case Cursor.MOVE_CURSOR:
                return GLFW.GLFW_IBEAM_CURSOR;
            case Cursor.S_RESIZE_CURSOR:
            case Cursor.N_RESIZE_CURSOR:
                return GLFW.GLFW_VRESIZE_CURSOR;
            case Cursor.W_RESIZE_CURSOR:
            case Cursor.E_RESIZE_CURSOR:
                return GLFW.GLFW_HRESIZE_CURSOR;
            default:
                return GLFW.GLFW_ARROW_CURSOR;
        }
    }

    @Override
    public boolean onCursorChange(CefBrowser browser, final int cursorType) {
        //modified by nowandfuture
        //modified for minecraft 1.13+(lwjgl3+)
        window_handle_ = getMCEFWindowsHandler();
        //cast to a system cursor
        long systemCursor = GLFW.glfwCreateStandardCursor(remapCursor(cursorType));
        GLFW.glfwSetCursor(window_handle_, systemCursor);
        return true;
    }

    private static final class SyntheticDragGestureRecognizer extends DragGestureRecognizer {
        public SyntheticDragGestureRecognizer(Component c, int action, MouseEvent triggerEvent) {
            super(new DragSource(), c, action);
            appendEvent(triggerEvent);
        }

        protected void registerListeners() {
        }

        protected void unregisterListeners() {
        }
    }

    ;

    @Override
    public boolean startDragging(CefBrowser browser, CefDragData dragData, int mask, int x, int y) {
        int action = (mask & CefDragData.DragOperations.DRAG_OPERATION_MOVE) == 0
                ? DnDConstants.ACTION_COPY
                : DnDConstants.ACTION_MOVE;
        MouseEvent triggerEvent =
                new MouseEvent(dc_, MouseEvent.MOUSE_DRAGGED, 0, 0, x, y, 0, false);
        DragGestureEvent ev = new DragGestureEvent(
                new SyntheticDragGestureRecognizer(dc_, action, triggerEvent), action,
                new Point(x, y), new ArrayList<>(Collections.singletonList(triggerEvent)));

        DragSource.getDefaultDragSource().startDrag(ev, /*dragCursor=*/null,
                new StringSelection(dragData.getFragmentText()), new DragSourceAdapter() {
                    @Override
                    public void dragDropEnd(DragSourceDropEvent dsde) {
                        dragSourceEndedAt(dsde.getLocation(), mask);
                        dragSourceSystemDragEnded();
                    }
                });
        return true;
    }

    @Override
    public void updateDragCursor(CefBrowser browser, int operation) {
        // TODO(JCEF) Prepared for DnD support using OSR mode.
    }

    //modify by nowandfuture
    private synchronized long getWindowHandle() {
        return 0;
    }

    private void createBrowserIfRequired(boolean hasParent) {
        long windowHandle = 0;
        if (hasParent) {
            windowHandle = 0;
        }

        if (getNativeRef("CefBrowser") == 0) {
            if (getParentBrowser() != null) {
                createDevTools(getParentBrowser(), getClient(), windowHandle, true, isTransparent_,
                        null, getInspectAt());
            } else {
                createBrowser(getClient(), windowHandle, getUrl(), true, isTransparent_, null,
                        getRequestContext());
            }
        } else if (hasParent && justCreated_) {
            notifyAfterParentChanged();
            setFocus(true);
            justCreated_ = false;
        }
    }

    private void notifyAfterParentChanged() {
        // With OSR there is no native window to reparent but we still need to send the
        // notification.
        getClient().onAfterParentChanged(this);
    }

    @Override
    public void close() {
        if (CLEANUP) {
            renderer_.cleanup();
        }

        super.close(true); //true to ignore confirmation popups
        activate = false;
    }

    @Override
    public boolean isActivate() {
        return activate;
    }

    @Override
    public void resize(int width, int height) {
        browser_rect_.setBounds(0, 0, width, height);
        dc_.setBounds(browser_rect_);
        dc_.setVisible(true);
        wasResized(width, height);
    }

    @Override
    public void draw(double x1, double y1, double x2, double y2) {
        renderer_.render(x1, y1, x2, y2);
    }

    @Override
    public int getTextureID() {
        return renderer_.getTextureId();
    }

    @Override
    public void injectMouseMove(int x, int y, int mods, boolean left) {
        //FIXME: 'left' is not used as it causes bugs since MCEF 1.11
        MouseEvent ev = new MouseEvent(dc_, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), mods, x, y, 0, false);
        lastMouseEvent = ev;
        sendMouseEvent(ev);
    }

    @Override
    public void injectMouseDrag(int x, int y, int btn, int dragX, int dragY) {
        MouseEvent ev = new MouseEvent(dc_, MouseEvent.MOUSE_DRAGGED, 0, InputEvent.getMaskForButton(btn), x, y, 1, false, btn);
        sendMouseEvent(ev);
    }

    int lastBtn = 0;

    @Override
    public void injectMouseButton(int x, int y, int mods, int btn, boolean pressed, int ccnt) {
        MouseEvent ev = new MouseEvent(dc_, pressed ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), mods, x, y, ccnt, false, btn);
        lastBtn = btn;
        sendMouseEvent(ev);
    }

    @Override
    public void injectKeyTyped(char c, int key_code, int mods) {
        KeyEvent ev = new KeyEvent(dc_, KeyEvent.KEY_TYPED, System.currentTimeMillis(), mods, key_code, c);
        sendKeyEvent(ev);
    }

    private static final HashMap<Integer, Character> WORST_HACK = new HashMap<>();

    @Override
    public void injectKeyPressedByKeyCode(int keyCode, char c, int mods) {
        if (c != '\0') {
            synchronized (WORST_HACK) {
                WORST_HACK.put(keyCode, c);
            }
        }

        KeyEvent ev = new KeyEvent(dc_, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), mods, remapKeycode(keyCode, c), c);
        sendKeyEvent(ev);
    }

    @Override
    public void injectKeyReleasedByKeyCode(int keyCode, char c, int mods) {
        if (c == '\0') {
            synchronized (WORST_HACK) {
                c = WORST_HACK.getOrDefault(keyCode, '\0');
            }
        }

        KeyEvent ev = new KeyEvent(dc_, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), mods, remapKeycode(keyCode, c), c);
        sendKeyEvent(ev);
    }

    @Override
    public void injectMouseWheel(int x, int y, int mods, int amount, int rot) {
        MouseWheelEvent ev = new MouseWheelEvent(dc_, MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(), mods, x, y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, amount, rot);
        sendMouseWheelEvent(ev);
    }

    @Override
    public void runJS(String script, String frame) {
        executeJavaScript(script, frame, 0);
    }

    @Override
    public void visitSource(IStringVisitor isv) {
        getSource(new StringVisitor(isv));
    }

    @Override
    public boolean isPageLoading() {
        return isLoading();
    }

    public static int remapKeycode(int kc, char c) {
        switch (kc) {
            case GLFW.GLFW_KEY_BACKSPACE:
                return KeyEvent.VK_BACK_SPACE;
            case GLFW.GLFW_KEY_DELETE:
                return KeyEvent.VK_DELETE;
            case GLFW.GLFW_KEY_DOWN:
                return KeyEvent.VK_DOWN;
            case GLFW.GLFW_KEY_ENTER:
                return KeyEvent.VK_ENTER;
            case GLFW.GLFW_KEY_ESCAPE:
                return KeyEvent.VK_ESCAPE;
            case GLFW.GLFW_KEY_LEFT:
                return KeyEvent.VK_LEFT;
            case GLFW.GLFW_KEY_RIGHT:
                return KeyEvent.VK_RIGHT;
            case GLFW.GLFW_KEY_TAB:
                return KeyEvent.VK_TAB;
            case GLFW.GLFW_KEY_UP:
                return KeyEvent.VK_UP;
            case GLFW.GLFW_KEY_PAGE_UP:
                return KeyEvent.VK_PAGE_UP;
            case GLFW.GLFW_KEY_PAGE_DOWN:
                return KeyEvent.VK_PAGE_DOWN;
            case GLFW.GLFW_KEY_END:
                return KeyEvent.VK_END;
            case GLFW.GLFW_KEY_HOME:
                return KeyEvent.VK_HOME;

            default:
                return c;
        }
    }
}
