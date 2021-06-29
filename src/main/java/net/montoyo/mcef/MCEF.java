package net.montoyo.mcef;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.montoyo.mcef.client.ClientProxy;
import net.montoyo.mcef.utilities.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

@Mod("mcef")
public class MCEF {
    
    public static final String VERSION = "1.21";
    public static boolean ENABLE_EXAMPLE;
    public static boolean SKIP_UPDATES;
    public static boolean WARN_UPDATES;
    public static boolean USE_FORGE_SPLASH;
    public static String FORCE_MIRROR;
    public static String HOME_PAGE;
    public static String[] CEF_ARGS;
    public static boolean CHECK_VRAM_LEAK;
    public static SSLSocketFactory SSL_SOCKET_FACTORY;
    public static boolean SHUTDOWN_JCEF;
    public static boolean SECURE_MIRRORS_ONLY;
    public static int FPS_TAKE_ON;
    
    public static MCEF INSTANCE;
    
    public static BaseProxy PROXY;

    // TODO: 2021/6/7 Java-cef never write the handler of audio, so the audio can't be controlled now
    public MCEF(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onReload);

        MinecraftForge.EVENT_BUS.addListener(this::onMinecraftWorldUnload);


        INSTANCE = this;
        PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> BaseProxy::new);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        Log.info("Now commonSetup MCEF v%s...", VERSION);
        PROXY.onPreInit();
    }

    public void loadComplete(FMLLoadCompleteEvent ev) {
        Log.info("Now loadComplete MCEF v%s...", VERSION);
        PROXY.onInit();
    }

    public void loadConfig(){
        ENABLE_EXAMPLE = Config.ENABLE_EXAMPLE.get();
        SKIP_UPDATES = Config.SKIP_UPDATES.get();
        WARN_UPDATES = Config.WARN_UPDATES.get();
        USE_FORGE_SPLASH = Config.USE_FORGE_SPLASH.get();
        CEF_ARGS = Config.CEF_ARGS.get().split("\\s+");
        HOME_PAGE = Config.HOME_PAGE.get();
        CHECK_VRAM_LEAK = Config.CHECK_VRAM_LEAK.get();
        SHUTDOWN_JCEF = Config.SHUTDOWN_JCEF.get();
        SECURE_MIRRORS_ONLY = Config.SECURE_MIRRORS_ONLY.get();
        FPS_TAKE_ON = Config.FPS_TAKE_ON.get();
    }

    //Called by Minecraft.run() if the ShutdownPatcher succeeded
    public void onMinecraftWorldUnload(WorldEvent.Unload ev) {
        if(ev.getWorld() instanceof World) {
            Log.info("Minecraft shutdown hook called!");
            PROXY.stopActivateBrowser();
        }
    }

    public void onLoad(final ModConfig.Loading configEvent) {
        //update the config
        importLetsEncryptCertificate();
        //set the ssl factory if needed
        //Util.SSL_SOCKET_FACTORY = SSL_SOCKET_FACTORY;
        loadConfig();
    }

    public void onReload(final ModConfig.Reloading configEvent) {
        //update the config
        loadConfig();
    }

    //This is needed, otherwise for some reason HTTPS doesn't work
    private static void importLetsEncryptCertificate() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            InputStream inputStream = Minecraft.getInstance()
                    .getResourceManager().getResource(new ResourceLocation("mcef:r3.crt")).getInputStream();
            Certificate cert = cf.generateCertificate(inputStream);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("r3", cert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ks);

            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, tmf.getTrustManagers(), new SecureRandom());

            SSL_SOCKET_FACTORY = sslCtx.getSocketFactory();
            Log.info("Successfully loaded Let's Encrypt certificate");
        } catch(Throwable t) {
            Log.error("Could not import Let's Encrypt certificate!! HTTPS downloads WILL fail...");
            t.printStackTrace();
        }
    }

}
