package vin35.autoattack.config;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import vin35.autoattack.AutoAttack;

public class KeyBindingConfig extends AutoAttack {

    private static KeyBinding preventsHittingBlocksKeyBinding;
    private static KeyBinding afkAttackKeyBinding;

    @Override
    public void onInitializeClient() {
        preventsHittingBlocksKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoattack.preventsHittingBlocks", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_H, // The keycode of the key
                "category.autoattack.autoattack" // The translation key of the keybinding's category.
        ));
        afkAttackKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoattack.afkAttack", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_K, // The keycode of the key
                "category.autoattack.autoattack" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (preventsHittingBlocksKeyBinding.wasPressed() && mc.player != null) {
                if (AutoAttackConfig.preventsHittingBlocks) {
                    //mc.player.sendMessage(Text.literal("Prevents Hitting Blocks OFF!"));
                    mc.player.sendMessage(Text.translatable("text.KeyBindingConfig.preventsHittingBlocks.OFF"));
                    AutoAttackConfig.preventsHittingBlocks = false;
                } else {
                    //mc.player.sendMessage(Text.literal("Prevents Hitting Blocks ON!"));
                    mc.player.sendMessage(Text.translatable("text.KeyBindingConfig.preventsHittingBlocks.ON"));
                    AutoAttackConfig.preventsHittingBlocks = true;
                }
            }
            if (afkAttackKeyBinding.wasPressed() && mc.player != null){
                if (AutoAttackConfig.afkAttack) {
                    mc.player.sendMessage(Text.translatable("text.KeyBindingConfig.afkAttack.OFF"));
                    AutoAttackConfig.afkAttack = false;
                } else {
                    mc.player.sendMessage(Text.translatable("text.KeyBindingConfig.afkAttack.ON"));
                    AutoAttackConfig.afkAttack = true;
                }
            }
        });
    }
}
