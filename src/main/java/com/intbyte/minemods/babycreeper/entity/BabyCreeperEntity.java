package com.intbyte.minemods.babycreeper.entity;

import com.intbyte.minemods.babycreeper.ai.BabyCreeperRandomWalkGoal;
import com.intbyte.minemods.babycreeper.ai.FollowOwnerWithLockingGoal;
import com.intbyte.minemods.babycreeper.ai.LockingTask;
import com.intbyte.minemods.babycreeper.ai.TamedCreeperAroundOwnerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class BabyCreeperEntity extends TamableAnimal implements LockingTask {

    private static final EntityDataAccessor<Boolean> ENCHANTED_APPLE_CHARGED =
            SynchedEntityData.defineId(BabyCreeperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DANCING =
            SynchedEntityData.defineId(BabyCreeperEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int MIN_TAME_GUNPOWDER = 2;
    private static final int MAX_TAME_GUNPOWDER = 5;
    private static final int GOLDEN_APPLE_DURATION = 2400;
    private static final int GOLDEN_APPLE_USE_COOLDOWN = 200;
    private static final float GUNPOWDER_HEAL_AMOUNT = 4.0f;
    private static final float NORMAL_DEATH_EXPLOSION_POWER = 2.4f;
    private static final float CHARGED_EXPLOSION_POWER = 4.0f;

    private boolean locked;
    private int tameGunpowderRequired = this.randomTameRequirement();
    private int tameGunpowderFed;
    private int injurySoundCooldown;
    private int enchantedAppleChargeTicks;
    private int goldenAppleCooldownTicks;
    private int lastOwnerHurtTimestamp = -1;
    @Nullable
    private LivingEntity chargedAttackTarget;

    public BabyCreeperEntity(EntityType<? extends BabyCreeperEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ENCHANTED_APPLE_CHARGED, false);
        this.entityData.define(DANCING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new EnchantedOwnerDefenseGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerWithLockingGoal(this, 1.0, 4.0f, 2.0f));
        this.goalSelector.addGoal(7, new BabyCreeperRandomWalkGoal(this));
        this.goalSelector.addGoal(8, new TamedCreeperAroundOwnerGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    public static boolean canSpawn(EntityType<BabyCreeperEntity> entityType, ServerLevelAccessor level,
                                    MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (spawnType == MobSpawnType.SPAWN_EGG
                || spawnType == MobSpawnType.SPAWNER
                || spawnType == MobSpawnType.COMMAND) {
            return true;
        }
        return random.nextFloat() < 0.005f
                && Animal.checkAnimalSpawnRules(entityType, level, spawnType, pos, random);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.tickInjurySound();
            this.tickGoldenAppleCooldown();
            this.tickEnchantedAppleCharge();
            this.tickGoldenAppleParticles();
            this.tickJukeboxDancing();
            this.tickCreeperRepellent();
        }
    }

    private void tickInjurySound() {
        if (this.injurySoundCooldown > 0) {
            --this.injurySoundCooldown;
        }
        if (this.isAlive() && this.getHealth() < 14.0f
                && this.injurySoundCooldown <= 0
                && this.getRandom().nextInt(35) == 0) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.CREEPER_HURT, SoundSource.NEUTRAL, 0.45f, 1.25f);
            this.injurySoundCooldown = 80 + this.getRandom().nextInt(81);
        }
    }

    private void tickGoldenAppleCooldown() {
        if (this.goldenAppleCooldownTicks > 0) {
            --this.goldenAppleCooldownTicks;
        }
    }

    private void tickEnchantedAppleCharge() {
        if (this.enchantedAppleChargeTicks > 0) {
            --this.enchantedAppleChargeTicks;
            this.setEnchantedAppleCharged(true);
            // Периодические GLITTER (ENCHANT) частицы пока действует заряд зачарованного яблока
            if (this.tickCount % 8 == 0) {
                this.spawnParticles(ParticleTypes.ENCHANT, 3);
            }
        } else if (this.isEnchantedAppleCharged()) {
            this.clearEnchantedAppleCharge();
        }
    }

    private void tickGoldenAppleParticles() {
        // Периодические GLITTER частицы пока действует эффект обычного золотого яблока
        if (this.tickCount % 10 == 0 && this.hasEffect(MobEffects.REGENERATION)) {
            this.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 4);
        }
    }

    private void tickJukeboxDancing() {
        boolean shouldDance = false;
        BlockPos base = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-6, -3, -6), base.offset(6, 3, 6))) {
            if (this.level().getBlockState(pos).is(Blocks.JUKEBOX)
                    && Boolean.TRUE.equals(this.level().getBlockState(pos).getValue(JukeboxBlock.HAS_RECORD))) {
                shouldDance = true;
                break;
            }
        }
        this.setDancing(shouldDance);
        if (shouldDance) {
            this.navigation.stop();
        }
    }

    private void tickCreeperRepellent() {
        if (this.tickCount % 20 != 0) return;
        for (Creeper creeper : this.level().getEntitiesOfClass(Creeper.class, this.getBoundingBox().inflate(6.0))) {
            if (!creeper.isAlive()) continue;
            if (creeper.getTarget() == this) {
                creeper.setTarget(null);
            }
            double dx = creeper.getX() - this.getX();
            double dz = creeper.getZ() - this.getZ();
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length < 0.001) {
                dx = this.getRandom().nextDouble() - 0.5;
                dz = this.getRandom().nextDouble() - 0.5;
                length = Math.sqrt(dx * dx + dz * dz);
            }
            BlockPos fleePos = BlockPos.containing(
                    creeper.getX() + dx / length * 8.0,
                    creeper.getY(),
                    creeper.getZ() + dz / length * 8.0);
            creeper.getNavigation().moveTo(
                    fleePos.getX() + 0.5, fleePos.getY(), fleePos.getZ() + 0.5, 1.15);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("isLocked", this.isLocked());
        compound.putInt("TameGunpowderRequired", this.tameGunpowderRequired);
        compound.putInt("TameGunpowderFed", this.tameGunpowderFed);
        compound.putInt("InjurySoundCooldown", this.injurySoundCooldown);
        compound.putInt("EnchantedAppleChargeTicks", this.enchantedAppleChargeTicks);
        compound.putInt("GoldenAppleCooldownTicks", this.goldenAppleCooldownTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setLock(compound.getBoolean("isLocked"));
        this.tameGunpowderRequired = compound.contains("TameGunpowderRequired")
                ? compound.getInt("TameGunpowderRequired")
                : this.randomTameRequirement();
        if (this.tameGunpowderRequired < MIN_TAME_GUNPOWDER
                || this.tameGunpowderRequired > MAX_TAME_GUNPOWDER) {
            this.tameGunpowderRequired = this.randomTameRequirement();
        }
        this.tameGunpowderFed = compound.getInt("TameGunpowderFed");
        this.injurySoundCooldown = compound.getInt("InjurySoundCooldown");
        this.enchantedAppleChargeTicks = compound.getInt("EnchantedAppleChargeTicks");
        this.goldenAppleCooldownTicks = compound.getInt("GoldenAppleCooldownTicks");
        this.setEnchantedAppleCharged(this.enchantedAppleChargeTicks > 0);
    }

    /**
     * FIX #3: При смерти крипера шлем выпадает как предмет, а не исчезает.
     */
    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.explodeWithoutBlockDamage(NORMAL_DEATH_EXPLOSION_POWER);

            ItemStack helmet = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty()) {
                this.spawnAtLocation(helmet);
                this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        }
        super.die(damageSource);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            if (this.isEnchantedAppleCharged() && livingTarget == this.chargedAttackTarget) {
                this.explodeChargedAndSurvive();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        float healthAfterDamage = this.getHealth() - amount;

        if (!this.level().isClientSide
                && this.isEnchantedAppleCharged()
                && healthAfterDamage <= 0.0F) {

            this.explodeChargedAndSurvive();
            return;
        }

        super.actuallyHurt(source, amount);
    }

    private void explodeChargedAndSurvive() {
        this.clearEnchantedAppleCharge();
        boolean oldInvulnerable = this.isInvulnerable();
        this.setInvulnerable(true);
        this.explodeWithoutBlockDamage(CHARGED_EXPLOSION_POWER);
        this.setInvulnerable(oldInvulnerable);
        this.setHealth(Math.max(1.0f, Math.min(6.0f, this.getMaxHealth())));
        this.invulnerableTime = 20;
        this.setTarget(null);
        this.chargedAttackTarget = null;
        this.spawnParticles(ParticleTypes.EXPLOSION, 1);
    }

    private void explodeWithoutBlockDamage(float power) {
        this.level().explode(this,
                this.getX(), this.getY(), this.getZ(),
                power,
                Level.ExplosionInteraction.NONE);
    }

    private void clearEnchantedAppleCharge() {
        this.enchantedAppleChargeTicks = 0;
        this.setEnchantedAppleCharged(false);
        this.removeEffect(MobEffects.REGENERATION);
        this.chargedAttackTarget = null;
        this.setTarget(null);
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

        // Снять шлем (shift + пустая рука или shift + любой предмет)
        if (player.isShiftKeyDown()
                && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                && this.isOwnedBy(player)) {
            return this.removeHelmet(player);
        }

        // Зачарованное золотое яблоко
        if (heldItem.is(Items.ENCHANTED_GOLDEN_APPLE)
                && !player.isShiftKeyDown()
                && this.isTame()
                && this.isOwnedBy(player)) {
            return this.feedEnchantedGoldenApple(player, heldItem);
        }

        // Обычное золотое яблоко
        if (heldItem.is(Items.GOLDEN_APPLE)
                && !player.isShiftKeyDown()
                && this.isTame()
                && this.isOwnedBy(player)) {
            return this.feedGoldenApple(player, heldItem);
        }

        // Порох
        if (heldItem.is(Items.GUNPOWDER) && !player.isShiftKeyDown()) {
            if (!this.isTame()) {
                return this.feedForTaming(player, heldItem);
            }
            if (this.isOwnedBy(player) && this.getHealth() < this.getMaxHealth()) {
                return this.healWithGunpowder(player, heldItem);
            }
        }

        // Надеть шлем
        if (!heldItem.isEmpty()
                && !player.isShiftKeyDown()
                && this.isOwnedBy(player)
                && this.canEquipOnHead(heldItem)
                && this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return this.equipHelmet(player, heldItem);
        }

        // Переключить блокировку
        if (this.isTame()
                && this.isOwnedBy(player)
                && (heldItem.isEmpty() || heldItem.getItem() instanceof DyeItem)) {
            if (!this.level().isClientSide) {
                this.setLock(!this.isLocked());
                this.spawnParticles(ParticleTypes.ENCHANT, 7);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult feedForTaming(Player player, ItemStack heldItem) {
        if (!this.level().isClientSide) {
            this.consumeOne(player, heldItem);
            ++this.tameGunpowderFed;
            if (this.tameGunpowderFed >= this.tameGunpowderRequired) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.spawnParticles(ParticleTypes.HEART, 7);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.5f, 1.0f);
                this.heal(5.0f);
                this.tameGunpowderFed = 0;
                this.tameGunpowderRequired = this.randomTameRequirement();
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.spawnParticles(ParticleTypes.SMOKE, 5);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.CREEPER_PRIMED, SoundSource.NEUTRAL, 0.25f, 1.6f);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private InteractionResult healWithGunpowder(Player player, ItemStack heldItem) {
        if (!this.level().isClientSide) {
            this.consumeOne(player, heldItem);
            this.heal(GUNPOWDER_HEAL_AMOUNT);
            this.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 6);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.6f, 1.2f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /**
     * FIX #4: GLITTER частицы (ParticleTypes.ENCHANT) и характерный звук при применении
     * обычного золотого яблока. Частицы появляются сразу при применении и затем периодически.
     */
    private InteractionResult feedGoldenApple(Player player, ItemStack heldItem) {
        if (this.goldenAppleCooldownTicks > 0) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (!this.level().isClientSide) {
            this.consumeOne(player, heldItem);
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, GOLDEN_APPLE_DURATION, 0));
            this.goldenAppleCooldownTicks = GOLDEN_APPLE_USE_COOLDOWN;
            // GLITTER частицы как идентификатор применения
            this.spawnParticles(ParticleTypes.ENCHANT, 20);
            this.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 8);
            // Характерный звук применения
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.6f, 1.2f);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8f, 1.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /**
     * FIX #4: GLITTER частицы (ParticleTypes.ENCHANT) и характерный звук при применении
     * зачарованного золотого яблока. Частицы появляются сразу при применении и затем периодически.
     */
    private InteractionResult feedEnchantedGoldenApple(Player player, ItemStack heldItem) {
        if (this.goldenAppleCooldownTicks > 0) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (!this.level().isClientSide) {
            this.consumeOne(player, heldItem);
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, GOLDEN_APPLE_DURATION, 3));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 0));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0));
            this.enchantedAppleChargeTicks = GOLDEN_APPLE_DURATION;
            this.goldenAppleCooldownTicks = GOLDEN_APPLE_USE_COOLDOWN;
            this.setEnchantedAppleCharged(true);
            // GLITTER частицы как идентификатор применения зачарованного яблока
            this.spawnParticles(ParticleTypes.ENCHANT, 30);
            this.spawnParticles(ParticleTypes.END_ROD, 10);
            // Характерный звук применения зачарованного яблока
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 0.8f, 1.2f);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 0.6f, 1.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private InteractionResult equipHelmet(Player player, ItemStack heldItem) {
        if (!this.level().isClientSide) {
            ItemStack equipped = heldItem.copy();
            equipped.setCount(1);
            this.setItemSlot(EquipmentSlot.HEAD, equipped);
            this.setDropChance(EquipmentSlot.HEAD, 1.0f);
            this.consumeOne(player, heldItem);
            this.playAmbientSound();
            this.spawnParticles(ParticleTypes.ENCHANT, 7);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.NEUTRAL, 0.7f, 1.1f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private InteractionResult removeHelmet(Player player) {
        if (!this.level().isClientSide) {
            ItemStack removed = this.getItemBySlot(EquipmentSlot.HEAD).copy();
            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            if (!player.getInventory().add(removed)) {
                this.spawnAtLocation(removed);
            }
            this.spawnParticles(ParticleTypes.ENCHANT, 7);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.NEUTRAL, 0.7f, 0.85f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private boolean canEquipOnHead(ItemStack stack) {
        Item item = stack.getItem();
        if (stack.canEquip(EquipmentSlot.HEAD, this)) return true;
        if (LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.HEAD) return true;
        return item instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    private void consumeOne(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private int randomTameRequirement() {
        return MIN_TAME_GUNPOWDER + this.getRandom().nextInt(MAX_TAME_GUNPOWDER - MIN_TAME_GUNPOWDER + 1);
    }

    private void spawnParticles(ParticleOptions particle, int count) {
        Level level = this.level();
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < count; ++i) {
                double x = this.getX() + (this.getRandom().nextDouble() * 0.7 - 0.35);
                double y = this.getY() + 0.2 + this.getRandom().nextDouble() * 0.8;
                double z = this.getZ() + (this.getRandom().nextDouble() * 0.7 - 0.35);
                double motionX = this.getRandom().nextGaussian() * 0.02;
                double motionY = 0.02 + this.getRandom().nextGaussian() * 0.02;
                double motionZ = this.getRandom().nextGaussian() * 0.02;
                serverLevel.sendParticles(particle, x, y, z, 1, motionX, motionY, motionZ, 0.0);
            }
        }
    }

    public boolean isEnchantedAppleCharged() {
        return this.entityData.get(ENCHANTED_APPLE_CHARGED);
    }

    private void setEnchantedAppleCharged(boolean charged) {
        this.entityData.set(ENCHANTED_APPLE_CHARGED, charged);
    }

    public boolean isDancing() {
        return this.entityData.get(DANCING);
    }

    private void setDancing(boolean dancing) {
        this.entityData.set(DANCING, dancing);
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public void setLock(boolean locked) {
        this.locked = locked;
    }

    // ==================== EnchantedOwnerDefenseGoal ====================

    private static final class EnchantedOwnerDefenseGoal extends Goal {
        private final BabyCreeperEntity creeper;
        @Nullable
        private LivingEntity target;

        private EnchantedOwnerDefenseGoal(BabyCreeperEntity creeper) {
            this.creeper = creeper;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.creeper.isTame() || !this.creeper.isEnchantedAppleCharged() || this.creeper.isOrderedToSit()) {
                return false;
            }
            LivingEntity owner = this.creeper.getOwner();
            if (owner == null) return false;
            LivingEntity attacker = owner.getLastHurtByMob();
            int timestamp = owner.getLastHurtByMobTimestamp();
            if (attacker == null || !attacker.isAlive() || attacker == this.creeper
                    || timestamp == this.creeper.lastOwnerHurtTimestamp) {
                return false;
            }
            this.target = attacker;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && this.creeper.isEnchantedAppleCharged();
        }

        @Override
        public void start() {
            LivingEntity owner = this.creeper.getOwner();
            if (owner != null) {
                this.creeper.lastOwnerHurtTimestamp = owner.getLastHurtByMobTimestamp();
            }
            this.creeper.chargedAttackTarget = this.target;
            this.creeper.setTarget(this.target);
            this.creeper.setOrderedToSit(false);
        }

        @Override
        public void stop() {
            this.creeper.navigation.stop();
            if (!this.creeper.isEnchantedAppleCharged()) {
                this.creeper.chargedAttackTarget = null;
                this.creeper.setTarget(null);
            }
        }

        @Override
        public void tick() {
            if (this.target == null) return;
            this.creeper.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
            this.creeper.getNavigation().moveTo(this.target, 1.25);
            if (this.creeper.distanceToSqr(this.target) <= 4.0) {
                this.creeper.explodeChargedAndSurvive();
            }
        }
    }
}
