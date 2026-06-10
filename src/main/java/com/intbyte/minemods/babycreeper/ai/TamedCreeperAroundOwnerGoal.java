package com.intbyte.minemods.babycreeper.ai;

import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Random;

public class TamedCreeperAroundOwnerGoal extends Goal implements LockingTask {
    private static final Random RANDOM = new Random();

    private final BabyCreeperEntity entity;
    private int time;
    private Vec3 pos;
    private CreeperState state;
    private boolean goAway;
    private boolean locked;

    public TamedCreeperAroundOwnerGoal(BabyCreeperEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private void newState() {
        LivingEntity owner = this.entity.getOwner();
        if (owner == null) {
            return;
        }
        this.goAway = false;
        this.state = CreeperState.newState();
        this.time = 40 + RANDOM.nextInt(100);
        if (this.state == CreeperState.WALK) {
            this.pos = DefaultRandomPos.getPosTowards(this.entity, 4, 1, owner.position(), Math.PI / 2.0D);
        } else if (this.state == CreeperState.STAY || this.state == CreeperState.STAY_AND_LOOK_AT_THE_PLAYER) {
            this.time = 20 + RANDOM.nextInt(60);
            this.pos = null;
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.entity.getOwner();
        return owner != null && this.entity.distanceToSqr(owner) < 7.0D;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        newState();
    }

    private void lookAtThePlayer() {
        LivingEntity owner = this.entity.getOwner();
        if (owner != null) {
            this.entity.getLookControl().setLookAt(owner.getX(), owner.getEyeY(), owner.getZ(), 10.0F, this.entity.getMaxHeadXRot());
        }
    }

    @Override
    public void tick() {
        LivingEntity owner = this.entity.getOwner();
        if (owner == null) {
            return;
        }
        if (this.entity.isLocked()) {
            lookAtThePlayer();
            this.entity.getNavigation().stop();
            return;
        }
        if (this.time-- < 0 || this.state == null) {
            newState();
        }
        if (this.state == CreeperState.WALK) {
            if (this.entity.distanceToSqr(owner) < 2.0D && !this.goAway) {
                this.pos = DefaultRandomPos.getPosAway(this.entity, 4, 1, owner.position());
                this.goAway = true;
            }
            if (this.pos == null) {
                newState();
                return;
            }
            this.entity.getNavigation().moveTo(this.pos.x, this.pos.y, this.pos.z, 0.5D);
        } else if (this.state == CreeperState.STAY_AND_LOOK_AT_THE_PLAYER && !this.entity.isPassenger()) {
            lookAtThePlayer();
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
