package me.miquiis.wardrobe.common.cache;

public class TextureCache {

    private final byte[] textureBytes;
    private final byte[] textureHash;
    private final boolean textureIsSlim;
    private final boolean textureIsBaby;

    public TextureCache(byte[] textureBytes, byte[] textureHash, boolean textureIsSlim, boolean textureIsBaby)
    {
        this.textureBytes = textureBytes;
        this.textureHash = textureHash;
        this.textureIsSlim = textureIsSlim;
        this.textureIsBaby = textureIsBaby;
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

    public boolean isTextureIsBaby() {
        return textureIsBaby;
    }
}
