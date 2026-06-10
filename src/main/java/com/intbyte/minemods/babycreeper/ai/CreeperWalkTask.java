package com.intbyte.minemods.babycreeper.ai;


import com.intbyte.minemods.babycreeper.BabyCreeper;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

public class CreeperWalkTask extends EntityAIBase implements LockingTask {
    private final BabyCreeper entity;
    private int time = 90;
    private Vec3d pos;
    private boolean locked;

    public CreeperWalkTask(BabyCreeper babyCreeper) {
        entity = babyCreeper;
    }

    private void updatePosition() {
        pos = RandomPositionGenerator.findRandomTarget(entity, 3, 1);
        if (pos == null) pos = new Vec3d(entity.posX, entity.posY, entity.posZ);

    }

    @Override
    public boolean shouldExecute() {
        return !entity.isLocked() && entity.getOwner() == null;
    }

    @Override
    public void startExecuting() {
        updatePosition();
    }

    @Override
    public void updateTask() {
        super.updateTask();

        if (time++ % 100 == 0) {
            updatePosition();

        }
        entity.getNavigator().tryMoveToXYZ(pos.x, pos.y, pos.z, 0.5);

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
