package com.herobrine.mod.entities;

import com.herobrine.mod.util.entities.EntityRegistry;
import com.herobrine.mod.util.items.ItemList;
import com.herobrine.mod.util.savedata.SaveDataUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

public class InfectedLlamaEntity extends LlamaEntity implements IMob {
    private static final DataParameter<Integer> DATA_VARIANT_ID = EntityDataManager.defineId(InfectedLlamaEntity.class, DataSerializers.INT);

    public InfectedLlamaEntity(EntityType<? extends InfectedLlamaEntity> type, World worldIn) {
        super(type, worldIn);
        xpReward = 3;
    }

    public InfectedLlamaEntity(World worldIn) {
        this(EntityRegistry.INFECTED_LLAMA_ENTITY, worldIn);
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D);
    }

    public static boolean isValidLightLevel(@NotNull IServerWorld worldIn, @NotNull BlockPos pos, @NotNull Random randomIn) {
        if (worldIn.getBrightness(LightType.SKY, pos) > randomIn.nextInt(32)) {
            return false;
        } else {
            int i = worldIn.getLevel().isThundering() ? worldIn.getMaxLocalRawBrightness(pos, 10) : worldIn.getLightEmission(pos);
            return i <= randomIn.nextInt(8);
        }
    }

    public static boolean canSpawn(EntityType<? extends InfectedLlamaEntity> type, @NotNull IServerWorld worldIn, SpawnReason reason, BlockPos pos, Random randomIn) {
        return worldIn.getDifficulty() != Difficulty.PEACEFUL && hasViewOfSky(worldIn, pos) && isValidLightLevel(worldIn, pos, randomIn) && checkMobSpawnRules(type, worldIn, reason, pos, randomIn) && SaveDataUtil.canHerobrineSpawn(worldIn);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.targetSelector.addGoal(1, new InfectedLlamaEntity.HurtByTargetGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.25D, 40, 20.0F));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractSurvivorEntity.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, GolemEntity.class, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookAtGoal(this, AbstractSurvivorEntity.class, 8.0F));
        this.goalSelector.addGoal(9, new LookAtGoal(this, GolemEntity.class, 8.0F));
        this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public @NotNull SoundCategory getSoundSource() {
        return SoundCategory.HOSTILE;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof HolyWaterEntity) {
            LlamaEntity entity = this.convertTo(EntityType.LLAMA, false);
            assert entity != null;
            entity.finalizeSpawn((IServerWorld) this.level, this.level.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.CONVERSION, null, null);
            entity.setVariant(this.getVariant());
            this.level.broadcastEntityEvent(this, (byte) 16);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected int getInventorySize() {
        return 0;
    }

    @Override
    protected boolean handleEating(@NotNull PlayerEntity player, @NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT_ID, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            if (!this.isSilent()) {
                this.level.playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundCategory.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected boolean isImmobile() {
        return this.getHealth() <= 0.0F;
    }

    @Override
    public boolean canMate(@NotNull AnimalEntity otherAnimal) {
        return false;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected @NotNull LlamaEntity makeBabyLlama() {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
        return SoundEvents.LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LLAMA_DEATH;
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockIn) {
        this.playSound(SoundEvents.LLAMA_STEP, 0.15F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant());

    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(compound.getInt("Variant"));
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    public int getVariant() {
        return MathHelper.clamp(this.entityData.get(DATA_VARIANT_ID), 0, 3);
    }

    public void setVariant(int variantIn) {
        this.entityData.set(DATA_VARIANT_ID, variantIn);
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        this.spit(target);
    }

    private void spit(@NotNull LivingEntity target) {
        LlamaSpitEntity llamaSpitEntity = new LlamaSpitEntity(this.level, this);
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - llamaSpitEntity.getY();
        double d2 = target.getZ() - this.getZ();
        float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;
        llamaSpitEntity.shoot(d0, d1 + (double) f, d2, 1.5F, 10.0F);
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        this.level.addFreshEntity(llamaSpitEntity);
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier) {
        int i = this.calculateFallDamage(distance, damageMultiplier);
        if (i <= 0) {
            return false;
        } else {
            if (distance >= 6.0F) {
                this.hurt(DamageSource.FALL, (float) i);
                if (this.isVehicle()) {
                    for (Entity entity : this.getIndirectPassengers()) {
                        entity.hurt(DamageSource.FALL, (float) i);
                    }
                }
            }

            this.playBlockFallSound();
            return true;
        }
    }

    static class LlamaData extends AgeableEntity.AgeableData {
        public final int variant;

        private LlamaData(int variantIn) {
            super(false);
            this.variant = variantIn;
        }
    }

    @Override
    public void baseTick() {
        if (!level.isClientSide) {
            if (!SaveDataUtil.canHerobrineSpawn(level)) {
                this.remove();
            }
        }
        super.baseTick();
    }

    @Nullable
    @Override
    public ILivingEntityData finalizeSpawn(@NotNull IServerWorld worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        int i;
        if (spawnDataIn instanceof InfectedLlamaEntity.LlamaData) {
            i = ((InfectedLlamaEntity.LlamaData) spawnDataIn).variant;
        } else {
            i = this.random.nextInt(4);
            spawnDataIn = new InfectedLlamaEntity.LlamaData(i);
        }
        this.setVariant(i);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public void tick() {
        super.tick();
        this.setChest(false);
    }

    @Override
    public int getInventoryColumns() {
        return 0;
    }

    @Override
    public @NotNull ActionResultType mobInteract(@NotNull PlayerEntity player, @NotNull Hand hand) {
        return ActionResultType.FAIL;
    }

    @Override
    public boolean isTamed() {
        return false;
    }

    public static boolean hasViewOfSky(@NotNull IWorld worldIn, @NotNull BlockPos pos) {
        return worldIn.canSeeSky(pos);
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
        Random rand = new Random();
        if (rand.nextInt(100) <= 20 * (looting + 1)) {
            this.spawnAtLocation(new ItemStack(ItemList.cursed_dust, 1));
        }
    }

    @Override
    public @NotNull ResourceLocation getDefaultLootTable() {
        return EntityType.LLAMA.getDefaultLootTable();
    }

    static class HurtByTargetGoal extends net.minecraft.entity.ai.goal.HurtByTargetGoal {
        public HurtByTargetGoal(InfectedLlamaEntity llama) {
            super(llama);
        }

        @Override
        public boolean canContinueToUse() {
            return true;
        }
    }
}