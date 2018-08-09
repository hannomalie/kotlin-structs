package de.hanno.struct.benchmark;


import java.nio.ByteBuffer;

public class SimpleSlidingWindow {
    private ByteBuffer buffer;
    public int baseByteOffset = 0;
    public SimpleSlidingWindow(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public float getX() {
        return buffer.getFloat(baseByteOffset);
    }
    public void setX(float x) {
        buffer.putFloat(baseByteOffset, x);
    }
    public float getY() {
        return buffer.getFloat(baseByteOffset+4);
    }
    public void setY(float y) {
        buffer.putFloat(baseByteOffset+4, y);
    }
    public float getZ() {
        return buffer.getFloat(baseByteOffset+8);
    }
    public void setZ(float z) {
        buffer.putFloat(baseByteOffset+8, z);
    }
}