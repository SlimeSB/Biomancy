package com.github.elenterius.biomancy.client.render.item.armor;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.armor.OverseerArmorItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class OverseerArmorRenderer extends GeoArmorRenderer<OverseerArmorItem> {

	public OverseerArmorRenderer() {
		super(new DefaultedItemGeoModel<>(BiomancyMod.createRL("armor/overseer_armor")));
	}

	@Override
	public RenderType getRenderType(OverseerArmorItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityCutout(texture);
	}

}