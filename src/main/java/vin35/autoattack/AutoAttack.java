package vin35.autoattack;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import vin35.autoattack.config.AutoAttackConfig;


public class AutoAttack implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	//public static final Logger LOGGER = LoggerFactory.getLogger("autoattack");
	private static KeyBinding keyBinding;

	@Override
	public void onInitializeClient() {
		//LOGGER.warn("Hello Fabric world!");

		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.autoattack.preventsHittingBlocks", // The translation key of the keybinding's name
				InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_H, // The keycode of the key
				"category.autoattack.autoattack" // The translation key of the keybinding's category.
		));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (keyBinding.wasPressed()) {
				if (AutoAttackConfig.preventsHittingBlocks) {
					mc.player.sendMessage(Text.literal("Prevents Hitting Blocks OFF!"));
					AutoAttackConfig.preventsHittingBlocks = false;
				}else{
					mc.player.sendMessage(Text.literal("Prevents Hitting Blocks ON!"));
					AutoAttackConfig.preventsHittingBlocks = true;
				}
			}

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
							if (result != null){
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
}
