package com.herobrine.mod.client.renders.layers;

import com.herobrine.mod.HerobrineMod;
import com.herobrine.mod.entities.InfectedMooshroomEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.AbstractEyesLayer;
import net.minecraft.client.renderer.entity.model.CowModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class InfectedMooshroomEyesLayer extends AbstractEyesLayer<InfectedMooshroomEntity, CowModel<InfectedMooshroomEntity>> {
    public InfectedMooshroomEyesLayer(IEntityRenderer<InfectedMooshroomEntity, CowModel<InfectedMooshroomEntity>> renderer) {
        super(renderer);
    }

    @Override
    public @NotNull RenderType renderType() {
        return RenderType.eyes(HerobrineMod.location("textures/entity/eyes/infected_mooshroom.png"));
    }
}