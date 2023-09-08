package vin35.autoattack;

import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vin35.autoattack.config.AutoAttackConfig;
import vin35.autoattack.util.UpdateUtil;

public class AutoAttack implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("autoattack");
	public static String AUTOATTACK_VERSION;
	public static String SERVER_VERSION;
	public static String MINECRAFT_VERSION;
	public static Boolean UPDATE = false;

	@Override
	public void onInitializeClient() {
		checkUpdates();

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {

			//check update
			if (UPDATE && AutoAttackConfig.checkUpdate) {
				if (mc.player != null) {
					mc.player.sendMessage(Text.of("AutoAttack: new Update Detected! Version: " + SERVER_VERSION));
					UPDATE = false;
				}
			}

			//auto attack
			AutoMeleeTick(mc);

			//auto bow
			AutoBowTick(mc);
		});
	}

	private void checkUpdates(){
		LOGGER.info("Checking for updates...");
		ModContainer autoattack = FabricLoader.getInstance().getModContainer("autoattack")
				.orElseThrow(() -> new IllegalStateException("Couldn't find the mod container for autoattack"));

		AUTOATTACK_VERSION = String.valueOf(autoattack.getMetadata().getVersion());
		MINECRAFT_VERSION = UpdateUtil.getMinecraftVersion();

		JsonObject json = UpdateUtil.getJsonObject("https://raw.githubusercontent.com/vin350/AutoAttack/updates/updates.json");

		if(json == null){
			LOGGER.warn("There was an unexpected error while retrieving update data.");
			return;
		}

		var jsonObj = json.get(MINECRAFT_VERSION);
		if (jsonObj != null) {
			SERVER_VERSION = jsonObj.getAsJsonObject().get("latest").getAsString();
			UPDATE = UpdateUtil.compare(AUTOATTACK_VERSION, SERVER_VERSION) == -1;
		}
	}

	private void AutoMeleeTick(MinecraftClient mc){
		if ((!mc.options.attackKey.isPressed() && !AutoAttackConfig.afkAttack) || mc.player == null || mc.world == null || mc.interactionManager == null
				|| !(mc.player.getAttackCooldownProgress(0) >= 1)) {
			return;
		}

		if (mc.crosshairTarget == null) {
			if(AutoAttackConfig.alwaysAttack){
				mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
				mc.player.swingHand(Hand.MAIN_HAND);
			}
			return;
		}

		if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK && AutoAttackConfig.cleanCut) {
			BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
			BlockPos blockPos = blockHit.getBlockPos();
			BlockState blockState = mc.world.getBlockState(blockPos);

			if (blockState.getCollisionShape(mc.world, blockPos).isEmpty() || blockState.getHardness(mc.world, blockPos) == 0.0F) {
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

	private void AutoBowTick(MinecraftClient mc){
		if (mc.options.useKey.isPressed() && mc.player != null && mc.interactionManager != null) {
			ItemStack stack = mc.player.getActiveItem();
			Item item = stack.getItem();

			if (item == Items.BOW && AutoAttackConfig.autoBow) {
				float progress = BowItem.getPullProgress(stack.getMaxUseTime() - mc.player.getItemUseTimeLeft() - 1);
				if (progress >= 1.0F) {
					mc.interactionManager.stopUsingItem(mc.player);
				}
			}

			if (item == Items.CROSSBOW && AutoAttackConfig.autoCrossBow) {
				float pullTime = CrossbowItem.getPullTime(stack);
				float timeLeft = mc.player.getItemUseTimeLeft();
				float progress = (pullTime - timeLeft) / pullTime;
				if (progress >= 1.3F) {
					mc.interactionManager.stopUsingItem(mc.player);
					mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
				}
			}

			if (item == Items.TRIDENT && AutoAttackConfig.autoTrident) {
				float progress = (stack.getMaxUseTime() - mc.player.getItemUseTimeLeft()) / 10.0F;
				if (progress >= 1.3F) {
					mc.interactionManager.stopUsingItem(mc.player);
				}
			}
		}
	}
}
