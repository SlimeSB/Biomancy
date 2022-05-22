package com.github.elenterius.biomancy.world.serum;

import com.github.elenterius.biomancy.mixin.ZombieVillagerMixinAccessor;
import com.github.elenterius.biomancy.world.entity.MobUtil;
import com.github.elenterius.biomancy.world.entity.fleshblob.FleshBlob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public class CleansingSerum extends Serum {

	public CleansingSerum(int color) {
		super(color);
	}

	@Override
	public void affectEntity(CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		clearPotionEffects(target);
		if (target instanceof FleshBlob fleshBlob) {
			fleshBlob.clearStoredDNA();
		}

		if (!target.level.isClientSide) {
			if (target instanceof ZombieVillager) {
				if (ForgeEventFactory.canLivingConvert(target, EntityType.VILLAGER, timer -> {})) {
					((ZombieVillagerMixinAccessor) target).biomancy_cureZombie((ServerLevel) target.level);
				}
			}
			else if (target instanceof WitherSkeleton skeleton) {
				MobUtil.convertMobTo((ServerLevel) target.level, skeleton, EntityType.SKELETON);
			}
		}
	}

	@Override
	public void affectPlayerSelf(CompoundTag tag, Player targetSelf) {
		clearPotionEffects(targetSelf);
	}

	private void clearPotionEffects(LivingEntity target) {
		if (!target.level.isClientSide) target.removeAllEffects();
	}

}
