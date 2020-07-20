package com.herobrine.mod.blocks;

import com.herobrine.mod.HerobrineMod;
import com.herobrine.mod.config.Config;
import com.herobrine.mod.util.blocks.ModBlockStates;
import com.herobrine.mod.util.blocks.ModMaterial;
import com.herobrine.mod.util.items.ItemList;
import com.herobrine.mod.util.savedata.Variables;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class HerobrineAlter extends Block {
    @GameRegistry.ObjectHolder(HerobrineMod.MODID + ":herobrine_alter")
    public static final Block block = new Block(ModMaterial.HEROBRINE_ALTER_MATERIAL);

    public HerobrineAlter() {
        super(ModMaterial.HEROBRINE_ALTER_MATERIAL, MapColor.RED);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setRegistryName(HerobrineMod.location("herobrine_alter"));
        this.setTranslationKey(HerobrineMod.MODID + "." + "herobrine_alter");
        this.setHardness(1.5f);
        this.setResistance(1.5f);
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ModBlockStates.TYPE, 0));
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {
        return this.getDefaultState().withProperty(ModBlockStates.TYPE, 0);
    }

    @Override
    public boolean hasComparatorInputOverride(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(@NotNull IBlockState blockState, @NotNull World worldIn, @NotNull BlockPos pos) {
        int i = blockState.getValue(ModBlockStates.TYPE);
        switch (i) {
            case 1:
                return 8;
            case 2:
                return 15;
        }
        return 0;
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(ModBlockStates.TYPE, meta);
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState state) {
        int i = 0;

        if (state.getValue(ModBlockStates.TYPE) == 1) {
            i |= 1;
        }

        if (state.getValue(ModBlockStates.TYPE) == 2) {
            i |= 2;
        }

        return i;
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ModBlockStates.TYPE);
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public int getLightValue(@NotNull IBlockState state) {
        int i = state.getValue(ModBlockStates.TYPE);
        switch (i) {
            case 1:
                return 8;
            case 2:
                return 15;
        }
        return 0;
    }

    @Override
    public boolean canSpawnInBlock() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@NotNull IBlockState stateIn, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull Random rand) {
        int i = stateIn.getValue(ModBlockStates.TYPE);
        if (i == 1) {
            double d0 = (double) pos.getX() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
            double d1 = (double) pos.getY() + 0.05F;
            double d2 = (double) pos.getZ() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
            worldIn.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    private boolean shrineAccepted(@NotNull BlockPos pos, World world) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if(!Config.AltarRequiresShrine) {
            return true;
        } else {
            return world.getBlockState(new BlockPos(x, y - 1, z)) == Blocks.NETHERRACK.getDefaultState() && world.getBlockState(new BlockPos(x, y - 1, z + 1)) == Blocks.GOLD_BLOCK.getDefaultState() && world.getBlockState(new BlockPos(x, y - 1, z - 1)) == Blocks.GOLD_BLOCK.getDefaultState() && world.getBlockState(new BlockPos(x + 1, y - 1, z)) == Blocks.GOLD_BLOCK.getDefaultState() && world.getBlockState(new BlockPos(x - 1, y - 1, z)) == Blocks.GOLD_BLOCK.getDefaultState() && world.getBlockState(new BlockPos(x + 1, y, z)) == Blocks.REDSTONE_TORCH.getDefaultState() && world.getBlockState(new BlockPos(x - 1, y, z)) == Blocks.REDSTONE_TORCH.getDefaultState() && world.getBlockState(new BlockPos(x, y, z + 1)) == Blocks.REDSTONE_TORCH.getDefaultState() && world.getBlockState(new BlockPos(x, y, z - 1)) == Blocks.REDSTONE_TORCH.getDefaultState() && world.getBlockState(new BlockPos(x - 1, y - 1, z - 1)) == Blocks.LAVA.getDefaultState() && world.getBlockState(new BlockPos(x + 1, y - 1, z + 1)) == Blocks.LAVA.getDefaultState() && world.getBlockState(new BlockPos(x + 1, y - 1, z - 1)) == Blocks.LAVA.getDefaultState() && world.getBlockState(new BlockPos(x - 1, y - 1, z + 1)) == Blocks.LAVA.getDefaultState();
        }
    }

    @Override
    public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        Variables.SaveData.get(world).syncData(world);
        ItemStack itemStack = player.getHeldItem(hand);
        if(this.shrineAccepted(pos, world)) {
            int i = state.getValue(ModBlockStates.TYPE);
            if(i == 0 && itemStack.getItem() == ItemList.cursed_diamond || i == 0 && itemStack.getItem() == ItemList.purified_diamond) {
                player.swingArm(hand);
                if(itemStack.getItem() == ItemList.cursed_diamond) {
                    if(!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    world.setBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), this.getDefaultState().withProperty(ModBlockStates.TYPE, 1), 3);
                    world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), false));
                    if(!Variables.SaveData.get(world).Spawn) {
                        if (world.isRemote) {
                            player.sendMessage(new TextComponentString("<Herobrine> You have no idea what you have done!"));
                        }
                        Variables.SaveData.get(world).Spawn = true;
                        Variables.SaveData.get(world).syncData(world);
                    }
                }
                if(itemStack.getItem() == ItemList.purified_diamond) {
                    if(!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    world.setBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), this.getDefaultState().withProperty(ModBlockStates.TYPE, 2), 3);
                    world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), false));
                    if(Variables.SaveData.get(world).Spawn) {
                        if (world.isRemote) {
                            player.sendMessage(new TextComponentString("<Herobrine> I shall return!"));
                        }
                        Variables.SaveData.get(world).Spawn = false;
                        Variables.SaveData.get(world).syncData(world);
                    }
                }
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }
}