package com.intbyte.minemods.babycreeper;


import com.intbyte.minemods.babycreeper.ai.CreeperWalkTask;
import com.intbyte.minemods.babycreeper.ai.FollowOwnerWithLocking;
import com.intbyte.minemods.babycreeper.ai.LockingTask;
import com.intbyte.minemods.babycreeper.ai.TamedCreeperWalkTask;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BabyCreeper extends EntityTameable implements LockingTask {


    private boolean locked;

    public BabyCreeper(World world) {
        super(world);
        setSize(0.6F, 0.9F);
    }


    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAISit(this));

        tasks.addTask(4, new EntityAILeapAtTarget(this, 0.4F));
        tasks.addTask(5, new EntityAIAttackMelee(this, 1.0D, true));
        tasks.addTask(6, new FollowOwnerWithLocking(this, 1.0D, 4.0f, 2.0F));

        tasks.addTask(0, new CreeperWalkTask(this));
        tasks.addTask(0, new TamedCreeperWalkTask(this));
        targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));


        super.initEntityAI();

    }


    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("isLocked", isLocked());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setLock(compound.getBoolean("isLocked"));
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));

        if (flag) {
            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return SoundEvents.ENTITY_CREEPER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CREEPER_DEATH;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null; // Disable breeding for now
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

    }

    private void particles(EnumParticleTypes types) {
        for (int i = 0; i < 7; i++) {
            double x = posX + rand.nextFloat() * 0.5 - 0.25;
            double y = posY + rand.nextFloat() * 0.5;
            double z = posZ + rand.nextFloat() * 0.5 - 0.25;
            double motionX = rand.nextGaussian() * 0.02;
            double motionY = rand.nextGaussian() * 0.02;
            double motionZ = rand.nextGaussian() * 0.02;
            world.spawnParticle(types, x, y, z, motionX, motionY, motionZ);
        }
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d p_184199_2_, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.getItem() == Items.GUNPOWDER && !player.isSneaking()) {

            setTamed(true);
            setOwnerId(player.getUniqueID());
            // Heal the baby creeper
            heal(5.0F);

            heldItem.shrink(1); // Consume the gunpowder item
            world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.5F, 1.0F);
            particles(EnumParticleTypes.HEART);
            return EnumActionResult.SUCCESS;
        } else if (isTamed() && heldItem.getItem() != Items.NAME_TAG) {
            setLock(!isLocked());
            particles(EnumParticleTypes.CRIT_MAGIC);
            return EnumActionResult.SUCCESS;
        }
        return super.applyPlayerInteraction(player, p_184199_2_, hand);
    }

    @Override
    public String getName() {
        return super.getName();
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


