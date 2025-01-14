package com.herobrine.mod.entities;

import com.google.common.collect.Sets;
import com.herobrine.mod.util.entities.SurvivorTrades;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class AbstractSurvivorEntity extends CreatureEntity implements IMerchant, INPC {
    protected AbstractSurvivorEntity(EntityType<? extends AbstractSurvivorEntity> type, World world) {
        super(type, world);
        this.xpReward = 5;
    }

    @Nullable
    private PlayerEntity customer;
    @Nullable
    protected MerchantOffers offers;
    private final Inventory survivorInventory = new Inventory(27);
    private int healTimer = 80;

    //Initializes string for saving texture resource location to nbt data.
    public String textureLocation;

    WaterAvoidingRandomWalkingGoal wanderGoal = new WaterAvoidingRandomWalkingGoal(this, 0.8D);

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, MonsterEntity.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, SlimeEntity.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, InfectedLlamaEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractHerobrineEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractIllagerEntity.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractRaiderEntity.class, true));
        this.targetSelector.addGoal(6, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(7, new LookAtCustomerGoal(this));
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 64.0F));
        this.goalSelector.addGoal(9, new LookAtGoal(this, AbstractSurvivorEntity.class, 64.0F));
        this.goalSelector.addGoal(10, new LookAtGoal(this, MonsterEntity.class, 64.0F));
        this.goalSelector.addGoal(10, new LookAtGoal(this, SlimeEntity.class, 64.0F));
        this.goalSelector.addGoal(10, new LookAtGoal(this, InfectedLlamaEntity.class, 64.0F));
        this.goalSelector.addGoal(11, new LookAtGoal(this, AbstractHerobrineEntity.class, 64.0F));
        this.goalSelector.addGoal(12, new LookAtGoal(this, GolemEntity.class, 64.0F));
        this.goalSelector.addGoal(13, new LookAtGoal(this, AbstractVillagerEntity.class, 64.0F));
        this.goalSelector.addGoal(14, new LookAtGoal(this, AbstractIllagerEntity.class, 64.0F));
        this.goalSelector.addGoal(15, new LookAtGoal(this, AbstractRaiderEntity.class, 64.0F));
        this.goalSelector.addGoal(16, new LookRandomlyGoal(this));
    }

    @Override
    public void customServerAiStep() {
        if (!this.hasNoCustomer()) {
            this.goalSelector.removeGoal(this.wanderGoal);
        }

        if (this.hasNoCustomer()) {
            this.goalSelector.addGoal(17, this.wanderGoal);
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entityIn) {
        boolean flag = super.doHurtTarget(entityIn);
        if (flag) {
            float f = this.level.getCurrentDifficultyAt(this.getOnPos()).getEffectiveDifficulty();
            if (this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
                entityIn.setSecondsOnFire(2 * (int) f);
            }
        }
        return flag;
    }

    @Override
    public ILivingEntityData finalizeSpawn(@NotNull IServerWorld worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        this.setPersistenceRequired();
        this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setItemSlot(EquipmentSlotType.OFFHAND, new ItemStack(Items.SHIELD));
        this.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(Items.IRON_HELMET));
        this.setItemSlot(EquipmentSlotType.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        this.setItemSlot(EquipmentSlotType.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        this.setItemSlot(EquipmentSlotType.FEET, new ItemStack(Items.IRON_BOOTS));
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundNBT compound) {
        super.addAdditionalSaveData(compound);

        //Registers writing the texture location string to nbt.
        compound.putString("textureLocation", textureLocation);

        compound.putInt("RegenSpeed", this.healTimer);
        MerchantOffers merchantoffers = this.getOffers();
        if (!merchantoffers.isEmpty()) {
            compound.put("Offers", merchantoffers.createTag());
        }

        ListNBT listnbt = new ListNBT();

        for (int i = 0; i < this.survivorInventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.survivorInventory.getItem(i);
            if (!itemstack.isEmpty()) {
                listnbt.add(itemstack.save(new CompoundNBT()));
            }
        }

        compound.put("Inventory", listnbt);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundNBT compound) {
        super.readAdditionalSaveData(compound);

        //Registers reading the texture location string from nbt.
        this.textureLocation = compound.getString("textureLocation");

        this.healTimer = compound.getInt("RegenSpeed");
        if (compound.contains("Offers", 10)) {
            this.offers = new MerchantOffers(compound.getCompound("Offers"));
        }

        ListNBT listnbt = compound.getList("Inventory", 10);

        for (int i = 0; i < listnbt.size(); ++i) {
            ItemStack itemstack = ItemStack.of(listnbt.getCompound(i));
            if (!itemstack.isEmpty()) {
                this.survivorInventory.addItem(itemstack);
            }
        }
    }

    protected void resetCustomer() {
        this.setTradingPlayer(null);
    }

    //Placeholder to allow each renderer to properly reference the getSkin function. Overridden in the specific survivor's class file.
    public ResourceLocation getSkin() {
        return null;
    }

    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        this.resetCustomer();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
        if (this.getTarget() != null && !this.hasNoCustomer()) {
            this.resetCustomer();
        }
        if (this.isAlive() && this.getHealth() < this.getMaxHealth()) {
            if (this.healTimer < 1 && this.getHealth() < this.getMaxHealth()) {
                this.healTimer = 80;
                this.heal(1.0F);
            }
            if (this.healTimer > 80) {
                this.healTimer = 80;
            }
            --this.healTimer;
            this.customServerAiStep();
        }

        //Makes every entity that extends MonsterEntity attack Survivors. This is to allow any vanilla or modded monster to properly recognise the survivor as an enemy before being attacked. There is an exception for neutral mobs and Herobrine Stalkers because of how they interact with players.
        AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(64.0D, 64.0D, 64.0D);
        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
        if (!list.isEmpty()) {
            for (LivingEntity entity : list) {
                if (entity instanceof MonsterEntity && ((MonsterEntity) entity).getTarget() == null && !(entity instanceof IAngerable) && !(entity instanceof HerobrineStalkerEntity) && this.canSee(entity)) {
                    ((MonsterEntity) entity).setTarget(this);
                }
                if (entity instanceof SlimeEntity && ((SlimeEntity) entity).getTarget() == null && this.canSee(entity)) {
                    ((SlimeEntity) entity).setTarget(this);
                }
            }
        }
    }

    @Nullable
    @Override
    public PlayerEntity getTradingPlayer() {
        return this.customer;
    }

    public boolean hasNoCustomer() {
        return this.customer == null;
    }

    @Override
    public void setTradingPlayer(@Nullable PlayerEntity player) {
        this.customer = player;
    }

    @Override
    public @NotNull ActionResultType mobInteract(@NotNull PlayerEntity player, @NotNull Hand hand) {
        if (this.isAlive() && this.hasNoCustomer()) {
            if (this.getOffers().isEmpty()) {
                return super.mobInteract(player, hand);
            } else {
                if (!this.level.isClientSide) {
                    this.setTradingPlayer(player);
                    this.openTradingScreen(player, this.getDisplayName(), -1);
                }

                return ActionResultType.SUCCESS;
            }
        } else {
            return super.mobInteract(player, hand);
        }
    }

    @Override
    public void overrideOffers(@org.jetbrains.annotations.Nullable MerchantOffers p_213703_1_) {
    }

    protected void populateTradeData() {
        SurvivorTrades.ITrade[] asurvivortrades$itrade = SurvivorTrades.SURVIVOR_TRADES.get(1);
        SurvivorTrades.ITrade[] asurvivortrades$itrade1 = SurvivorTrades.SURVIVOR_TRADES.get(2);
        if (asurvivortrades$itrade != null && asurvivortrades$itrade1 != null) {
            MerchantOffers merchantoffers = this.getOffers();
            this.addTrades(merchantoffers, asurvivortrades$itrade);
            this.addTrades(merchantoffers, asurvivortrades$itrade1);
        }
    }

    @Override
    public @NotNull MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.populateTradeData();
        }
        return this.offers;
    }

    @Override
    public void notifyTrade(@NotNull MerchantOffer offer) {
        offer.resetUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.onSurvivorTrade(offer);
    }

    @Override
    public void notifyTradeUpdated(@NotNull ItemStack p_110297_1_) {
    }

    @Override
    public @NotNull World getLevel() {
        return this.level;
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int p_213702_1_) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public @NotNull SoundEvent getNotifyTradeSound() {
        return null;
    }

    protected void onSurvivorTrade(@NotNull MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            int i = 3 + this.random.nextInt(4);
            this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));
        }
    }

    protected void addTrades(MerchantOffers givenMerchantOffers, SurvivorTrades.ITrade @NotNull [] newTrades) {
        Set<Integer> set = Sets.newHashSet();
        if (newTrades.length > 64) {
            while(set.size() < 64) {
                set.add(this.random.nextInt(newTrades.length));
            }
        } else {
            for(int i = 0; i < newTrades.length; ++i) {
                set.add(i);
            }
        }
        for (Integer integer : set) {
            SurvivorTrades.ITrade survivortrades$itrade = newTrades[integer];
            MerchantOffer merchantoffer = survivortrades$itrade.getOffer(this, this.random);
            if (merchantoffer != null) {
                givenMerchantOffers.add(merchantoffer);
            }
        }
    }

    protected static class LookAtCustomerGoal extends LookAtGoal {
        private final AbstractSurvivorEntity survivorEntity;

        public LookAtCustomerGoal(AbstractSurvivorEntity survivorIn) {
            super(survivorIn, PlayerEntity.class, 8.0F);
            this.survivorEntity = survivorIn;
        }

        @Override
        public boolean canUse() {
            if (!this.survivorEntity.hasNoCustomer()) {
                this.lookAt = this.survivorEntity.getTradingPlayer();
            }
            return !this.survivorEntity.hasNoCustomer();
        }
    }
}