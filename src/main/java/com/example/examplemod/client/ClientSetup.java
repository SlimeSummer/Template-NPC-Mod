//客户端渲染器
package com.example.examplemod.client;

import com.example.examplemod.SlimeSummer;
import com.example.examplemod.entity.EntityRegistry;
import com.example.examplemod.entity.npc.SteveNPC;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SlimeSummer.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册SteveNPC渲染器使用玩家模型（史蒂夫模型）
        EntityRenderers.register(EntityRegistry.STEVE_NPC.get(), (context) -> {
            return new HumanoidMobRenderer<SteveNPC, PlayerModel<SteveNPC>>(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f) {
                @Override
                public ResourceLocation getTextureLocation(SteveNPC entity) {
                    return ResourceLocation.fromNamespaceAndPath("slime_summer", "textures/entity/player/steve.png");
                }
            };
        });
    }
}
