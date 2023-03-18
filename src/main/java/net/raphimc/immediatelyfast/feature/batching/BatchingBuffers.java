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

import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.core.BatchableImmediate;
import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;

/**
 * Class which holds various allocated buffers used for batching various rendered elements.
 * <p>
 * Also contains references to vertex consumers which are called within mixins to redirect the vertex data into the batching buffer.
 * <p>
 * Once a begin method is called, all vertex data between the begin and end method will be redirected into the batching buffer and drawn in one batch at the end.
 */
public class BatchingBuffers {

    /*
     * The references into which specific vertex data is redirected.
     *
     * Set to null if batching is disabled and the data should be drawn immediately as usual.
     */
    public static MultiBufferSource FILL_CONSUMER = null;
    public static MultiBufferSource TEXTURE_CONSUMER = null;
    public static MultiBufferSource TEXT_CONSUMER = null;
    public static MultiBufferSource LIT_ITEM_MODEL_CONSUMER = null;
    public static MultiBufferSource UNLIT_ITEM_MODEL_CONSUMER = null;
    public static MultiBufferSource ITEM_OVERLAY_CONSUMER = null;

    /*
     * The batching buffers which hold the vertex data of the batch.
     */
    private static final BatchableImmediate FILL_BATCH = new BatchableImmediate();
    private static final BatchableImmediate TEXTURE_BATCH = new BatchableImmediate();
    private static final BatchableImmediate TEXT_BATCH = new BatchableImmediate();
    private static final BatchableImmediate LIT_ITEM_MODEL_BATCH = new ItemModelBatchableImmediate(true);
    private static final BatchableImmediate UNLIT_ITEM_MODEL_BATCH = new ItemModelBatchableImmediate(false);
    private static final BatchableImmediate ITEM_OVERLAY_BATCH = new BatchableImmediate();

    public static void beginHudBatching() {
        beginFillBatching();
        beginTextureBatching();
        beginTextBatching();
    }

    public static void endHudBatching() {
        endFillBatching();
        endTextureBatching();
        endTextBatching();
    }

    public static void beginItemBatching() {
        beginItemModelBatching();
        beginItemOverlayBatching();
    }

    public static void endItemBatching() {
        endItemModelBatching();
        endItemOverlayBatching();
    }


    public static void beginTextureBatching() {
        if (TEXTURE_BATCH.hasActiveLayers()) {
            ImmediatelyFast.LOGGER.warn("Texture batching was already active! endTextureBatching() was not called before beginTextureBatching(). This will cause rendering issues.");
            TEXTURE_BATCH.close();
        }
        TEXTURE_CONSUMER = TEXT_BATCH;
    }

    public static void endTextureBatching() {
        TEXTURE_CONSUMER = null;
        TEXTURE_BATCH.endBatch();
    }

    public static boolean isTextureBatching() {
        return TEXTURE_CONSUMER != null;
    }

    public static void beginFillBatching() {
        if (FILL_BATCH.hasActiveLayers()) {
            ImmediatelyFast.LOGGER.warn("Fill batching was already active! endFillBatching() was not called before beginFillBatching(). This will cause rendering issues.");
            FILL_BATCH.close();
        }
        FILL_CONSUMER = FILL_BATCH;
    }

    public static void endFillBatching() {
        FILL_CONSUMER = null;
        FILL_BATCH.endBatch();
    }

    public static boolean isFillBatching() {
        return FILL_CONSUMER != null;
    }

    public static void beginTextBatching() {
        if (TEXT_BATCH.hasActiveLayers()) {
            ImmediatelyFast.LOGGER.warn("Text batching was already active! endTextBatching() was not called before beginTextBatching(). This will cause rendering issues.");
            TEXT_BATCH.close();
        }
        TEXT_CONSUMER = TEXT_BATCH;
    }

    public static void endTextBatching() {
        TEXT_CONSUMER = null;
        TEXT_BATCH.endBatch();
    }

    public static boolean isTextBatching() {
        return TEXT_CONSUMER != null;
    }

    public static void beginItemModelBatching() {
        if (LIT_ITEM_MODEL_BATCH.hasActiveLayers() || UNLIT_ITEM_MODEL_BATCH.hasActiveLayers()) {
            ImmediatelyFast.LOGGER.warn("Item model batching was already active! endItemModelBatching() was not called before beginItemModelBatching(). This will cause rendering issues.");
            LIT_ITEM_MODEL_BATCH.close();
            UNLIT_ITEM_MODEL_BATCH.close();
        }
        LIT_ITEM_MODEL_CONSUMER = LIT_ITEM_MODEL_BATCH;
        UNLIT_ITEM_MODEL_CONSUMER = UNLIT_ITEM_MODEL_BATCH;
    }

    public static void endItemModelBatching() {
        LIT_ITEM_MODEL_CONSUMER = null;
        UNLIT_ITEM_MODEL_CONSUMER = null;

        UNLIT_ITEM_MODEL_BATCH.endBatch();
        LIT_ITEM_MODEL_BATCH.endBatch();
    }

    public static boolean isItemModelBatching() {
        return LIT_ITEM_MODEL_CONSUMER != null || UNLIT_ITEM_MODEL_CONSUMER != null;
    }

    public static void beginItemOverlayBatching() {
        if (ITEM_OVERLAY_BATCH.hasActiveLayers()) {
            ImmediatelyFast.LOGGER.warn("Item overlay batching was already active! endItemOverlayBatching() was not called before beginItemOverlayBatching(). This will cause rendering issues.");
            ITEM_OVERLAY_BATCH.close();
        }
        ITEM_OVERLAY_CONSUMER = ITEM_OVERLAY_BATCH;
    }

    public static void endItemOverlayBatching() {
        ITEM_OVERLAY_CONSUMER = null;
        ITEM_OVERLAY_BATCH.endBatch();
    }

    public static boolean isItemOverlayBatching() {
        return ITEM_OVERLAY_CONSUMER != null;
    }

    /**
     * Creates a map of layer buffers for the given RenderLayer's.
     *
     * @param layers The RenderLayer's for which to create the layer buffers.
     * @return A map of layer buffers for the given RenderLayer's.
     */
    public static Map<RenderType, BufferBuilder> createLayerBuffers(final RenderType... layers) {
        final Object2ObjectMap<RenderType, BufferBuilder> layerBuffers = new Object2ObjectLinkedOpenHashMap<>(layers.length);
        for (final RenderType layer : layers) {
            layerBuffers.put(layer, new BufferBuilder(layer.bufferSize()));
        }
        return layerBuffers;
    }

}