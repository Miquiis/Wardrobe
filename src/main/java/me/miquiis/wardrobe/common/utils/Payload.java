package me.miquiis.wardrobe.common.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.List;

public class Payload {

    private final CompoundNBT payload;

    public Payload()
    {
        this.payload = new CompoundNBT();
    }

    public Payload(CompoundNBT compoundNBT)
    {
        this.payload = compoundNBT;
    }

    public Payload putList(String key, List<INBT> compoundNBTList)
    {
        ListNBT listNBT = new ListNBT();
        listNBT.addAll(compoundNBTList);
        payload.put(key, listNBT);
        return this;
    }

    public Payload putBoolean(String key, boolean value)
    {
        payload.putBoolean(key, value);
        return this;
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
