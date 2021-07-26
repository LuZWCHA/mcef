package net.montoyo.mcef.example;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.IBrowser;

import java.awt.*;
import java.awt.event.MouseEvent;

public class BrowserScreen extends Screen {

    IBrowser browser = null;
    private int lastWidth = -1, lastHeight = -1;

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
    private static final String ALL = ".*";

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
        if (ExampleMod.INSTANCE.hasBackup()) {
            browser = ExampleMod.INSTANCE.getBrowser();
            //check the browser, if it has been closed remove it.
            if (browser != null && !browser.isActivate()) {
                browser = null;
            }
            ExampleMod.INSTANCE.setBackup(null);
        }

        if (browser == null) {
            //Grab the API and make sure it isn't null.
            API api = ExampleMod.INSTANCE.getAPI();
            if (api == null)
                return;

            //Create a browser and resize it to fit the screen
            browser = api.createBrowser((urlToLoad == null) ? MCEF.HOME_PAGE : urlToLoad, false);

            urlToLoad = null;
        }

        //Resize the browser if window size changed
        if (browser != null && minecraft != null) {
            lastWidth = minecraft.getMainWindow().getWidth();
            lastHeight = minecraft.getMainWindow().getHeight() - scaleY(20);
            if (lastWidth > 0 && lastHeight > 0)
                browser.resize(lastWidth, lastHeight);
        }

        //Create GUI
        //may remove the code, super class has cleared them
        buttons.clear();
        children.clear();

        if (url == null) {
            buttons.add(back = (new ExtendedButton(0, 0, 20, 20, new StringTextComponent("<"), new Button.IPressable() {
                @Override
                public void onPress(Button btn) {
                    if (browser == null) return;
                    browser.goBack();
                }
            })));
            buttons.add(fwd = (new ExtendedButton(20, 0, 20, 20, new StringTextComponent(">"), new Button.IPressable() {
                @Override
                public void onPress(Button btn) {
                    if (browser == null) return;
                    browser.goForward();
                }
            })));
            buttons.add(go = (new ExtendedButton(width - 60, 0, 20, 20, new StringTextComponent("Go"), new Button.IPressable() {
                @Override
                public void onPress(Button btn) {
                    if (browser == null) return;
                    String data = url.getText();
                    String fixedURL = ExampleMod.INSTANCE.getAPI().punycode(data);
                    browser.loadURL(fixedURL);
                }
            })));
            buttons.add(min = (new ExtendedButton(width - 20, 0, 20, 20, new StringTextComponent("_"), new Button.IPressable() {
                @Override
                public void onPress(Button btn) {

                    ExampleMod.INSTANCE.setBackup(BrowserScreen.this);
                    if (minecraft != null) {
                        closeScreen();
                    }
                }
            })));
            buttons.add(vidMode = (new ExtendedButton(width - 40, 0, 20, 20, new StringTextComponent("[]"), new Button.IPressable() {
                @Override
                public void onPress(Button btn) {
                    if (browser == null) return;
                    //do nothing
                    String loc = browser.getURL();
                    String vId = null;
                    boolean redo = false;

                    String type = null;
                    // For Chinese User Bili may be a popular web
                    if (loc.matches(YT_REGEX1)) {
                        vId = loc.replaceFirst(YT_REGEX1, "$1");
                        type = "ytb";
                    } else if (loc.matches(YT_REGEX2)) {
                        vId = loc.replaceFirst(YT_REGEX2, "$1");
                        type = "ytb";
                    } else if (loc.matches(YT_REGEX3)) {
                        redo = true;
                        type = "ytb";
                    } else if (loc.matches(BILI_REGEX1)) {
                        redo = true;
                        type = "bili";
                    } else if (loc.matches(BILI_REGEX2)) {
                        vId = loc.replaceFirst(BILI_REGEX2, "$1");
                        type = "bili";
                    } else if (loc.matches(BILI_REGEX3)) {
                        // for a live full screen it will be also useful...
                        vId = loc.replaceFirst(BILI_REGEX3, "$1");
                        type = "bili_live";
                    } else {
                        redo = true;
                        type = "all";
                    }


                    if (minecraft != null && vId != null || redo) {
                        ExampleMod.INSTANCE.setBackup(BrowserScreen.this);
                        minecraft.displayGuiScreen(new ScreenCfg(browser, vId, type));
                    }
                }
            })));
            vidMode.active = true;

            url = new TextFieldWidget(font, 40, 0, width - 100, 20, new StringTextComponent(""));
            url.setMaxStringLength(65535);
            url.setText(urlToLoad);
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

            String old = url.getText();
            url = new TextFieldWidget(font, 40, 0, width - 100, 20, new StringTextComponent(""));
            url.setMaxStringLength(65535);
            url.setText(old);
        }

