package vin35.autoattack.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRenderMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "updateTargetedEntity", at = @At("TAIL"), cancellable = true)
    public void updateTargetedEntity(float tickDelta, CallbackInfo ci) {
        if (this.client.crosshairTarget != null && this.client.player != null && this.client.world != null) {
            if (this.client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) this.client.crosshairTarget;
                BlockPos blockPos = blockHit.getBlockPos();
                BlockState blockState = this.client.world.getBlockState(blockPos);

                if (blockState.getCollisionShape(this.client.world, blockPos).isEmpty() || blockState.getHardness(this.client.world, blockPos) == 0.0F) {
                    float reach = this.client.interactionManager.getReachDistance();
                    Vec3d camera = this.client.player.getCameraPosVec(1.0F);
                    Vec3d rotation = this.client.player.getRotationVec(1.0F);
                    Vec3d end = camera.add(rotation.x * reach, rotation.y * reach, rotation.z * reach);
                    EntityHitResult result = ProjectileUtil.raycast(this.client.player, camera, end, new Box(camera, end), e -> !e.isSpectator() && e.isAttackable(), reach * reach);
                    if (result != null && result.getEntity().isAlive()){
                        this.client.targetedEntity = result.getEntity();
                    }
                }
            }
        }
    }
}
