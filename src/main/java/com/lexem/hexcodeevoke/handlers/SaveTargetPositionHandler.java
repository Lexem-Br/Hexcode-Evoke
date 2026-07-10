package com.lexem.hexcodeevoke.handlers;

import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.events.SaveTargetPositionEvent;

import java.util.function.Consumer;

public class SaveTargetPositionHandler implements Consumer<SaveTargetPositionEvent> {

    @Override
    public void accept(SaveTargetPositionEvent event) {
        if (!event.playerRef().isValid()) return;

        var store = event.playerRef().getStore();

        EvokerComponent evoker = store.getComponent(event.playerRef(), EvokerComponent.getComponentType());
        if (evoker == null) return;

        evoker.setTargetPosition(event.targetPosition());
    }
}
