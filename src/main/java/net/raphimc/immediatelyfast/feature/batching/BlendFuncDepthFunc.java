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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

public record BlendFuncDepthFunc(boolean DEPTH_TEST, int GL_BLEND_SRC_RGB, int GL_BLEND_SRC_ALPHA, int GL_BLEND_DST_RGB,
                                 int GL_BLEND_DST_ALPHA, int GL_DEPTH_FUNC) {

    public static BlendFuncDepthFunc current() {
        return new BlendFuncDepthFunc(
            GlStateManager.DEPTH.mode.enabled,
            GlStateManager.BLEND.srcRgb,
            GlStateManager.BLEND.srcAlpha,
            GlStateManager.BLEND.dstRgb,
            GlStateManager.BLEND.dstAlpha,
            GlStateManager.DEPTH.func
        );
    }

    public void apply() {
        RenderSystem.blendFuncSeparate(GL_BLEND_SRC_RGB, GL_BLEND_DST_RGB, GL_BLEND_SRC_ALPHA, GL_BLEND_DST_ALPHA);
        if (DEPTH_TEST) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL_DEPTH_FUNC);
        } else {
            RenderSystem.disableDepthTest();
        }
    }
}