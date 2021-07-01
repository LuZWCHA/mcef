package net.montoyo.mcef.client;

import net.minecraft.client.Minecraft;
import net.montoyo.mcef.api.IDisplayHandler;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefBrowserOsr;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandler;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

public class DisplayHandler implements CefDisplayHandler {
    
    private final ArrayList<IDisplayHandler> list = new ArrayList<>();
    private final ArrayList<EventData> queue = new ArrayList<>();

    private enum EventType {

        ADDRESS_CHANGE,
        TITLE_CHANGE,
        TOOLTIP,
        STATUS_MESSAGE,
        CURSOR_CHANGE

    }

    private static final class EventData {

        private final CefBrowser browser;
        private final String data;
        private final EventType type;

        private EventData(CefBrowser b, String d, EventType t) {
            browser = b;
            data = d;
            type = t;
        }

        private void execute(IDisplayHandler idh) {
            switch(type) {
                case ADDRESS_CHANGE:
                    idh.onAddressChange((CefBrowserOsr) browser, data);
                    break;

                case TITLE_CHANGE:
                    idh.onTitleChange((CefBrowserOsr) browser, data);
                    break;

                case TOOLTIP:
                    idh.onTooltip((CefBrowserOsr) browser, data);
                    break;

                case STATUS_MESSAGE:
                    idh.onStatusMessage((CefBrowserOsr) browser, data);
                    break;

                case CURSOR_CHANGE:
//                    idh.onCursorChange((CefBrowserOsr) browser, data);
                    break;
            }
        }

    }

    @Override
    public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
        synchronized(queue) {
            queue.add(new EventData(browser, url, EventType.ADDRESS_CHANGE));
        }
    }

    @Override
    public void onTitleChange(CefBrowser browser, String title) {
        synchronized(queue) {
            queue.add(new EventData(browser, title, EventType.TITLE_CHANGE));
        }
    }

    @Override
    public boolean onTooltip(CefBrowser browser, String text) {
        synchronized(queue) {
            queue.add(new EventData(browser, text, EventType.TOOLTIP));
        }

        return false;
    }

    @Override
    public void onStatusMessage(CefBrowser browser, String value) {
        synchronized(queue) {
            queue.add(new EventData(browser, value, EventType.STATUS_MESSAGE));
        }
    }

    @Override
    public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line) {
        return false;
    }

    private long getMCEFWindowsHandler(){
        return Minecraft.getInstance().getMainWindow().getHandle();
    }

    public static int remapCursor(int cursorType){
        switch (cursorType){
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
    public boolean onCursorChange(CefBrowser browser, int cursorType) {
        //modified by nowandfuture
        //modified for minecraft 1.13+(lwjgl3+)
        long window_handle_ = getMCEFWindowsHandler();
        //cast to a system cursor
        long systemCursor = GLFW.glfwCreateStandardCursor(remapCursor(cursorType));
        GLFW.glfwSetCursor(window_handle_, systemCursor);
        return true;
    }

    public void addHandler(IDisplayHandler h) {
        list.add(h);
    }

    public void update() {
        synchronized(queue) {
            while(!queue.isEmpty()) {
                EventData ed = queue.remove(0);

                for(IDisplayHandler idh : list)
                    ed.execute(idh);
            }
        }
    }

}
