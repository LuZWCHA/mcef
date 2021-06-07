// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

// Modified by montoyo for MCEF

package org.cef.browser;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nowandfuture.mod.renderer.PBOFrameTexture;
import com.nowandfuture.mod.utilities.Log;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static com.mojang.blaze3d.systems.RenderSystem.bindTexture;
import static com.mojang.blaze3d.systems.RenderSystem.enableBlend;
import static org.lwjgl.opengl.GL11.*;

//added by nowandfuture
public class CefPBORenderer extends CefRenderer {

    // This 'id' was used at my mod Movement's video-renderer;
    // I just moved here but it is never really used logically
    private long id = 0;

    private PBOFrameTexture pboFrameTexture;

    protected CefPBORenderer(boolean transparent) {
        super(transparent);
    }

    protected void cleanup() {
        if (pboFrameTexture != null)
            pboFrameTexture.deleteGlTexture();
    }

    public void render(double x1, double y1, double x2, double y2) {
        if (view_width_ == 0 || view_height_ == 0)
            return;

        Tessellator t = Tessellator.getInstance();
        BufferBuilder vb = t.getBuilder();

        RenderSystem.bindTexture(pboFrameTexture.getGlTextureId());
        vb.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.vertex(x1, y1, 0.0).uv(0.0f, 1.0f).endVertex();
        vb.vertex(x2, y1, 0.0).uv(1.f, 1.f).endVertex();
        vb.vertex(x2, y2, 0.0).uv(1.f, 0.0f).endVertex();
        vb.vertex(x1, y2, 0.0).uv(0.0f, 0.0f).endVertex();
        t.end();
        RenderSystem.bindTexture(0);
    }

    protected void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender) {
        if (transparent_) // Enable alpha blending.
            enableBlend();
        final int size = (width * height) << 2;
        if (size > buffer.limit()) {
            Log.warning("Bad data passed to CefRenderer.onPaint() triggered safe guards... (1)");
            return;
        }

        if (pboFrameTexture == null)
            pboFrameTexture = new PBOFrameTexture(width, height);

        // Enable 2D textures.
        RenderSystem.enableTexture();

        int oldAlignement = glGetInteger(GL_UNPACK_ALIGNMENT);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        if (!popup) {
            if (completeReRender || width != view_width_ || height != view_height_) {
                // Update/resize the whole texture.
                view_width_ = width;
                view_height_ = height;

                pboFrameTexture.setHeight(height);
                pboFrameTexture.setWidth(width);

                //Update the buffer to GPU cause crash that I have to update the image size and the submit the buffer by subTexImage next
                //glTexImage2D first without buffer update
                pboFrameTexture.updateBuffer(0, id++);

                glPixelStorei(GL_UNPACK_ROW_LENGTH, view_width_);
                glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
                glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
                pboFrameTexture.subBuffer(buffer, 0,0, width, height, id++);
                glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
                glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
                glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);

            }else {
                glPixelStorei(GL_UNPACK_ROW_LENGTH, view_width_);

                // Update just the dirty rectangles.
                for (Rectangle rect : dirtyRects) {
                    if (rect.x < 0 || rect.y < 0 || rect.x + rect.width > view_width_ || rect.y + rect.height > view_height_)
                        Log.warning("Bad data passed to CefRenderer.onPaint() triggered safe guards... (2)");
                    else {
                        glPixelStorei(GL_UNPACK_SKIP_PIXELS, rect.x);
                        glPixelStorei(GL_UNPACK_SKIP_ROWS, rect.y);
                        pboFrameTexture.subBuffer(buffer, rect.x, rect.y, rect.width, rect.height, id++);
                    }
                }

                glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
                glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
                glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            }

        } else if (popup_rect_.width > 0 && popup_rect_.height > 0) {
            int skip_pixels = 0, x = popup_rect_.x;
            int skip_rows = 0, y = popup_rect_.y;
            int w = width;
            int h = height;

            // Adjust the popup to fit inside the view.
            if (x < 0) {
                skip_pixels = -x;
                x = 0;
            }
            if (y < 0) {
                skip_rows = -y;
                y = 0;
            }
            if (x + w > view_width_)
                w -= x + w - view_width_;
            if (y + h > view_height_)
                h -= y + h - view_height_;

            // Update the popup rectangle.
            glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, skip_pixels);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, skip_rows);
            pboFrameTexture.subBuffer(buffer, x, y, w, h, id++);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, oldAlignement);
    }

    @Override
    public int getTextureId() {
        if (pboFrameTexture == null)
            return 0;
        return pboFrameTexture.getGlTextureId();
    }
}
