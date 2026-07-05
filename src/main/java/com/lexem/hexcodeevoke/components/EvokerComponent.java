package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector3d;

public class EvokerComponent implements Component<EntityStore>{

    private static ComponentType<EntityStore, EvokerComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, EvokerComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, EvokerComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<EvokerComponent> CODEC = BuilderCodec
            .builder(EvokerComponent.class, EvokerComponent::new)
            .append(
                    new KeyedCodec<>("TargetPosition", Vector3dUtil.CODEC),
                    (component, value) -> component.targetPosition = value,
                    component -> component.targetPosition
            ).add()
            .build();

    private Vector3d targetPosition;

    public EvokerComponent(){
    }

    public EvokerComponent(Vector3d targetPosition){
        this.targetPosition = targetPosition;
    }

    public Vector3d getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Vector3d newTargetPosition) {
        this.targetPosition = newTargetPosition;
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new EvokerComponent(this.targetPosition);
    }

    @Override
    public String toString() {
        return "EvokerComponent{targetPosition=" + getTargetPosition() + "}";
    }
}
