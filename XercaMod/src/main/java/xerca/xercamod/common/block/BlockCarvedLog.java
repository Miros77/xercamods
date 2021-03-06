package xerca.xercamod.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCarvedLog extends HorizontalBlock {
    public BlockCarvedLog(String registryName, boolean isTransparent) {
        super(isTransparent ? Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(2.0F).sound(SoundType.WOOD).notSolid()
                : Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(2.0F).sound(SoundType.WOOD));
        this.setRegistryName(registryName);
    }
    public BlockCarvedLog(String registryName) {
        this(registryName, false);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 5;
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 5;
    }
}
