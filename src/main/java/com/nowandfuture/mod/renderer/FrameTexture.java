package com.nowandfuture.mod.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nowandfuture.mod.utilities.Log;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;

public abstract class FrameTexture extends DynamicTexture {
    protected static final int BYTES_PER_PIXEL = 4;
    private int width,height;
    private int aw,ah;//sub image size;
    protected long frameId;

    public FrameTexture(NativeImage nativeImage) {
        super(nativeImage);
        width = nativeImage.getWidth();
        height = nativeImage.getHeight();
        close();
    }

    public FrameTexture(int width, int height) {
        this(width, height, true);
    }

    public FrameTexture(int width, int height, boolean init) {
        //we can't skip the construction...
        super(width, height, init);
        //close native image and its texture in GPU
        close();
        if (this.glTextureId == -1) {
            this.glTextureId = TextureUtil.generateTextureId();
        }
        this.width = width;
        this.height = height;
    }

    public void updateBuffer(ByteBuffer byteBuffer, long frameId){
        this.frameId = frameId;
        bindTexture();
        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_BGR, GL_UNSIGNED_BYTE,byteBuffer);

        int error = GL11.glGetError();
        if(error != GL_NO_ERROR){
            Log.warning("OpenGL Error:" + error);
        }
    }

    public void updateBufferedImage(BufferedImage image,long frameId){
        this.frameId = frameId;
        bindTexture();
        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL)
                .put(buffer.getData());
        byteBuffer.flip();

        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_BGR, GL_UNSIGNED_BYTE,byteBuffer);

        int error = GL11.glGetError();
        if(error != GL_NO_ERROR){
            Log.warning("OpenGL Error:" + error);
        }
    }

    public void subBuffer(ByteBuffer byteBuffer,int offsetX,int offsetY, int w , int h, long frameId){
        if(frameId == this.frameId) return;
        this.frameId = frameId;
        bindTexture();

        //Send texel data to OpenGL
        glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX,offsetY, w, h, GL_BGR, GL_UNSIGNED_BYTE, byteBuffer);

        int error = GL11.glGetError();
        if(error != GL_NO_ERROR){
            Log.warning("OpenGL Error:" + error);
        }
    }

    public void subBufferedImage(BufferedImage image,int offsetX,int offsetY,long frameId){
        if(frameId == this.frameId) return;
        this.frameId = frameId;
        RenderSystem.bindTexture(glTextureId);

        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL)
                .put(buffer.getData());
        byteBuffer.flip();

        //Send texel data to OpenGL
        glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX,offsetY, image.getWidth(), image.getHeight(), GL_BGR, GL_UNSIGNED_BYTE,byteBuffer);

        int error = GL11.glGetError();
        if(error != GL_NO_ERROR){
            Log.warning("OpenGL Error:" + error);
        }
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getFrameId() {
        return frameId;
    }

    public int getRealHeight() {
        return ah;
    }

    public int getRealWidth() {
        return aw;
    }

    public void setRealHeight(int ah) {
        this.ah = ah;
    }

    public void setRealWidth(int aw) {
        this.aw = aw;
    }

    @Override
    public void close() {
        super.close();
    }
}
