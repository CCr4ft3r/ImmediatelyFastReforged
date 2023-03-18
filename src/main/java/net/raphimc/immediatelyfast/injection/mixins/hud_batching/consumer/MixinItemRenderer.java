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
import net.raphimc.immediatelyfast.feature.batching.BatchingRenderLayers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 1500)
public abstract class MixinItemRenderer {

    @ModifyArg(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"))
    private MultiBufferSource renderItemIntoBuffer(ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model) {
        if (BatchingBuffers.LIT_ITEM_MODEL_CONSUMER != null || BatchingBuffers.UNLIT_ITEM_MODEL_CONSUMER != null) {
            // Get the model view transformations and apply them to the empty matrix stack.
            // When rendering that batch the model view matrix will be set to the identity matrix to not apply the model view transformations twice.
            matrices.last().pose().load(RenderSystem.getModelViewMatrix());

            return model.usesBlockLight() ? BatchingBuffers.LIT_ITEM_MODEL_CONSUMER : BatchingBuffers.UNLIT_ITEM_MODEL_CONSUMER;
        }

        return vertexConsumers;
    }

    @ModifyArg(method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I"))
    private MultiBufferSource renderTextInfoBuffer(MultiBufferSource vertexConsumers) {
        return BatchingBuffers.ITEM_OVERLAY_CONSUMER != null ? BatchingBuffers.ITEM_OVERLAY_CONSUMER : vertexConsumers;
    }

    @Inject(method = "fillRect", at = @At("HEAD"), cancellable = true)
    private void renderGuiQuadIntoBuffer(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha, CallbackInfo ci) {
        if (BatchingBuffers.ITEM_OVERLAY_CONSUMER != null) {
            ci.cancel();
            int color = alpha << 24 | red << 16 | green << 8 | blue;
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            color = FastColor.ARGB32.multiply(color, argb);
            final VertexConsumer vertexConsumer = BatchingBuffers.ITEM_OVERLAY_CONSUMER.getBuffer(BatchingRenderLayers.GUI_QUAD);
            vertexConsumer.vertex(x, y, 0F).color(color).endVertex();
            vertexConsumer.vertex(x, y + height, 0F).color(color).endVertex();
            vertexConsumer.vertex(x + width, y + height, 0F).color(color).endVertex();
            vertexConsumer.vertex(x + width, y, 0F).color(color).endVertex();
        }
    }
}