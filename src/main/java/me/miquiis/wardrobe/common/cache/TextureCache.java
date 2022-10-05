package me.miquiis.wardrobe.common.cache;

public class TextureCache {

    private final byte[] textureBytes;
    private final byte[] textureHash;

    public TextureCache(byte[] textureBytes, byte[] textureHash)
    {
        this.textureBytes = textureBytes;
        this.textureHash = textureHash;
    }

    public byte[] getTextureBytes() {
        return textureBytes;
    }

    public byte[] getTextureHash() {
        return textureHash;
    }
}
