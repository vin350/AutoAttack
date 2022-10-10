package vin35.autoattack;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import vin35.autoattack.config.AutoAttackConfig;
import vin35.autoattack.mixin.InGameHudMixin;


public class AutoAttack implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	//public static final Logger LOGGER = LoggerFactory.getLogger("autoattack");

	static boolean isAttackable = false;

	@Override
	public void onInitializeClient() {
		//LOGGER.warn("Hello Fabric world!");

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			//isAttackable
			if (mc.player != null && mc.crosshairTarget != null) {
				if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK && AutoAttackConfig.cleanCut) {
					BlockPos blockPos = new BlockPos(mc.crosshairTarget.getPos().x, mc.crosshairTarget.getPos().y, mc.crosshairTarget.getPos().z);
					BlockState state = mc.world.getBlockState(blockPos);

					if (state.getCollisionShape(mc.world, blockPos).isEmpty() || state.getHardness(mc.world, blockPos) == 0.0F) {
						float reach = mc.interactionManager.getReachDistance();
						Vec3d camera = mc.player.getCameraPosVec(1.0F);
						Vec3d rotation = mc.player.getRotationVec(1.0F);
						Vec3d end = camera.add(rotation.x * reach, rotation.y * reach, rotation.z * reach);
						EntityHitResult result = ProjectileUtil.raycast(mc.player, camera, end, new Box(camera, end), e -> !e.isSpectator() && e.isAttackable(), reach * reach);
						if (result != null && result.getEntity().isAlive()){
							isAttackable = true;
						}else isAttackable = false;
					}else isAttackable = false;
				}else isAttackable = false;
			}

			//auto attack
			if (mc.options.attackKey.isPressed() && mc.player != null
					&& mc.player.getAttackCooldownProgress(0) >= 1) {
				if (mc.crosshairTarget != null) {
					if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK && AutoAttackConfig.cleanCut) {
						BlockPos blockPos = new BlockPos(mc.crosshairTarget.getPos().x, mc.crosshairTarget.getPos().y, mc.crosshairTarget.getPos().z);
						BlockState state = mc.world.getBlockState(blockPos);

						if (state.getCollisionShape(mc.world, blockPos).isEmpty() || state.getHardness(mc.world, blockPos) == 0.0F) {
							float reach = mc.interactionManager.getReachDistance();
							Vec3d camera = mc.player.getCameraPosVec(1.0F);
							Vec3d rotation = mc.player.getRotationVec(1.0F);
							Vec3d end = camera.add(rotation.x * reach, rotation.y * reach, rotation.z * reach);
							EntityHitResult result = ProjectileUtil.raycast(mc.player, camera, end, new Box(camera, end), e -> !e.isSpectator() && e.isAttackable(), reach * reach);
							if (result != null && result.getEntity().isAlive()){
								mc.interactionManager.attackEntity(mc.player, result.getEntity());
								mc.player.swingHand(Hand.MAIN_HAND);
							}
						}
					} else if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
						Entity entity = ((EntityHitResult) mc.crosshairTarget).getEntity();
						if (entity.isAlive() && entity.isAttackable()) {
							mc.interactionManager.attackEntity(mc.player, entity);
							mc.player.swingHand(Hand.MAIN_HAND);
						}
					}
				}
			}
		});
	}

	public static float getProgress(float weaponProgress) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		ItemStack mainHand = player.getMainHandStack();
		ItemStack offHand = player.getOffHandStack();

		if (weaponProgress < 1.0F)
			return weaponProgress;

		if (isAttackable) {
			return 2.0F;
		}

		return 1.0F;
	}
}
