package com.lexem.hexcodeevoke.builtin.glyphs.evoke;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.*;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.lexem.hexcodeevoke.builtin.glyphs.evoke.style.EvokeStyle;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
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

import java.lang.Math;
import java.util.Map;

public class EvokeGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final String ID = "Evoke";
    private static int damageCauseIndex = -1;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar target = glyph.readSlot(EvokeSlots.TARGET, hexContext);

        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
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

    private void handleBlockTarget(Glyph glyph, HexContext hexContext, BlockVar blockVar,
                                   CommandBuffer<EntityStore> accessor,
                                   World world) {

        Vector3i blockPos = blockVar.getValue();
        BlockType blockType = world.getBlockType(blockPos);
        Map.Entry<String, String> hexItem = HexItemRegistery.getByBlockId(blockType.getId());

        if (blockPos == null) {
            LOGGER.atWarning().log("evoke: block target position is null");
            return;
        } else if (hexItem == null) {
            LOGGER.atWarning().log("evoke: block must be a Hex item");
            return;
        }

        Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);

        int blockRotationIndex = world.getBlockRotationIndex(blockPos.x, blockPos.y, blockPos.z);
        RotationTuple rotation = RotationTuple.get(blockRotationIndex);
        Rotation3f blockRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);

        world.breakBlock(blockPos.x, blockPos.y, blockPos.z, 0);

        accessor.run(_store -> {
            NPCPlugin.get().spawnNPC(_store, hexItem.getValue(), null, blockVector, blockRotation);
        });

        EvokeStyle.renderImpact(accessor, blockVector, hexContext);

        glyph.writeOutput(new BlockVar(blockPos), hexContext);
    }

}
