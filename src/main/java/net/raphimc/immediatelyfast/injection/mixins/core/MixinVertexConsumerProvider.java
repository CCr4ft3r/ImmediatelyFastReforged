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
package net.raphimc.immediatelyfast.injection.mixins.core;

import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.core.BatchableImmediate;
import net.raphimc.immediatelyfast.injection.interfaces.IBufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Map;

@Mixin(MultiBufferSource.class)
public interface MixinVertexConsumerProvider {

    /**
     * @author RK_01
     * @reason Universal Batching
     */
    @Overwrite
    static MultiBufferSource.BufferSource immediateWithBuffers(Map<RenderType, BufferBuilder> layerBuffers, BufferBuilder fallbackBuffer) {
        if (ImmediatelyFast.config.debug_only_and_not_recommended_disable_universal_batching) {
            return new MultiBufferSource.BufferSource(fallbackBuffer, layerBuffers);
        }

        if (!fallbackBuffer.equals(Tesselator.getInstance().getBuilder())) {
            ((IBufferBuilder) fallbackBuffer).release();
        }
        return new BatchableImmediate(layerBuffers);
    }

}