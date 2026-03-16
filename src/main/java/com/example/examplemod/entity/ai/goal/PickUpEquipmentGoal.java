package com.example.examplemod.entity.ai.goal;

import com.example.examplemod.entity.INPC;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.EnumSet;
import java.util.List;

public class PickUpEquipmentGoal extends Goal {
    private final PathfinderMob npc;
    private final INPC npcInterface;
    private ItemEntity targetItem;

    public PickUpEquipmentGoal(PathfinderMob npc, INPC npcInterface) {
        this.npc = npc;
        this.npcInterface = npcInterface;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 查找附近的装备物品
        List<ItemEntity> items = this.npc.level().getEntitiesOfClass(
                ItemEntity.class,
                this.npc.getBoundingBox().inflate(10.0D)
        );

        // 筛选出装备物品
        for (ItemEntity item : items) {
            if (this.npcInterface.isEquipment(item.getItem())) {
                this.targetItem = item;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null && this.targetItem.isAlive() && 
               this.npcInterface.isEquipment(this.targetItem.getItem()) &&
               this.npc.distanceTo(this.targetItem) < 10.0D;
    }

    @Override
    public void start() {
        // 开始移动到物品位置
        if (this.targetItem != null) {
            this.npc.getNavigation().moveTo(this.targetItem, 1.2D);
        }
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.npc.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetItem != null && this.targetItem.isAlive()) {
            // 移动到物品位置
            this.npc.getNavigation().moveTo(this.targetItem, 1.2D);
            
            // 当靠近物品时捡起并装备
            if (this.npc.distanceTo(this.targetItem) < 1.0D) {
                this.npcInterface.equipItem(this.targetItem.getItem().copy());
                this.targetItem.discard();
                this.targetItem = null;
            }
        }
    }
}