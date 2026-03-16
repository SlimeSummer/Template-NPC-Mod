package com.example.examplemod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.example.examplemod.entity.EntityRegistry;  //导入实体注册类
import org.slf4j.Logger;

// 此处的值应与 META-INF/mods.toml 文件中的条目匹配
@Mod(SlimeSummer.MODID)
public class SlimeSummer
{
    // 在公共位置定义mod id，方便所有地方引用
    public static final String MODID = "slime_summer";
    // 直接引用slf4j日志记录器
    private static final Logger LOGGER = LogUtils.getLogger();
    // 创建延迟注册器来保存方块，所有方块都将在"slime_summer"命名空间下注册
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // 创建延迟注册器来保存物品，所有物品都将在"slime_summer"命名空间下注册
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // 创建延迟注册器来保存创造模式标签页，所有标签页都将在"slime_summer"命名空间下注册
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 创建一个新方块，id为"slime_summer:example_block"，结合命名空间和路径
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // 创建一个新方块物品，id为"slime_summer:example_block"，结合命名空间和路径
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // 创建一个新食物物品，id为"slime_summer:example_item"，营养值1，饱和度2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));

    // 给SteveNPC添加生物蛋
    public static final RegistryObject<Item> STEVE_NPC_SPAWN_EGG = ITEMS.register("steve_npc_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(EntityRegistry.STEVE_NPC, 0x00FF00, 0x0000FF,
                    new Item.Properties()));

    // 创建一个创造模式标签页，id为"slime_summer:example_tab"，放置在战斗标签页之后
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // 将示例物品添加到标签页
                output.accept(STEVE_NPC_SPAWN_EGG.get()); // 将SteveNPC生物蛋添加到创造模式物品栏
            }).build());

    public SlimeSummer(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // 注册mod加载的commonSetup方法
        modEventBus.addListener(this::commonSetup);

        // 将延迟注册器注册到mod事件总线，以便方块被注册
        BLOCKS.register(modEventBus);
        // 将延迟注册器注册到mod事件总线，以便物品被注册
        ITEMS.register(modEventBus);
        // 将延迟注册器注册到mod事件总线，以便标签页被注册
        CREATIVE_MODE_TABS.register(modEventBus);
        // 通过EntityRegistry类注册所有生物
        EntityRegistry.register(modEventBus);

        // 注册我们自己感兴趣的服务器和其他游戏事件
        MinecraftForge.EVENT_BUS.register(this);

        // 将物品注册到创造模式标签页
        modEventBus.addListener(this::addCreative);

        // 通过EntityRegistry注册生物属性
        modEventBus.addListener(EntityRegistry::registerAttributes);

        // 注册我们mod的ForgeConfigSpec，以便Forge为我们创建和加载配置文件
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // 一些通用设置代码
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
        
        // 注册网络数据包
        com.example.examplemod.network.ModNetwork.registerPackets();
    }

    // 将示例方块物品添加到建筑方块标签页
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // 你可以使用SubscribeEvent，让事件总线发现要调用的方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // 服务器启动时执行某些操作
        LOGGER.info("HELLO from server starting");
    }

    // 你可以使用EventBusSubscriber自动注册类中所有带有@SubscribeEvent注解的静态方法
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // 一些客户端设置代码
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
