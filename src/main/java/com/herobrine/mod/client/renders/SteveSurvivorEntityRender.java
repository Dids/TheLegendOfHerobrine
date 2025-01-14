package com.herobrine.mod.client.renders;

import com.herobrine.mod.client.models.AbstractSurvivorEntityModel;
import com.herobrine.mod.entities.AbstractSurvivorEntity;
import com.herobrine.mod.entities.SteveSurvivorEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class SteveSurvivorEntityRender extends MobRenderer<AbstractSurvivorEntity, AbstractSurvivorEntityModel> {
    private SteveSurvivorEntityRender(EntityRendererManager manager) {
        super(manager, new AbstractSurvivorEntityModel(0.0F, false), 0.5F);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new BeeStingerLayer<>(this));
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull AbstractSurvivorEntity entity) {
        return entity.getSkin();
    }

    @Override
    protected void scale(@NotNull AbstractSurvivorEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
        matrixStackIn.scale(0.9375F, 0.9375F, 0.9375F);
        super.scale(entitylivingbaseIn, matrixStackIn, partialTickTime);
    }

    @SuppressWarnings("rawtypes")
    public static class RenderFactory implements IRenderFactory {
        @Override
        public EntityRenderer<? super SteveSurvivorEntity> createRenderFor(EntityRendererManager manager) {
            return new SteveSurvivorEntityRender(manager);
        }
    }
}
