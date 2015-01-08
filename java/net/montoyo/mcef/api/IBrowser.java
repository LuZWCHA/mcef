package net.montoyo.mcef.api;

public interface IBrowser {
	
	/**
	 * Destroys the web view.
	 */
	public void close();
	
	/**
	 * Resizes the web view.
	 * 
	 * @param width The new width in pixels.
	 * @param height The new height in pixels.
	 */
	public void resize(int width, int height);
	
	/**
	 * Renders the web view into Minecraft.
	 * 
	 * @param x1 The first X coord of the rectangle to render the web view on (left).
	 * @param y1 The first Y coord of the rectangle to render the web view on (top).
	 * @param x2 The second X coord of the rectangle to render the web view on (right).
	 * @param y2 The second Y coord of the rectangle to render the web view on (bottom).
	 */
	public void draw(double x1, double y1, double x2, double y2);
	
	/**
	 * Gets the OpenGL texture ID of the web view.
	 * @return the OpenGL texture ID of the web view.
	 */
	public int getTextureID();
	
	/**
	 * Simulates a mouse move.
	 * 
	 * @param x The X coord of the mouse.
	 * @param y The Y coord of the mouse.
	 * @param mods The key modifiers (shift, ctrl, alt)
	 * @param left true if the mouse is out of (left) the web view.
	 */
	public void injectMouseMove(int x, int y, int mods, boolean left);
	
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
	public void injectMouseButton(int x, int y, int mods, int btn, boolean pressed, int ccnt);
	
	/**
	 * Simulates a keyboard type.
	 * 
	 * @param c The typed character.
	 * @param mods The key modifiers (shift, ctrl, alt)
	 */
	public void injectKeyTyped(char c, int mods);
	
	/**
	 * Simulates a key press.
	 * 
	 * @param c The pressed key's character.
	 * @param mods The key modifiers (shift, ctrl, alt)
	 */
	public void injectKeyPressed(char c, int mods);
	
	/**
	 * Simulates a key release.
	 * 
	 * @param c The released key's character.
	 * @param mods The key modifiers (shift, ctrl, alt)
	 */
	public void injectKeyReleased(char c, int mods);
	
	/**
	 * Simulates a mouse wheel.
	 * 
	 * @param x The X coord of the mouse.
	 * @param y The Y coord of the mouse.
	 * @param mods The key modifiers (shift, ctrl, alt)
	 * @param amount The amount to scroll.
	 * @param rot The number of "clicks" by which the mouse wheel was rotated.
	 */
	public void injectMouseWheel(int x, int y, int mods, int amount, int rot);
	
	/**
	 * Runs JavaScript code on the web view.
	 * 
	 * @param script The script to run.
	 * @param frame The URL of the frame to run the script on. Let this empty for the default frame.
	 */
	public void runJS(String script, String frame);
	
	/**
	 * Changes the current page's address.
	 * @param url The URL to load.
	 */
	public void loadURL(String url);
	
	/**
	 * Returns to the previous address.
	 */
	public void goBack();
	
	/**
	 * Undoes {@link #goBack()}
	 */
	public void goForward();
	
	/**
	 * Retrieves the current browser's location.
	 * @return The current browser's URL.
	 */
	public String getURL();

}