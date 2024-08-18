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

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ForceMuteMicrophonePacket extends StandardPacket {

    private UUID client;
    private boolean canSpeak;

    public void handle(DataInputStream dataInputStream) throws IOException {
        ForceMuteMicrophonePacket self = OpenAudioFabric.getGson().fromJson(dataInputStream.readUTF(), ForceMuteMicrophonePacket.class);
        this.canSpeak = self.isCanSpeak();
        this.client = self.getClient();
    }

    public PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF(OpenAudioFabric.getGson().toJson(this));
        return packetWriter;
    }
}
