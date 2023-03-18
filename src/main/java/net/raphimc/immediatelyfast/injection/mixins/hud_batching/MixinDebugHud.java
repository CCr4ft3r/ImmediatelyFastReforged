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
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DebugScreenOverlay.class, priority = 500)
public abstract class MixinDebugHud {

    @Shadow protected abstract void drawSystemInformation(PoseStack p_94080_);

    @Shadow protected abstract void drawGameInformation(PoseStack p_94077_);

    @SuppressWarnings("unused")
    @Redirect(method = "render", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;drawGameInformation(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void if$batching(DebugScreenOverlay instance, PoseStack k) {
        BatchingBuffers.beginHudBatching();
        drawGameInformation(k);
        BatchingBuffers.endHudBatching();
    }

    @Redirect(method = "render", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;drawSystemInformation(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void if$batching2(DebugScreenOverlay instance, PoseStack k) {
        BatchingBuffers.beginHudBatching();
        drawSystemInformation( k);
        BatchingBuffers.endHudBatching();
    }
}