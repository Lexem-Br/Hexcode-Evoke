package com.lexem.hexcodeevoke.builtin.glyphs.evoke.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;
import org.joml.Vector3d;

public class EvokeStyle {

    private EvokeStyle() {
    }

    private static GlyphAsset asset() {
        return (GlyphAsset)GlyphAsset.getAssetMap().getAsset("Evoke");
    }

    public static void renderImpact(ComponentAccessor<EntityStore> accessor, Vector3d position, HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), position, accessor);
    }

}
