// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

// Modified by montoyo for MCEF

package org.cef.browser;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.nowandfuture.mod.renderer.PBOFrameTexture;
import com.nowandfuture.mod.utilities.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import java.awt.*;
import java.nio.ByteBuffer;

import static com.mojang.blaze3d.systems.RenderSystem.enableBlend;
import static org.lwjgl.opengl.GL11.*;

//added by nowandfuture
public class CefPBORenderer extends CefRenderer {

    // This 'id' was used at my mod Movement's video-renderer;
    // I just moved here but it is never really used logically
    private long id = 0;

    private PBOFrameTexture pboFrameTexture;
    private final TextureManager textureManager;
    private ResourceLocation location;

    {
        location = null;
        textureManager = Minecraft.getInstance().getTextureManager();
    }


    protected CefPBORenderer(boolean transparent) {
        super(transparent);
    }

    protected void cleanup() {
        if (location != null) {
            //release the pointer in memory
            //pbo not delete here
            textureManager.deleteTexture(location);
            location = null;
        }

        if (pboFrameTexture != null) {
            //make sure the pbo is cleanup.
            pboFrameTexture.close();
            pboFrameTexture = null;
        }
    }

    public void render(MatrixStack stack, float x1, float y1, float x2, float y2) {
        if (view_width_ == 0 || view_height_ == 0 || location == null)
            return;

        IRenderTypeBuffer.Impl t = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        IVertexBuilder vb = t.getBuffer(RenderType.getText(location));
        Matrix4f matrix4f = stack.getLast().getMatrix();
        RenderSystem.bindTexture(getTextureId());
//        vb.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(matrix4f, x1, y1, 0.0f).color(255, 255, 255, 255).tex(0.0f, 1.0f).lightmap(15728880).endVertex();
        vb.pos(matrix4f, x2, y1, 0.0f).color(255, 255, 255, 255).tex(1.f, 1.f).lightmap(15728880).endVertex();
        vb.pos(matrix4f, x2, y2, 0.0f).color(255, 255, 255, 255).tex(1.f, 0.0f).lightmap(15728880).endVertex();
        vb.pos(matrix4f, x1, y2, 0.0f).color(255, 255, 255, 255).tex(0.0f, 0.0f).lightmap(15728880).endVertex();
//        t.draw();
        t.finish();
        RenderSystem.bindTexture(0);
    }

    protected void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender) {
        if (transparent_) // Enable alpha blending.
            enableBlend();
        int size = (width * height) << 2;
        if (size > buffer.limit()) {
            Log.warning("Bad data passed to CefRenderer.onPaint() triggered safe guards... (1)");
            return;
        }

        if (pboFrameTexture == null) {
            pboFrameTexture = new PBOFrameTexture(width, height);
            location = textureManager.getDynamicTextureLocation("cef_frame", pboFrameTexture);
        }

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
                    size = rect.height * rect.width * 4;
                    if (size > buffer.limit() || rect.x < 0 || rect.y < 0 || rect.x + rect.width > view_width_ || rect.y + rect.height > view_height_)
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

    @Override
    public ResourceLocation getTextureLocation() {
        return location;
    }
}
