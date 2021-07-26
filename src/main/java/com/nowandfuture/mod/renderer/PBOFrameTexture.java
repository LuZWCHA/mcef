package com.nowandfuture.mod.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.NativeImage;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.EXTBGRA.GL_BGRA_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;

public class PBOFrameTexture extends FrameTexture {
    private PixelBufferObject pbo;
    private static int BYTE_PER_PIXEL = 4; // GBRA

    public PBOFrameTexture(NativeImage nativeImage) {
        super(nativeImage);
    }

    public PBOFrameTexture(int width, int height) {
        super(width, height);
        pbo = new PixelBufferObject();
    }

    @Override
    public void updateBuffer(ByteBuffer buffer, long id) {
        pbo.setTag(id);
        bindTexture();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, getWidth(), getHeight(), 0, GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);
        RenderSystem.bindTexture(0);
    }

    public void updateBuffer(long pixels, long id) {
        pbo.setTag(id);
        bindTexture();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, getWidth(), getHeight(), 0, GL_BGRA, GL_UNSIGNED_BYTE, pixels);
//        RenderSystem.bindTexture(0);
    }

    public void updateBufferedImage(BufferedImage image, long id) {

//        BufferedImage bufferedimage = new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(getWidth() * getHeight() * 3).put(buffer.getData());
        byteBuffer.flip();
        updateBuffer(byteBuffer, id);
//        bufferedimage.getGraphics().dispose();
    }

    @Override
    public void subBuffer(ByteBuffer buffer, int offsetX, int offsetY, int w, int h, long frameId) {
        if (frameId == pbo.getTag() || pbo.getPBOId() == -1 || getGlTextureId() == -1) return;
        setRealHeight(h);
        setRealWidth(w);
        pbo.setTag(frameId);
        bindTexture();
        pbo.bindPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
        pbo.pboByteData(GL21.GL_PIXEL_UNPACK_BUFFER,
                buffer.limit(), GL21.GL_STREAM_DRAW);
        ByteBuffer b = pbo.mapPBO(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.GL_WRITE_ONLY, null);
        if (b != null && b.hasRemaining()) {
            b.put(buffer);
            pbo.unmapPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
        }

        //Send texel data to OpenGL
//        glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX, offsetY, w, h, GL_BGR, GL_UNSIGNED_BYTE, 0);GL_BGRA_EXT
        GlStateManager.texSubImage2D(GL_TEXTURE_2D, 0, offsetX, offsetY, w, h, GL_BGRA, GL_UNSIGNED_BYTE, 0);
        pbo.unbindPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
        RenderSystem.bindTexture(0);
    }

    @Override
    public void subBufferedImage(BufferedImage image, int offsetX, int offsetY, long frameId) {
        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL)
                .put(buffer.getData());
        byteBuffer.flip();
        subBuffer(byteBuffer, offsetX, offsetY, image.getWidth(), image.getHeight(), frameId);
    }

    @Override
    public void deleteGlTexture() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                if (pbo != null)
                    pbo.delete();
                super.deleteGlTexture();
            });
        } else {
            if (pbo != null)
                pbo.delete();
            super.deleteGlTexture();
        }
    }

    @Override
    public void close() {
        this.deleteGlTexture();
    }
}
