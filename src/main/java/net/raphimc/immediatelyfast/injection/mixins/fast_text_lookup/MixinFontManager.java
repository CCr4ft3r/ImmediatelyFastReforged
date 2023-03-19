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
package net.raphimc.immediatelyfast.injection.mixins.fast_text_lookup;

import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("CommentedOutCode")
@Mixin(value = FontManager.class, priority = 500)
public abstract class MixinFontManager {

    @Mutable
    @Shadow
    @Final
    private PreparableReloadListener reloadListener;

    @Shadow
    @Final
    Map<ResourceLocation, FontSet> fontSets;

    @Shadow
    private Map<ResourceLocation, ResourceLocation> renames;

    @Shadow
    protected abstract FontSet lambda$createFont$1(ResourceLocation par1);

    @Unique
    private final Map<ResourceLocation, FontSet> overriddenFontStorages = new Object2ObjectOpenHashMap<>();

    @Unique
    private FontSet defaultFontStorage;

    @Unique
    private FontSet unicodeFontStorage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void if$hookReloader(TextureManager manager, CallbackInfo ci) {
        this.reloadListener = new SimplePreparableReloadListener<Map<ResourceLocation, List<GlyphProvider>>>() {
            private final PreparableReloadListener delegate = reloadListener;

            @Override
            protected @NotNull Map<ResourceLocation, List<GlyphProvider>> prepare(@NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
                return ((ISinglePreparationResourceReloader) this.delegate).invokePrepare(manager, profiler);
            }

            @Override
            protected void apply(@NotNull Map<ResourceLocation, List<GlyphProvider>> prepared, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
                ((ISinglePreparationResourceReloader) this.delegate).invokeApply(prepared, manager, profiler);
                if$rebuildOverriddenFontStorages();
            }

            @Override
            public @NotNull String getName() {
                return this.delegate.getName();
            }
        };
    }

    @Inject(method = "setRenames", at = @At("RETURN"))
    private void if$rebuildOverriddenFontStorages(CallbackInfo ci) {
        this.if$rebuildOverriddenFontStorages();
    }

/*    // 1.19.0 injection
    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "InvalidInjectorMethodSignature"})
    @ModifyArg(method = {"createTextRenderer"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;<init>(Ljava/util/function/Function;)V"), require = 0)
    private Function<ResourceLocation, FontSet> if$overrideFontStorage2(Function<ResourceLocation, FontSet> original) {
        return this.if$overrideFontStorage(original);
    }*/

    // 1.19.2 injection
    @ModifyArg(method = {"createFont", "createFontFilterFishy"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;<init>(Ljava/util/function/Function;Z)V"), require = 0)
    private Function<ResourceLocation, FontSet> if$overrideFontStorage(Function<ResourceLocation, FontSet> original) {
        return id -> {
            // Fast path for default font
            if (Minecraft.DEFAULT_FONT.equals(id) && this.defaultFontStorage != null) {
                return this.defaultFontStorage;
            } else if (Minecraft.UNIFORM_FONT.equals(id) && this.unicodeFontStorage != null) {
                return this.unicodeFontStorage;
            }

            // Try to get the font storage from the overridden map otherwise
            final FontSet storage = this.overriddenFontStorages.get(id);
            if (storage != null) {
                return storage;
            }

            // In case some mod is doing cursed stuff call the original function
            return original.apply(id);
        };
    }

    @Unique
    private void if$rebuildOverriddenFontStorages() {
        this.overriddenFontStorages.clear();
        this.overriddenFontStorages.putAll(this.fontSets);
        for (ResourceLocation key : this.renames.keySet()) {
            this.overriddenFontStorages.put(key, lambda$createFont$1(key));
        }

        this.defaultFontStorage = this.overriddenFontStorages.get(Minecraft.DEFAULT_FONT);
        this.unicodeFontStorage = this.overriddenFontStorages.get(Minecraft.UNIFORM_FONT);
    }
}