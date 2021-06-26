package net.montoyo.mcef.client;

import com.nowandfuture.mod.renderer.gui.FPSGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModWorkManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.progress.StartupMessageManager;
import net.montoyo.mcef.BaseProxy;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.IDisplayHandler;
import net.montoyo.mcef.api.IJSQueryHandler;
import net.montoyo.mcef.api.IScheme;
import net.montoyo.mcef.example.ExampleMod;
import net.montoyo.mcef.utilities.Log;
import net.montoyo.mcef.utilities.Util;
import net.montoyo.mcef.virtual.VirtualBrowser;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.*;
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig;
import org.cef.handler.CefLifeSpanHandler;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientProxy extends BaseProxy {

    // TODO: 2021/6/7 I will modify the way of loading Libraries, for example, moving the libs to java lib path
    public static String ROOT = ".";
    public static String JCEF_ROOT = ".";
    public static boolean VIRTUAL = false;

    private CefApp cefApp;
    private CefClient cefClient;
    private CefMessageRouter cefRouter;
    private final ArrayList<CefBrowserOsr> browsers = new ArrayList<>();
    private String updateStr;
    private final Minecraft mc = Minecraft.getInstance();
    private final DisplayHandler displayHandler = new DisplayHandler();
    private final HashMap<String, String> mimeTypeMap = new HashMap<>();
    private final CefLifeSpanHandler cefLifeSpanHandlerAdapter = new CefLifeSpanHandlerAdapter() {

        @Override
        public void onAfterCreated(CefBrowser browser) {
        }

        @Override
        public boolean doClose(CefBrowser browser) {
            browser.close(true);
            return false;
        }

        @Override
        public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url, String target_frame_name) {
            browser.loadURL(target_url);
            return true;
        }
    };
    private final AppHandler appHandler = new AppHandler();
    private ExampleMod exampleMod;

    public static final String LINUX_WIKI = "https://montoyo.net/wdwiki/Linux";

    private boolean checkFiles() {

        return false;
    }

    private List<String> downloadSourceInfo(){

        return null;
    }

    private boolean downloadSources(){

        return true;
    }


    @Override
    public void onPreInit() {
        exampleMod = new ExampleMod();
        exampleMod.onPreInit();
//        Optional<Consumer<String>> mcLoaderConsumer = StartupMessageManager.mcLoaderConsumer();
//
//        mcLoaderConsumer.ifPresent(stringConsumer -> stringConsumer.accept("MCEF: start check libraries"));
//
//        if(checkFiles()){
//
//            return;
//        }
//
//        mcLoaderConsumer.ifPresent(stringConsumer -> stringConsumer.accept("MCEF: start collect download source list."));
//
//        List<String> infos = downloadSourceInfo();
//
//        mcLoaderConsumer.ifPresent(stringConsumer -> stringConsumer.accept("MCEF: start download lib files."));
//
//        if(!downloadSources()){
//            mcLoaderConsumer.ifPresent(stringConsumer -> stringConsumer.accept("MCEF: download failed, go to Virtual mode."));
//            VIRTUAL = true;
//            return;
//        }
//
//        if(!checkFiles()){
//            mcLoaderConsumer.ifPresent(stringConsumer -> stringConsumer.accept("MCEF: files check invaild, go to Virtual mode."));
//            VIRTUAL = true;
//        }

    }

    //to improve the fps of minecraft, I add the way to limit the browser's fps by skip some message-loops
    private final KeyBinding key = new KeyBinding("Browser FPS proportion", GLFW.GLFW_KEY_HOME, "key.categories.ui");

    //montoyo never provide the downloadable source for the latest libs, I removed the codes
    //for the new source's version of CEF and java-cef see my mcef-1.12.2 fork's branch
    @Override
    public void onInit() {
        ClientRegistry.registerKeyBinding(key);

        // TODO: 2021/6/8 download the libs now

        // CefApp CefClient should init at minecraft's main thread
        Runnable runnable = () -> {
            appHandler.setArgs(MCEF.CEF_ARGS);

            ROOT = mc.gameDirectory.getAbsolutePath().replaceAll("\\\\", "/");
            if (ROOT.endsWith("."))
                ROOT = ROOT.substring(0, ROOT.length() - 1);

            // get the root path
            if (ROOT.endsWith("/"))
                ROOT = ROOT.substring(0, ROOT.length() - 1);

            JCEF_ROOT = ROOT + "/" + "jcef";
            if(!Files.exists(Paths.get(JCEF_ROOT))){
                try {
                    Files.createDirectories(Paths.get(JCEF_ROOT));
                } catch (IOException e) {
                    e.printStackTrace();
                    VIRTUAL = true;
                }
            }

            Log.info("Now adding \"%s\" to jcef.library.path", JCEF_ROOT);

            boolean success = false;
            String libraryPath = JCEF_ROOT;
            success = Util.addPath2JcefLibPath(JCEF_ROOT);

            if (!success) {
                VIRTUAL = true;
                Log.warning("Failed to add \"%s\" to jcef.library.path", libraryPath);
                return;
            }

            Log.info("Done without errors.");

            //modify the permission on Linux

            String exeSuffix;
            if (OS.isWindows())
                exeSuffix = ".exe";
            else
                exeSuffix = "";

            File subproc = new File(JCEF_ROOT, "jcef_helper" + exeSuffix);
            if (OS.isLinux() && !subproc.canExecute()) {
                try {
                    int retCode = Runtime.getRuntime().exec(new String[]{"/usr/bin/chmod", "+x", subproc.getAbsolutePath()}).waitFor();

                    if (retCode != 0)
                        throw new RuntimeException("chmod exited with code " + retCode);
                } catch (Throwable t) {
                    Log.errorEx("Error while giving execution rights to jcef_helper. MCEF will probably enter virtual mode. You can fix this by chmoding jcef_helper manually.", t);
                }
            } else if (OS.isMacintosh()) {
                Path path = Paths.get(libraryPath,
                        "../Frameworks/jcef Helper.app/Contents/MacOS/jcef Helper");

                File file = path.toFile();
                if (!file.exists()) {
                    VIRTUAL = true;
                    Log.warning("Failed to find the Jcef Helper file at " + path);
                    return;
                }

            }

            CefSettings settings = new CefSettings();
            settings.windowless_rendering_enabled = true;
            settings.background_color = settings.new ColorType(0, 255, 255, 255);
            settings.locales_dir_path = (new File(JCEF_ROOT, "MCEFLocales")).getAbsolutePath();
            settings.cache_path = (new File(JCEF_ROOT, "MCEFCache")).getAbsolutePath();
            settings.browser_subprocess_path = subproc.getAbsolutePath();
            //For debug, to make the log effective I leave it here...
            settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_VERBOSE;

            try {
                ArrayList<String> libs = new ArrayList<>();

                if (OS.isWindows()) {
                    //these libs are options
                    libs.add("d3dcompiler_47.dll");
                    libs.add("libGLESv2.dll");
                    libs.add("libEGL.dll");
                    //required by windows CEF
                    libs.add("chrome_elf.dll");
                    libs.add("libcef.dll");
                    //add jcef
                    libs.add("jcef.dll");
                } else if (OS.isLinux()) {
                    libs.add("libcef.so");
                    //add jcef
                    libs.add("libjcef.so");
                } else if (OS.isMacintosh()) {

            /*
              Chromium Embedded Framework will added at init step.
              @see CefApp#startup(String[])
             */

                    //add jcef
                    libs.add("libjcef.pylib");
                }

                for (String lib : libs) {
                    File f = new File(JCEF_ROOT, lib);
                    try {
                        f = f.getCanonicalFile();
                    } catch (IOException ex) {
                        f = f.getAbsoluteFile();
                    }

                    Log.info("Adding lib:  " + f.toPath());
                    System.load(f.getPath());
                }

                CefApp.startup(MCEF.CEF_ARGS);

                if (CefApp.getState() != CefApp.CefAppState.INITIALIZED) {
                    loadMimeTypeMapping();
                    CefApp.addAppHandler(appHandler);

                    cefApp = CefApp.getInstance(settings);
                } else {
                    cefApp = CefApp.getInstance();
                }
                cefClient = cefApp.createClient();
            } catch (Throwable t) {
                Log.error("Going in virtual mode; couldn't initialize CEF.");
                t.printStackTrace();

                VIRTUAL = true;
                return;
            }

            Log.info(cefApp.getVersion().toString());
            cefRouter = CefMessageRouter.create(new CefMessageRouterConfig("mcefQuery", "mcefCancel"));

            cefClient.addMessageRouter(cefRouter);
            cefClient.addDisplayHandler(displayHandler);

//            if (MCEF.SHUTDOWN_JCEF)
            (new ShutdownThread()).start();
            MinecraftForge.EVENT_BUS.addListener(ClientProxy.this::onTick);
            MinecraftForge.EVENT_BUS.addListener(ClientProxy.this::onLogin);

            if (MCEF.ENABLE_EXAMPLE)
                exampleMod.onInit();

            Log.info("MCEF loaded successfuly.");
        };

        Minecraft.getInstance().execute(runnable);

    }

    public CefApp getCefApp() {
        return cefApp;
    }

    @Override
    public IBrowser createBrowser(String url, boolean transp) {
        if (VIRTUAL)
            return new VirtualBrowser();

        CefBrowserOsr ret = (CefBrowserOsr) cefClient.createBrowser(url, true, transp);
        ret.setCloseAllowed();

        ret.getClient().removeLifeSpanHandler();
        ret.getClient().addLifeSpanHandler(cefLifeSpanHandlerAdapter);

        ret.createImmediately();

        browsers.add(ret);
        return ret;
    }

    @Override
    public void registerDisplayHandler(IDisplayHandler idh) {
        displayHandler.addHandler(idh);
    }

    @Override
    public boolean isVirtual() {
        return VIRTUAL;
    }

    @Override
    public void openExampleBrowser(String url) {
        if (MCEF.ENABLE_EXAMPLE)
            exampleMod.showScreen(url);
    }

    @Override
    public void registerJSQueryHandler(IJSQueryHandler iqh) {
        if (!VIRTUAL)
            cefRouter.addHandler(new MessageRouter(iqh), false);
    }

    @Override
    public void registerScheme(String name, Class<? extends IScheme> schemeClass, boolean std, boolean local, boolean displayIsolated, boolean secure, boolean corsEnabled, boolean cspBypassing, boolean fetchEnabled) {
        appHandler.registerScheme(name, schemeClass, std, local, displayIsolated, secure, corsEnabled, cspBypassing, fetchEnabled);
    }

    @Override
    public boolean isSchemeRegistered(String name) {
        return appHandler.isSchemeRegistered(name);
    }

    private final Random random = new Random();

    public void onTick(TickEvent.RenderTickEvent ev) {
        if (ev.phase == TickEvent.Phase.START) {

            //Check if our key was pressed
            if (key.isDown()) {
                //Display the UI.
                mc.setScreen(new FPSGui());
            }

            mc.getProfiler().startTick();

            //see the var 'key'  comment
            if (cefApp != null && browsers.size() > 0 &&
                    random.nextInt(100) >= MathHelper.clamp(100 - MCEF.FPS_TAKE_ON, 0, 100)) {
                cefApp.N_DoMessageLoopWork();
            }

            for (CefBrowserOsr b : browsers)
                b.mcefUpdate();

            displayHandler.update();
            mc.getProfiler().endTick();
        }
    }

    public void onLogin(PlayerEvent.PlayerLoggedInEvent ev) {
        if (updateStr == null || !MCEF.WARN_UPDATES)
            return;

        Style cs = Style.EMPTY;
        cs.withColor(TextFormatting.LIGHT_PURPLE);

        StringTextComponent cct = new StringTextComponent(updateStr);
        cct.setStyle(cs);

        ev.getPlayer().sendMessage(cct, ev.getPlayer().getUUID());
    }

    public void removeBrowser(CefBrowserOsr b) {
        browsers.remove(b);
    }

    @Override
    public IBrowser createBrowser(String url) {
        if (VIRTUAL)
            return new VirtualBrowser();
        return createBrowser(url, false);
    }

    private void runMessageLoopFor(long ms) {
        final long start = System.currentTimeMillis();
        Runnable runnable = () -> {
            do {
                cefApp.N_DoMessageLoopWork();
            } while (System.currentTimeMillis() - start < ms);
        };

        //run the loop later in minecraft thread
        Minecraft.getInstance().submitAsync(runnable);

    }

    @Override
    public void onShutdown() {
        if (VIRTUAL)
            return;

        Log.info("Shutting down JCEF...");
        CefBrowserOsr.CLEANUP = false; //Workaround

        for (CefBrowserOsr b : browsers)
            b.close();

        browsers.clear();

        if (MCEF.CHECK_VRAM_LEAK)
            CefRenderer.dumpVRAMLeak();

        runMessageLoopFor(100);
        CefApp.forceShutdownState();
        cefClient.dispose();

        if (MCEF.SHUTDOWN_JCEF)
            cefApp.N_Shutdown();
    }

    public void loadMimeTypeMapping() {
        Pattern p = Pattern.compile("^(\\S+)\\s+(\\S+)\\s*(\\S*)\\s*(\\S*)$");
        String line = "";
        int cLine = 0;
        mimeTypeMap.clear();

        try {
            InputStream reader = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("mcef:mime.types")).getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(reader));

            while (true) {
                cLine++;
                line = br.readLine();
                if (line == null)
                    break;

                line = line.trim();
                if (!line.startsWith("#")) {
                    Matcher m = p.matcher(line);
                    if (!m.matches())
                        continue;

                    mimeTypeMap.put(m.group(2), m.group(1));
                    if (m.groupCount() >= 4 && !m.group(3).isEmpty()) {
                        mimeTypeMap.put(m.group(3), m.group(1));

                        if (m.groupCount() >= 5 && !m.group(4).isEmpty())
                            mimeTypeMap.put(m.group(4), m.group(1));
                    }
                }
            }

            Util.close(br);
        } catch (Throwable e) {
            Log.error("[Mime Types] Error while parsing \"%s\" at line %d:", line, cLine);
            e.printStackTrace();
        }

        Log.info("Loaded %d mime types", mimeTypeMap.size());
    }

    @Override
    public String mimeTypeFromExtension(String ext) {
        ext = ext.toLowerCase();
        String ret = mimeTypeMap.get(ext);
        if (ret != null)
            return ret;

        //If the mimeTypeMap couldn't be loaded, fall back to common things
        switch (ext) {
            case "htm":
            case "assets/mcef/html":
                return "text/html";

            case "css":
                return "text/css";

            case "js":
                return "text/javascript";

            case "png":
                return "image/png";

            case "jpg":
            case "jpeg":
                return "image/jpeg";

            case "gif":
                return "image/gif";

            case "svg":
                return "image/svg+xml";

            case "xml":
                return "text/xml";

            case "txt":
                return "text/plain";

            default:
                return null;
        }
    }
}
