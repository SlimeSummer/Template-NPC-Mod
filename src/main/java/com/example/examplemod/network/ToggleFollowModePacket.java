package com.example.examplemod.network;

import com.example.examplemod.entity.INPC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleFollowModePacket {
    private final int entityId;

    public ToggleFollowModePacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(ToggleFollowModePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.entityId);
    }

    public static ToggleFollowModePacket decode(FriendlyByteBuf buf) {
        return new ToggleFollowModePacket(buf.readInt());
    }

    public static void handle(ToggleFollowModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 在服务器端处理
            if (context.getSender() != null) {
                System.out.println("ToggleFollowModePacket received, entityId: " + packet.entityId);
                Entity entity = context.getSender().level().getEntity(packet.entityId);
                if (entity instanceof PathfinderMob && entity instanceof INPC npc) {
                    System.out.println("Before toggle: followMode = " + npc.isFollowMode());
                    npc.setFollowMode(!npc.isFollowMode());
                    System.out.println("After toggle: followMode = " + npc.isFollowMode());
                } else {
                    System.out.println("Entity not found or not an NPC: " + entity);
                }
            }
        });
        context.setPacketHandled(true);
    }
}