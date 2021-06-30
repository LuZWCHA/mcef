package net.montoyo.mcef;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Config {

    public static final String CATEGORY_MAIN = "main";
    public static final String CATEGORY_EXAMPLE_BROWSER = "example_browser";
    public static final String CATEGORY_DEBUG = "debug";

    public static ForgeConfigSpec.BooleanValue SKIP_UPDATES;
    public static ForgeConfigSpec.BooleanValue ENABLE_EXAMPLE;
    public static ForgeConfigSpec.BooleanValue WARN_UPDATES;
    public static ForgeConfigSpec.BooleanValue USE_FORGE_SPLASH;
    public static ForgeConfigSpec.IntValue FPS_TAKE_ON;
    public static ForgeConfigSpec.ConfigValue<String> HOME_PAGE;
    public static ForgeConfigSpec.ConfigValue<String> CEF_ARGS;
    public static ForgeConfigSpec.BooleanValue CHECK_VRAM_LEAK;
    public static ForgeConfigSpec.BooleanValue SHUTDOWN_JCEF;
    public static ForgeConfigSpec.BooleanValue SECURE_MIRRORS_ONLY;

    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    static {

        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        CLIENT_BUILDER.comment("Main settings").push(CATEGORY_MAIN);
        SKIP_UPDATES = CLIENT_BUILDER.comment("Do not update binaries.").define("skipUpdate", false);
        WARN_UPDATES = CLIENT_BUILDER.comment("Tells in the chat if a new version of MCEF is available.").define("warnUpdate", true);
        USE_FORGE_SPLASH    = CLIENT_BUILDER.comment("Use Forge's splash screen to display resource download progress (may be unstable).").define("useForgeSplash", true);
        CEF_ARGS            = CLIENT_BUILDER.comment("Command line arguments passed to CEF. For advanced users.").define("cefArgs", "--disable-gpu");
        SHUTDOWN_JCEF       = CLIENT_BUILDER.comment("Set this to true if your Java process hangs after closing Minecraft. This is disabled by default because it makes the launcher think Minecraft crashed...").define("shutdownJcef", false);
        SECURE_MIRRORS_ONLY = CLIENT_BUILDER.comment( "Only enable secure (HTTPS) mirror. This should be kept to true unless you know what you're doing.").define("secureMirrorsOnly", true);
        FPS_TAKE_ON         = CLIENT_BUILDER.comment( "Setting the FPS of browser take on.").defineInRange("fpsTakeOn", 100, 0, 100);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("exampleBrowser setting").push(CATEGORY_EXAMPLE_BROWSER);
        ENABLE_EXAMPLE      = CLIENT_BUILDER.comment("Set this to false if you don't want to enable the F10 browser.").define("enable", true);
        HOME_PAGE           = CLIENT_BUILDER.comment("The home page of the F10 browser.").define("home"  ,  "mod://mcef/home.html");
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("debug setting").push(CATEGORY_DEBUG);
        CHECK_VRAM_LEAK     = CLIENT_BUILDER.comment("Track allocated OpenGL textures to make sure there's no leak").define("checkForVRAMLeak", false);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }
}
