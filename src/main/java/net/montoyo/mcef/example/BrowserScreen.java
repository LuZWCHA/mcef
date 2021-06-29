package net.montoyo.mcef.example;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.MCEFApi;

import java.awt.*;
import java.awt.event.MouseEvent;

public class BrowserScreen extends Screen {
    
    IBrowser browser = null;
    private Button back = null;
    private Button fwd = null;
    private Button go = null;
    private Button min = null;
    private Button vidMode = null;
    private TextFieldWidget url = null;
    private String urlToLoad = null;

    private static final String YT_REGEX1 = "^https?://(?:www\\.)?youtube\\.com/watch\\?v=([a-zA-Z0-9_\\-]+)$";
    private static final String YT_REGEX2 = "^https?://(?:www\\.)?youtu\\.be/([a-zA-Z0-9_\\-]+)$";
    private static final String YT_REGEX3 = "^https?://(?:www\\.)?youtube\\.com/embed/([a-zA-Z0-9_\\-]+)(\\?.+)?$";

    //https://www.bilibili.com/video/BV1LE411A7n3?dad
    private static final String BILI_REGEX1 = "^https?://(?:www\\.)?player\\.bilibili\\.com/player\\.html\\?bvid=([a-zA-Z0-9_\\-]+)(\\?.*)?$";
    private static final String BILI_REGEX2 = "^https?://(?:www\\.)?bilibili\\.com[\\\\/]video[\\\\/]([a-zA-Z0-9_\\-]+)($|(\\?.*)$)";

    private static final String BILI_REGEX3 = "^https?://(?:www\\.)?live\\.bilibili\\.com[\\\\/]([a-zA-Z0-9_\\-]+)($|(\\?.*)$)";

    public BrowserScreen() {
        this(null);
    }

    public BrowserScreen(String url) {
        super(new TranslationTextComponent("mcef_title"));
        urlToLoad = (url == null) ? MCEF.HOME_PAGE : url;
    }

    @Override
    protected void init() {
        super.init();
        ExampleMod.INSTANCE.hudBrowser = null;


        // to remove the backup browser, when it is opened in this screen
        if(ExampleMod.INSTANCE.hasBackup()){
            browser = ExampleMod.INSTANCE.getBrowser();
            ExampleMod.INSTANCE.setBackup(null);
        }

        if(browser == null) {
            //Grab the API and make sure it isn't null.
            API api = MCEFApi.getAPI();
            if(api == null)
                return;

            //Create a browser and resize it to fit the screen
            browser = api.createBrowser((urlToLoad == null) ? MCEF.HOME_PAGE : urlToLoad, false);

            urlToLoad = null;
        }

        //Resize the browser if window size changed
        if(browser != null && minecraft != null)
            browser.resize(minecraft.getWindow().getScreenWidth(), minecraft.getWindow().getScreenHeight() - scaleY(20));

        //Create GUI
        //may remove the code, super has clear them
        buttons.clear();
        children.clear();

        if(url == null) {
            buttons.add(back = (new Button(0, 0, 20, 20, new StringTextComponent("<"), new Button.IPressable() {
                @Override
                public void onPress(Button p_onPress_1_) {
                    if(browser == null) return;
                    browser.goBack();
                }
            })));
            buttons.add(fwd = (new Button(20, 0, 20, 20, new StringTextComponent(">"), new Button.IPressable() {
                @Override
                public void onPress(Button p_onPress_1_) {
                    if(browser == null) return;
                    browser.goForward();
                }
            })));
            buttons.add(go = (new Button(width - 60, 0, 20, 20, new StringTextComponent("Go"), new Button.IPressable() {
                @Override
                public void onPress(Button p_onPress_1_) {
                    if(browser == null) return;
                    String data = url.getValue();
                    String fixedURL = ExampleMod.INSTANCE.getAPI().punycode(data);
                    browser.loadURL(fixedURL);
                }
            })));
            buttons.add(min = (new Button(width - 20, 0, 20, 20, new StringTextComponent("_"), new Button.IPressable() {
                @Override
                public void onPress(Button p_onPress_1_) {

                    ExampleMod.INSTANCE.setBackup(BrowserScreen.this);
                    if (minecraft != null) {
                        onClose();
                    }
                }
            })));
            buttons.add(vidMode = (new Button(width - 40, 0, 20, 20, new StringTextComponent("YT"), new Button.IPressable() {
                @Override
                public void onPress(Button p_onPress_1_) {
                    if(browser == null) return;
                    //do nothing
                    String loc = browser.getURL();
                    String vId = null;
                    boolean redo = false;

                    String type = null;
                    // For Chinese User Bili may be a popular web
                    if(loc.matches(YT_REGEX1)) {
                        vId = loc.replaceFirst(YT_REGEX1, "$1");
                        type = "ytb";
                    }else if(loc.matches(YT_REGEX2)) {
                        vId = loc.replaceFirst(YT_REGEX2, "$1");
                        type = "ytb";
                    }else if(loc.matches(YT_REGEX3)) {
                        redo = true;
                        type = "ytb";
                    }else if(loc.matches(BILI_REGEX1)){
                        redo = true;
                        type = "bili";
                    }else if(loc.matches(BILI_REGEX2)){
                        vId = loc.replaceFirst(BILI_REGEX2, "$1");
                        type = "bili";
                    }else if(loc.matches(BILI_REGEX3)){
                        // for a live full screen it will be also useful...
                        vId = loc.replaceFirst(BILI_REGEX3, "$1");
                        type = "bili_live";
                    }


                    if(minecraft != null && vId != null || redo ) {
                        ExampleMod.INSTANCE.setBackup(BrowserScreen.this);
                        minecraft.setScreen(new ScreenCfg(browser, vId, type));
                    }
                }
            })));
            vidMode.active = false;

            url = new TextFieldWidget(font, 40, 0, width - 100, 20, new StringTextComponent(""));
            url.setMaxLength(65535);
            //url.setText("mod://mcef/home.html");
        } else {
            buttons.add(back);
            buttons.add(fwd);
            buttons.add(go);
            buttons.add(min);
            buttons.add(vidMode);

            //Handle resizing
            vidMode.x = width - 40;
            go.x = width - 60;
            min.x = width - 20;

            String old = url.getValue();
            url = new TextFieldWidget(font, 40, 0, width - 100, 20, new StringTextComponent(""));
            url.setMaxLength(65535);
            url.setValue(old);
        }

        //children's Input methods will be called by parent
        children.addAll(buttons);
        addWidget(url);
    }
    
