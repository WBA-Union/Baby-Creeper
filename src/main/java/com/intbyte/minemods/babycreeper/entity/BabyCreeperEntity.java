package com.intbyte.minemods.babycreeper.entity;

import com.intbyte.minemods.babycreeper.ai.BabyCreeperRandomWalkGoal;
import com.intbyte.minemods.babycreeper.ai.FollowOwnerWithLockingGoal;
import com.intbyte.minemods.babycreeper.ai.LockingTask;
import com.intbyte.minemods.babycreeper.ai.TamedCreeperAroundOwnerGoal;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BabyCreeperEntity extends TamableAnimal implements LockingTask {
    private boolean locked;

    public BabyCreeperEntity(EntityType<? extends BabyCreeperEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new FollowOwnerWithLockingGoal(this, 1.0D, 4.0F, 2.0F));
        this.goalSelector.addGoal(7, new BabyCreeperRandomWalkGoal(this));
        this.goalSelector.addGoal(8, new TamedCreeperAroundOwnerGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("isLocked", isLocked());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setLock(compound.getBoolean("isLocked"));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean flag = target.hurt(this.damageSources().mobAttack(this), damage);
        if (flag && target instanceof LivingEntity livingTarget) {
            this.doEnchantDamageEffects(this, livingTarget);
        }
        return flag;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CREEPER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(Items.GUNPOWDER) && !player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                this.tame(player);
                this.heal(5.0F);
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.5F, 1.0F);
                spawnParticles(ParticleTypes.HEART);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isTame() && !(heldItem.getItem() instanceof DyeItem)) {
            if (!this.level().isClientSide) {
                setLock(!isLocked());
                spawnParticles(ParticleTypes.ENCHANT);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private void spawnParticles(ParticleOptions particle) {
        for (int i = 0; i < 7; ++i) {
            double x = this.getX() + this.getRandom().nextFloat() * 0.5D - 0.25D;
            double y = this.getY() + this.getRandom().nextFloat() * 0.5D;
            double z = this.getZ() + this.getRandom().nextFloat() * 0.5D - 0.25D;
            double motionX = this.getRandom().nextGaussian() * 0.02D;
            double motionY = this.getRandom().nextGaussian() * 0.02D;
            double motionZ = this.getRandom().nextGaussian() * 0.02D;
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(particle, x, y, z, 1, motionX, motionY, motionZ, 0.0D);
            } else {
                this.level().addParticle(particle, x, y, z, motionX, motionY, motionZ);
            }
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
