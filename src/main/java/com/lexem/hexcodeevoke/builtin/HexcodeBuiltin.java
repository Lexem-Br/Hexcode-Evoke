package com.lexem.hexcodeevoke.builtin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.lexem.hexcodeevoke.builtin.glyphs.evoke.EvokeGlyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class HexcodeBuiltin {
    public static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static void Setup() {
        GlyphAsset.register(EvokeGlyph.class, EvokeGlyph.TYPE, EvokeGlyph::new);
        LOGGER.atInfo().log("Registered Hexcode Evoke Builtin Glyph");
    }
}
