package com.github.elenterius.biomancy.inventory;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModContainerTypes;
import com.github.elenterius.biomancy.inventory.slot.OutputSlot;
import com.github.elenterius.biomancy.tileentity.ChewerTileEntity;
import com.github.elenterius.biomancy.tileentity.state.ChewerStateData;
import com.github.elenterius.biomancy.util.BiofuelUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.apache.logging.log4j.MarkerManager;

public class ChewerContainer extends ContainerWithPlayerInv {

	protected final SimpleInventory fuelInventory;
	protected final SimpleInventory emptyBucketInventory;
	protected final SimpleInventory inputInventory;
	protected final SimpleInventory outputInventory;
	private final ChewerStateData stateData;
	private final World world;

	private ChewerContainer(int screenId, PlayerInventory playerInventory, SimpleInventory fuelInventory, SimpleInventory emptyBucketInventory, SimpleInventory inputInventory, SimpleInventory outputInventory, ChewerStateData stateData) {
		super(ModContainerTypes.CHEWER.get(), screenId, playerInventory);
		world = playerInventory.player.level;

		this.fuelInventory = fuelInventory;
		this.emptyBucketInventory = emptyBucketInventory;
		this.inputInventory = inputInventory;
		this.outputInventory = outputInventory;
		this.stateData = stateData;

		addDataSlots(stateData);

		addSlot(new Slot(fuelInventory, 0, 17, 17) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return BiofuelUtil.isItemValidFuel(stack);
			}
		});
		addSlot(new OutputSlot(emptyBucketInventory, 0, 17, 44));

		addSlot(new Slot(inputInventory, 0, 71, 26));
		addSlot(new OutputSlot(outputInventory, 0, 107, 26));
	}

	public static ChewerContainer createServerContainer(int screenId, PlayerInventory playerInventory, SimpleInventory fuelInventory, SimpleInventory emptyBucketInventory, SimpleInventory inputInventory, SimpleInventory outputInventory, ChewerStateData stateData) {
		return new ChewerContainer(screenId, playerInventory, fuelInventory, emptyBucketInventory, inputInventory, outputInventory, stateData);
	}

	public static ChewerContainer createClientContainer(int screenId, PlayerInventory playerInventory, PacketBuffer extraData) {
		SimpleInventory fuelInventory = SimpleInventory.createClientContents(ChewerTileEntity.FUEL_SLOTS);
		SimpleInventory emptyBucketInventory = SimpleInventory.createClientContents(ChewerTileEntity.EMPTY_BUCKET_SLOTS);
		SimpleInventory inputInventory = SimpleInventory.createClientContents(ChewerTileEntity.INPUT_SLOTS);
		SimpleInventory outputInventory = SimpleInventory.createClientContents(ChewerTileEntity.OUTPUT_SLOTS);
		ChewerStateData stateData = new ChewerStateData();
		return new ChewerContainer(screenId, playerInventory, fuelInventory, emptyBucketInventory, inputInventory, outputInventory, stateData);
	}

	@Override
	public boolean stillValid(PlayerEntity playerIn) {
		//we don't check all three inventories because they all call the same method in the decomposer tile entity
		return inputInventory.stillValid(playerIn);
	}

	public float getCraftingProgressNormalized() {
		if (stateData.timeForCompletion == 0) return 0f;
		return MathHelper.clamp(stateData.timeElapsed / (float) stateData.timeForCompletion, 0f, 1f);
	}

	public FluidTank getFuelTank() {
		return stateData.fuelTank;
	}

	/**
	 * copied from: https://github.com/TheGreyGhost/MinecraftByExample/blob/1-16-3-final/src/main/java/minecraftbyexample/mbe31_inventory_furnace/ContainerFurnace.java
	 */
	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int sourceSlotIndex) {

		Slot sourceSlot = slots.get(sourceSlotIndex); // side-effect: throws error if the sourceSlotIndex is out of range (index < 0 || index >= size())
		if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
		ItemStack sourceStack = sourceSlot.getItem();
		ItemStack copyOfSourceStack = sourceStack.copy();

		boolean successfulTransfer = false;
		SlotZone sourceZone = SlotZone.getZoneFromIndex(sourceSlotIndex);

		switch (sourceZone) {
			case OUTPUT_ZONE:
				successfulTransfer = mergeInto(SlotZone.PLAYER_HOTBAR, sourceStack, true) || mergeInto(SlotZone.PLAYER_MAIN_INVENTORY, sourceStack, true);
				if (successfulTransfer) sourceSlot.onQuickCraft(sourceStack, copyOfSourceStack);
				break;

			case INPUT_ZONE:
			case FUEL_ZONE:
			case EMPTY_BUCKET_ZONE:
				successfulTransfer = mergeInto(SlotZone.PLAYER_MAIN_INVENTORY, sourceStack, false) || mergeInto(SlotZone.PLAYER_HOTBAR, sourceStack, false);
				break;

			case PLAYER_HOTBAR:
			case PLAYER_MAIN_INVENTORY:
				if (ChewerTileEntity.RECIPE_TYPE.getRecipeForItem(world, sourceStack).isPresent()) {
					successfulTransfer = mergeInto(SlotZone.INPUT_ZONE, sourceStack, false);
				}
				if (!successfulTransfer && BiofuelUtil.isItemValidFuel(sourceStack)) {
					successfulTransfer = mergeInto(SlotZone.FUEL_ZONE, sourceStack, true);
				}
				if (!successfulTransfer) {
					SlotZone targetZone = sourceZone == SlotZone.PLAYER_HOTBAR ? SlotZone.PLAYER_MAIN_INVENTORY : SlotZone.PLAYER_HOTBAR;
					successfulTransfer = mergeInto(targetZone, sourceStack, false);
				}
				break;

			default:
				throw new IllegalArgumentException("unexpected sourceZone:" + sourceZone);
		}

		if (!successfulTransfer) return ItemStack.EMPTY;

		if (sourceStack.isEmpty()) sourceSlot.set(ItemStack.EMPTY);
		else sourceSlot.setChanged();

		if (sourceStack.getCount() == copyOfSourceStack.getCount()) {
			BiomancyMod.LOGGER.warn(MarkerManager.getMarker(getClass().getSimpleName()), "Stack transfer failed in an unexpected way!");
			return ItemStack.EMPTY; //transfer error
		}

		sourceSlot.onTake(playerIn, sourceStack);
		return copyOfSourceStack;
	}

	private boolean mergeInto(SlotZone destinationZone, ItemStack sourceStack, boolean fillFromEnd) {
		return moveItemStackTo(sourceStack, destinationZone.firstIndex, destinationZone.lastIndexPlus1, fillFromEnd);
	}

	private enum SlotZone {
		PLAYER_HOTBAR(0, 9),
		PLAYER_MAIN_INVENTORY(PLAYER_HOTBAR.lastIndexPlus1, 3 * 9),
		FUEL_ZONE(PLAYER_MAIN_INVENTORY.lastIndexPlus1, ChewerTileEntity.FUEL_SLOTS),
		EMPTY_BUCKET_ZONE(FUEL_ZONE.lastIndexPlus1, ChewerTileEntity.EMPTY_BUCKET_SLOTS),
		INPUT_ZONE(EMPTY_BUCKET_ZONE.lastIndexPlus1, ChewerTileEntity.INPUT_SLOTS),
		OUTPUT_ZONE(INPUT_ZONE.lastIndexPlus1, ChewerTileEntity.OUTPUT_SLOTS);

		public final int firstIndex;
		public final int slotCount;
		public final int lastIndexPlus1;

		SlotZone(int firstIndex, int numberOfSlots) {
			this.firstIndex = firstIndex;
			this.slotCount = numberOfSlots;
			this.lastIndexPlus1 = firstIndex + numberOfSlots;
		}

		public static SlotZone getZoneFromIndex(int slotIndex) {
			for (SlotZone slotZone : SlotZone.values()) {
				if (slotIndex >= slotZone.firstIndex && slotIndex < slotZone.lastIndexPlus1) return slotZone;
			}
			throw new IndexOutOfBoundsException("Unexpected slotIndex");
		}
	}
}