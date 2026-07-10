package com.lexem.hexcodeevoke.builtin.glyphs.evoke;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.SpawHexCreature;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.glyphs.bolt.BoltGlyphSlots;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.VolatilityTracker;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.utils.HexVarUtil;
import org.joml.*;

public class EvokeGlyph implements GlyphHandler {
    public static final String ID = "Evoke";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean consumeVolatility(Glyph glyph, HexContext hexContext) {
        VolatilityTracker tracker = hexContext.getVolatilityTracker();
        if (tracker == null)
            return true;

        HexVar magInput = glyph.readSlot(BoltGlyphSlots.POWER, hexContext);
        double magnitude = HexVarUtil.numberOrDefault(magInput, 15.0);

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        float areaScale = computeAreaScale(magnitude, asset);

        float cost = VolatilityTracker.computeGlyphCost(glyph) * areaScale;
        return tracker.consumeVolatility(cost);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar target = glyph.readSlot(BoltGlyphSlots.TARGET, hexContext);

        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(target, hexContext);
        BlockVar blockVar = entityVar == null ? HexVarUtil.resolveBlockVar(target, hexContext) : null;
        if (blockVar != null) {
            Vector3i blockPos = blockVar.getValue();
            Ref<EntityStore> refESPlayer = hexContext.getCasterRef();
            assert refESPlayer != null;

            SpawHexCreature.trySpawn(blockPos, refESPlayer, accessor);
        } else {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,"Target must be a Block");
            return;
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

}