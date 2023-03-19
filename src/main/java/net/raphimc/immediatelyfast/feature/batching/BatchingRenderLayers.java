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
package net.raphimc.immediatelyfast.feature.batching;

import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.Util.*;

/**
 * Class which defines how different elements (RenderLayer's) are rendered.
 */
public class BatchingRenderLayers {

    public static final BiFunction<Integer, BlendFuncDepthFunc, RenderType> COLORED_TEXTURE = memoize((id, blendFuncDepthFunc) -> new ImmediatelyFastRenderLayer("texture", VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, false, () -> {
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        blendFuncDepthFunc.apply();
        RenderSystem.setShaderTexture(0, id);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    }, () -> {
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.disableTexture();
    }));

    public static final Function<BlendFuncDepthFunc, RenderType> FILLED_QUAD = memoize(blendFuncDepthFunc -> new ImmediatelyFastRenderLayer("filled_quad", VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, false, () -> {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        blendFuncDepthFunc.apply();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }, () -> {
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }));

    public static final RenderType GUI_QUAD = new ImmediatelyFastRenderLayer("gui_quad", VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, false, () -> {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    });

    public static <A> Function<A, RenderType> memoizeTemp(final Function<A, RenderType> function) {
        return new Function<>() {
            private final Map<A, RenderType> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).<A, RenderType>build().asMap();

            public RenderType apply(final A arg1) {
                return this.cache.computeIfAbsent(arg1, function);
            }
        };
    }

    public static <A, B> BiFunction<A, B, RenderType> memoizeTemp(final BiFunction<A, B, RenderType> function) {
        return new BiFunction<>() {
            private final Map<Pair<A, B>, RenderType> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).<Pair<A, B>, RenderType>build().asMap();

            public RenderType apply(final A arg1, final B arg2) {
                return this.cache.computeIfAbsent(Pair.of(arg1, arg2), (pair) -> function.apply(pair.getLeft(), pair.getRight()));
            }
        };
    }

    private static class ImmediatelyFastRenderLayer extends RenderType {

        private ImmediatelyFastRenderLayer(final String name, final VertexFormat.Mode drawMode, final VertexFormat vertexFormat, final boolean translucent, final Runnable startAction, final Runnable endAction) {
            super(ImmediatelyFast.MOD_ID + "_" + name, vertexFormat, drawMode, 2048, false, translucent, startAction, endAction);
        }
    }
}