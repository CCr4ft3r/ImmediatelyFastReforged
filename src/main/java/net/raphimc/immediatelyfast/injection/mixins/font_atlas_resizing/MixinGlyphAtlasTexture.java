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
package net.raphimc.immediatelyfast.injection.mixins.font_atlas_resizing;

import net.minecraft.client.gui.font.FontTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Modifies the size of the glyph atlas texture to 2048x2048.
 * <p>
 * Vanilla uses a 256x256 texture, which is too small for high resolution fonts.
 * If the texture is too small, there may only be under ten glyphs per texture which causes a lot of texture switching when rendering text.
 */
@Mixin(FontTexture.class)
public abstract class MixinGlyphAtlasTexture {

    @ModifyConstant(method = "*", constant = @Constant(intValue = 256))
    private int modifyGlyphAtlasTextureSize(int original) {
        return 2048;
    }

    @ModifyConstant(method = "*", constant = @Constant(floatValue = 256F))
    private float modifyGlyphAtlasTextureSize(float original) {
        return 2048F;
    }

}