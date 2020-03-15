package com.turtywurty.tutorialmod.tileentity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.turtywurty.tutorialmod.init.ModTileEntityTypes;
import com.turtywurty.tutorialmod.util.helpers.NBTHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class QuarryTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
	
	public final ItemStackHandler inventory = new ItemStackHandler(27) 
	{
		@Override
		public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) 
		{
			return true;
		}
		
		@Override
		protected void onContentsChanged(final int slot) 
		{
			super.onContentsChanged(slot);
			QuarryTileEntity.this.markDirty();
		}
	};
	
	private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal = LazyOptional.of(() -> this.inventory);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return super.getCapability(cap, side);
	}

	@Override
	public Container createMenu(int menu, PlayerInventory playerInv, PlayerEntity player) {
		return null;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}
	
	

	public int x, y, z, tick;
	boolean initialized = false;
	
	public QuarryTileEntity(final TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}
	
	public QuarryTileEntity() {
		this(ModTileEntityTypes.QUARRY.get());
	}
	
	@Override
	public void tick() {
		if(!initialized) init();
		tick++;
		if(tick == 40) {
			tick = 0;
			if(y > 2) execute();
		}
	}

	private void init() {
		initialized = true;
		x = this.pos.getX() - 1;
		y = this.pos.getY() - 1;
		z = this.pos.getZ() - 1;
		tick = 0;
	}

	private void execute() {
		int index = 0;
		Block[] blocksRemoved = new Block[9];
		for(int x = 0; x < 3; x++) {
			for(int z = 0; z < 3; z++) {
				BlockPos posToBreak = new BlockPos(this.x + x, this.y, this.z + z);
				blocksRemoved[index] = this.world.getBlockState(posToBreak).getBlock();
				destroyBlock(posToBreak, true, null);
				index++;
				
				this.markDirty();
			}
		}
		this.y--;
	}

	private boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity) {
		BlockState blockstate = world.getBlockState(pos);
		if(blockstate.isAir(world, pos)) return false;
		else {
			IFluidState ifluidstate = world.getFluidState(pos);
			world.playEvent(2001, pos, Block.getStateId(blockstate));
			if(dropBlock) {
				TileEntity tileentity = blockstate.hasTileEntity() ? world.getTileEntity(pos) : null;
				
				List<ItemStack> drops = Block.getDrops(blockstate, (ServerWorld)world, pos, tileentity);
				for(ItemStack stack : drops) {
					int slot = 0;
					for(int i = 0; i < this.inventory.getSlots(); i++) {
						if(this.inventory.isItemValid(i, stack)) slot = i;
					}
					this.inventory.insertItem(slot, stack, false);
				}
				
			}
			return world.setBlockState(pos, ifluidstate.getBlockState(), 3);
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("initvalues", NBTHelper.toNBT(this));
		return super.write(compound);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		CompoundNBT initValues = compound.getCompound("initvalues");
		if(initValues != null) {
			this.x = initValues.getInt("x");
			this.y = initValues.getInt("y");
			this.z = initValues.getInt("z");
			this.tick = 0;
			initialized = true;
			return;
		}
		init();
	}
}