package vin35.autoattack.config;

import eu.midnightdust.lib.config.MidnightConfig.Comment;
import eu.midnightdust.lib.config.MidnightConfig.Entry;
import vin35.autoattack.AutoAttack;

public class AutoAttackConfig extends AutoAttack {
    @Comment
    public static Comment text1;
    @Entry
    public static boolean preventsHittingBlocks = false;
    @Entry
    public static boolean preventsHittingBlocksSwords = true;
    @Entry
    public  static  boolean cleanCut = true;
}
