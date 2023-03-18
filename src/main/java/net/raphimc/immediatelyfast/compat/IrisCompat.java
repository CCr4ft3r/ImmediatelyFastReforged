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
package net.raphimc.immediatelyfast.compat;

import net.raphimc.immediatelyfast.ImmediatelyFast;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.lenni0451.reflect.accessor.FieldAccessor;

import java.lang.invoke.*;
import java.util.function.BooleanSupplier;

public class IrisCompat {

    public static boolean IRIS_LOADED = false;

    public static BooleanSupplier isRenderingLevel;
    public static BooleanConsumer renderWithExtendedVertexFormat;
    public static TriConsumer<BufferBuilder, VertexFormat.Mode, VertexFormat> iris$beginWithoutExtending;

    public static void init() {
        IRIS_LOADED = true;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final Class<?> immediateStateClass = Class.forName("net.coderbot.iris.vertices.ImmediateState");
            final Class<?> extendingBufferBuilderClass = Class.forName("net.coderbot.iris.vertices.ExtendingBufferBuilder");

            isRenderingLevel = FieldAccessor.makeGetter(BooleanSupplier.class, null, immediateStateClass.getDeclaredField("isRenderingLevel"));
            renderWithExtendedVertexFormat = FieldAccessor.makeSetter(BooleanConsumer.class, null, immediateStateClass.getDeclaredField("renderWithExtendedVertexFormat"));

            final MethodHandle iris$beginWithoutExtendingMH = lookup.findVirtual(extendingBufferBuilderClass, "iris$beginWithoutExtending", MethodType.methodType(void.class, VertexFormat.Mode.class, VertexFormat.class));
            final CallSite iris$beginWithoutExtendingCallSite = LambdaMetafactory.metafactory(lookup, "accept", MethodType.methodType(TriConsumer.class), MethodType.methodType(void.class, Object.class, Object.class, Object.class), iris$beginWithoutExtendingMH, iris$beginWithoutExtendingMH.type());
            iris$beginWithoutExtending = (TriConsumer<BufferBuilder, VertexFormat.Mode, VertexFormat>) iris$beginWithoutExtendingCallSite.getTarget().invoke();
        } catch (Throwable e) {
            ImmediatelyFast.LOGGER.error("Failed to initialize Iris compatibility. Try updating Iris before reporting this on GitHub", e);
            System.exit(-1);
        }
    }

}