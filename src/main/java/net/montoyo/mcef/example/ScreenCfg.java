package net.montoyo.mcef.example;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.StringTextComponent;
import net.montoyo.mcef.api.IBrowser;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ScreenCfg extends Screen {

    private IBrowser browser;
    private String vId;
    private String type;
    private int scr_width = 160;
    private int scr_height = 90;
    private int scr_x = 10;
    private int scr_y = 10;
    private int offsetX = 0;
    private int offsetY = 0;
    private boolean dragging = false;
    private boolean resizing = false;
    private boolean drawSquare = true;
    private int squareSize = 6;

    public ScreenCfg(IBrowser browser, String vId, String type) {
        super(new StringTextComponent("config_screen"));
        this.browser = browser;
        this.vId = vId;
        this.type = type;
    }

    @Override
    protected void init() {
        super.init();
        if (vId != null) {
            String finalUrl = Strings.EMPTY;
            boolean reload = true;
            switch (type){
                case "ytb":
                    finalUrl = "https://www.youtube.com/embed/" + vId + "?autoplay=1";
                    break;
                case "bili":
                    finalUrl = "https://player.bilibili.com/player.html?bvid=" + vId;
                    break;
                case "bili_live":
                    finalUrl = "https://live.bilibili.com/" + vId;
                    reload = false;
                    break;
                default:
                    reload = false;
                    break;
            }

            if (reload)
                browser.loadURL(finalUrl);

        }
        browser.resize((int) scaleX(scr_width), (int) scaleY(scr_height));
    }

    public boolean isBrowserActivate() {
        return browser != null && browser.isActivate();
    }

    @Override
    public boolean keyPressed(int code, int p_231046_2_, int p_231046_3_) {
        if (minecraft == null) return true;
        if (code == GLFW.GLFW_KEY_ESCAPE) {
            drawSquare = false;
            ExampleMod.INSTANCE.hudBrowser = this;
            browser.injectMouseMove(-10, -10, 0, true);
            minecraft.displayGuiScreen(null);
        }
        return super.keyPressed(code, p_231046_2_, p_231046_3_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        assert minecraft != null;

        int dx = (int) (x - this.scr_x);
        int dy = (int) (y - this.scr_y);

        if (btn == 0 && isInMiniScreen(dx, dy))
            browser.injectMouseButton((int) scaleX(dx), (int) scaleY(dy), 0, 1, true, 1);
        else if(btn == 0 && isInResizeRect(dx, dy))//resize start
            resizing = true;
        else if(btn == 1 && isInMiniScreen(dx, dy)) {//dragging start
            dragging = true;
            offsetX = dx;
            offsetY = dy;
        }

        return super.mouseClicked(x, y, btn);
    }


    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        super.mouseReleased(x, y, btn);
        int sx = (int) (x - this.scr_x);
        int sy = (int) (y - this.scr_y);

        if(!resizing && !dragging && btn == 0 && isInMiniScreen(sx, sy)){
            browser.injectMouseButton((int) scaleX(sx), (int) scaleY(sy), 0, 1, false, 1);
        }

        if (resizing && btn == 0) {
            resizing = false;
            browser.resize((int)scaleX(scr_width), (int) scaleY(scr_height));
        }else if(dragging && btn == 1){
            dragging = false;
        }

        return true;
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
        int sx = (int) (x - this.scr_x);
        int sy = (int) (y - this.scr_y);

        if(resizing){

            if (sx >= 32 && sy >= 18) {
                if (sy >= sx) {
                    double dw = ((double) sy) * (16.0 / 9.0);
                    scr_width = (int) dw;
                    scr_height = sy;
                } else {
                    double dh = ((double) sx) * (9.0 / 16.0);
                    scr_width = sx;
                    scr_height = (int) dh;
                }
            }
        }else if(dragging){
            this.scr_x = (int) (x - offsetX);
            this.scr_y = (int) (y - offsetY);
        }

        if(!dragging && !resizing && isInMiniScreen(sx, sy))
            browser.injectMouseMove((int) scaleX(sx), (int) scaleY(sy), 0, false);
    }

    private boolean isInMiniScreen(int sx, int sy) {
        return sx > 0 && sx <= scr_width && sy > 0 && sy <= scr_height;
    }

    private boolean isInResizeRect(int sx, int sy){
        return sx >= scr_width && sy >= scr_height && sx < scr_width + squareSize && sy < scr_height + squareSize;
    }

    @Override
    public void render(MatrixStack stack, int x, int y, float p_230430_4_) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableTexture();
        browser.draw(stack, unscaleX(scr_x), unscaleY(scr_height + scr_y), unscaleX(scr_width + scr_x), unscaleY(scr_y));

        if (drawSquare) {
            boolean in = isInResizeRect(x - scr_x, y - scr_y) || resizing;
            Tessellator t = Tessellator.getInstance();
            BufferBuilder vb = t.getBuffer();
            if (in) {
                RenderSystem.clearColor(0, 1, 0, 1);
                vb.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                vb.pos(unscaleX(scr_x + scr_width), unscaleY(scr_y + scr_height), 0.0).color(0, 255, 0, 255).endVertex();
                vb.pos(unscaleX(scr_x + scr_width + squareSize), unscaleY(scr_y + scr_height), 0.0).color(0, 255, 0, 255).endVertex();
                vb.pos(unscaleX(scr_x + scr_width + squareSize), unscaleY(scr_y + scr_height + squareSize), 0.0).color(0, 255, 0, 255).endVertex();
                vb.pos(unscaleX(scr_x + scr_width), unscaleY(scr_y + scr_height + squareSize), 0.0).color(0, 255, 0, 255).endVertex();
            }else {
                vb.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                vb.pos(unscaleX(scr_x + scr_width), unscaleY(scr_y + scr_height), 0.0).color(255, 255, 255, 255).endVertex();
                vb.pos(unscaleX(scr_x + scr_width + squareSize), unscaleY(scr_y + scr_height), 0.0).color(255, 255, 255, 255).endVertex();
                vb.pos(unscaleX(scr_x + scr_width + squareSize), unscaleY(scr_y + scr_height + squareSize), 0.0).color(255, 255, 255, 255).endVertex();
                vb.pos(unscaleX(scr_x + scr_width), unscaleY(scr_y + scr_height + squareSize), 0.0).color(255, 255, 255, 255).endVertex();
            }
            t.draw();
        }


        RenderSystem.enableDepthTest();

    }

    @Deprecated
    public double unscaleX(int x) {
        assert minecraft != null;
        return x;
    }

    @Deprecated
    public double unscaleY(int y) {
        assert minecraft != null;
        return y;
    }

    public double scaleX(int x) {
        assert minecraft != null;
        return ((double) x) * ((double) minecraft.getMainWindow().getWidth()) / ((double) width);
    }

    public double scaleY(int y) {
        assert minecraft != null;
        return ((double) y) * ((double) minecraft.getMainWindow().getHeight()) / ((double) height);
    }


//    public double unscaleX(int x) {
//        assert minecraft != null;
//        return ((double) x) / ((double) minecraft.getWindow().getWidth()) * ((double) width);
//    }
//
//    public double unscaleY(int y) {
//        assert minecraft != null;
//        return ((double) y) / ((double) minecraft.getWindow().getHeight()) * ((double) height);
//    }

}
