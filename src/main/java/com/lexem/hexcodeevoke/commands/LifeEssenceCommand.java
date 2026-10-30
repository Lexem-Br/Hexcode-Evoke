package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.utils.LifeEssenceUtils;

import javax.annotation.Nonnull;
import java.util.UUID;

public class LifeEssenceCommand extends AbstractPlayerCommand {
    private final RequiredArg<Float> amountArg;

    public LifeEssenceCommand() {
        super("lifeEssence", "Set the Life Essence stored in the selected Hex Creatures (all of them if none is selected)");
        this.amountArg = this.withRequiredArg("amount", "Life Essence amount", ArgTypes.FLOAT);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker == null) return;

        float amount = Math.max(0, amountArg.get(context));
        String[] uuids = evoker.getSelectedHexCreatures().length > 0 ? evoker.getSelectedHexCreatures() : evoker.getHexCreatureUUIDs();

        int count = 0;
        for (String uuidString : uuids) {
            UUID uuid = LifeEssenceUtils.parseUUID(uuidString);
            Ref<EntityStore> npcRef = uuid != null ? store.getExternalData().getRefFromUUID(uuid) : null;
            HexCreatureComponent hexCreature = npcRef != null ? store.getComponent(npcRef, HexCreatureComponent.getComponentType()) : null;
            if (hexCreature == null) continue;

            float capacity = LifeEssenceUtils.getCapacity(LifeEssenceUtils.getHexItem(hexCreature));
            if (capacity <= 0) continue;

            hexCreature.setLifeEssence(Math.min(amount, capacity));
            LifeEssenceUtils.syncToEvoker(store, hexCreature);
            Dirty dirty = store.getComponent(npcRef, Dirty.getComponentType());
            if (dirty != null) dirty.markDirty();
            count++;
        }

        playerRef.sendMessage(Message.raw("[Evoke] Life Essence set to " + LifeEssenceUtils.formatAmount(amount) + " in " + count + " Hex Creature(s)"));
    }
}
