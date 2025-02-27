package com.nowandfuture.mod.renderer;

import com.nowandfuture.mod.utilities.Log;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

//target:GL_PIXEL_PACK_BUFFER or GL_PIXEL_UNPACK_BUFFER
public class PixelBufferObject {

    private int pboId;
    private long tag;

    public PixelBufferObject(){
        pboId = GL15.glGenBuffers();
        checkError();
    }

    public void unbindPBO(int target){
        GL15.glBindBuffer(target,0);
        checkError();
    }

    public void bindPBO(int target){
        if(pboId != -1) {
            GL15.glBindBuffer(target, pboId);
            checkError();
        }
    }

    //usage:GL_STREAM_DRAW,GL_STREAM_READ
    public void pboByteData(int target, ByteBuffer byteBuffer,int usage){
        GL15.glBufferData(target,byteBuffer,usage);
        checkError();
    }

    //usage:GL_STREAM_DRAW,GL_STREAM_READ
    public void pboByteData(int target, int size,int usage){
        GL15.glBufferData(target,size,usage);
        checkError();
    }

    //access:read data from the PBO (GL_READ_ONLY_ARB), write data to the PBO (GL_WRITE_ONLY_ARB), or both (GL_READ_WRITE_ARB)
    public ByteBuffer mapPBO(int target,int access,ByteBuffer oldBuffer){
        ByteBuffer byteBuffer =
                GL15.glMapBuffer(target, access, oldBuffer);
        checkError();
        return byteBuffer;
    }

    public ByteBuffer mapPBO(int target,int access,long length,ByteBuffer oldBuffer){
        ByteBuffer byteBuffer =
                GL15.glMapBuffer(target,access,length,oldBuffer);
        checkError();
        return byteBuffer;
    }

    public boolean unmapPBO(int target){
        boolean t = GL15.glUnmapBuffer(target);
        checkError();
        return t;
    }

    public void readPixels(int x,int y,int width,int height,int format,int type,long pixels){
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
        checkError();
    }

    public void delete(){
        if(pboId != -1)
            GL15.glDeleteBuffers(pboId);
        pboId = -1;

        checkError();
    }

    public int getPBOId() {
        return pboId;
    }

    private void checkError(){
        int error = GL11.glGetError();
        if(error != GL11.GL_NO_ERROR){
            Log.warning("OpenGL Error:" + error);
        }
    }

    public long getTag() {
        return tag;
    }

    public void setTag(long tag) {
        this.tag = tag;
    }
}
