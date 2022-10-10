package vin35.autoattack.config;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import vin35.autoattack.AutoAttack;

public class KeyBindingConfig extends AutoAttack {

    private static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
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
                } else {
                    mc.player.sendMessage(Text.literal("Prevents Hitting Blocks ON!"));
                    AutoAttackConfig.preventsHittingBlocks = true;
                }
            }
        });
    }
}
