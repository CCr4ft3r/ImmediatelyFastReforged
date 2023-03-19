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
package net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.*;

@Mixin(value = MapRenderer.MapInstance.class, priority = 1100)
// TODO: Workaround for Porting-Lib which relies on the LVT to be intact
public abstract class MixinMapRenderer_MapTexture {

    @Shadow
    private MapItemSavedData data;

    @Mutable
    @Shadow
    @Final
    private DynamicTexture texture;

    @Mutable
    @Shadow @Final private RenderType renderType;

    @Unique
    private static final DynamicTexture DUMMY_TEXTURE;

    @Unique
    private int atlasX;

    @Unique
    private int atlasY;

    @Unique
    private MapAtlasTexture atlasTexture;

    static {
        try {
            DUMMY_TEXTURE = (DynamicTexture) ImmediatelyFast.UNSAFE.allocateInstance(DynamicTexture.class);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAtlasParameters(MapRenderer this$0, int p_168783_, MapItemSavedData p_168784_, CallbackInfo ci) {
        final int packedLocation = ((IMapRenderer) this$0).getAtlasMapping(p_168783_);
        if (packedLocation == -1) {
            this.texture = new DynamicTexture(128, 128, true);
            ResourceLocation resourcelocation = ((IMapRenderer) this$0).getTextureManager().register("map/" + p_168783_, this.texture);
            this.renderType = RenderType.text(resourcelocation);
            ImmediatelyFast.LOGGER.warn("Map " + p_168783_ + " is not in an atlas");
            // Leave atlasTexture null to indicate that this map is not in an atlas, and it should use the vanilla system instead
            return;
        }

        this.atlasX = ((packedLocation >> 8) & 0xFF) * MAP_SIZE;
        this.atlasY = (packedLocation & 0xFF) * MAP_SIZE;
        this.atlasTexture = ((IMapRenderer) this$0).getMapAtlasTexture(packedLocation >> 16);
        this.texture = null;
        ResourceLocation resourcelocation = atlasTexture.getIdentifier();
        this.renderType = RenderType.text(resourcelocation);
    }

    // Dirty workaround, because normally mixins cannot inject into constructors at non-return instructions - reassignment is done in initAtlasParameters
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/renderer/texture/DynamicTexture"))
    private DynamicTexture dontAllocateTexture(int width, int height, boolean useMipmaps) {
        return DUMMY_TEXTURE;
    }

    // Dirty workaround, because normally mixins cannot inject into constructors at non-return instructions - reassignment is done in initAtlasParameters
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Ljava/lang/String;Lnet/minecraft/client/renderer/texture/DynamicTexture;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation getAtlasTextureIdentifier(TextureManager textureManager, String id, DynamicTexture texture) {
        return null;
    }

    // Dirty workaround, because normally mixins cannot inject into constructors at non-return instructions - reassignment is done in initAtlasParameters
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;text(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType getAtlasTextureIdentifier(ResourceLocation p_110498_) {
        return null;
    }

    @Inject(method = "updateTexture", at = @At("HEAD"), cancellable = true)
    private void updateAtlasTexture(CallbackInfo ci) {
        if (this.atlasTexture != null) {
            ci.cancel();
            final DynamicTexture atlasTexture = this.atlasTexture.getTexture();
            final NativeImage atlasImage = atlasTexture.getPixels();
            if (atlasImage == null) {
                throw new IllegalStateException("Atlas texture has already been closed");
            }

            for (int x = 0; x < MAP_SIZE; x++) {
                for (int y = 0; y < MAP_SIZE; y++) {
                    final int i = x + y * MAP_SIZE;
                    atlasImage.setPixelRGBA(this.atlasX + x, this.atlasY + y, MaterialColor.getColorFromPackedId(this.data.colors[i]));
                }
            }
            atlasTexture.bind();
            atlasImage.upload(0, this.atlasX, this.atlasY, this.atlasX, this.atlasY, MAP_SIZE, MAP_SIZE, false, false);
        }
    }

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;uv(FF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lcom/mojang/math/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 0), to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;endVertex()V", ordinal = 3)))
    private VertexConsumer drawAtlasTexture(VertexConsumer instance, float u, float v) {
        if (this.atlasTexture != null) {
            if (u == 0 && v == 1) {
                u = (float) this.atlasX / ATLAS_SIZE;
                v = (float) (this.atlasY + MAP_SIZE) / ATLAS_SIZE;
            } else if (u == 1 && v == 1) {
                u = (float) (this.atlasX + MAP_SIZE) / ATLAS_SIZE;
                v = (float) (this.atlasY + MAP_SIZE) / ATLAS_SIZE;
            } else if (u == 1 && v == 0) {
                u = (float) (this.atlasX + MAP_SIZE) / ATLAS_SIZE;
                v = (float) this.atlasY / ATLAS_SIZE;
            } else if (u == 0 && v == 0) {
                u = (float) this.atlasX / ATLAS_SIZE;
                v = (float) this.atlasY / ATLAS_SIZE;
            }
        }

        return instance.uv(u, v);
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    private void dontCloseDummyTexture(CallbackInfo ci) {
        if (this.atlasTexture != null) {
            ci.cancel();
        }
    }
}