package xerca.xercamod.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class BlockRope extends SixWayBlock {
    protected BlockRope() {
        super(0.125F, Properties.create(Material.WOOL).notSolid().sound(SoundType.CLOTH));
        this.setDefaultState(this.stateContainer.getBaseState().with(NORTH, Boolean.FALSE).with(EAST, false).with(SOUTH, Boolean.FALSE).with(WEST, Boolean.FALSE).with(UP, Boolean.FALSE).with(DOWN, Boolean.FALSE));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.makeConnections(context.getWorld(), context.getPos());
    }

    boolean isConnectable(BlockState bs){
        return bs.isSolid() || bs.getBlock() == this;
    }

    public BlockState makeConnections(IBlockReader blockReader, BlockPos pos) {
        BlockState downBlock = blockReader.getBlockState(pos.down());
        BlockState upBlock = blockReader.getBlockState(pos.up());
        BlockState northBlock = blockReader.getBlockState(pos.north());
        BlockState eastBlock = blockReader.getBlockState(pos.east());
        BlockState southBlock = blockReader.getBlockState(pos.south());
        BlockState westBlock = blockReader.getBlockState(pos.west());
        return this.getDefaultState().with(DOWN, downBlock.isSolid()).
                with(UP, isConnectable(upBlock)).
                with(NORTH, isConnectable(northBlock)).
                with(EAST, isConnectable(eastBlock)).
                with(SOUTH, isConnectable(southBlock)).
                with(WEST, isConnectable(westBlock));
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     */
    @Override
    @ParametersAreNonnullByDefault
    @Nonnull
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!stateIn.isValidPosition(worldIn, currentPos)) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
            return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        } else {
            return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), isConnectable(facingState));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }


    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return true;
    }
}
