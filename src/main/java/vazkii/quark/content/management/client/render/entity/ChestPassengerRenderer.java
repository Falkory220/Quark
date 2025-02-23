package vazkii.quark.content.management.client.render.entity;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import vazkii.quark.content.management.entity.ChestPassenger;

/**
 * @author WireSegal
 * Created at 2:02 PM on 9/3/19.
 */
public class ChestPassengerRenderer extends EntityRenderer<ChestPassenger> {

    public ChestPassengerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    	public void render(ChestPassenger entity, float yaw, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int light) {
        if(!entity.isPassenger())
            return;

        Entity riding = entity.getVehicle();
        if (riding == null)
            return;

        Boat boat = (Boat) riding;
        super.render(entity, yaw, partialTicks, matrix, buffer, light);
        
        float rot = 180F - yaw;

        ItemStack stack = entity.getChestType();

        matrix.pushPose();
        matrix.translate(0, 0.375, 0);
        matrix.mulPose(Vector3f.YP.rotationDegrees(rot));
        float timeSinceHit = boat.getHurtTime() - partialTicks;
        float damageTaken = boat.getDamage() - partialTicks;

        if (damageTaken < 0.0F)
            damageTaken = 0.0F;

        if (timeSinceHit > 0.0F) {
        	double angle = Mth.sin(timeSinceHit) * timeSinceHit * damageTaken / 10.0F * boat.getHurtDir();
            matrix.mulPose(Vector3f.XP.rotationDegrees((float) angle));
        }

        float rock = boat.getBubbleAngle(partialTicks);
        if (!Mth.equal(rock, 0.0F)) {
        	 matrix.mulPose(Vector3f.XP.rotationDegrees(rock));
        }

        if (riding.getPassengers().size() > 1)
        	matrix.translate(0F, 0F, -0.6F);
        else
        	matrix.translate(0F, 0F, -0.45F);

        matrix.translate(0F, 0.7F - 0.375F, 0.6F - 0.15F);

        matrix.scale(1.75F, 1.75F, 1.75F);

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, TransformType.FIXED, light, OverlayTexture.NO_OVERLAY, matrix, buffer, 0);
        matrix.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(@Nonnull ChestPassenger entity) {
        return null;
    }

}