    public int scaleY(int y) {
        assert minecraft != null;
        double sy =  y / (double)height * minecraft.getWindow().getScreenHeight();
        return (int) sy;
    }
    
    public void loadURL(String url) {
        if(browser == null)
            urlToLoad = url;
        else
            browser.loadURL(url);
    }

    @Override
    public void tick() {
        super.tick();

        if(urlToLoad != null && browser != null) {
            browser.loadURL(urlToLoad);
            urlToLoad = null;
        }

        if(url.isFocused()) {
            url.tick();
        }else{
            url.moveCursorToEnd();
        }

    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float pt) {
        //Render the URL box first because it overflows a bit
        url.render(matrixStack, x, y, pt);

        //Render buttons
        super.render(matrixStack, x, y, pt);

        //Renders the browser if itsn't null
        if(browser != null) {
            RenderSystem.disableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
            browser.draw(0d, height, width, 20.d); //Don't forget to flip Y axis.
            RenderSystem.enableDepthTest();
        }

        //debug codes
//        if(minecraft != null && width > 0 && height > 0) {
//            int sx = (int) (x / (float)width * minecraft.getWindow().getScreenWidth());
//            int sy = (int) ((y - 20) / (float)height * minecraft.getWindow().getScreenHeight());
//
//            drawCenteredString(matrixStack, font, sx + ", " + sy + " (" + minecraft.getWindow().getScreenWidth() + ", " + (minecraft.getWindow().getScreenHeight() - scaleY(20)) + ")", 50, 40, 313);
//        }
    }

    @Override
    public void onClose() {
        //Make sure to close the browser when you don't need it anymore.
        if(!ExampleMod.INSTANCE.hasBackup() && browser != null)
            browser.close();
        super.onClose();
    }

    @Override
    public boolean charTyped(char key, int code) {
        boolean consume = super.charTyped(key, code);
        if (browser != null && !consume) {
            browser.injectKeyTyped(key, code, getMask());
            return true;
        }

        return consume;
    }

