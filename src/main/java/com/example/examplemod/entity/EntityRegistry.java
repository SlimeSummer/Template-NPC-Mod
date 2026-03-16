//实体管理类，用于批量注册实体
package com.example.examplemod.entity;

import com.example.examplemod.SlimeSummer;
import com.example.examplemod.entity.npc.SteveNPC;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 实体注册管理类
 * 集中管理所有实体的注册和属性设置
 */
public class EntityRegistry {

    // 实体注册器
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SlimeSummer.MODID);

    // 实体属性映射表（用于批量注册属性）
    private static final Map<RegistryObject<? extends EntityType<?>>, EntityType.Builder<?>> ENTITY_BUILDERS = new HashMap<>();

    // ========== 实体定义 ==========

    // Steve NPC
    public static final RegistryObject<EntityType<SteveNPC>> STEVE_NPC =
            registerEntity("steve_npc", SteveNPC::new, MobCategory.CREATURE, 0.6F, 1.95F);

    // 这里可以继续添加更多NPC
    // public static final RegistryObject<EntityType<PoliceNPC>> POLICE_NPC = ...
    // public static final RegistryObject<EntityType<SoldierNPC>> SOLDIER_NPC = ...

    /**
     * 注册所有实体到事件总线
     */
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    /**
     * 注册所有实体属性
     * 在 EntityAttributeCreationEvent 事件中调用
     */
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // 注册SteveNPC属性
        event.put(STEVE_NPC.get(), SteveNPC.createAttributes().build());

        // 这里可以继续添加更多NPC的属性
        // event.put(POLICE_NPC.get(), PoliceNPC.createAttributes().build());
    }

    /**
     * 通用实体注册方法
     *
     * @param name         实体名称
     * @param factory      实体工厂
     * @param category     生物类别
     * @param width        实体宽度
     * @param height       实体高度
     * @param <T>          实体类型
     * @return             注册对象
     */
    private static <T extends net.minecraft.world.entity.LivingEntity> RegistryObject<EntityType<T>> registerEntity(
            String name,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            float width,
            float height) {

        return ENTITY_TYPES.register(name, () ->
                EntityType.Builder.of(factory, category)
                        .sized(width, height)
                        .build(name)
        );
    }
}
