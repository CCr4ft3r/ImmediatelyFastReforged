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
package net.raphimc.immediatelyfast.injection.mixins.hud_batching.consumer;

import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Font.class, priority = 1500)
public abstract class MixinTextRenderer {


    @ModifyArg(method = "drawInternal(Ljava/lang/String;FFILcom/mojang/math/Matrix4f;ZZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZIIZ)I"))
    private MultiBufferSource renderTextIntoBuffer1(MultiBufferSource vertexConsumers) {
        return BatchingBuffers.TEXT_CONSUMER != null ? BatchingBuffers.TEXT_CONSUMER : vertexConsumers;
    }

    @ModifyArg(method = "drawInternal(Lnet/minecraft/util/FormattedCharSequence;FFILcom/mojang/math/Matrix4f;Z)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I"))
    private MultiBufferSource renderTextIntoBuffer2(MultiBufferSource vertexConsumers) {
        return BatchingBuffers.TEXT_CONSUMER != null ? BatchingBuffers.TEXT_CONSUMER : vertexConsumers;
    }

}