package com.intbyte.minemods.babycreeper.ai;

import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;

public class FollowOwnerWithLockingGoal extends FollowOwnerGoal {
    private final BabyCreeperEntity babyCreeper;

    public FollowOwnerWithLockingGoal(BabyCreeperEntity tameable, double speedModifier, float startDistance, float stopDistance) {
        super(tameable, speedModifier, startDistance, stopDistance, false);
        this.babyCreeper = tameable;
    }

    @Override
    public boolean canUse() {
        return !this.babyCreeper.isLocked() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.babyCreeper.isLocked() && super.canContinueToUse();
    }
}
