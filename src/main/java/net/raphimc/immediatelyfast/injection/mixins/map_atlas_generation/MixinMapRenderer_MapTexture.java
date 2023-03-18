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

import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MapRenderer.MapInstance.class, priority = 1100)
// TODO: Workaround for Porting-Lib which relies on the LVT to be intact
public abstract class MixinMapRenderer_MapTexture {
    @Shadow
    private MapItemSavedData data;

    @Mutable
    @Shadow
    @Final
    private DynamicTexture texture;

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

    @Inject(method = "<init>", at = @At(value = "RETURN", remap = false))
    private void initAtlasParameters(MapRenderer mapRenderer, int id, MapItemSavedData state, CallbackInfo ci) {
        final int packedLocation = ((IMapRenderer) mapRenderer).getAtlasMapping(id);
        if (packedLocation == -1) {
            ImmediatelyFast.LOGGER.warn("Map " + id + " is not in an atlas");
            // Leave atlasTexture null to indicate that this map is not in an atlas, and it should use the vanilla system instead
            return;
        }

        this.atlasX = ((packedLocation >> 8) & 0xFF) * MapAtlasTexture.MAP_SIZE;
        this.atlasY = (packedLocation & 0xFF) * MapAtlasTexture.MAP_SIZE;
        this.atlasTexture = ((IMapRenderer) mapRenderer).getMapAtlasTexture(packedLocation >> 16);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;<init>(IIZ)V"), remap = false)
    private DynamicTexture dontAllocateTexture(int width, int height, boolean useMipmaps) {
        if (this.atlasTexture != null) {
            return DUMMY_TEXTURE;
        } else {
            return new DynamicTexture(width, height, useMipmaps);
        }
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Ljava/lang/String;Lnet/minecraft/client/renderer/texture/DynamicTexture;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation getAtlasTextureIdentifier(TextureManager textureManager, String id, DynamicTexture texture) {
        if (this.atlasTexture != null) {
            this.texture = null; // Don't leave the texture field pointing to the uninitialized dummy texture
            return this.atlasTexture.getIdentifier();
        } else {
            return textureManager.register(id, texture);
        }
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

            for (int x = 0; x < MapAtlasTexture.MAP_SIZE; x++) {
                for (int y = 0; y < MapAtlasTexture.MAP_SIZE; y++) {
                    final int i = x + y * MapAtlasTexture.MAP_SIZE;
                    atlasImage.blendPixel(this.atlasX + x, this.atlasY + y, MaterialColor.getColorFromPackedId(this.data.colors[i]));
                }
            }
            atlasTexture.bind();
            atlasImage.upload(0, this.atlasX, this.atlasY, this.atlasX, this.atlasY, MapAtlasTexture.MAP_SIZE, MapAtlasTexture.MAP_SIZE, false, false);
        }
    }

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;uv(FF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lcom/mojang/math/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 0), to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;endVertex()V", ordinal = 3)))
    private VertexConsumer drawAtlasTexture(VertexConsumer instance, float u, float v) {
        if (this.atlasTexture != null) {
            if (u == 0 && v == 1) {
                u = (float) this.atlasX / MapAtlasTexture.ATLAS_SIZE;
                v = (float) (this.atlasY + MapAtlasTexture.MAP_SIZE) / MapAtlasTexture.ATLAS_SIZE;
            } else if (u == 1 && v == 1) {
                u = (float) (this.atlasX + MapAtlasTexture.MAP_SIZE) / MapAtlasTexture.ATLAS_SIZE;
                v = (float) (this.atlasY + MapAtlasTexture.MAP_SIZE) / MapAtlasTexture.ATLAS_SIZE;
            } else if (u == 1 && v == 0) {
                u = (float) (this.atlasX + MapAtlasTexture.MAP_SIZE) / MapAtlasTexture.ATLAS_SIZE;
                v = (float) this.atlasY / MapAtlasTexture.ATLAS_SIZE;
            } else if (u == 0 && v == 0) {
                u = (float) this.atlasX / MapAtlasTexture.ATLAS_SIZE;
                v = (float) this.atlasY / MapAtlasTexture.ATLAS_SIZE;
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