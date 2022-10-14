package vin35.autoattack;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import vin35.autoattack.config.AutoAttackConfig;

public class Main implements ModInitializer {

    @Override
    public void onInitialize() {
        MidnightConfig.init("autoattack", AutoAttackConfig.class);
    }
}
