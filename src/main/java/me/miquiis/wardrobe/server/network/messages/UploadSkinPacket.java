package me.miquiis.wardrobe.server.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

public class UploadSkinPacket {

    private byte[] skinBytes;

    public UploadSkinPacket(byte[] skinBytes) {
        this.skinBytes = skinBytes;
    }

    public static void encodePacket(UploadSkinPacket packet, PacketBuffer buf) {
        buf.writeByteArray(packet.skinBytes);
    }

    public static UploadSkinPacket decodePacket(PacketBuffer buf) {
        return new UploadSkinPacket(buf.readByteArray());
    }

    public static void handlePacket(final UploadSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
        File skinsFolder = new File(ServerLifecycleHooks.getCurrentServer().getDataDirectory(), "skins");
        if (!skinsFolder.exists()) skinsFolder.mkdir();
        File skinFile = new File(skinsFolder, "skin_output.png");
        try {
            Files.write(skinFile.toPath(), msg.skinBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
