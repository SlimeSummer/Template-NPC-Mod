package com.example.examplemod.entity.ai.goal;

import com.example.examplemod.entity.INPC;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;

import java.util.EnumSet;

public class ProtectProtectedPlayerGoal extends Goal {
    private final PathfinderMob npc;
    private final INPC npcInterface;
    private LivingEntity protectedPlayerLastHurtBy;
    private int timestamp;

    public ProtectProtectedPlayerGoal(PathfinderMob npc, INPC npcInterface) {
        this.npc = npc;
        this.npcInterface = npcInterface;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.npcInterface.getCurrentProtectedPlayer() == null) return false;

        // 检查保护目标最近是否被攻击
        this.protectedPlayerLastHurtBy = this.npcInterface.getCurrentProtectedPlayer().getLastHurtByMob();
        if (this.protectedPlayerLastHurtBy == null) return false;

        // 检查时间戳（只响应最近2秒内的攻击）
        int lastHurtTimestamp = this.npcInterface.getCurrentProtectedPlayer().getLastHurtByMobTimestamp();
        int currentTimestamp = this.npcInterface.getCurrentProtectedPlayer().tickCount;
        if (currentTimestamp - lastHurtTimestamp > 40) return false; // 2秒 = 40 ticks

        // 检查是否在保护范围内
        if (this.npc.distanceTo(this.npcInterface.getCurrentProtectedPlayer()) > 32.0D) return false;

        // 检查攻击者是否是敌对生物
        return this.protectedPlayerLastHurtBy instanceof Enemy;
    }

    @Override
    public void start() {
        this.npc.setTarget(this.protectedPlayerLastHurtBy);
        this.timestamp = this.npc.tickCount;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.npc.getTarget() == null) return false;
        if (!this.npc.getTarget().isAlive()) return false;
        return this.npc.distanceTo(this.npcInterface.getCurrentProtectedPlayer()) <= 32.0D;
    }
}