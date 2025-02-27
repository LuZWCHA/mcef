package net.montoyo.mcef.api;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface IBrowser {

    /**
     * Destroys the web view.
     */
    void close();

    boolean isActivate();

    /**
     * Resizes the web view.
     *
     * @param width  The new width in pixels.
     * @param height The new height in pixels.
     */
    void resize(int width, int height);

    /**
     * Renders the web view into Minecraft.
     *
     * @param stack the matrix stack
     * @param x1    The first X coord of the rectangle to render the web view on (left).
     * @param y1    The first Y coord of the rectangle to render the web view on (top).
     * @param x2    The second X coord of the rectangle to render the web view on (right).
     * @param y2    The second Y coord of the rectangle to render the web view on (bottom).
     */
    void draw(MatrixStack stack, double x1, double y1, double x2, double y2);

    /**
     * Gets the OpenGL texture ID of the web view.
     *
     * @return the OpenGL texture ID of the web view.
     */
    int getTextureID();

    /**
     * Gets the Minecraft texture resource location of the web view.
     *
     * @return the Minecraft texture resource location of the web view.
     */
    @Nullable
    default ResourceLocation getTextureLocation() {
        return null;
    }

    /**
     * Simulates a mouse move.
     *
     * @param x    The X coord of the mouse.
     * @param y    The Y coord of the mouse.
     * @param mods The key modifiers (shift, ctrl, alt)
     * @param left true if the mouse is out of (left) the web view.
     */
    void injectMouseMove(int x, int y, int mods, boolean left);

    void injectMouseDrag(int x, int y, int btn, int dragX, int dragY);

    /**
     * Simulates a mouse click.
     *
     * @param x The X coord of the mouse.
     * @param y The Y coord of the mouse.
     * @param mods The key modifiers (shift, ctrl, alt)
     * @param btn The mouse button to press. See {@link java.awt.event.MouseEvent}
     * @param pressed true if the button is pressed, false if it is released.
     * @param ccnt The click count. You probably want this to be 1.
     */
    void injectMouseButton(int x, int y, int mods, int btn, boolean pressed, int ccnt);
    
    /**
     * Simulates a keyboard type.
     * 
     * @param c The typed character.
     * @param mods The key modifiers (shift, ctrl, alt)
     */
    void injectKeyTyped(char c, int key_code, int mods);
    
    /**
     * Simulates a key press.
     * 
     * @param keyCode LWJGL key code of the pressed key.
     * @param c The resulting character
     * @param mods The key modifiers (shift, ctrl, alt)
     */
    void injectKeyPressedByKeyCode(int keyCode, char c, int mods);
    
    /**
     * Simulates a key release.
     * 
     * @param keyCode LWJGL key code of the pressed key.
     * @param c The resulting character
     * @param mods The key modifiers (shift, ctrl, alt)
     */
    void injectKeyReleasedByKeyCode(int keyCode, char c, int mods);
    
    /**
     * Simulates a mouse wheel.
     * 
     * @param x The X coord of the mouse.
     * @param y The Y coord of the mouse.
     * @param mods The key modifiers (shift, ctrl, alt)
     * @param amount The amount to scroll.
     * @param rot The number of "clicks" by which the mouse wheel was rotated.
     */
    void injectMouseWheel(int x, int y, int mods, int amount, int rot);
    
    /**
     * Runs JavaScript code on the web view.
     * 
     * @param script The script to run.
     * @param frame The URL of the frame to run the script on. Let this EMPTY (!= null) for the default frame.
     */
    void runJS(String script, String frame);
    
    /**
     * Changes the current page's address.
     * @param url The URL to load.
     */
    void loadURL(String url);
    
    /**
     * Returns to the previous address.
     */
    void goBack();
    
    /**
     * Undoes {@link #goBack()}
     */
    void goForward();
    
    /**
     * Retrieves the current browser's location.
     * @return The current browser's URL.
     */
    String getURL();
    
    /**
     * Asynchronously retrieves the current page's source code.
     * FIXME: This is asynchronous; so you won't get your source right after calling this method!
     * 
     * @param isv An object that handles strings.
     */
    void visitSource(IStringVisitor isv);

    /**
     * Checks if the page is currently being loaded.
     *
     * @return true if the page is still being loaded, false otherwise.
     */
    boolean isPageLoading();

}
