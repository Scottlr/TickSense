package com.ticksense.activities;

import com.ticksense.activities.araxxor.AraxxorModule;
import com.ticksense.activities.construction.ConstructionModule;
import com.ticksense.activities.gemmining.GemMiningModule;
import com.ticksense.activities.hunllef.CorruptedGauntletModule;
import com.ticksense.activities.hunllef.HunllefModule;
import com.ticksense.activities.inferno.InfernoModule;
import com.ticksense.activities.phantommuspah.PhantomMuspahModule;
import com.ticksense.activities.planned.PlannedActivityModule;
import com.ticksense.activities.scurrius.ScurriusModule;
import com.ticksense.activities.vardorvis.VardorvisModule;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.common.ImmutableCollections;
import com.ticksense.core.ActivityType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ActivityModuleCatalog
{
    private ActivityModuleCatalog()
    {
    }

    public static List<ActivityModule> productionModules()
    {
        return productionModules(ActivitySupportConfig.current());
    }

    public static List<ActivityModule> productionModules(ActivitySupportConfig supportConfig)
    {
        final ActivitySupportConfig normalizedSupportConfig = Objects.requireNonNull(supportConfig, "supportConfig");
        final List<ActivityModule> modules = new ArrayList<>();
        for (ActivityModule module : availableModules())
        {
            if (normalizedSupportConfig.isActivitySupported(module.definition().getActivityType()))
            {
                modules.add(module);
            }
        }
        return ImmutableCollections.immutableList(modules);
    }

    public static List<ActivityModule> availableModules()
    {
        final List<ActivityModule> modules = new ArrayList<>();
        modules.add(new GemMiningModule());
        modules.add(new ConstructionModule());
        modules.add(new AraxxorModule());
        modules.add(new VardorvisModule());
        modules.add(new InfernoModule());
        modules.add(new ScurriusModule());
        modules.add(new PhantomMuspahModule());
        modules.add(new HunllefModule());
        modules.add(new CorruptedGauntletModule());
        modules.addAll(plannedModules());
        return ImmutableCollections.immutableList(modules);
    }

    private static List<ActivityModule> plannedModules()
    {
        return List.of(
            planned(ActivityType.BARROWS, "Barrows", 15, true),
            planned(ActivityType.GAUNTLET_PREP, "Gauntlet Prep", 15, false),
            planned(ActivityType.FIGHT_CAVES, "Fight Caves", 20, true),
            planned(ActivityType.FORTIS_COLOSSEUM, "Fortis Colosseum", 25, true),
            planned(ActivityType.TEMPOROSS, "Tempoross", 20, true),
            planned(ActivityType.WINTERTODT, "Wintertodt", 20, true),
            planned(ActivityType.ZALCANO, "Zalcano", 20, true),
            planned(ActivityType.GUARDIANS_OF_THE_RIFT, "Guardians of the Rift", 20, false),
            planned(ActivityType.MAHOGANY_HOMES, "Mahogany Homes", 15, false),
            planned(ActivityType.GIANTS_FOUNDRY, "Giants' Foundry", 15, false),
            planned(ActivityType.MASTERING_MIXOLOGY, "Mastering Mixology", 15, false),
            planned(ActivityType.HUNTER_RUMOURS, "Hunter Rumours", 15, false),
            planned(ActivityType.SAILING_PORT_TASKS, "Sailing Port Tasks", 15, false),
            planned(ActivityType.ABYSSAL_SIRE, "Abyssal Sire", 25, true),
            planned(ActivityType.ALCHEMICAL_HYDRA, "Alchemical Hydra", 25, true),
            planned(ActivityType.AMOXLIATL, "Amoxliatl", 25, true),
            planned(ActivityType.ARTIO, "Artio", 25, true),
            planned(ActivityType.BARROWS_CHESTS, "Barrows Chests", 15, true),
            planned(ActivityType.BRUTUS, "Brutus", 25, true),
            planned(ActivityType.BRYOPHYTA, "Bryophyta", 25, true),
            planned(ActivityType.CALLISTO, "Callisto", 25, true),
            planned(ActivityType.CALVARION, "Calvar'ion", 25, true),
            planned(ActivityType.CERBERUS, "Cerberus", 25, true),
            planned(ActivityType.CHAMBERS_OF_XERIC, "Chambers of Xeric", 30, true),
            planned(ActivityType.CHAMBERS_OF_XERIC_CHALLENGE_MODE, "Chambers of Xeric: Challenge Mode", 30, true),
            planned(ActivityType.CHAOS_ELEMENTAL, "Chaos Elemental", 25, true),
            planned(ActivityType.CHAOS_FANATIC, "Chaos Fanatic", 25, true),
            planned(ActivityType.COMMANDER_ZILYANA, "Commander Zilyana", 25, true),
            planned(ActivityType.CORPOREAL_BEAST, "Corporeal Beast", 25, true),
            planned(ActivityType.CRAZY_ARCHAEOLOGIST, "Crazy Archaeologist", 25, true),
            planned(ActivityType.DAGANNOTH_PRIME, "Dagannoth Prime", 25, true),
            planned(ActivityType.DAGANNOTH_REX, "Dagannoth Rex", 25, true),
            planned(ActivityType.DAGANNOTH_SUPREME, "Dagannoth Supreme", 25, true),
            planned(ActivityType.DERANGED_ARCHAEOLOGIST, "Deranged Archaeologist", 25, true),
            planned(ActivityType.DOOM_OF_MOKHAIOTL, "Doom of Mokhaiotl", 25, true),
            planned(ActivityType.DUKE_SUCELLUS, "Duke Sucellus", 30, true),
            planned(ActivityType.GENERAL_GRAARDOR, "General Graardor", 25, true),
            planned(ActivityType.GIANT_MOLE, "Giant Mole", 25, true),
            planned(ActivityType.GROTESQUE_GUARDIANS, "Grotesque Guardians", 25, true),
            planned(ActivityType.HESPORI, "Hespori", 20, true),
            planned(ActivityType.KALPHITE_QUEEN, "Kalphite Queen", 25, true),
            planned(ActivityType.KING_BLACK_DRAGON, "King Black Dragon", 25, true),
            planned(ActivityType.KRAKEN, "Kraken", 25, true),
            planned(ActivityType.KREEARRA, "Kree'arra", 25, true),
            planned(ActivityType.KRIL_TSUTSAROTH, "K'ril Tsutsaroth", 25, true),
            planned(ActivityType.LUNAR_CHESTS, "Lunar Chests", 15, true),
            planned(ActivityType.MAGGOT_KING, "Maggot King", 25, true),
            planned(ActivityType.MIMIC, "Mimic", 20, true),
            planned(ActivityType.NEX, "Nex", 30, true),
            planned(ActivityType.NIGHTMARE, "Nightmare", 30, true),
            planned(ActivityType.PHOSANIS_NIGHTMARE, "Phosani's Nightmare", 30, true),
            planned(ActivityType.OBOR, "Obor", 20, true),
            planned(ActivityType.SARACHNIS, "Sarachnis", 25, true),
            planned(ActivityType.SCORPIA, "Scorpia", 25, true),
            planned(ActivityType.SHELLBANE_GRYPHON, "Shellbane Gryphon", 25, true),
            planned(ActivityType.SKOTIZO, "Skotizo", 25, true),
            planned(ActivityType.SOL_HEREDIT, "Sol Heredit", 30, true),
            planned(ActivityType.SPINDEL, "Spindel", 25, true),
            planned(ActivityType.THE_GAUNTLET, "The Gauntlet", 25, true),
            planned(ActivityType.THE_HUEYCOATL, "The Hueycoatl", 25, true),
            planned(ActivityType.THE_LEVIATHAN, "The Leviathan", 30, true),
            planned(ActivityType.THE_ROYAL_TITANS, "The Royal Titans", 25, true),
            planned(ActivityType.THE_WHISPERER, "The Whisperer", 30, true),
            planned(ActivityType.THEATRE_OF_BLOOD, "Theatre of Blood", 30, true),
            planned(ActivityType.THEATRE_OF_BLOOD_HARD_MODE, "Theatre of Blood: Hard Mode", 30, true),
            planned(ActivityType.THERMONUCLEAR_SMOKE_DEVIL, "Thermonuclear Smoke Devil", 25, true),
            planned(ActivityType.TOMBS_OF_AMASCUT, "Tombs of Amascut", 30, true),
            planned(ActivityType.TOMBS_OF_AMASCUT_EXPERT, "Tombs of Amascut: Expert", 30, true),
            planned(ActivityType.TZKAL_ZUK, "TzKal-Zuk", 30, true),
            planned(ActivityType.TZTOK_JAD, "TzTok-Jad", 25, true),
            planned(ActivityType.VENENATIS, "Venenatis", 25, true),
            planned(ActivityType.VETION, "Vet'ion", 25, true),
            planned(ActivityType.VORKATH, "Vorkath", 30, true),
            planned(ActivityType.YAMA, "Yama", 30, true),
            planned(ActivityType.ZULRAH, "Zulrah", 30, true),
            planned(ActivityType.MINING, "Mining", 10, false),
            planned(ActivityType.WOODCUTTING, "Woodcutting", 10, false),
            planned(ActivityType.FISHING, "Fishing", 10, false),
            planned(ActivityType.HUNTER_TRAPS, "Hunter Traps", 10, false),
            planned(ActivityType.BIRDHOUSE_RUNS, "Birdhouse Runs", 10, false),
            planned(ActivityType.FARMING_RUNS, "Farming Runs", 10, false),
            planned(ActivityType.HERBLORE_BANK_LOOP, "Herblore Bank Loop", 10, false),
            planned(ActivityType.FLETCHING_BANK_LOOP, "Fletching Bank Loop", 10, false),
            planned(ActivityType.CRAFTING_BANK_LOOP, "Crafting Bank Loop", 10, false),
            planned(ActivityType.COOKING_BANK_LOOP, "Cooking Bank Loop", 10, false),
            planned(ActivityType.SMITHING_BANK_LOOP, "Smithing Bank Loop", 10, false),
            planned(ActivityType.RUNECRAFTING, "Runecrafting", 10, false),
            planned(ActivityType.CLUE_STEPS, "Clue Steps", 10, false),
            planned(ActivityType.SLAYER_TASKS, "Slayer Tasks", 10, false),
            planned(ActivityType.ROGUES_DEN, "Rogues' Den", 10, false),
            planned(ActivityType.TEARS_OF_GUTHIX, "Tears of Guthix", 10, false),
            planned(ActivityType.BLAST_FURNACE, "Blast Furnace", 10, false),
            planned(ActivityType.BARBARIAN_ASSAULT, "Barbarian Assault", 10, false),
            planned(ActivityType.SOUL_WARS, "Soul Wars", 10, false),
            planned(ActivityType.LAST_MAN_STANDING, "Last Man Standing", 10, false),
            planned(ActivityType.BOUNTY_HUNTER, "Bounty Hunter", 10, false),
            planned(ActivityType.PVP_ARENA, "PvP Arena", 10, false),
            planned(ActivityType.WILDERNESS_BOSSING, "Wilderness Bossing", 20, true));
    }

    private static ActivityModule planned(ActivityType activityType, String displayName, int arbitrationPriority, boolean bossActivity)
    {
        return new PlannedActivityModule(activityType, displayName, arbitrationPriority, bossActivity);
    }

    public static List<ActivityModule> enabledModules(List<ActivityModule> modules)
    {
        final List<ActivityModule> enabled = new ArrayList<>();
        for (ActivityModule module : modules)
        {
            if (module.isEnabled())
            {
                enabled.add(module);
            }
        }
        return ImmutableCollections.immutableList(enabled);
    }

    public static ActivityStrategyFactory strategyFactory(List<ActivityModule> modules)
    {
        final List<ActivityModule> enabledModules = enabledModules(modules);
        return () ->
        {
            final List<ActivityStrategy> strategies = new ArrayList<>();
            for (ActivityModule module : enabledModules)
            {
                strategies.add(module.createStrategy());
            }
            return ImmutableCollections.immutableList(strategies);
        };
    }

    public static Map<ActivityType, ReportBuilder> reportBuilders(List<ActivityModule> modules)
    {
        final Map<ActivityType, ReportBuilder> builders = new LinkedHashMap<>();
        for (ActivityModule module : modules)
        {
            builders.put(module.definition().getActivityType(), module.reportBuilder());
        }
        return ImmutableCollections.immutableMap(builders);
    }
}
