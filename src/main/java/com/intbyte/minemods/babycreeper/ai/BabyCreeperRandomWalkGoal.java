package com.intbyte.minemods.babycreeper.ai;

import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BabyCreeperRandomWalkGoal extends Goal implements LockingTask {
    private final BabyCreeperEntity entity;
    private int time = 90;
    private Vec3 pos;
    private boolean locked;

    public BabyCreeperRandomWalkGoal(BabyCreeperEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private void updatePosition() {
        this.pos = DefaultRandomPos.getPos(this.entity, 3, 1);
        if (this.pos == null) {
            this.pos = this.entity.position();
        }
    }

    @Override
    public boolean canUse() {
        return !this.entity.isLocked() && this.entity.getOwner() == null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        updatePosition();
    }

    @Override
    public void tick() {
        if (this.time++ % 100 == 0) {
            updatePosition();
        }
        if (this.pos != null) {
            this.entity.getNavigation().moveTo(this.pos.x, this.pos.y, this.pos.z, 0.5D);
        }
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public void setLock(boolean locked) {
        this.locked = locked;
    }
}