        //children's Input methods will be called by parent
        children.addAll(buttons);
        addListener(url);
    }

    public int scaleY(int y) {
        assert minecraft != null;
        double sy = y / (double) height * minecraft.getMainWindow().getHeight();
        return (int) sy;
    }

    public void loadURL(String url) {
        if (browser == null)
            urlToLoad = url;
        else
            browser.loadURL(url);
    }

    @Override
    public void tick() {
        super.tick();

        if (urlToLoad != null && browser != null) {
            browser.loadURL(urlToLoad);
            urlToLoad = null;
        }

        if (url != null) {
            if (url.isFocused()) {
                url.tick();
            } else {
                url.setCursorPositionEnd();
            }
        }

        if (minecraft != null && browser != null && browser.isActivate()) {
            int curWidth = minecraft.getMainWindow().getWidth();
            int curHeight = minecraft.getMainWindow().getHeight() - scaleY(20);
            if (curHeight > 0 && curWidth > 0 && (lastWidth != curWidth || lastHeight != curHeight)) {
                browser.resize(curWidth, curHeight);
            }
        }

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        //Render the URL box first because it overflows a bit
        if (url != null) {
            url.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        //Render buttons
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        ResourceLocation location = browser.getTextureLocation();
        //Renders the browser if itsn't null
        if (browser != null && location != null) {
            RenderSystem.enableTexture();
            browser.draw(matrixStack, 0d, height, width, 20.d); //Don't forget to flip Y axis.
        }
    }


    @Override
    public void onClose() {
        super.onClose();
        //Make sure to close the browser when you don't need it anymore.

        if (!ExampleMod.INSTANCE.hasBackup() && browser != null) {
            browser.close();
            browser = null;
        }

        super.onClose();
    }

    @Override
    public boolean charTyped(char key, int mod) {
        boolean consume = super.charTyped(key, mod);
        if (browser != null && !consume) {
            browser.injectKeyTyped(key, key, getMask());
            return true;
        }

        return consume;
    }

    @Override
    public boolean mouseDragged(double ox, double oy, int btn, double nx, double ny) {
        boolean consume = super.mouseDragged(ox, oy, btn, nx, ny);
        if (browser != null && !consume) {
            int sx = (int) (ox / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) ((oy - 20) / (float) height * minecraft.getMainWindow().getHeight());
            int ex = (int) (ox / (float) width * minecraft.getMainWindow().getWidth());
            int ey = (int) ((oy - 20) / (float) height * minecraft.getMainWindow().getHeight());
            browser.injectMouseDrag(sx, sy, remapBtn(btn), ex, ey);
        }

        return consume;
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
        if (browser != null && minecraft != null && activateBtn == -1) {
            int sx = (int) (x / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) ((y - 20) / (float) height * minecraft.getMainWindow().getHeight());
            browser.injectMouseMove(sx, sy, getMask(), y < 0);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        activateBtn = btn;

        boolean consume = super.mouseClicked(x, y, btn);
        if (!consume && browser != null && minecraft != null) {
            int sx = (int) (x / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) ((y - 20) / (float) height * minecraft.getMainWindow().getHeight());
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
            int sx = (int) (x / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) (((y - 20) / (float) height) * minecraft.getMainWindow().getHeight());
            browser.injectMouseButton(sx, sy, getMask(), remapBtn(btn), false, 1);
            return true;
        }

        return consume;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheel) {
        boolean consume = super.mouseScrolled(x, y, wheel);
        if (!consume && browser != null && minecraft != null) {
            int sx = (int) (x / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) (((y - 20) / (float) height) * minecraft.getMainWindow().getHeight());
            browser.injectMouseWheel(sx, sy, getMask(), 1, ((int) wheel * 100));
            return true;
        }
        return consume;
    }

    @Override
    public boolean keyPressed(int keycode, int p_231046_2_, int p_231046_3_) {
        boolean consume = super.keyPressed(keycode, p_231046_2_, p_231046_3_);
        if (minecraft == null) return true;

        char c = (char) keycode;

        if (!consume && browser != null) {
            browser.injectKeyPressedByKeyCode(keycode, c, getMask());
            return true;
        }

        return consume;
    }

    @Override
    public boolean keyReleased(int key, int p_223281_2_, int p_223281_3_) {
        boolean consume = super.keyReleased(key, p_223281_2_, p_223281_3_);
        char c = (char) key;
        if (browser != null && !consume) {
            browser.injectKeyReleasedByKeyCode(key, c, getMask());
            return true;
        }
        return consume;
    }

    //Called by ExampleMod when the current browser's URL changes.
    public void onUrlChanged(IBrowser b, String nurl) {
        if (b == browser && url != null) {
            url.setText(nurl);
        }
    }

    //remap from GLFW to AWT's button ids
    private int remapBtn(int btn) {
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
        int sx = (int) (x / (float) width * minecraft.getMainWindow().getHeight());
        // 20 is the top search box's height
        int sy = (int) ((y - 20) / (float) height * minecraft.getMainWindow().getHeight());
        point.setLocation(sx, sy);
        return point;
    }
}
