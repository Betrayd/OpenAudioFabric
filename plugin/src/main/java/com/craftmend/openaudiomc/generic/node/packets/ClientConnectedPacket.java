package com.craftmend.openaudiomc.generic.node.packets;

import com.craftmend.openaudiomc.generic.proxy.messages.PacketWriter;
import com.craftmend.openaudiomc.generic.proxy.messages.StandardPacket;
import com.openaudiofabric.OpenAudioFabric;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class ClientConnectedPacket extends StandardPacket {

    @Getter private UUID clientUuid;

    public void handle(DataInputStream dataInputStream) throws IOException {
        ClientConnectedPacket mirror = OpenAudioFabric.getGson().fromJson(dataInputStream.readUTF(), ClientConnectedPacket.class);
        this.clientUuid = mirror.getClientUuid();
    }

    public PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF(OpenAudioFabric.getGson().toJson(this));
        return packetWriter;
    }
}
