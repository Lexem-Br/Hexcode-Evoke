package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.OffHandItemStateUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

public class OffHandItemStateResetSystem extends EntityTickingSystem<EntityStore> {
    private static final Set<InteractionType> ACTIVE_TYPES = EnumSet.of(
            InteractionType.Primary,
            InteractionType.Secondary,
            InteractionType.Ability1,
            InteractionType.Ability2,
            InteractionType.Ability3,
            InteractionType.Ability4,
            InteractionType.Use
    );

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        InventoryComponent.Utility utility = archetypeChunk.getComponent(index, InventoryComponent.Utility.getComponentType());
        if (utility == null || !OffHandItemStateUtils.hasTransientState(utility)) return;

        InteractionManager interactionManager = archetypeChunk.getComponent(index, InteractionModule.get().getInteractionManagerComponent());
        if (interactionManager != null) {
            for (InteractionChain chain : interactionManager.getChains().values()) {
                if (ACTIVE_TYPES.contains(chain.getType())) return;
            }
        }

        OffHandItemStateUtils.resetTransientState(utility);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Player.getComponentType(),
                InventoryComponent.Utility.getComponentType()
        );
    }
}
