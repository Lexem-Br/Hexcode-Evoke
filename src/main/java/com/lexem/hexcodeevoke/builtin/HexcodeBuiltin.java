package com.lexem.hexcodeevoke.builtin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.lexem.hexcodeevoke.builtin.glyphs.evoke.EvokeGlyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;

public class HexcodeBuiltin {
    public static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static void Setup() {
        GlyphRegistry.register(new EvokeGlyph());
        LOGGER.atInfo().log("Registered Hexcode Builtin Glyph");
    }
}
