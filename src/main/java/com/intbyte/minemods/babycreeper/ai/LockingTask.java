package com.intbyte.minemods.babycreeper.ai;

public interface LockingTask {
    boolean isLocked();
    void setLock(boolean lock);
    default void switchLock(){
        setLock(!isLocked());
    }
}
