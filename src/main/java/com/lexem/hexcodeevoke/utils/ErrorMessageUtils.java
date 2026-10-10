package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;

public class ErrorMessageUtils {

    public ErrorMessageUtils() {
    }

    public static void sendMessageError(PlayerRef playerRef, String title, String description) {
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation(title),
                    Message.translation(description)
            );
        }
    }

    public static void sendMessageErrorJoinInt(PlayerRef playerRef, String title, int number, String description2) {
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation(title),
                    Message.join(
                            Message.raw(number + " "),
                            Message.translation(description2)
                    )
            );
        }
    }

    public static void sendMessageErrorJoinInt2(PlayerRef playerRef, String title, String description1, int number, String description2) {
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation(title),
                    Message.join(
                            Message.translation(description1),
                            Message.raw(" " + number + " "),
                            Message.translation(description2)
                    )
            );
        }
    }
}
