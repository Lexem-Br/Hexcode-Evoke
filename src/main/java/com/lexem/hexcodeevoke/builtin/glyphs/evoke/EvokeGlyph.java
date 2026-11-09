package com.lexem.hexcodeevoke.builtin.glyphs.evoke;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.VarType;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class EvokeGlyph extends GlyphAsset {
    public static final String TYPE = "Evoke";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar target = glyph.readSlot(EvokeSlots.TARGET, hexContext);

        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,"Target required");
            return;
        }

        if (target.type() == VarType.POSITION || target.type() == VarType.TRANSFORM) {
            handleBlockTarget(glyph, hexContext, Vector3dUtil.toVector3i(target.position(hexContext.getAccessor())));
        } else {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,"Target must be a Block");
            return;
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void handleBlockTarget(
            Glyph glyph,
            HexContext hexContext,
            Vector3i blockPos
    ) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        Ref<EntityStore> refESPlayer = hexContext.getRootRef();

        Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);

        boolean spawned = HexCreatureUtils.trySpawnHexCreature(blockPos, refESPlayer, accessor);
        if (!spawned) return;

        EffectRenderer.render(EffectSlot.Block, this, hexContext, blockVector, hexContext.getAccessor());

        glyph.writeOutput(new BlockVar(blockPos), hexContext);
    }

}
