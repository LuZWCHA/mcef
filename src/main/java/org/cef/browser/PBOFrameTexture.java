package org.cef.browser;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTBgra;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;

public class PBOFrameTexture extends FrameTexture {
    private PixelBufferObject pbo;
    private static int BYTE_PER_PIXEL = 4;

    public PBOFrameTexture(BufferedImage p_i1270_1_) {
        super(p_i1270_1_);

    }

    public PBOFrameTexture(int p_i1271_1_, int p_i1271_2_) {
        super(p_i1271_1_, p_i1271_2_);
        pbo = new PixelBufferObject();
    }

    @Override
    public void updateBuffer(ByteBuffer buffer, long id) {
        pbo.setTag(id);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, getWidth(), getHeight(), 0, EXTBgra.GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, getWidth(), getHeight(), 0, GL_BGR, GL_UNSIGNED_BYTE,byteBuffer);
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
    public void subBuffer(ByteBuffer buffer, int offsetX, int offsetY, int w, int h, long id) {
        if (id == pbo.getTag() && pbo.getPBOId() != -1) return;
        setRealHeight(h);
        setRealWidth(w);
        pbo.setTag(id);
        GlStateManager.bindTexture(glTextureId);

        pbo.bindPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
        pbo.pboByteData(GL21.GL_PIXEL_UNPACK_BUFFER,
                buffer.limit(), GL15.GL_STREAM_DRAW);
        ByteBuffer b = pbo.mapPBO(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.GL_WRITE_ONLY, null);
        if (b != null && b.hasRemaining()) {
            b.put(buffer);
            pbo.unmapPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
        }

        //Send texel data to OpenGL
//        glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX, offsetY, w, h, GL_BGR, GL_UNSIGNED_BYTE, 0);
        glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX, offsetY, w, h, EXTBgra.GL_BGRA_EXT, GL_UNSIGNED_BYTE, 0);
        pbo.unbindPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
    }

    @Override
    public void subBufferedImage(BufferedImage image, int offsetX, int offsetY, long id) {
        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL)
                .put(buffer.getData());
        byteBuffer.flip();
        subBuffer(byteBuffer, offsetX, offsetY, image.getWidth(), image.getHeight(), id);
    }

    @Override
    public void deleteGlTexture() {
        pbo.delete();
        super.deleteGlTexture();
    }
}
