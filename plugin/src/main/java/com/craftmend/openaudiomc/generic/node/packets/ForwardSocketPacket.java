package com.craftmend.openaudiomc.generic.node.packets;

import com.craftmend.openaudiomc.generic.networking.abstracts.AbstractPacket;
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
public class ForwardSocketPacket extends StandardPacket {

    private AbstractPacket payload;

    public void handle(DataInputStream dataInputStream) throws IOException {
        ForwardSocketPacket self = OpenAudioFabric.getGson().fromJson(dataInputStream.readUTF(), ForwardSocketPacket.class);
        this.payload = self.getPayload();
    }

    public PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF(OpenAudioFabric.getGson().toJson(this));
        return packetWriter;
    }
}
