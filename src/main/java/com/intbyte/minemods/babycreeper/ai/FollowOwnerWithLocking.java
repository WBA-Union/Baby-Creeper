package com.intbyte.minemods.babycreeper.ai;

import com.intbyte.minemods.babycreeper.BabyCreeper;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.passive.EntityTameable;

public class FollowOwnerWithLocking extends EntityAIFollowOwner {
    private boolean locked = false;
    BabyCreeper babyCreeper;
    public FollowOwnerWithLocking(BabyCreeper tameableIn, double followSpeedIn, float minDistIn, float maxDistIn) {
        super(tameableIn, followSpeedIn, minDistIn, maxDistIn);
        babyCreeper = tameableIn;
    }


    @Override
    public boolean shouldExecute() {
        return !babyCreeper.isLocked() && super.shouldExecute();
    }


    @Override
    public boolean shouldContinueExecuting() {
        return !babyCreeper.isLocked() && super.shouldContinueExecuting();
    }
}
