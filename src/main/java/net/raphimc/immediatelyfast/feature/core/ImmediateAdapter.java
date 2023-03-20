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
package net.raphimc.immediatelyfast.feature.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.raphimc.immediatelyfast.compat.IrisCompat;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.raphimc.immediatelyfast.util.ImmediateUtil.*;

public abstract class ImmediateAdapter extends MultiBufferSource.BufferSource implements AutoCloseable {

    /**
     * A fallback buffer has to be defined because Iris tries to release that buffer, so it can't be null. It should be fine
     * to reuse/release the buffer multiple times, as it won't ever be written into by minecraft or Iris.
     */
    private final static BufferBuilder FALLBACK_BUFFER = new BufferBuilder(0);

    protected final Reference2ObjectMap<RenderType, ReferenceSet<BufferBuilder>> fallbackBuffers = new Reference2ObjectLinkedOpenHashMap<>();
    protected final ReferenceSet<RenderType> activeLayers = new ReferenceLinkedOpenHashSet<>();

    private boolean drawFallbackLayersFirst = false;

    public ImmediateAdapter() {
        this(ImmutableMap.of());
    }

    public ImmediateAdapter(final Map<RenderType, BufferBuilder> fixedBuffers) {
        super(FALLBACK_BUFFER, fixedBuffers);
    }

    @Override
    public @NotNull VertexConsumer getBuffer(final @NotNull RenderType layer) {
        final BufferBuilder bufferBuilder = this.getOrCreateBufferBuilder(layer);
        if (bufferBuilder.building() && sharedVerticesComparator(layer.mode().primitiveStride)) {
            throw new IllegalStateException("Tried to write shared vertices into the same buffer");
        }

        if (!this.drawFallbackLayersFirst) {
            final Optional<RenderType> newLayer = layer.asOptional();
            if (!this.lastState.equals(newLayer)) {
                if (this.lastState.isPresent() && !this.fixedBuffers.containsKey(this.lastState.get())) {
                    this.drawFallbackLayersFirst = true;
                }
            }
            this.lastState = newLayer;
        }

        if (!bufferBuilder.building()) {
            if (IrisCompat.IRIS_LOADED && !IrisCompat.isRenderingLevel.getAsBoolean()) {
                IrisCompat.iris$beginWithoutExtending.accept(bufferBuilder, layer.mode(), layer.format());
            } else {
                bufferBuilder.begin(layer.mode(), layer.format());
            }
            this.activeLayers.add(layer);
        }
        return bufferBuilder;
    }

    @Override
    public void endLastBatch() {
        this.lastState = Optional.empty();
        this.drawFallbackLayersFirst = false;

        this.activeLayers.stream().filter(l -> !this.fixedBuffers.containsKey(l)).sorted((l1, l2) -> {
            if (l1.sortOnUpload == l2.sortOnUpload) return 0;
            return l1.sortOnUpload ? 1 : -1;
        }).forEachOrdered(this::endBatch);
    }

    @Override
    public void endBatch() {
        if (this.activeLayers.isEmpty()) {
            this.close();
            return;
        }

        this.endLastBatch();
        for (RenderType layer : this.fixedBuffers.keySet()) {
            this.endBatch(layer);
        }
    }

    @Override
    public void endBatch(final @NotNull RenderType layer) {
        if (this.drawFallbackLayersFirst) {
            this.endLastBatch();
        }

        if (IrisCompat.IRIS_LOADED && !IrisCompat.isRenderingLevel.getAsBoolean()) {
            IrisCompat.renderWithExtendedVertexFormat.accept(false);
        }

        this.activeLayers.remove(layer);
        this._draw(layer);
        this.fallbackBuffers.remove(layer);

        if (IrisCompat.IRIS_LOADED && !IrisCompat.isRenderingLevel.getAsBoolean()) {
            IrisCompat.renderWithExtendedVertexFormat.accept(true);
        }
    }

    @Override
    public void close() {
        this.lastState = Optional.empty();
        this.drawFallbackLayersFirst = false;

        for (RenderType layer : this.activeLayers) {
            for (BufferBuilder bufferBuilder : this.getBufferBuilder(layer)) {
                bufferBuilder.end();
                bufferBuilder.clear();
            }
        }

        this.activeLayers.clear();
        this.fallbackBuffers.clear();
    }

    public boolean hasActiveLayers() {
        return !this.activeLayers.isEmpty();
    }

    protected abstract void _draw(RenderType layer);

    protected BufferBuilder getOrCreateBufferBuilder(final RenderType layer) {
        if (sharedVerticesComparator(layer.mode().primitiveStride)) {
            return this.addNewFallbackBuffer(layer);
        } else if (this.fixedBuffers.containsKey(layer)) {
            return this.fixedBuffers.get(layer);
        } else if (this.fallbackBuffers.containsKey(layer)) {
            return this.fallbackBuffers.get(layer).iterator().next();
        } else {
            return this.addNewFallbackBuffer(layer);
        }
    }

    protected Set<BufferBuilder> getBufferBuilder(final RenderType layer) {
        if (this.fallbackBuffers.containsKey(layer)) {
            return this.fallbackBuffers.get(layer);
        } else if (this.fixedBuffers.containsKey(layer)) {
            return Collections.singleton(this.fixedBuffers.get(layer));
        } else {
            return Collections.emptySet();
        }
    }

    protected BufferBuilder addNewFallbackBuffer(final RenderType layer) {
        final BufferBuilder bufferBuilder = BufferBuilderPool.get();
        this.fallbackBuffers.computeIfAbsent(layer, k -> new ReferenceLinkedOpenHashSet<>()).add(bufferBuilder);
        return bufferBuilder;
    }
}