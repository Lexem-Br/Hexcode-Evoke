package com.lexem.hexcodeevoke.builtin.glyphs.evoke;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.builtin.glyphs.evoke.style.EvokeStyle;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

import org.joml.*;

import java.util.Map;


public class EvokeGlyph implements GlyphHandler {
    public static final String ID = "Evoke";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar target = glyph.readSlot(EvokeSlots.TARGET, hexContext);

        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,"Target required");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        World world = accessor.getExternalData().getWorld();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(target, hexContext);
        BlockVar blockVar = entityVar == null ? HexVarUtil.resolveBlockVar(target, hexContext) : null;
        if (blockVar != null) {
            handleBlockTarget(glyph, hexContext, blockVar, accessor, world);
        } else {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,"Target must be a Block");
            return;
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void handleBlockTarget(
            Glyph glyph,
            HexContext hexContext,
            BlockVar blockVar,
            CommandBuffer<EntityStore> accessor,
            World world) {

        Vector3i blockPos = blockVar.getValue();
        BlockType blockType = world.getBlockType(blockPos);
        if (blockType == null) { return; }

        Map.Entry<String, String> hexItem = HexItemRegistery.getByBlockId(blockType.getId());

        if (hexItem == null) {
            LOGGER.atWarning().log("Evoke: block must be a Hex item");
            return;
        }

        Ref<EntityStore> refESPlayer = hexContext.getCasterRef(accessor);
        if (refESPlayer == null) { return; }

        Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);

        boolean spawned = HexCreatureUtils.trySpawnHexCreature(blockPos, refESPlayer, accessor);
        if (!spawned) { return; }

        EvokeStyle.renderImpact(accessor, blockVector, hexContext);

        glyph.writeOutput(new BlockVar(blockPos), hexContext);
    }

}
