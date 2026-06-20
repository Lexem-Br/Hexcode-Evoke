package com.lexem.hexcodeevoke.builtin.glyphs.evoke.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.utils.VfxUtil;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class EvokeStyle {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String GLYPH_ID = "Evoke";
    private static final String SHOCK_EFFECT_ID = "Hexcode_Shock";
    private static final Vector3f DEFAULT_COLOR = new Vector3f(0.6F, 0.9F, 1.0F);
    private static final float BEAM_THICKNESS = 0.2F;
    private static final float BEAM_DURATION = 0.3F;
    private static final int PARTICLES_PER_EVOKE = 12;

    private EvokeStyle() {
    }

    private static GlyphAsset asset() {
        return (GlyphAsset)GlyphAsset.getAssetMap().getAsset("Evoke");
    }

    public static Vector3f resolveColor(HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        Color c = overrides != null ? overrides.getPrimaryColor() : null;
        if (c == null) {
            HexStyleAsset glyphStyle = asset() != null ? asset().getStyle() : null;
            c = glyphStyle != null ? glyphStyle.getPrimaryColor() : null;
        }

        return c != null ? HexColors.toVector3f(c) : DEFAULT_COLOR;
    }

    public static void renderEvoke(ComponentAccessor<EntityStore> accessor, World world, Vector3d sourcePos, Vector3d targetPos, HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        Vector3f color = resolveColor(ctx);
        VfxUtil.line(accessor, world, sourcePos, targetPos, color, (double)0.2F, 0.3F, 0);
        String particleId = particleSystemId();
        if (particleId != null) {
            VfxUtil.particleAlongPath(particleId, sourcePos, targetPos, 12, accessor);
        }

        VfxUtil.spawnPrimary(overrides, asset(), sourcePos, accessor);
    }

    public static void renderImpact(ComponentAccessor<EntityStore> accessor, Vector3d position, HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), position, accessor);
    }

    public static void applyShockEffect(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> targetRef) {
        EntityEffect shockEffect = (EntityEffect)EntityEffect.getAssetMap().getAsset("Hexcode_Shock");
        if (shockEffect == null) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("bolt: Hexcode_Shock effect asset not found");
        } else {
            EffectControllerComponent controller = (EffectControllerComponent)accessor.getComponent(targetRef, EffectControllerComponent.getComponentType());
            if (controller != null) {
                controller.addEffect(targetRef, shockEffect, 1.0F, OverlapBehavior.OVERWRITE, accessor);
            }
        }
    }

    private static String particleSystemId() {
        GlyphAsset a = asset();
        return a != null && a.getStyle() != null && a.getStyle().getPrimaryParticle() != null ? a.getStyle().getPrimaryParticle().getSystemId() : null;
    }
}
