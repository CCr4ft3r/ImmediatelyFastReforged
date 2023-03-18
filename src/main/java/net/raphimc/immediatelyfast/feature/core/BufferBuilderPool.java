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

import net.raphimc.immediatelyfast.injection.interfaces.IBufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

public class BufferBuilderPool {

    private static final int INITIAL_SIZE = 256;
    private static final Set<Pair<BufferBuilder, Long>> POOL = new ReferenceArraySet<>(INITIAL_SIZE);

    private static long lastCleanup = 0;

    private BufferBuilderPool() {
    }

    public static BufferBuilder get() {
        if (lastCleanup < System.currentTimeMillis() - 5_000) {
            lastCleanup = System.currentTimeMillis();
            cleanup();
        }

        for (Pair<BufferBuilder, Long> entry : POOL) {
            final BufferBuilder bufferBuilder = entry.getKey();
            if (!bufferBuilder.building() && !((IBufferBuilder) bufferBuilder).isReleased()) {
                entry.setValue(System.currentTimeMillis());
                return bufferBuilder;
            }
        }

        final BufferBuilder bufferBuilder = new BufferBuilder(256);
        POOL.add(new MutablePair<>(bufferBuilder, System.currentTimeMillis()));
        return bufferBuilder;
    }

    public static int getAllocatedSize() {
        cleanup();
        return POOL.size();
    }

    private static void cleanup() {
        POOL.removeIf(b -> ((IBufferBuilder) b.getKey()).isReleased());
        POOL.removeIf(b -> {
            if (b.getValue() < System.currentTimeMillis() - 120_000) {
                ((IBufferBuilder) b.getKey()).release();
                return true;
            }
            return false;
        });
    }

}