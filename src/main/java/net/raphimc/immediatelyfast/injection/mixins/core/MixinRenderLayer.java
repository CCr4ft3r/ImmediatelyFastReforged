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

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;
import static com.mojang.blaze3d.vertex.VertexFormat.Mode.*;
import static net.minecraft.client.renderer.RenderStateShard.*;
import static net.minecraft.client.renderer.RenderType.CompositeState.*;

@SuppressWarnings({"unused", "CodeBlock2Expr"})
@Mixin(value = RenderType.class, priority = 500)
public abstract class MixinRenderLayer {
    private static CompositeRenderType create(String p_173216_, RenderType.CompositeState p_173222_) {
        return new CompositeRenderType(p_173216_, POSITION_COLOR_TEX_LIGHTMAP, QUADS, 256, false, false, p_173222_);
    }
    @Final
    @Shadow
    private static final Function<ResourceLocation, RenderType> TEXT = Util.memoize((p_173249_) -> {
        return create("text", builder().setShaderState(RENDERTYPE_TEXT_SHADER).setTextureState(new TextureStateShard(p_173249_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).createCompositeState(false));
    });

    @Final
    @Shadow
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY = Util.memoize((p_181451_) -> {
        return create("text_intensity", builder().setShaderState(RENDERTYPE_TEXT_INTENSITY_SHADER).setTextureState(new TextureStateShard(p_181451_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).createCompositeState(false));
    });
    @Final
    @Shadow
    private static final Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET = Util.memoize((p_181449_) -> {
        return create("text_polygon_offset", builder().setShaderState(RENDERTYPE_TEXT_SHADER).setTextureState(new TextureStateShard(p_181449_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setLayeringState(POLYGON_OFFSET_LAYERING).createCompositeState(false));
    });
    @Final
    @Shadow
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize((p_173246_) -> {
        return create("text_intensity_polygon_offset", builder().setShaderState(RENDERTYPE_TEXT_INTENSITY_SHADER).setTextureState(new TextureStateShard(p_173246_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setLayeringState(POLYGON_OFFSET_LAYERING).createCompositeState(false));
    });
    @Final
    @Shadow
    private static final Function<ResourceLocation, RenderType> TEXT_SEE_THROUGH = Util.memoize((p_173244_) -> {
        return create("text_see_through", builder().setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER).setTextureState(new TextureStateShard(p_173244_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    });
    @Final
    @Shadow
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_SEE_THROUGH = Util.memoize((p_234341_) -> {
        return create("text_intensity_see_through", builder().setShaderState(RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH_SHADER).setTextureState(new TextureStateShard(p_234341_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    });
}