package com.github.elenterius.biomancy.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;

import java.util.Optional;

public final class BlockPropertyUtil {

	public static final ImmutableMap<IntegerProperty, Integer> maxAgeMappings;
	public static final String AGE_PROPERTY = "age";

	static {
		//we can't know if someone might have manipulated the properties, so we search for the max value
		maxAgeMappings = ImmutableMap.<IntegerProperty, Integer>builder()
				.put(BlockStateProperties.AGE_1, BlockStateProperties.AGE_1.getPossibleValues().stream().max(Integer::compareTo).orElse(1))
				.put(BlockStateProperties.AGE_3, BlockStateProperties.AGE_3.getPossibleValues().stream().max(Integer::compareTo).orElse(3))
				.put(BlockStateProperties.AGE_5, BlockStateProperties.AGE_5.getPossibleValues().stream().max(Integer::compareTo).orElse(5))
				.put(BlockStateProperties.AGE_7, BlockStateProperties.AGE_7.getPossibleValues().stream().max(Integer::compareTo).orElse(7))
				.put(BlockStateProperties.AGE_15, BlockStateProperties.AGE_15.getPossibleValues().stream().max(Integer::compareTo).orElse(15))
				.put(BlockStateProperties.AGE_25, BlockStateProperties.AGE_25.getPossibleValues().stream().max(Integer::compareTo).orElse(25))
				.build();
	}

	private BlockPropertyUtil() {}

	public static int getMaxAge(IntegerProperty property) {
		if (maxAgeMappings.containsKey(property)) {
			return maxAgeMappings.get(property);
		}
		else {
			//we can't use the last element of the collection since the order of the underlying HashSet is not guaranteed to remain constant
			return property.getPossibleValues().stream().max(Integer::compareTo).orElse(0);
		}
	}

	public static Optional<IntegerProperty> getAgeProperty(BlockState state) {
		if (state.getBlock() instanceof CropsBlock) {
			return Optional.of(((CropsBlock) state.getBlock()).getAgeProperty());
		}
		for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equals(AGE_PROPERTY) && prop instanceof IntegerProperty) {
				return Optional.of((IntegerProperty) prop);
			}
		}
		return Optional.empty();
	}

	public static int getAge(BlockState state) {
		return getAgeProperty(state).map(state::getValue).orElse(0);
	}

	public static Optional<int[]> getCurrentAgeAndMaxAge(BlockState state) {
		if (state.getBlock() instanceof CropsBlock) {
			CropsBlock block = (CropsBlock) state.getBlock();
			return Optional.of(new int[]{state.getValue(block.getAgeProperty()), block.getMaxAge()});
		}

		for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equals(AGE_PROPERTY) && prop instanceof IntegerProperty) {
				IntegerProperty ageProperty = (IntegerProperty) prop;
				return Optional.of(new int[]{state.getValue(ageProperty), getMaxAge(ageProperty)});
			}
		}

		return Optional.empty();
	}
}