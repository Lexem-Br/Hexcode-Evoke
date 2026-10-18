package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasHarvestableCrop;
import com.lexem.hexcodeevoke.utils.CropUtils;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.List;

public class FilterHasHarvestableCrop extends EntityFilterBase {
    private final double horizontalRange;
    private final double verticalRange;
    private final List<String> soilBlocks;

   public FilterHasHarvestableCrop(@Nonnull BuilderFilterHasHarvestableCrop builder, @Nonnull BuilderSupport support) {
       this.horizontalRange = builder.getHorizontalRange(support);
       this.verticalRange = builder.getVerticalRange(support);
       String[] soils = builder.getSoilBlocks(support);
       this.soilBlocks = soils != null && soils.length > 0 ? List.of(soils) : null;
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
       World world = store.getExternalData().getWorld();
       TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
       if (transformComponent == null) return false;

       Vector3i crop = FinderUtils.findNearestBlockBFS(
               transformComponent.getPosition(),
               (int) Math.ceil(horizontalRange),
               (int) Math.ceil(verticalRange),
               cropValidator,
               world
       );

       return crop != null;
   }

    private final FinderUtils.BlockValidator<World> cropValidator = this::isHarvestableCrop;

    private boolean isHarvestableCrop(@Nonnull Vector3i block, @Nonnull World world) {
        return CropUtils.isHarvestableCrop(block, world, soilBlocks);
    }

   @Override
   public int cost() {
      return 100;
   }
}
