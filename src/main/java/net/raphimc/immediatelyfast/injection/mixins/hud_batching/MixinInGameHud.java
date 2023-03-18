/*
 * This file is part of ImmediatelyFast Reforged - https://github.com/CCr4ft3r/ImmediatelyFastReforged
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast.injection.mixins.hud_batching;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Gui.class, priority = 500)
public abstract class MixinInGameHud {

    @Shadow
    protected abstract void renderPlayerHealth(PoseStack p_93084_);

    @Shadow
    public abstract void renderEffects(PoseStack p_93029_);

    @Shadow
    public abstract void renderSelectedItemName(PoseStack p_93070_);

    @Shadow
    public abstract void renderCrosshair(PoseStack p_93081_);

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderPlayerHealth(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching1(final Gui instance, final PoseStack matrices) {
        BatchingBuffers.beginHudBatching();
        renderPlayerHealth(matrices);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching2(final Gui instance, final PoseStack matrices) {
        BatchingBuffers.beginHudBatching();
        renderPlayerHealth(matrices);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching3(final Gui instance, final PoseStack matrices) {
        BatchingBuffers.beginHudBatching();
        renderEffects(matrices);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching4(final Gui instance, final PoseStack matrices) {
        BatchingBuffers.beginHudBatching();
        renderCrosshair(matrices);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching5(BossHealthOverlay instance, PoseStack l) {
        BatchingBuffers.beginHudBatching();
        instance.render(l);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SubtitleOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching6(SubtitleOverlay instance, PoseStack k) {
        BatchingBuffers.beginHudBatching();
        instance.render(k);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching7(SpectatorGui instance, PoseStack k) {
        BatchingBuffers.beginHudBatching();
        instance.renderTooltip(k);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching8(SpectatorGui instance, PoseStack j) {
        BatchingBuffers.beginHudBatching();
        instance.renderHotbar(j);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSelectedItemName(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching9(final Gui instance, final PoseStack matrices) {
        BatchingBuffers.beginHudBatching();
        renderSelectedItemName(matrices);
        BatchingBuffers.endHudBatching();
    }

    @SuppressWarnings("unused")
    @Redirect(method = "render", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V")
    )
    private void if$Batching1(ChatComponent instance, PoseStack matrices, int x) {
        BatchingBuffers.beginHudBatching();
        instance.render(matrices, x);
        BatchingBuffers.endHudBatching();
    }

    @SuppressWarnings("unused")
    @Redirect(method = "render", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lcom/mojang/blaze3d/vertex/PoseStack;I)V"))
    private void if$Batching2(Gui instance, PoseStack matrices, int x) {
        BatchingBuffers.beginHudBatching();
        instance.renderJumpMeter(matrices, x);
        BatchingBuffers.endHudBatching();
    }

    @SuppressWarnings("unused")
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lcom/mojang/blaze3d/vertex/PoseStack;I)V")
    )
    private void if$Batching3(Gui instance, PoseStack matrices, int x) {
        BatchingBuffers.beginHudBatching();
        instance.renderExperienceBar(matrices, x);
        BatchingBuffers.endHudBatching();
    }

    @SuppressWarnings("unused")
    @Redirect(method = "render", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V")
    )
    private void if$Batching(PlayerTabOverlay instance, PoseStack playerinfo, int l, Scoreboard scoreboard, Objective formattedcharsequence) {
        BatchingBuffers.beginHudBatching();
        instance.render(playerinfo, l, scoreboard, formattedcharsequence);
        BatchingBuffers.endHudBatching();
    }

    @SuppressWarnings("unused")
    @Redirect(method = "render", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;displayScoreboardSidebar(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/scores/Objective;)V")
    )
    private void if$Batching(Gui instance, PoseStack component1, Objective score) {
        BatchingBuffers.beginHudBatching();
        instance.displayScoreboardSidebar(component1, score);
        BatchingBuffers.endHudBatching();
    }

    @SuppressWarnings("unused")
    @Redirect(method = "render", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHotbar(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void if$Batching(Gui instance, float tickDelta, PoseStack matrices) {
        if (ImmediatelyFast.config.experimental_item_hud_batching) {
            BatchingBuffers.beginHudBatching();
            BatchingBuffers.beginItemBatching();
            instance.renderHotbar(tickDelta, matrices);
            BatchingBuffers.endHudBatching();
            BatchingBuffers.endItemBatching();
        } else {
            instance.renderHotbar(tickDelta, matrices);
        }
    }
}