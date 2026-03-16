package com.example.examplemod.entity.ai.goal;

import com.example.examplemod.entity.INPC;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FollowPlayerGoal extends Goal {
    private final PathfinderMob npc;
    private final INPC npcInterface;
    private final double speedModifier;
    private final double stopDistance;
    private final double startDistance;

    public FollowPlayerGoal(PathfinderMob npc, INPC npcInterface, double speed, double stopDist, double startDist) {
        this.npc = npc;
        this.npcInterface = npcInterface;
        this.speedModifier = speed;
        this.stopDistance = stopDist;
        this.startDistance = startDist;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.npcInterface.getCurrentProtectedPlayer() == null) {
            System.out.println("FollowPlayerGoal canUse: currentProtectedPlayer is null");
            return false;
        }
        if (!this.npcInterface.isFollowMode()) {
            System.out.println("FollowPlayerGoal canUse: followMode is false");
            return false;
        }

        double distance = this.npc.distanceTo(this.npcInterface.getCurrentProtectedPlayer());
        System.out.println("FollowPlayerGoal canUse: distance = " + distance + ", startDistance = " + this.startDistance);
        // 当距离超过开始距离时跟随
        return distance > this.startDistance;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.npcInterface.getCurrentProtectedPlayer() == null) {
            System.out.println("FollowPlayerGoal canContinueToUse: currentProtectedPlayer is null");
            return false;
        }
        if (!this.npcInterface.isFollowMode()) {
            System.out.println("FollowPlayerGoal canContinueToUse: followMode is false");
            return false;
        }

        double distance = this.npc.distanceTo(this.npcInterface.getCurrentProtectedPlayer());
        System.out.println("FollowPlayerGoal canContinueToUse: distance = " + distance + ", stopDistance = " + this.stopDistance);
        // 距离小于停止距离时停止跟随
        return distance > this.stopDistance;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        this.npc.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.npcInterface.getCurrentProtectedPlayer() != null && this.npcInterface.getCurrentProtectedPlayer().isAlive()) {
            this.npc.getLookControl().setLookAt(this.npcInterface.getCurrentProtectedPlayer(), 10.0F, this.npc.getMaxHeadXRot());
            this.npc.getNavigation().moveTo(this.npcInterface.getCurrentProtectedPlayer(), this.speedModifier);
        }
    }
}