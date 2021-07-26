package net.montoyo.mcef.example;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.IBrowser;

import javax.annotation.Nullable;
import java.awt.event.MouseEvent;

//never used
public class BrowserWidget extends Widget implements ITickable {
    IBrowser browser;
    private String urlToLoad = null;
    private Minecraft minecraft;

    public BrowserWidget(int p_i232254_1_, int p_i232254_2_, int p_i232254_3_, int p_i232254_4_, ITextComponent p_i232254_5_) {
        super(p_i232254_1_, p_i232254_2_, p_i232254_3_, p_i232254_4_, p_i232254_5_);
        minecraft = Minecraft.getInstance();
    }

    public IBrowser getBrowser() {
        return browser;
    }

    public void setBrowser(IBrowser browser) {
        this.browser = browser;
    }

    public void setUrlToLoad(@Nullable String urlToLoad) {
        this.urlToLoad = urlToLoad;
    }

    public void init(){
        if(browser == null) {
            //Grab the API and make sure it isn't null.
            API api = ExampleMod.INSTANCE.getAPI();
            if(api == null)
                return;

            //Create a browser and resize it to fit the screen
            browser = api.createBrowser((urlToLoad == null) ? MCEF.HOME_PAGE : urlToLoad, false);

            urlToLoad = null;
        }

        //Resize the browser if window size changed
        if(browser != null && minecraft != null)
            browser.resize(minecraft.getMainWindow().getWidth(), minecraft.getMainWindow().getHeight());

    }

    public void loadURL(String url) {
        if(browser == null)
            urlToLoad = url;
        else
            browser.loadURL(url);
    }

    @Override
    public void tick() {
        //check url change
        if(urlToLoad != null && browser != null) {
            browser.loadURL(urlToLoad);
            urlToLoad = null;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float pt) {
        super.render(matrixStack, x, y, pt);
        if(browser != null) {
            RenderSystem.disableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
            browser.draw(matrixStack, 0, height, width, 0); //Don't forget to flip Y axis.
            RenderSystem.enableDepthTest();
        }
    }

    public void dispose() {

        //Make sure to close the browser when you don't need it anymore.
        if(browser != null)
            browser.close();
        //enableRepeatEvents(false);
    }

    @Override
    public boolean charTyped(char key, int code) {
        if(!isFocused()) return false;
        boolean consume = super.charTyped(key, code);
        if(browser != null && !consume) {
            browser.injectKeyTyped(key, code, 0);
            return true;
        }

        return consume;
    }

    @Override
    public void mouseMoved(double x, double y) {
        if(!isHovered()) return;
        super.mouseMoved(x, y);
        if(browser != null && minecraft != null) {
            int sx = (int) (x / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) (y / (float) height * minecraft.getMainWindow().getHeight());
            browser.injectMouseMove(sx, sy, 0, y < 0);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if(!isHovered()) return false;
        setFocused(true);

        boolean consume = super.mouseClicked(x, y, btn);

        if(!consume && browser != null && minecraft != null) {
            int sx = (int) (x / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) (y / (float) height * minecraft.getMainWindow().getHeight());
            browser.injectMouseButton(sx, sy, 0, remapBtn(btn), true, 1);
            return true;
        }

        return consume;
    }

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        if(!isHovered()) return false;
        boolean consume = super.mouseReleased(x, y, btn);

        if(!consume && browser != null && minecraft != null) {
            int sx = (int) (x / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) (y / (float) height * minecraft.getMainWindow().getHeight());
            browser.injectMouseButton(sx, sy, 0, remapBtn(btn), false, 1);
            return true;
        }

        return consume;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheel) {
        if(!isHovered()) return false;
        boolean consume = super.mouseScrolled(x, y, wheel);
        if(!consume && browser != null && minecraft != null) {
            int sx = (int) (x / (float) width * minecraft.getMainWindow().getWidth());
            int sy = (int) (y / (float) height * minecraft.getMainWindow().getHeight());
            browser.injectMouseWheel(sx, sy, 0, 1, ((int) wheel * 100));
            return true;
        }
        return consume;
    }

    @Override
    public boolean keyPressed(int keycode, int p_231046_2_, int p_231046_3_) {
        if(!isFocused()) return false;
        boolean consume = super.keyPressed(keycode, p_231046_2_, p_231046_3_);
        if(minecraft == null) return true;

        char c = (char) keycode;

        if(!consume && browser != null) {
            browser.injectKeyPressedByKeyCode(keycode, c, 0);
            return true;
        }

        return consume;
    }


    private int remapBtn(int btn){
        if(btn == 0){
            btn = MouseEvent.BUTTON1;
        }else if(btn == 1){
            btn = MouseEvent.BUTTON3;
        }else{
            btn = MouseEvent.BUTTON2;
        }
        return btn;
    }
}
