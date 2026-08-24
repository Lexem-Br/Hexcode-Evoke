package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasWettableSoil;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FilterHasWettableSoil extends EntityFilterBase {
    private final double horizontalRange;
    private final double verticalRange;

   public FilterHasWettableSoil(@Nonnull BuilderFilterHasWettableSoil builder, @Nonnull BuilderSupport support) {
       this.horizontalRange = builder.getHorizontalRange(support);
       this.verticalRange = builder.getVerticalRange(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
       World world = store.getExternalData().getWorld();
       TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
       if (transformComponent == null) return false;

       Vector3i soil = FinderUtils.findNearestBlockBFS(
               transformComponent.getPosition(),
               (int) Math.ceil(horizontalRange),
               (int) Math.ceil(verticalRange),
               soilValidator,
               world
       );

       return soil != null;
   }

    private final FinderUtils.BlockValidator<World> soilValidator = (block, world) -> {
        BlockType blockType = world.getBlockType(block);
        if (blockType == null || blockType.getId() == null) {
            return false;
        } else {
            return Objects.equals(blockType.getId(), "Soil_Dirt_Tilled");
        }
    };

   @Override
   public int cost() {
      return 100;
   }
}
