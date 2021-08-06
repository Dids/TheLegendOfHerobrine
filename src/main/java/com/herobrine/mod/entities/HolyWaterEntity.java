package com.herobrine.mod.entities;

import com.herobrine.mod.util.entities.EntityRegistry;
import com.herobrine.mod.util.items.ItemList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HolyWaterEntity extends SnowballEntity {
    public HolyWaterEntity(EntityType<? extends HolyWaterEntity> entityType, World world) {
        super(entityType, world);
    }

    public HolyWaterEntity(World worldIn, LivingEntity throwerIn) {
        super(worldIn, throwerIn);
    }

    @SuppressWarnings("unchecked")
    public HolyWaterEntity(World worldIn) {
        this((EntityType<? extends HolyWaterEntity>) EntityRegistry.HOLY_WATER_ENTITY, worldIn);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull ItemStack getItem() {
        return new ItemStack(ItemList.holy_water, 1);
    }

    @Override
    protected void onHit(@NotNull RayTraceResult result) {
        if (!this.level.isClientSide) {
            AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(1.0D, 1.0D, 1.0D);
            List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
            if (!list.isEmpty()) {
                for (LivingEntity entity : list) {
                    entity.removeAllEffects();
                    entity.addEffect(new EffectInstance(Effects.REGENERATION, 300, 1));
                    entity.addEffect(new EffectInstance(Effects.HEALTH_BOOST, 300, 1));
                    entity.clearFire();
                    if (entity instanceof AbstractInfectedEntity || entity instanceof InfectedLlamaEntity) {
                        entity.hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
                    }
                    if (entity instanceof AbstractHerobrineEntity) {
                        entity.hurt(DamageSource.thrown(this, this.getOwner()), 12.0F);
                    }
                    if (entity instanceof FakeHerobrineMageEntity) {
                        entity.remove();
                    }
                }
            }
            this.playSound(SoundEvents.GLASS_BREAK, 0.8F, 0.9F / (random.nextFloat() * 0.4F + 0.8F));
            this.level.broadcastEntityEvent(this, (byte) 3);
            this.remove();
        }
    }
}