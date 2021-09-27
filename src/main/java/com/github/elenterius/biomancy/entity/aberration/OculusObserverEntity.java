package com.github.elenterius.biomancy.entity.aberration;

import com.github.elenterius.biomancy.entity.ai.goal.FleeFromAttackerFlyGoal;
import com.github.elenterius.biomancy.entity.ai.goal.RandomFlyGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.ITag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class OculusObserverEntity extends CreatureEntity implements IFlyingAnimal {

	private int underWaterTicks;

	public OculusObserverEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
		super(type, worldIn);
		moveControl = new FlyingMovementController(this, 20 /* max pitch? */, true);
		setPathfindingMalus(PathNodeType.DANGER_FIRE, -1f);
		setPathfindingMalus(PathNodeType.WATER, -1f);
		setPathfindingMalus(PathNodeType.WATER_BORDER, 16f);
		setPathfindingMalus(PathNodeType.FENCE, -1f);
	}

	public static AttributeModifierMap.MutableAttribute createAttributes() {
		return MobEntity.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 12d)
				.add(Attributes.FLYING_SPEED, 0.6d)
				.add(Attributes.MOVEMENT_SPEED, 0.3d)
				.add(Attributes.FOLLOW_RANGE, 32d)
				.add(Attributes.ATTACK_DAMAGE, 2d)
				.add(Attributes.ARMOR, 15d) //2 + 5 + 6 + 2 = equal to full iron armor
				.add(Attributes.ARMOR_TOUGHNESS, 0.5d);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new SwimGoal(this));
		goalSelector.addGoal(2, new LookAtGoal(this, PlayerEntity.class, 8f));
		goalSelector.addGoal(3, new LookRandomlyGoal(this));
		goalSelector.addGoal(4, new RandomFlyGoal(this, 1d));
		goalSelector.addGoal(5, new FleeFromAttackerFlyGoal(this, 3d));
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, IWorldReader worldIn) {
		return worldIn.getBlockState(pos).isAir() ? 10f : 0f;
	}

	@Override
	protected PathNavigator createNavigation(World worldIn) {
		FlyingPathNavigator pathNavigator = new FlyingPathNavigator(this, worldIn);
		pathNavigator.setCanOpenDoors(false);
		pathNavigator.setCanFloat(false);
		pathNavigator.setCanPassDoors(true);
		return pathNavigator;
	}

	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
		//prevent updating of fallDistance and stuff related to falling
	}

	@Override
	protected void jumpInLiquid(ITag<Fluid> fluidTag) {
		setDeltaMovement(getDeltaMovement().add(0d, 0.01d, 0d));
	}

	@Override
	protected void customServerAiStep() {
		if (isInWaterOrBubble()) ++underWaterTicks;
		else underWaterTicks = 0;
		if (underWaterTicks > 20) hurt(DamageSource.DROWN, 1f);
	}

	@Override
	protected boolean makeFlySound() {
		return true;
	}

	@Override
	protected float playFlySound(float volume) {
		playSound(SoundEvents.PARROT_FLY, 0.15f, 0.8f);
		return volume;
	}

	@Override
	protected float getVoicePitch() {
		return super.getVoicePitch() - 1f;
	}

	@Override
	protected float getSoundVolume() {
		return 0.5F;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		//prevent step sound effect
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

}