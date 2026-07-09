# Activity Support Research

This audit lists activity families TickSense could support and separates them from activities that should stay hidden until replay evidence, ID coverage, and post-activity metrics are ready.

## Sources

- RuneLite 1.12.32 resolved dependency, corroborated against [`HiscoreSkill`](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/hiscore/HiscoreSkill.java): exposes skills, activities, raids, and boss hiscore entries including Sailing, Guardians of the Rift, Colosseum Glory, Araxxor, Doom of Mokhaiotl, Royal Titans, Yama, raids, Wintertodt, Tempoross, Zalcano, and current boss kill-count surfaces.
- RuneLite 1.12.32 resolved dependency, corroborated against [`Boss`](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/bosstimer/Boss.java): exposes a smaller respawn-timer boss set. It is useful as corroborating metadata only; it is package-private and does not define TickSense strategy support.
- [RuneLite Plugin Hub](https://runelite.net/plugin-hub/) shows strong player demand around activity plugins such as Guardians of the Rift Helper, Sailing, Tombs of Amascut, The Gauntlet, Mahogany Homes, Tempoross, Giants' Foundry, Hunter Rumours, Fight Caves, Fortis Colosseum, Zalcano, Motherlode Mine, Tears of Guthix, and boss utilities.
- [RuneLite 1.6.1 release notes](https://runelite.net/blog/show/2020-01-09-1.6.1-Release/) confirm that RuneLite's hiscore plugin has supported boss hiscores since 2020; this is useful metadata, not TickSense strategy support.
- TickSense repo telemetry coverage currently captures game ticks, client ticks, menu clicks, menu entries, NPC state, object state, inventory changes, stat changes, widgets, projectiles, graphics, hitsplats, game state, world-view changes, movement snapshots, and normalized timeline storage.

## Code Exposure Policy

- Compiled activity modules are not automatically supported.
- `ActivitySupportConfig.current()` is the code-owned release gate for activity modules.
- Add a future activity module behind a `private static final boolean ENABLE_* = false` gate until it has verified IDs, replay fixtures, activity termination behavior, and report wording.
- Keep player-facing RuneLite config separate from these gates. These switches are not manual activity selection and should not appear in normal settings.
- Use `ActivityDescriptor.reportable` only when the report is good enough to show after completion. Use `reportsDisabled` or a disabled support gate for anything still being verified.

## Current Activity Modules

| Activity | Current module | Report mode | Support gate | Notes |
| --- | --- | --- | --- | --- |
| Gem Mining | `GemMiningModule` | Reportable | Enabled | Good skilling proof case. |
| Construction | `ConstructionModule` | Reportable | Enabled | Good repeat-action/menu-efficiency case. |
| Vardorvis | `VardorvisModule` | Reportable | Enabled | Strong boss timing candidate. |
| Inferno | `InfernoModule` | Reportable | Enabled | High-value wave/death/recovery analytics; keep conservative. |
| Araxxor | `AraxxorModule` | Reports disabled | Enabled | Observe-only until verification is complete. |
| Scurrius | `ScurriusModule` | Reports disabled | Enabled | Good beginner boss candidate once metrics are validated. |
| Phantom Muspah | `PhantomMuspahModule` | Reports disabled | Enabled | Good phase/projectile candidate after evidence. |
| Hunllef | `HunllefModule` | Reports disabled | Enabled | Good Gauntlet fight candidate; prayer/weapon timing risk. |
| Corrupted Gauntlet | `CorruptedGauntletModule` | Reports disabled | Enabled | Same as Hunllef with higher stakes. |

## Best Next Candidates

These fit TickSense's retrospective model and can likely be detected from current telemetry without gameplay automation.

| Candidate | Family | Why it fits | Likely evidence | First metrics |
| --- | --- | --- | --- | --- |
| Barrows | Boss/minigame | Clear chest loop and route decisions. | Regions, NPCs, widgets, chest object, inventory/loot. | Time per run, tunnel downtime, brother response delays. |
| The Gauntlet prep | Skilling/combat instance | Clear room/resource/prep/fight phases. | Regions, objects, NPCs, item changes, Hunllef spawn. | Prep idle, missed resources, late weapon/armor completion. |
| Fight Caves | Wave challenge | Clear wave cadence and death/completion boundaries. | NPC spawns, region, hitsplats, prayers if verified. | Wave downtime, target swaps, recovery windows. |
| Fortis Colosseum / Sol Heredit | Wave challenge | Similar to Inferno but modern hiscore surface. | NPC spawns, region, projectiles, hitsplats. | Wave downtime, solve/recovery timing, death timeline. |
| Tempoross | Skilling boss | Strong Plugin Hub demand and clear object/NPC/widget loop. | Regions, objects, inventory, stat changes, widgets. | Idle ticks, late tether/cook/load, phase transitions. |
| Wintertodt | Skilling boss | Clear repeated skill loop and damage/recovery windows. | Region, objects, inventory, hitsplats, stat changes. | Idle ticks, brazier downtime, recovery delay. |
| Zalcano | Skilling boss | Object/projectile/movement-heavy fight. | NPC, projectiles, objects, movement, inventory. | Reaction delay, missed mining/smithing/throw windows. |
| Guardians of the Rift | Minigame/skilling | Popular helper target with clear portals/altars/widgets. | Widgets, objects, inventory, regions, stat changes. | Portal response, altar choice latency, idle ticks. |
| Mahogany Homes | Contract skilling | Route and menu efficiency fit retrospective reporting. | Widgets, menu clicks, objects, inventory, stat changes. | Contract travel downtime, wrong-object/menu delays. |
| Giants' Foundry | Skilling minigame | Repeated temperature/action windows. | Widgets, objects, stat changes, inventory. | Missed action windows, heat/quality downtime. |
| Mastering Mixology | Skilling minigame | Widget/recipe/action execution loop. | Widgets, inventory, menu clicks, objects. | Recipe delay, hand-in downtime, wrong station usage. |
| Hunter Rumours | Skilling contracts | Clear task loop and travel/action breakdown. | NPC/dialog widgets, regions, traps, inventory. | Travel time, trap idle, task completion time. |
| Sailing tasks | Skill/activity | RuneLite exposes Sailing and Plugin Hub already has activity demand. | Widgets, movement, objects, inventory, regions. | Port-task route time, cargo delay, navigation downtime. |

## Boss Backlog From RuneLite Hiscores

RuneLite hiscores expose these boss/raid kill-count surfaces in the resolved dependency. They are possible activity keys, but each still needs TickSense-owned detection, termination, and metrics before exposure:

`Abyssal Sire`, `Alchemical Hydra`, `Amoxliatl`, `Araxxor`, `Artio`, `Barrows Chests`, `Brutus`, `Bryophyta`, `Callisto`, `Calvar'ion`, `Cerberus`, `Chambers of Xeric`, `Chambers of Xeric: Challenge Mode`, `Chaos Elemental`, `Chaos Fanatic`, `Commander Zilyana`, `Corporeal Beast`, `Crazy Archaeologist`, `Dagannoth Prime`, `Dagannoth Rex`, `Dagannoth Supreme`, `Deranged Archaeologist`, `Doom of Mokhaiotl`, `Duke Sucellus`, `General Graardor`, `Giant Mole`, `Grotesque Guardians`, `Hespori`, `Kalphite Queen`, `King Black Dragon`, `Kraken`, `Kree'arra`, `K'ril Tsutsaroth`, `Lunar Chests`, `Maggot King`, `Mimic`, `Nex`, `Nightmare`, `Phosani's Nightmare`, `Obor`, `Phantom Muspah`, `Sarachnis`, `Scorpia`, `Scurrius`, `Shellbane Gryphon`, `Skotizo`, `Sol Heredit`, `Spindel`, `Tempoross`, `The Gauntlet`, `The Corrupted Gauntlet`, `The Hueycoatl`, `The Leviathan`, `The Royal Titans`, `The Whisperer`, `Theatre of Blood`, `Theatre of Blood: Hard Mode`, `Thermonuclear Smoke Devil`, `Tombs of Amascut`, `Tombs of Amascut: Expert`, `TzKal-Zuk`, `TzTok-Jad`, `Vardorvis`, `Venenatis`, `Vet'ion`, `Vorkath`, `Wintertodt`, `Yama`, `Zalcano`, `Zulrah`.

## Skilling And Minigame Backlog

These do not always appear as boss hiscores, but they are plausible TickSense activities because the repo already captures the event classes needed for post-session analysis:

- Repeated node skills: mining, woodcutting, fishing, hunter traps, birdhouses, farming runs.
- Bank/menu loops: herblore cleaning/mixing, fletching, crafting, cooking, smithing, construction, runecrafting.
- Contract/task loops: Mahogany Homes, Hunter Rumours, Sailing port tasks, clue steps, Slayer tasks.
- Minigames: Guardians of the Rift, Tempoross, Wintertodt, Zalcano, Giants' Foundry, Mastering Mixology, Rogue's Den, Tears of Guthix, Blast Furnace, Barbarian Assault, Soul Wars, Last Man Standing.
- Raids: Chambers of Xeric, Theatre of Blood, Tombs of Amascut.
- PvP: LMS, Bounty Hunter, PvP Arena, Wilderness bosses with player-interruption metadata.

## Prioritization Recommendation

1. Promote observe-only current modules first: Scurrius, Phantom Muspah, Hunllef/Corrupted Gauntlet, Araxxor.
2. Add one clear skilling/minigame loop next: Tempoross, Guardians of the Rift, or Mahogany Homes.
3. Add one wave challenge refinement next: Fight Caves or Fortis Colosseum, reusing Inferno patterns.
4. Defer raids until room/span modeling is explicit; they should be multi-span activities, not one oversized strategy.
5. Defer PvP until reports can separate player execution from opponent disruption and avoid live-combat advice.