    @Override
    public boolean mouseDragged(double ox, double oy, int btn, double nx, double ny) {
        boolean consume = super.mouseDragged(ox, oy, btn, nx, ny);
        if (browser != null && !consume) {
            int sx = (int) (ox / (float) width * minecraft.getWindow().getScreenWidth());
            int sy = (int) ((oy - 20) / (float) height * minecraft.getWindow().getScreenHeight());
            int ex = (int) (ox / (float) width * minecraft.getWindow().getScreenWidth());
            int ey = (int) ((oy - 20) / (float) height * minecraft.getWindow().getScreenHeight());
            browser.injectMouseDrag(sx, sy, remapBtn(btn), ex, ey);
        }

        return consume;
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
        if (browser != null && minecraft != null && activateBtn == -1) {
            int sx = (int) (x / (float) width * minecraft.getWindow().getScreenWidth());
            int sy = (int) ((y - 20) / (float) height * minecraft.getWindow().getScreenHeight());
            browser.injectMouseMove(sx, sy, getMask(), y < 0);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        activateBtn = btn;

        boolean consume = super.mouseClicked(x, y, btn);
        if (!consume && browser != null && minecraft != null) {
            int sx = (int) (x / (float) width * minecraft.getWindow().getScreenWidth());
            int sy = (int) ((y - 20) / (float) height * minecraft.getWindow().getScreenHeight());
            browser.injectMouseButton(sx, sy, getMask(), remapBtn(btn), true, 1);
            return true;
        }

        return consume;
    }


    private int activateBtn = -1;

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        activateBtn = activateBtn == btn ? -1 : activateBtn;
        boolean consume = super.mouseReleased(x, y, btn);
        if (!consume && browser != null && minecraft != null) {
            int sx = (int) (x / (float) width * minecraft.getWindow().getScreenWidth());
            int sy = (int) ((y - 20) / (float) height * minecraft.getWindow().getScreenHeight());
            browser.injectMouseButton(sx, sy, getMask(), remapBtn(btn), false, 1);
            return true;
        }

        return consume;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheel) {
        boolean consume = super.mouseScrolled(x, y, wheel);
        if(!consume && browser != null && minecraft != null) {
            int sx = (int) (x / (float)width * minecraft.getWindow().getScreenWidth());
            int sy = (int) ((y - 20) / (float)height * minecraft.getWindow().getScreenHeight());
            browser.injectMouseWheel(sx, sy, getMask(), 1, ((int) wheel * 100));
            return true;
        }
        return consume;
    }

    @Override
    public boolean keyPressed(int keycode, int p_231046_2_, int p_231046_3_) {
        boolean consume = super.keyPressed(keycode, p_231046_2_, p_231046_3_);
        if(minecraft == null) return true;

        char c = (char) keycode;

        if(!consume && browser != null) {
            browser.injectKeyPressedByKeyCode(keycode, c, getMask());
            return true;
        }

        return consume;
    }

    @Override
    public boolean keyReleased(int key, int p_223281_2_, int p_223281_3_) {
        boolean consume = super.keyReleased(key, p_223281_2_, p_223281_3_);
        char c = (char) key;
        if(browser != null && !consume) {
            browser.injectKeyReleasedByKeyCode(key, c, getMask());
            return true;
        }
        return consume;
    }

    //Called by ExampleMod when the current browser's URL changes.
    public void onUrlChanged(IBrowser b, String nurl) {
        if (b == browser && url != null) {
            url.setValue(nurl);
            vidMode.active = nurl.matches(YT_REGEX1) || nurl.matches(YT_REGEX2) || nurl.matches(YT_REGEX3)
                    || nurl.matches(BILI_REGEX1) || nurl.matches(BILI_REGEX2) || nurl.matches(BILI_REGEX3);
        }
    }

    //remap from GLFW to AWT's button ids
    private int remapBtn(int btn){
        if (btn == 0) {
            btn = MouseEvent.BUTTON1;
        } else if (btn == 1) {
            btn = MouseEvent.BUTTON3;
        } else {
            btn = MouseEvent.BUTTON2;
        }
        return btn;
    }

    private static int getMask() {
        return (hasShiftDown() ? MouseEvent.SHIFT_DOWN_MASK : 0) |
                (hasAltDown() ? MouseEvent.ALT_DOWN_MASK : 0) |
                (hasControlDown() ? MouseEvent.CTRL_DOWN_MASK : 0);
    }


    //never used
    private final Point point = new Point();

    private Point transform2BrowserSize(double x, double y) {
        int sx = (int) (x / (float) width * minecraft.getWindow().getScreenWidth());
        // 20 is the top search box's height
        int sy = (int) ((y - 20) / (float) height * minecraft.getWindow().getScreenHeight());
        point.setLocation(sx, sy);
        return point;
    }
}
