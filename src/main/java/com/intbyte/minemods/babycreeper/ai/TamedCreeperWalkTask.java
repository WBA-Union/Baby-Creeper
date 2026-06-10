package com.intbyte.minemods.babycreeper.ai;

import com.intbyte.minemods.babycreeper.BabyCreeper;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Random;


enum CreeperState {
    STAY, WALK, STAY_AND_LOOK_AT_THE_PLAYER;

    private static final Random PRNG = new Random();

    public static CreeperState newState() {
        CreeperState[] values = values();
        return values[PRNG.nextInt(values.length)];
    }
}

public class TamedCreeperWalkTask extends EntityAIBase implements LockingTask {
    private static final Random PRNG = new Random();
    private final BabyCreeper entity;
    private int time;
    private Vec3d pos;
    private CreeperState state;
    private boolean goAway = false;
    private boolean locked;

    public TamedCreeperWalkTask(BabyCreeper babyCreeper) {
        entity = babyCreeper;
    }

    private void newState() {
        goAway = false;
        state = CreeperState.newState();
        time = 40 + PRNG.nextInt(100);

        if (state == CreeperState.WALK) {
            pos = RandomPositionGenerator.findRandomTargetBlockTowards(entity, 4, 1, entity.getOwner().getPositionVector());
        } else if (state == CreeperState.STAY || state == CreeperState.STAY_AND_LOOK_AT_THE_PLAYER) {
            time = 20 + PRNG.nextInt(60);
        }
    }

    @Override
    public boolean shouldExecute() {
        return entity.getOwner() != null && entity.getDistanceSq(entity.getOwner()) < 7;
    }


    @Override
    public void startExecuting() {
        newState();
    }


    private void lookAtThePLayer() {
        entity.getLookHelper().setLookPosition(entity.getOwner().posX, entity.getOwner().posY + (double) entity.getOwner().getEyeHeight(), entity.getOwner().posZ, 10.0F, (float) entity.getVerticalFaceSpeed());
    }

    @Override
    public void updateTask() {

        if (entity.isLocked()) {
            lookAtThePLayer();
            entity.getNavigator().clearPath();
            return;
        }
        super.updateTask();
        if (time-- < 0) {
            newState();
        }

        switch (state) {
            case WALK:
                if (entity.getDistanceSq(entity.getOwner()) < 2 && !goAway) {
                    pos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, 4, 1, entity.getOwner().getPositionVector());
                    goAway = true;
                }
                if (pos == null) {
                    newState();
                    return;
                }
                entity.getNavigator().tryMoveToXYZ(pos.x, pos.y, pos.z, 0.5);
                break;
            case STAY_AND_LOOK_AT_THE_PLAYER:
                if (!entity.hasPath()) lookAtThePLayer();
                break;

        }
    }


    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLock(boolean lock) {
        locked = lock;
    }
}
