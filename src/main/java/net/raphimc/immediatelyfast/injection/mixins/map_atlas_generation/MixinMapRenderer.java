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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapRenderer.class)
public abstract class MixinMapRenderer implements IMapRenderer {

    @Shadow
    @Final
    TextureManager textureManager;

    @Unique
    private final Int2ObjectMap<MapAtlasTexture> mapAtlasTextures = new Int2ObjectOpenHashMap<>();

    @Unique
    private final Int2IntMap mapIdToAtlasMapping = new Int2IntOpenHashMap();

    @Inject(method = "resetData", at = @At("RETURN"))
    private void clearMapAtlasTextures(final CallbackInfo ci) {
        for (MapAtlasTexture texture : this.mapAtlasTextures.values()) {
            texture.close();
        }

        this.mapAtlasTextures.clear();
        this.mapIdToAtlasMapping.clear();
    }

    @Inject(method = "getOrCreateMapInstance", at = @At("HEAD"))
    private void createMapAtlasTexture(int id, MapItemSavedData state, CallbackInfoReturnable<MapRenderer.MapInstance> cir) {
        this.mapIdToAtlasMapping.computeIfAbsent(id, k -> {
            for (MapAtlasTexture atlasTexture : this.mapAtlasTextures.values()) {
                final int location = atlasTexture.getNextMapLocation();
                if (location != -1) {
                    return location;
                }
            }

            final MapAtlasTexture atlasTexture = new MapAtlasTexture(this.mapAtlasTextures.size());
            this.mapAtlasTextures.put(atlasTexture.getId(), atlasTexture);
            return atlasTexture.getNextMapLocation();
        });
    }

    @Override
    public MapAtlasTexture getMapAtlasTexture(int id) {
        return this.mapAtlasTextures.get(id);
    }

    @Override
    public int getAtlasMapping(int mapId) {
        return this.mapIdToAtlasMapping.getOrDefault(mapId, -1);
    }

    @Override
    public TextureManager getTextureManager() {
        return textureManager;
    }
}