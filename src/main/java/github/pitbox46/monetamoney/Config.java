package github.pitbox46.monetamoney;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final String CATEGORY_GENERAL = "general";

    public static ForgeConfigSpec SERVER_CONFIG;

    public static ForgeConfigSpec.BooleanValue RECIPES_ARE_ADVANCEMENTS;
    public static ForgeConfigSpec.DoubleValue KILL_MONEY;
    public static ForgeConfigSpec.LongValue INITIAL_BAL;
    public static ForgeConfigSpec.LongValue DAILY_REWARD;
    public static ForgeConfigSpec.LongValue BASE_CHUNKLOADER;
    public static ForgeConfigSpec.DoubleValue MULTIPLIER_CHUNKLOADER;
    public static ForgeConfigSpec.LongValue OVERDRAFT_FEE;
    public static ForgeConfigSpec.LongValue ADVANCEMENT_REWARD;
    public static ForgeConfigSpec.LongValue LIST_FEE;
    public static ForgeConfigSpec.DoubleValue MULTIPILER_LIST;
    public static ForgeConfigSpec.LongValue DAILY_LIST_FEE;
    public static ForgeConfigSpec.DoubleValue MULTIPILER_DAILY_LIST;
    public static ForgeConfigSpec.LongValue RESTOCK_TIME;

    static {
        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

        SERVER_BUILDER.comment("General Settings").push(CATEGORY_GENERAL);

        RECIPES_ARE_ADVANCEMENTS = SERVER_BUILDER.comment("Recipes count as advancements")
                .define("recipe_advance", false);
        INITIAL_BAL = SERVER_BUILDER.comment("Initial player balance")
                .defineInRange("init_bal", 1000, 0, Long.MAX_VALUE);
        KILL_MONEY = SERVER_BUILDER.comment("Percent of account balance that is transferred on kill if the killed player has no physical money on person")
                .defineInRange("kill_money", 0.1d, 0, 1);
        DAILY_REWARD = SERVER_BUILDER.comment("Reward amount for players logging in every 24 hours")
                .defineInRange("daily_reward", 100, 0, Long.MAX_VALUE);
        ADVANCEMENT_REWARD = SERVER_BUILDER.comment("Reward that a player gets per advancement")
                .defineInRange("advancement_reward", 100, 0, Long.MAX_VALUE);
        BASE_CHUNKLOADER = SERVER_BUILDER.comment("Base chunk loader cost per 24 hours")
                .defineInRange("base_chunkloader", 100, 0, Long.MAX_VALUE);
        MULTIPLIER_CHUNKLOADER = SERVER_BUILDER.comment("The amount that chunk loader cost scales by. " +
                "The cost for a new loader is this multiplier multiplied by the previous cost")
                .defineInRange("multi_chunkloader", 1.1d, 0, Double.MAX_VALUE);
        OVERDRAFT_FEE = SERVER_BUILDER.comment("If a chunk for some reason is still force loaded when a team has no money, an overdraft fee is incurred")
                .defineInRange("overdraft_fee", 100, 0, Long.MAX_VALUE);
        LIST_FEE = SERVER_BUILDER.comment("Fee incurred for listing an item")
                .defineInRange("list_fee", 100, 0, Long.MAX_VALUE);
        MULTIPILER_LIST = SERVER_BUILDER.comment("The amount that initial listing fee cost scales by. " +
                "The cost for a listing is this multiplier multiplied by the previous cost")
                .defineInRange("multi_chunkloader", 1.1d, 0, Double.MAX_VALUE);
        DAILY_LIST_FEE = SERVER_BUILDER.comment("Fee incurred for listing an item")
                .defineInRange("daily_list_fee", 100, 0, Long.MAX_VALUE);
        MULTIPILER_DAILY_LIST = SERVER_BUILDER.comment("The amount that daily listing fee cost scales by. " +
                "The cost for the daily listing fee is this multiplier multiplied by the previous cost")
                .defineInRange("multi_chunkloader", 1.1d, 0, Double.MAX_VALUE);
        RESTOCK_TIME = SERVER_BUILDER.comment("Time in between shop restocks in MINUTES")
                .defineInRange("restock_time", 60 * 24, 1, Long.MAX_VALUE);

        SERVER_BUILDER.pop();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }
}
