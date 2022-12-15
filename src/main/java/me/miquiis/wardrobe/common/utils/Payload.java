package me.miquiis.wardrobe.common.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

public class Payload {

    private final CompoundNBT payload;

    public Payload()
    {
        this.payload = new CompoundNBT();
    }

    public Payload putString(String key, String value)
    {
        payload.putString(key, value);
        return this;
    }

    public Payload putInt(String key, int value)
    {
        payload.putInt(key, value);
        return this;
    }

    public Payload put(String key, INBT value)
    {
        payload.put(key, value);
        return this;
    }

    public CompoundNBT getPayload()
    {
        return payload;
    }
}
