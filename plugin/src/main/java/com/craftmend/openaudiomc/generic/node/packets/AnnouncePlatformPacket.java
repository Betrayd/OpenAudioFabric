package com.craftmend.openaudiomc.generic.node.packets;

import com.craftmend.openaudiomc.generic.platform.Platform;
import com.craftmend.openaudiomc.generic.proxy.messages.PacketWriter;
import com.craftmend.openaudiomc.generic.proxy.messages.StandardPacket;
import com.openaudiofabric.OpenAudioFabric;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncePlatformPacket extends StandardPacket {

    private String parentPublicKey;
    private Platform platform;

    public void handle(DataInputStream dataInputStream) throws IOException {
        AnnouncePlatformPacket self = OpenAudioFabric.getGson().fromJson(dataInputStream.readUTF(), AnnouncePlatformPacket.class);
        this.platform = self.getPlatform();
        this.parentPublicKey = self.getParentPublicKey();
    }

    public PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF(OpenAudioFabric.getGson().toJson(this));
        return packetWriter;
    }
}
