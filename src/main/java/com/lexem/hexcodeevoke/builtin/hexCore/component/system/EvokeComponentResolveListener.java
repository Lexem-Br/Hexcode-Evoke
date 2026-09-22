package com.lexem.hexcodeevoke.builtin.hexCore.component.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.riprod.hexcode.api.dispatch.GlyphResolveEvent;
import com.riprod.hexcode.builtin.hexCore.components.component.ComponentPasteCache;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvokeComponentResolveListener extends EntityEventSystem<EntityStore, GlyphResolveEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final List<String> VALID_WANDS = List.of(
            "Crude_Evoke_Wand",
            "Wooden_Evoke_Wand",
            "Iron_Evoke_Wand"
    );

    private static final List<String> VALID_EVOKE_SHAPES = List.of(
            "Square",
            "Triangle",
            "Antisquare"
    );

    private static final List<String> VALID_EVOKE_GLYPHS = List.of("Evoke");

    public EvokeComponentResolveListener() {
        super(GlyphResolveEvent.class);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return ComponentPasteCache.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull GlyphResolveEvent event) {
        if (event.isResolved()) return;

        List<DrawnShapeComponent> shapes = event.getStructure().getShapes();
        if (shapes == null || shapes.isEmpty()) return;

        Glyph glyph = matchWithEvokeGlyphs(event, VALID_EVOKE_GLYPHS);

        boolean isHoldWand = isHoldingAnyItem(event.getPlayer(), store, VALID_WANDS);
        if (isHoldWand) {
            boolean isEvokeShape = hasAnyShape(shapes, VALID_EVOKE_SHAPES);
            if (!isEvokeShape) {
                event.setCancelled(true);
                return;
            }

            if (glyph == null) {
                event.setCancelled(true);
                return;
            }

            GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
            if (asset == null) {
                event.setCancelled(true);
                return;
            }

            event.setResolution(glyph, asset);
        } else {
            if (glyph != null) {
                event.setCancelled(true);
            }
        }
    }

    private boolean hasAnyShape(@Nonnull List<DrawnShapeComponent> shapes, @Nonnull List<String> shapeIds) {
        for (DrawnShapeComponent shape : shapes) {
            if (shape == null || shape.getShapeId() == null) continue;

            if (shapeIds.contains(shape.getShapeId())) {
                return true;
            }
        }

        return false;
    }

    private boolean isHoldingAnyItem(
            Ref<EntityStore> playerRef,
            Store<EntityStore> store,
            @Nonnull List<String> itemIds
    ) {
        if (itemIds.isEmpty()) {
            return false;
        }

        for (String itemId : itemIds) {
            if (itemId != null && !itemId.isEmpty()
                    && InventoryHelper.holdsItem(playerRef, store, itemId)) {
                return true;
            }
        }

        return false;
    }

    private Glyph matchWithEvokeGlyphs(GlyphResolveEvent event, @Nonnull List<String> validEvokeGlyphs) {
        List<DrawnShapeComponent> drawn = event.getStructure().getShapes();
        if (drawn == null || drawn.isEmpty()) return null;

        for (String glyphId : validEvokeGlyphs) {
            if (glyphId == null || glyphId.isEmpty()) continue;

            GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
            if (asset == null || !asset.isEnabled()) continue;

            List<ShapeAsset> evokeShapes = asset.getShapes();
            if (evokeShapes == null || evokeShapes.isEmpty()) continue;

            if (shapesMatch(drawn, evokeShapes)) {
                return new Glyph(asset, event.getStructure().getVolatility(), event.getStructure().getEfficiency());
            }
        }

        return null;
    }

    private boolean shapesMatch(
            @Nonnull List<DrawnShapeComponent> drawn,
            @Nonnull List<ShapeAsset> expected
    ) {
        if (drawn.size() != expected.size()) return false;
        if (drawn.isEmpty()) return true;

        Map<String, Integer> drawnCounts = new HashMap<>();
        for (DrawnShapeComponent d : drawn) {
            if (d == null || d.getShapeId() == null) return false;
            drawnCounts.merge(d.getShapeId(), 1, Integer::sum);
        }

        for (ShapeAsset e : expected) {
            if (e == null) return false;
            String baseId = e.getBaseShapeId();
            if (baseId == null) return false;

            Integer count = drawnCounts.get(baseId);
            if (count == null || count <= 0) return false;

            if (count == 1) {
                drawnCounts.remove(baseId);
            } else {
                drawnCounts.put(baseId, count - 1);
            }
        }

        return drawnCounts.isEmpty();
    }
}
