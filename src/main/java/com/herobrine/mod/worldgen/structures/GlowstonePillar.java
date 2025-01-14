package com.herobrine.mod.worldgen.structures;

import com.herobrine.mod.HerobrineMod;
import com.herobrine.mod.config.Config;
import com.herobrine.mod.util.savedata.SaveDataUtil;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Mod.EventBusSubscriber(modid = HerobrineMod.MODID)
public class GlowstonePillar {
    @SubscribeEvent
    public static void onBiomeLoad(@NotNull BiomeLoadingEvent event) {
        Feature<NoFeatureConfig> feature = new Feature<NoFeatureConfig>(NoFeatureConfig.CODEC) {
            @Override
            @SuppressWarnings("ConstantConditions")
            //suppresses passing null to argument annotated as NotNull for PlacementSettings.setChunk()
            public boolean place(@NotNull ISeedReader world, @NotNull ChunkGenerator generator, @NotNull Random random, @NotNull BlockPos pos, @NotNull NoFeatureConfig config) {
                int ci = (pos.getX() >> 4) << 4;
                int ck = (pos.getZ() >> 4) << 4;
                if ((random.nextInt(1000000) + 1) <= Config.COMMON.GlowstonePillarWeight.get()) {
                    int count = random.nextInt(1) + 1;
                    for (int a = 0; a < count; a++) {
                        int i = ci + random.nextInt(16);
                        int k = ck + random.nextInt(16);
                        int j = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, i, k);
                        j -= 1;
                        BlockState blockAt = world.getBlockState(new BlockPos(i, j, k));
                        boolean blockCriteria = !blockAt.isAir() && !BlockTags.FLOWERS.contains(blockAt.getBlock()) && !BlockTags.TALL_FLOWERS.contains(blockAt.getBlock()) && !BlockTags.SMALL_FLOWERS.contains(blockAt.getBlock()) && !BlockTags.LEAVES.contains(blockAt.getBlock()) && !blockAt.canBeReplaced(Fluids.WATER) && !blockAt.canBeReplaced(Fluids.LAVA) && !blockAt.canBeReplaced(Fluids.FLOWING_LAVA) && !blockAt.canBeReplaced(Fluids.FLOWING_WATER);
                        if (!blockCriteria)
                            continue;
                        BlockPos spawnTo = new BlockPos(i, j, k);
                        Template template = world.getLevel().getStructureManager().getOrCreate(new ResourceLocation(HerobrineMod.MODID, "glowstone_pillar"));
                        if (SaveDataUtil.canHerobrineSpawn(world.getLevel())) {
                            template.placeInWorld(world, spawnTo, new PlacementSettings().setRotation(Rotation.NONE).setRandom(random).setMirror(Mirror.NONE).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK).setChunkPos(null).setIgnoreEntities(false), random);
                        }
                    }
                }
                return true;
            }
        };
        event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> feature.configured(IFeatureConfig.NONE).decorated(Placement.NOPE.configured(IPlacementConfig.NONE)));
    }
}
