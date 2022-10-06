package me.miquiis.wardrobe.common.cache;

public class TextureCache {

    private final byte[] textureBytes;
    private final byte[] textureHash;
    private final boolean textureIsSlim;

    public TextureCache(byte[] textureBytes, byte[] textureHash, boolean textureIsSlim)
    {
        this.textureBytes = textureBytes;
        this.textureHash = textureHash;
        this.textureIsSlim = textureIsSlim;
    }

    public byte[] getTextureBytes() {
        return textureBytes;
    }

    public byte[] getTextureHash() {
        return textureHash;
    }

    public boolean isTextureIsSlim() {
        return textureIsSlim;
    }
}
