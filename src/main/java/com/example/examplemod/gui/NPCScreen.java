package com.example.examplemod.gui;

import com.example.examplemod.entity.INPC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.PathfinderMob;

public class NPCScreen extends Screen {
    private final PathfinderMob npc;
    private final INPC npcInterface;
    private Button followModeButton;
    // 客户端本地的跟随模式状态，用于立即更新按钮文本
    private boolean clientFollowMode;

    public NPCScreen(PathfinderMob npc, INPC npcInterface) {
        super(Component.translatable("gui.slime_summer.npc.title"));
        this.npc = npc;
        this.npcInterface = npcInterface;
        // 初始化客户端本地的跟随模式状态
        this.clientFollowMode = npcInterface.isFollowMode();
    }

    @Override
    protected void init() {
        super.init();
        
        // 重新获取服务器端的最新状态
        this.clientFollowMode = npcInterface.isFollowMode();
        
        // 计算按钮位置
        int buttonWidth = 150;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 添加跟随模式切换按钮
        followModeButton = this.addRenderableWidget(Button.builder(
                getFollowModeText(),
                (button) -> toggleFollowMode()
        ).pos(centerX - buttonWidth / 2, centerY - buttonHeight - 10)
                .size(buttonWidth, buttonHeight)
                .build());
        
        // 添加关闭按钮
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                (button) -> this.onClose()
        ).pos(centerX - buttonWidth / 2, centerY + 10)
                .size(buttonWidth, buttonHeight)
                .build());
    }



    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        // 渲染标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 渲染 NPC 信息
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.slime_summer.npc.owner", npcInterface.getCurrentProtectedPlayer() != null ? npcInterface.getCurrentProtectedPlayer().getName().getString() : "None"),
                this.width / 2, 40, 0xAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component getFollowModeText() {
        if (clientFollowMode) {
            return Component.translatable("gui.slime_summer.npc.follow_mode.on");
        } else {
            return Component.translatable("gui.slime_summer.npc.follow_mode.off");
        }
    }

    private void toggleFollowMode() {
        // 切换客户端本地的跟随模式状态
        clientFollowMode = !clientFollowMode;
        
        // 通过网络数据包同步到服务器
        if (Minecraft.getInstance().player != null) {
            com.example.examplemod.network.ModNetwork.sendToServer(
                    new com.example.examplemod.network.ToggleFollowModePacket(npc.getId())
            );
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.slime_summer.npc.follow_mode.toggle"));
            
            // 立即更新按钮文本，提供更好的用户体验
            followModeButton.setMessage(getFollowModeText());
        }
    }
}