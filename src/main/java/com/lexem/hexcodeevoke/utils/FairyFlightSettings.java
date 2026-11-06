package com.lexem.hexcodeevoke.utils;

public record FairyFlightSettings(
        double gain,
        double maxSpeed,
        double acceleration,
        double dashDistance,
        double dashStopDistance,
        double dashGain,
        double dashSpeed,
        double dashAcceleration,
        double wobbleRadius,
        double wobblePeriod,
        double bobHeight,
        double bobPeriod,
        double flitRadius,
        double flitMinInterval,
        double flitMaxInterval,
        double personalRadius,
        double avoidRadius,
        double maxTilt,
        double tiltFullSpeed,
        boolean matchEvokerLook
) {
}
