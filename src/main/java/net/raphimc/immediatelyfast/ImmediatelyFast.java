package net.raphimc.immediatelyfast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.raphimc.immediatelyfast.compat.IrisCompat;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.nio.file.Files;

@Mod(ImmediatelyFast.MOD_ID)
public class ImmediatelyFast {
    public static final Logger LOGGER = LoggerFactory.getLogger("ImmediatelyFast");
    public static final Unsafe UNSAFE = getUnsafe();
    public static ImmediatelyFastConfig config;

    public static final String MOD_ID = "immediatelyfast";

    public ImmediatelyFast() {
        LOGGER.info("Loading ImmediatelyFast");
        if (ModList.get().isLoaded("oculus")) {
            LOGGER.info("Found Oculus. Enabling compatibility.");
            IrisCompat.init();
        }
    }

    public static void loadConfig() {
        final File configFile = FMLPaths.CONFIGDIR.get().resolve(MOD_ID + ".json").toFile();
        if (configFile.exists()) {
            try {
                config = new Gson().fromJson(new FileReader(configFile), ImmediatelyFastConfig.class);
            } catch (Throwable e) {
                LOGGER.error("Failed to load ImmediatelyFast config. Resetting it.", e);
            }
        }
        if (config == null) {
            config = new ImmediatelyFastConfig();
        }
        try {
            Files.writeString(configFile.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(config));
        } catch (Throwable e) {
            LOGGER.error("Failed to save ImmediatelyFast config.", e);
        }
    }

    private static Unsafe getUnsafe() {
        try {
            for (Field field : Unsafe.class.getDeclaredFields()) {
                if (field.getType().equals(Unsafe.class)) {
                    field.setAccessible(true);
                    return (Unsafe) field.get(null);
                }
            }
        } catch (Throwable ignored) {
        }
        throw new IllegalStateException("Unable to get Unsafe instance");
    }
}