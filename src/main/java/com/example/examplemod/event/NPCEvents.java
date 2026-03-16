package com.example.examplemod.event;

import com.example.examplemod.SlimeSummer;
import com.example.examplemod.entity.npc.SteveNPC;
import com.example.examplemod.gui.NPCScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SlimeSummer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NPCEvents {
    /**
     * 处理玩家与 NPC 的交互事件
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof SteveNPC npc && event.getHand() == InteractionHand.MAIN_HAND) {
            // 设置当前玩家作为NPC的新保护目标
            npc.setCurrentProtectedPlayer((Player) event.getEntity());
            
            if (event.getLevel().isClientSide) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new NPCScreen(npc, npc));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}