package com.intbyte.minemods.babycreeper.ai;

import java.util.Random;

public enum CreeperState {
    STAY,
    WALK,
    STAY_AND_LOOK_AT_THE_PLAYER;

    private static final Random RANDOM = new Random();

    public static CreeperState newState() {
        CreeperState[] states = values();
        return states[RANDOM.nextInt(states.length)];
    }
}
