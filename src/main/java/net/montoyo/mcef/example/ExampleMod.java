package net.montoyo.mcef.example;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.montoyo.mcef.api.*;
import net.montoyo.mcef.utilities.Log;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * An example mod that shows you how to use MCEF.
 * Assuming that it is client-side only and that onInit() is called on initialization.
 * This example shows a simple 2D web browser when pressing F10.
 *
 * @author montoyo
 */
public class ExampleMod implements IDisplayHandler, IJSQueryHandler {

    public static ExampleMod INSTANCE;

    public ScreenCfg hudBrowser = null;
    private KeyBinding key = new KeyBinding("Open Browser", GLFW.GLFW_KEY_F10, "key.categories.misc");
    private Minecraft mc = Minecraft.getInstance();
    private BrowserScreen backup = null;
    private API api;

    public ExampleMod(){
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.addListener(this::onDrawHUD);
    }

    public API getAPI() {
        return api;
    }

    public void onPreInit() {
        api = MCEFApi.getAPI();
        if (api == null)
            return;

        //to send the modscheme in main thread or eventbus will not transform it from context loader
        api.registerScheme("mod", ModScheme.class, true, false, false, true, true, false, false);


        //This clazz list is contain the Clazz that will be load when create request and response by JNI(Jcef), this will be called at ITS IO THREAD
        //but it will cause the NULL Exception from Minecraft's Eventbus which do transform for all class while the Thread's context loader is null...
        //To avoid this, I do preloading classes before Jcef call the ClassLoader to make the EventBus cache the class info.
        List<String> classNames = Lists.newArrayList(
                "org.cef.misc.BoolRef",
                "org.cef.callback.CefCallback_N",
                "net.montoyo.mcef.api.IScheme",
                "net.montoyo.mcef.api.ISchemeResponseData",
                "net.montoyo.mcef.api.ISchemeResponseHeaders",
                "net.montoyo.mcef.api.IStringVisitor",
                "org.cef.callback.CefSchemeRegistrar_N",
                "net.montoyo.mcef.client.SchemeResourceHandler",
                "org.cef.handler.CefResourceHandlerAdapter",
                "net.montoyo.mcef.api.SchemePreResponse",
                "net.montoyo.mcef.client.SchemeResourceHandler",
                "net.montoyo.mcef.client.SchemeResourceHandler$1",
                "org.cef.network.CefResponse_N",
                "org.cef.network.CefResponse",
                "org.cef.misc.IntRef",
                "org.cef.misc.StringRef",
                "net.montoyo.mcef.client.SchemeResponseHeaders",
                "net.montoyo.mcef.client.SchemeResponseData"
        );

        List<String> preloadList = new ArrayList<>(classNames.size());
        classNames.forEach(s -> {
            try {
                Class clazz = Class.forName(s);
                preloadList.add(clazz.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                //skip
            }

        });

        Log.info("Preload: %o class.Ignore the info if you not care about the opportunity of the Class first load..", preloadList.size());
    }

    public void onInit() {

        //The Example Mod is always at client! In your mod to check the Side!.
        //Register key binding and listen to the FML event bus for ticks.
        ClientRegistry.registerKeyBinding(key);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);

        if (api != null) {
            //Register this class to handle onAddressChange and onQuery events
            api.registerDisplayHandler(this);
            api.registerJSQueryHandler(this);
        }
    }

    public void setBackup(BrowserScreen bu) {
        backup = bu;
    }

    public boolean hasBackup() {
        return (backup != null);
    }

    public BrowserScreen getBackup() {
        return backup;
    }

    public void showScreen(String url) {
        if (mc.currentScreen instanceof BrowserScreen)
            ((BrowserScreen) mc.currentScreen).loadURL(url);
        else if (hasBackup()) {
            mc.displayGuiScreen(backup);
            backup.loadURL(url);
            backup = null;
        } else
            mc.displayGuiScreen(new BrowserScreen(url));
    }

    public IBrowser getBrowser() {
        if (mc.currentScreen instanceof BrowserScreen)
            return ((BrowserScreen) mc.currentScreen).browser;
        else if (backup != null)
            return backup.browser;
        else
            return null;
    }

    public void onTick(TickEvent ev) {
        if (ev.phase == TickEvent.Phase.START && ev.side.isClient() && ev.type == TickEvent.Type.CLIENT) {
            //Check if our key was pressed
            if (key.isPressed() && !(mc.currentScreen instanceof BrowserScreen)) {
                //Display the web browser UI.
                mc.displayGuiScreen(hasBackup() ? backup : new BrowserScreen());
                backup = null;
            }
        }
    }

    @Override
    public void onAddressChange(IBrowser browser, String url) {
        //Called by MCEF if a browser's URL changes. Forward this event to the screen.
        if (mc.currentScreen instanceof BrowserScreen)
            ((BrowserScreen) mc.currentScreen).onUrlChanged(browser, url);
        else if (hasBackup())
            backup.onUrlChanged(browser, url);
    }

    @Override
    public void onTitleChange(IBrowser browser, String title) {
    }

    @Override
    public void onTooltip(IBrowser browser, String text) {
    }

    @Override
    public void onStatusMessage(IBrowser browser, String value) {
    }

    @Override
    public boolean handleQuery(IBrowser b, long queryId, String query, boolean persistent, IJSQueryCallback cb) {
        if (b != null && query.equalsIgnoreCase("username")) {
            if (b.getURL().startsWith("mod://")) {
                //Only allow MCEF URLs to get the player's username to keep his identity secret

                mc.runAsync(() -> {
                    //Add this to a scheduled task because this is NOT called from the main Minecraft thread...

                    try {
                        String name = null;
                        if (mc.player != null) {
                            name = mc.player.getName().getString();
                        }
                        cb.success(name);
                    } catch (Throwable t) {
                        cb.failure(500, "Internal error.");
                        Log.warning("Could not get username from JavaScript:");
                        t.printStackTrace();
                    }
                });
            } else
                cb.failure(403, "Can't access username from external page");

            return true;
        }

        return false;
    }

    @Override
    public void cancelQuery(IBrowser b, long queryId) {
    }

    public void onDrawHUD(RenderGameOverlayEvent.Post ev) {
        if (hudBrowser != null) {
            if (hudBrowser.isBrowserActivate()) {
                hudBrowser.render(ev.getMatrixStack(), 0, 0, ev.getPartialTicks());
            } else {
                hudBrowser = null;
            }
        }
    }

}
