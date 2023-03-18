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
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfileResults;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Minecraft.class, priority = 500)
public abstract class MixinMinecraftClient {

    @Shadow
    protected abstract void renderFpsMeter(PoseStack p_91141_, ProfileResults p_91142_);

    @Redirect(method = "runTick", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;renderFpsMeter(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/profiling/ProfileResults;)V"))
    private void if$Batching(Minecraft instance, PoseStack f1, ProfileResults f2) {
        BatchingBuffers.beginHudBatching();
        renderFpsMeter(f1, f2);
        BatchingBuffers.endHudBatching();
    }
}