package com.example.examplemod.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface INPC {
    /**
     * 获取当前保护目标玩家
     */
    Player getCurrentProtectedPlayer();
    
    /**
     * 获取跟随模式
     */
    boolean isFollowMode();

    /**
     * 设置跟随模式
     */
    void setFollowMode(boolean followMode);
    
    /**
     * 检查物品是否是装备
     */
    boolean isEquipment(ItemStack stack);
    
    /**
     * 装备物品
     */
    void equipItem(ItemStack stack);
}