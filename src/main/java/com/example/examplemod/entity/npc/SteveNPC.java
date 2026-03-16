package com.example.examplemod.entity.npc;

import com.example.examplemod.SlimeSummer;
import com.example.examplemod.entity.INPC;
import com.example.examplemod.entity.ai.goal.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class SteveNPC extends PathfinderMob implements INPC {

    // 要保护的玩家UUID
    private UUID currentProtectedPlayerUUID;
    // 要保护的玩家实体
    private Player currentProtectedPlayer;
    // 检测范围
    private static final double DETECTION_RANGE = 16.0D;
    // 跟随距离
    private static final double FOLLOW_DISTANCE = 10.0D;
    // 保护距离（玩家被攻击时响应范围）
    private static final double PROTECTION_RANGE = 32.0D;
    // 跟随模式
    private static final EntityDataAccessor<Boolean> FOLLOW_MODE = SynchedEntityData.defineId(SteveNPC.class, EntityDataSerializers.BOOLEAN);

    public SteveNPC(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // 初始化跟随模式为false
        this.entityData.set(FOLLOW_MODE, false);
        // 生成时自动寻找附近的玩家作为初始保护目标
        if (!level.isClientSide) {
            List<Player> players = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D));
            if (!players.isEmpty()) {
                this.setCurrentProtectedPlayer(players.get(0));
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        // 注册跟随模式数据访问器
        this.entityData.define(FOLLOW_MODE, false);
    }

    @Override
    protected void registerGoals() {
        GoalSelector goalSelector = this.goalSelector;

        // ========== 核心行为（最高优先级）==========
        goalSelector.addGoal(0, new FloatGoal(this));

        // ========== 保护目标行为（高优先级）==========
        // 当保护目标被攻击时，攻击攻击者
        goalSelector.addGoal(1, new ProtectProtectedPlayerGoal(this, this));
        // 近战攻击目标
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));

        // ========== 跟随保护目标 ==========
        // 当玩家与NPC距离超过跟随距离时，开始跟随玩家
        goalSelector.addGoal(3, new FollowPlayerGoal(this, this, 1.0D, 2.0D, FOLLOW_DISTANCE));

        // ========== 普通行为 ==========
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));   
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        // 自动捡起装备
        goalSelector.addGoal(7, new PickUpEquipmentGoal(this, this));

        // ========== 目标选择器 ==========
        // 主动寻找并攻击附近的敌对生物
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                entity -> entity instanceof Enemy && isNearCurrentProtectedPlayer(entity)));
    }

    @Override
    public boolean isFollowMode() {
        return this.entityData.get(FOLLOW_MODE);
    }

    @Override
    public void setFollowMode(boolean followMode) {
        this.entityData.set(FOLLOW_MODE, followMode);
    }

    /**
     * 检查实体是否在当前保护目标附近
     */
    private boolean isNearCurrentProtectedPlayer(Entity entity) {
        if (this.currentProtectedPlayer == null) return true;
        return this.currentProtectedPlayer.distanceTo(entity) <= PROTECTION_RANGE;
    }

    /**
     * 设置当前保护目标玩家
     * 任何玩家都可以通过交互成为新的保护目标
     * @param player 玩家实体
     */
    public void setCurrentProtectedPlayer(Player player) {
        this.currentProtectedPlayer = player;
        this.currentProtectedPlayerUUID = player.getUUID();
    }

    /**
     * 获取当前保护目标玩家
     * @return 玩家实体
     */
    @Override
    public Player getCurrentProtectedPlayer() {
        return this.currentProtectedPlayer;
    }

    /**
     * 检查是否是友好生物（用于判断是否反击）
     * @param entity 要检查的实体
     * @return 如果是友好生物返回true
     */
    private boolean isFriendlyMob(LivingEntity entity) {
        return !(entity instanceof Enemy);
    }

    /**
     * 当受到伤害时的处理
     * 被友好生物误伤不反击
     */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            // 如果是友好生物攻击，不设置反击目标
            if (isFriendlyMob(attacker)) {
                // 清除当前目标，防止反击
                if (this.getTarget() == attacker) {
                    this.setTarget(null);
                }
                return super.hurt(source, amount);
            }
        }
        return super.hurt(source, amount);
    }

    /**
     * 攻击目标时调用，触发攻击动画
     */
    @Override
    public void swing(net.minecraft.world.InteractionHand hand) {
        super.swing(hand);
    }

    /**
     * 检查物品是否是装备
     */
    public boolean isEquipment(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem || 
               stack.getItem() instanceof SwordItem ||
               stack.getItem() instanceof ShieldItem;
    }

    /**
     * 装备物品
     */
    public void equipItem(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            net.minecraft.world.entity.EquipmentSlot slot = armor.getEquipmentSlot();
            if (this.getItemBySlot(slot).isEmpty()) {
                this.setItemSlot(slot, stack);
            }
        } else if (stack.getItem() instanceof SwordItem || 
                   stack.getItem() instanceof ShieldItem) {
            if (this.getMainHandItem().isEmpty()) {
                this.setItemInHand(InteractionHand.MAIN_HAND, stack);
            }
        }
    }

    // 上一个目标
    private LivingEntity lastTarget = null;

    /**
     * 每tick更新
     */
    @Override
    public void tick() {
        super.tick();

        // 如果保护目标不在线或距离太远，尝试重新寻找
        if (this.currentProtectedPlayer == null && this.currentProtectedPlayerUUID != null && !this.level().isClientSide) {
            Player player = this.level().getPlayerByUUID(this.currentProtectedPlayerUUID);
            if (player != null) {
                this.currentProtectedPlayer = player;
            }
        }

        // 检测敌人
        if (!this.level().isClientSide) {
            LivingEntity currentTarget = this.getTarget();
            if (currentTarget != null && lastTarget == null) {
                // 发现新的敌人
                if (this.currentProtectedPlayer != null) {
                    this.currentProtectedPlayer.sendSystemMessage(Component.literal("发现敌人！"));
                }
            }
            lastTarget = currentTarget;
        }
    }

    /**
     * 创建SteveNPC的属性供应商
     * @return 属性供应商构建器
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D);
    }

    /**
     * 处理玩家聊天输入
     */
    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().getString().toLowerCase();
        
        // 查找附近的SteveNPC
        List<SteveNPC> npcs = player.level().getEntitiesOfClass(SteveNPC.class, player.getBoundingBox().inflate(16.0D));
        for (SteveNPC npc : npcs) {
            if (npc.currentProtectedPlayer != null && npc.currentProtectedPlayer.getUUID().equals(player.getUUID())) {
                if (message.equals("protect me")) {
                    npc.setFollowMode(true);
                    player.sendSystemMessage(Component.literal("NPC已切换为跟随模式"));
                } else if (message.equals("cease protect me")) {
                    npc.setFollowMode(false);
                    player.sendSystemMessage(Component.literal("NPC已切换为自由模式"));
                }
                break;
            }
        }
    }
}
