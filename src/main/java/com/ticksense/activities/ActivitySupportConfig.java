package com.ticksense.activities;

import com.ticksense.core.ActivityType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class ActivitySupportConfig
{
    private static final boolean ENABLE_GEM_MINING = true;
    private static final boolean ENABLE_CONSTRUCTION = true;
    private static final boolean ENABLE_VARDORVIS = true;
    private static final boolean ENABLE_INFERNO = true;
    private static final boolean ENABLE_ARAXXOR = true;
    private static final boolean ENABLE_SCURRIUS = true;
    private static final boolean ENABLE_PHANTOM_MUSPAH = true;
    private static final boolean ENABLE_HUNLLEF = true;
    private static final boolean ENABLE_CORRUPTED_GAUNTLET = true;
    private static final boolean ENABLE_BARROWS = false;
    private static final boolean ENABLE_GAUNTLET_PREP = false;
    private static final boolean ENABLE_FIGHT_CAVES = false;
    private static final boolean ENABLE_FORTIS_COLOSSEUM = false;
    private static final boolean ENABLE_TEMPOROSS = false;
    private static final boolean ENABLE_WINTERTODT = false;
    private static final boolean ENABLE_ZALCANO = false;
    private static final boolean ENABLE_GUARDIANS_OF_THE_RIFT = false;
    private static final boolean ENABLE_MAHOGANY_HOMES = false;
    private static final boolean ENABLE_GIANTS_FOUNDRY = false;
    private static final boolean ENABLE_MASTERING_MIXOLOGY = false;
    private static final boolean ENABLE_HUNTER_RUMOURS = false;
    private static final boolean ENABLE_SAILING_PORT_TASKS = false;
    private static final boolean ENABLE_ABYSSAL_SIRE = false;
    private static final boolean ENABLE_ALCHEMICAL_HYDRA = false;
    private static final boolean ENABLE_AMOXLIATL = false;
    private static final boolean ENABLE_ARTIO = false;
    private static final boolean ENABLE_BARROWS_CHESTS = false;
    private static final boolean ENABLE_BRUTUS = false;
    private static final boolean ENABLE_BRYOPHYTA = false;
    private static final boolean ENABLE_CALLISTO = false;
    private static final boolean ENABLE_CALVARION = false;
    private static final boolean ENABLE_CERBERUS = false;
    private static final boolean ENABLE_CHAMBERS_OF_XERIC = false;
    private static final boolean ENABLE_CHAMBERS_OF_XERIC_CHALLENGE_MODE = false;
    private static final boolean ENABLE_CHAOS_ELEMENTAL = false;
    private static final boolean ENABLE_CHAOS_FANATIC = false;
    private static final boolean ENABLE_COMMANDER_ZILYANA = false;
    private static final boolean ENABLE_CORPOREAL_BEAST = false;
    private static final boolean ENABLE_CRAZY_ARCHAEOLOGIST = false;
    private static final boolean ENABLE_DAGANNOTH_PRIME = false;
    private static final boolean ENABLE_DAGANNOTH_REX = false;
    private static final boolean ENABLE_DAGANNOTH_SUPREME = false;
    private static final boolean ENABLE_DERANGED_ARCHAEOLOGIST = false;
    private static final boolean ENABLE_DOOM_OF_MOKHAIOTL = false;
    private static final boolean ENABLE_DUKE_SUCELLUS = false;
    private static final boolean ENABLE_GENERAL_GRAARDOR = false;
    private static final boolean ENABLE_GIANT_MOLE = false;
    private static final boolean ENABLE_GROTESQUE_GUARDIANS = false;
    private static final boolean ENABLE_HESPORI = false;
    private static final boolean ENABLE_KALPHITE_QUEEN = false;
    private static final boolean ENABLE_KING_BLACK_DRAGON = false;
    private static final boolean ENABLE_KRAKEN = false;
    private static final boolean ENABLE_KREEARRA = false;
    private static final boolean ENABLE_KRIL_TSUTSAROTH = false;
    private static final boolean ENABLE_LUNAR_CHESTS = false;
    private static final boolean ENABLE_MAGGOT_KING = false;
    private static final boolean ENABLE_MIMIC = false;
    private static final boolean ENABLE_NEX = false;
    private static final boolean ENABLE_NIGHTMARE = false;
    private static final boolean ENABLE_PHOSANIS_NIGHTMARE = false;
    private static final boolean ENABLE_OBOR = false;
    private static final boolean ENABLE_SARACHNIS = false;
    private static final boolean ENABLE_SCORPIA = false;
    private static final boolean ENABLE_SHELLBANE_GRYPHON = false;
    private static final boolean ENABLE_SKOTIZO = false;
    private static final boolean ENABLE_SOL_HEREDIT = false;
    private static final boolean ENABLE_SPINDEL = false;
    private static final boolean ENABLE_THE_GAUNTLET = false;
    private static final boolean ENABLE_THE_HUEYCOATL = false;
    private static final boolean ENABLE_THE_LEVIATHAN = false;
    private static final boolean ENABLE_THE_ROYAL_TITANS = false;
    private static final boolean ENABLE_THE_WHISPERER = false;
    private static final boolean ENABLE_THEATRE_OF_BLOOD = false;
    private static final boolean ENABLE_THEATRE_OF_BLOOD_HARD_MODE = false;
    private static final boolean ENABLE_THERMONUCLEAR_SMOKE_DEVIL = false;
    private static final boolean ENABLE_TOMBS_OF_AMASCUT = false;
    private static final boolean ENABLE_TOMBS_OF_AMASCUT_EXPERT = false;
    private static final boolean ENABLE_TZKAL_ZUK = false;
    private static final boolean ENABLE_TZTOK_JAD = false;
    private static final boolean ENABLE_VENENATIS = false;
    private static final boolean ENABLE_VETION = false;
    private static final boolean ENABLE_VORKATH = false;
    private static final boolean ENABLE_YAMA = false;
    private static final boolean ENABLE_ZULRAH = false;
    private static final boolean ENABLE_MINING = false;
    private static final boolean ENABLE_WOODCUTTING = false;
    private static final boolean ENABLE_FISHING = false;
    private static final boolean ENABLE_HUNTER_TRAPS = false;
    private static final boolean ENABLE_BIRDHOUSE_RUNS = false;
    private static final boolean ENABLE_FARMING_RUNS = false;
    private static final boolean ENABLE_HERBLORE_BANK_LOOP = false;
    private static final boolean ENABLE_FLETCHING_BANK_LOOP = false;
    private static final boolean ENABLE_CRAFTING_BANK_LOOP = false;
    private static final boolean ENABLE_COOKING_BANK_LOOP = false;
    private static final boolean ENABLE_SMITHING_BANK_LOOP = false;
    private static final boolean ENABLE_RUNECRAFTING = false;
    private static final boolean ENABLE_CLUE_STEPS = false;
    private static final boolean ENABLE_SLAYER_TASKS = false;
    private static final boolean ENABLE_ROGUES_DEN = false;
    private static final boolean ENABLE_TEARS_OF_GUTHIX = false;
    private static final boolean ENABLE_BLAST_FURNACE = false;
    private static final boolean ENABLE_BARBARIAN_ASSAULT = false;
    private static final boolean ENABLE_SOUL_WARS = false;
    private static final boolean ENABLE_LAST_MAN_STANDING = false;
    private static final boolean ENABLE_BOUNTY_HUNTER = false;
    private static final boolean ENABLE_PVP_ARENA = false;
    private static final boolean ENABLE_WILDERNESS_BOSSING = false;

    private final Set<ActivityType> enabledActivityTypes;

    private ActivitySupportConfig(Set<ActivityType> enabledActivityTypes)
    {
        this.enabledActivityTypes = immutableEnumSet(enabledActivityTypes);
    }

    public static ActivitySupportConfig current()
    {
        final Builder builder = builder();
        builder.enableIf(ENABLE_GEM_MINING, ActivityType.GEM_MINING);
        builder.enableIf(ENABLE_CONSTRUCTION, ActivityType.CONSTRUCTION);
        builder.enableIf(ENABLE_VARDORVIS, ActivityType.VARDORVIS);
        builder.enableIf(ENABLE_INFERNO, ActivityType.INFERNO);
        builder.enableIf(ENABLE_ARAXXOR, ActivityType.ARAXXOR);
        builder.enableIf(ENABLE_SCURRIUS, ActivityType.SCURRIUS);
        builder.enableIf(ENABLE_PHANTOM_MUSPAH, ActivityType.PHANTOM_MUSPAH);
        builder.enableIf(ENABLE_HUNLLEF, ActivityType.HUNLLEF);
        builder.enableIf(ENABLE_CORRUPTED_GAUNTLET, ActivityType.CORRUPTED_GAUNTLET);
        builder.enableIf(ENABLE_BARROWS, ActivityType.BARROWS);
        builder.enableIf(ENABLE_GAUNTLET_PREP, ActivityType.GAUNTLET_PREP);
        builder.enableIf(ENABLE_FIGHT_CAVES, ActivityType.FIGHT_CAVES);
        builder.enableIf(ENABLE_FORTIS_COLOSSEUM, ActivityType.FORTIS_COLOSSEUM);
        builder.enableIf(ENABLE_TEMPOROSS, ActivityType.TEMPOROSS);
        builder.enableIf(ENABLE_WINTERTODT, ActivityType.WINTERTODT);
        builder.enableIf(ENABLE_ZALCANO, ActivityType.ZALCANO);
        builder.enableIf(ENABLE_GUARDIANS_OF_THE_RIFT, ActivityType.GUARDIANS_OF_THE_RIFT);
        builder.enableIf(ENABLE_MAHOGANY_HOMES, ActivityType.MAHOGANY_HOMES);
        builder.enableIf(ENABLE_GIANTS_FOUNDRY, ActivityType.GIANTS_FOUNDRY);
        builder.enableIf(ENABLE_MASTERING_MIXOLOGY, ActivityType.MASTERING_MIXOLOGY);
        builder.enableIf(ENABLE_HUNTER_RUMOURS, ActivityType.HUNTER_RUMOURS);
        builder.enableIf(ENABLE_SAILING_PORT_TASKS, ActivityType.SAILING_PORT_TASKS);
        builder.enableIf(ENABLE_ABYSSAL_SIRE, ActivityType.ABYSSAL_SIRE);
        builder.enableIf(ENABLE_ALCHEMICAL_HYDRA, ActivityType.ALCHEMICAL_HYDRA);
        builder.enableIf(ENABLE_AMOXLIATL, ActivityType.AMOXLIATL);
        builder.enableIf(ENABLE_ARTIO, ActivityType.ARTIO);
        builder.enableIf(ENABLE_BARROWS_CHESTS, ActivityType.BARROWS_CHESTS);
        builder.enableIf(ENABLE_BRUTUS, ActivityType.BRUTUS);
        builder.enableIf(ENABLE_BRYOPHYTA, ActivityType.BRYOPHYTA);
        builder.enableIf(ENABLE_CALLISTO, ActivityType.CALLISTO);
        builder.enableIf(ENABLE_CALVARION, ActivityType.CALVARION);
        builder.enableIf(ENABLE_CERBERUS, ActivityType.CERBERUS);
        builder.enableIf(ENABLE_CHAMBERS_OF_XERIC, ActivityType.CHAMBERS_OF_XERIC);
        builder.enableIf(ENABLE_CHAMBERS_OF_XERIC_CHALLENGE_MODE, ActivityType.CHAMBERS_OF_XERIC_CHALLENGE_MODE);
        builder.enableIf(ENABLE_CHAOS_ELEMENTAL, ActivityType.CHAOS_ELEMENTAL);
        builder.enableIf(ENABLE_CHAOS_FANATIC, ActivityType.CHAOS_FANATIC);
        builder.enableIf(ENABLE_COMMANDER_ZILYANA, ActivityType.COMMANDER_ZILYANA);
        builder.enableIf(ENABLE_CORPOREAL_BEAST, ActivityType.CORPOREAL_BEAST);
        builder.enableIf(ENABLE_CRAZY_ARCHAEOLOGIST, ActivityType.CRAZY_ARCHAEOLOGIST);
        builder.enableIf(ENABLE_DAGANNOTH_PRIME, ActivityType.DAGANNOTH_PRIME);
        builder.enableIf(ENABLE_DAGANNOTH_REX, ActivityType.DAGANNOTH_REX);
        builder.enableIf(ENABLE_DAGANNOTH_SUPREME, ActivityType.DAGANNOTH_SUPREME);
        builder.enableIf(ENABLE_DERANGED_ARCHAEOLOGIST, ActivityType.DERANGED_ARCHAEOLOGIST);
        builder.enableIf(ENABLE_DOOM_OF_MOKHAIOTL, ActivityType.DOOM_OF_MOKHAIOTL);
        builder.enableIf(ENABLE_DUKE_SUCELLUS, ActivityType.DUKE_SUCELLUS);
        builder.enableIf(ENABLE_GENERAL_GRAARDOR, ActivityType.GENERAL_GRAARDOR);
        builder.enableIf(ENABLE_GIANT_MOLE, ActivityType.GIANT_MOLE);
        builder.enableIf(ENABLE_GROTESQUE_GUARDIANS, ActivityType.GROTESQUE_GUARDIANS);
        builder.enableIf(ENABLE_HESPORI, ActivityType.HESPORI);
        builder.enableIf(ENABLE_KALPHITE_QUEEN, ActivityType.KALPHITE_QUEEN);
        builder.enableIf(ENABLE_KING_BLACK_DRAGON, ActivityType.KING_BLACK_DRAGON);
        builder.enableIf(ENABLE_KRAKEN, ActivityType.KRAKEN);
        builder.enableIf(ENABLE_KREEARRA, ActivityType.KREEARRA);
        builder.enableIf(ENABLE_KRIL_TSUTSAROTH, ActivityType.KRIL_TSUTSAROTH);
        builder.enableIf(ENABLE_LUNAR_CHESTS, ActivityType.LUNAR_CHESTS);
        builder.enableIf(ENABLE_MAGGOT_KING, ActivityType.MAGGOT_KING);
        builder.enableIf(ENABLE_MIMIC, ActivityType.MIMIC);
        builder.enableIf(ENABLE_NEX, ActivityType.NEX);
        builder.enableIf(ENABLE_NIGHTMARE, ActivityType.NIGHTMARE);
        builder.enableIf(ENABLE_PHOSANIS_NIGHTMARE, ActivityType.PHOSANIS_NIGHTMARE);
        builder.enableIf(ENABLE_OBOR, ActivityType.OBOR);
        builder.enableIf(ENABLE_SARACHNIS, ActivityType.SARACHNIS);
        builder.enableIf(ENABLE_SCORPIA, ActivityType.SCORPIA);
        builder.enableIf(ENABLE_SHELLBANE_GRYPHON, ActivityType.SHELLBANE_GRYPHON);
        builder.enableIf(ENABLE_SKOTIZO, ActivityType.SKOTIZO);
        builder.enableIf(ENABLE_SOL_HEREDIT, ActivityType.SOL_HEREDIT);
        builder.enableIf(ENABLE_SPINDEL, ActivityType.SPINDEL);
        builder.enableIf(ENABLE_THE_GAUNTLET, ActivityType.THE_GAUNTLET);
        builder.enableIf(ENABLE_THE_HUEYCOATL, ActivityType.THE_HUEYCOATL);
        builder.enableIf(ENABLE_THE_LEVIATHAN, ActivityType.THE_LEVIATHAN);
        builder.enableIf(ENABLE_THE_ROYAL_TITANS, ActivityType.THE_ROYAL_TITANS);
        builder.enableIf(ENABLE_THE_WHISPERER, ActivityType.THE_WHISPERER);
        builder.enableIf(ENABLE_THEATRE_OF_BLOOD, ActivityType.THEATRE_OF_BLOOD);
        builder.enableIf(ENABLE_THEATRE_OF_BLOOD_HARD_MODE, ActivityType.THEATRE_OF_BLOOD_HARD_MODE);
        builder.enableIf(ENABLE_THERMONUCLEAR_SMOKE_DEVIL, ActivityType.THERMONUCLEAR_SMOKE_DEVIL);
        builder.enableIf(ENABLE_TOMBS_OF_AMASCUT, ActivityType.TOMBS_OF_AMASCUT);
        builder.enableIf(ENABLE_TOMBS_OF_AMASCUT_EXPERT, ActivityType.TOMBS_OF_AMASCUT_EXPERT);
        builder.enableIf(ENABLE_TZKAL_ZUK, ActivityType.TZKAL_ZUK);
        builder.enableIf(ENABLE_TZTOK_JAD, ActivityType.TZTOK_JAD);
        builder.enableIf(ENABLE_VENENATIS, ActivityType.VENENATIS);
        builder.enableIf(ENABLE_VETION, ActivityType.VETION);
        builder.enableIf(ENABLE_VORKATH, ActivityType.VORKATH);
        builder.enableIf(ENABLE_YAMA, ActivityType.YAMA);
        builder.enableIf(ENABLE_ZULRAH, ActivityType.ZULRAH);
        builder.enableIf(ENABLE_MINING, ActivityType.MINING);
        builder.enableIf(ENABLE_WOODCUTTING, ActivityType.WOODCUTTING);
        builder.enableIf(ENABLE_FISHING, ActivityType.FISHING);
        builder.enableIf(ENABLE_HUNTER_TRAPS, ActivityType.HUNTER_TRAPS);
        builder.enableIf(ENABLE_BIRDHOUSE_RUNS, ActivityType.BIRDHOUSE_RUNS);
        builder.enableIf(ENABLE_FARMING_RUNS, ActivityType.FARMING_RUNS);
        builder.enableIf(ENABLE_HERBLORE_BANK_LOOP, ActivityType.HERBLORE_BANK_LOOP);
        builder.enableIf(ENABLE_FLETCHING_BANK_LOOP, ActivityType.FLETCHING_BANK_LOOP);
        builder.enableIf(ENABLE_CRAFTING_BANK_LOOP, ActivityType.CRAFTING_BANK_LOOP);
        builder.enableIf(ENABLE_COOKING_BANK_LOOP, ActivityType.COOKING_BANK_LOOP);
        builder.enableIf(ENABLE_SMITHING_BANK_LOOP, ActivityType.SMITHING_BANK_LOOP);
        builder.enableIf(ENABLE_RUNECRAFTING, ActivityType.RUNECRAFTING);
        builder.enableIf(ENABLE_CLUE_STEPS, ActivityType.CLUE_STEPS);
        builder.enableIf(ENABLE_SLAYER_TASKS, ActivityType.SLAYER_TASKS);
        builder.enableIf(ENABLE_ROGUES_DEN, ActivityType.ROGUES_DEN);
        builder.enableIf(ENABLE_TEARS_OF_GUTHIX, ActivityType.TEARS_OF_GUTHIX);
        builder.enableIf(ENABLE_BLAST_FURNACE, ActivityType.BLAST_FURNACE);
        builder.enableIf(ENABLE_BARBARIAN_ASSAULT, ActivityType.BARBARIAN_ASSAULT);
        builder.enableIf(ENABLE_SOUL_WARS, ActivityType.SOUL_WARS);
        builder.enableIf(ENABLE_LAST_MAN_STANDING, ActivityType.LAST_MAN_STANDING);
        builder.enableIf(ENABLE_BOUNTY_HUNTER, ActivityType.BOUNTY_HUNTER);
        builder.enableIf(ENABLE_PVP_ARENA, ActivityType.PVP_ARENA);
        builder.enableIf(ENABLE_WILDERNESS_BOSSING, ActivityType.WILDERNESS_BOSSING);
        return builder.build();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public boolean isActivitySupported(ActivityType activityType)
    {
        return enabledActivityTypes.contains(Objects.requireNonNull(activityType, "activityType"));
    }

    public Set<ActivityType> getEnabledActivityTypes()
    {
        return enabledActivityTypes;
    }

    public static final class Builder
    {
        private final EnumSet<ActivityType> enabledActivityTypes = EnumSet.noneOf(ActivityType.class);

        private Builder()
        {
        }

        public Builder enable(ActivityType activityType)
        {
            enabledActivityTypes.add(Objects.requireNonNull(activityType, "activityType"));
            return this;
        }

        public Builder enableIf(boolean enabled, ActivityType activityType)
        {
            if (enabled)
            {
                enable(activityType);
            }
            return this;
        }

        public ActivitySupportConfig build()
        {
            return new ActivitySupportConfig(enabledActivityTypes);
        }
    }

    private static Set<ActivityType> immutableEnumSet(Set<ActivityType> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(values));
    }
}
