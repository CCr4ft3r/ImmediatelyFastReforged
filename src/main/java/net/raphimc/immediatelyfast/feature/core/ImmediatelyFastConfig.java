package net.raphimc.immediatelyfast.feature.core;

@SuppressWarnings("unused")
public class ImmediatelyFastConfig {
    // Regular config values
    private final String REGULAR_INFO = "----- Regular config values below -----";
    public boolean font_atlas_resizing = true;
    public boolean map_atlas_generation = true;
    public boolean hud_batching = true;
    public boolean fast_text_lookup = true;
    public boolean fast_buffer_upload = true;

    // Cosmetic config values
    private final String COSMETIC_INFO = "----- Cosmetic only config values below (Does not optimize anything) -----";
    public boolean dont_add_info_into_debug_hud = false;

    // Experimental config values
    private final String EXPERIMENTAL_INFO = "----- Experimental config values below (Rendering glitches may occur) -----";
    public boolean experimental_item_hud_batching = false;

    // Debug config values
    private final String DEBUG_INFO = "----- Debug only config values below (Do not touch) -----";
    public boolean debug_only_and_not_recommended_disable_universal_batching = false;
    public boolean debug_only_and_not_recommended_disable_mod_conflict_handling = false;
}