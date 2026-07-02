# Replay Fixtures

`gem-mining-basic.jsonl` is a source-owned normalized verification fixture for the underground Shilo gem mine MVP slice.

Fixture rules:

- Record normalized TickSense telemetry only; do not commit raw RuneLite objects or screenshots.
- Remove or replace player names, account identifiers, and any unrelated chat text before commit.
- Keep only the evidence needed for verification: object snapshots showing gem rock availability/depletion/respawn, mine click interactions, mining animation changes, mining XP changes, inventory deltas for uncut gems, and region/instance context that proves the location.
- Preserve event ordering and timestamps because later replay tasks depend on timing fidelity.
- Re-verify the committed object, animation, item, and region IDs against current RuneLite-visible evidence after OSRS updates affecting gem rocks.
- The current fixture is source-owned normalized evidence rather than a raw player capture. It is intentionally scoped to the underground Shilo gem mine verification slice at region `11410`.

The current verification fixture proves:

- gem rock availability at the start of the session;
- at least one mine click on a verified gem rock object;
- mining confirmation through animation, XP, or inventory gain;
- rock depletion and respawn evidence from normalized object snapshots.

Reserved post-MVP Araxxor fixture names for T029:

- `araxxor-spider-basic.jsonl`
- `araxxor-teleport-midkill.jsonl`

Do not add those Araxxor files until they are source-owned normalized fixtures with verified boss IDs, named spider IDs, attackability evidence, and sanitized timing/order fidelity.

Araxxor verification review:

- Review date: `2026-07-03`
- Current decision: `BLOCKED`
- Verified source so far: official RuneLite `NpcID` constants for Araxxor and named spider variants, recorded in `src/main/java/com/ticksense/activities/araxxor/AraxxorIds.java`.
- Missing evidence: sanitized normalized spider spawn/availability telemetry, attack click evidence, interaction-changed evidence, damage evidence during spider windows, and teleport-mid-kill termination evidence.

Araxxor capture instructions for T029/T015 follow-up:

- Record normalized TickSense telemetry only; do not commit raw RuneLite objects, screenshots, or video.
- Remove or replace player names, account identifiers, and unrelated chat before commit.
- `araxxor-spider-basic.jsonl` should prove boss presence, named spider spawn/availability, attack click, local-player interaction change onto the spider, and at least one spider-window damage or completion signal.
- `araxxor-teleport-midkill.jsonl` should prove boss presence plus a teleport/instance exit or equivalent mid-kill termination path in normalized telemetry.
- Preserve event ordering and timestamps so downstream opportunity timing stays auditable.

Reserved post-MVP Construction fixture name for T024/T032 follow-up:

- `construction-basic.jsonl`

Construction verification review:

- Review date: `2026-07-03`
- Approved first method: `oak-larder`
- Current decision: `PARTIALLY_VERIFIED`
- Verified source so far: official RuneLite `ObjectID`, `ItemID`, `AnimationID`, and `WidgetID` constants in the pinned `runelite-api` dependency for oak larder build space/built variants, oak-plank-and-tool inventory evidence, Construction build animations, and bank widget groups.
- Missing evidence: sanitized normalized menu-open timing, build/remove click telemetry, construction-widget confirmation IDs, inventory delta proof for oak plank consumption, Construction XP confirmation, and servant-assisted refill evidence.

Construction capture instructions for T024/T032 follow-up:

- Record normalized TickSense telemetry only; do not commit raw RuneLite objects, screenshots, or video.
- Remove or replace player names, account identifiers, and unrelated chat before commit.
- `construction-basic.jsonl` should prove the `oak-larder` flow end to end: larder space presence, menu open, build click, construction widget confirmation, build animation, oak plank inventory delta, Construction XP gain, remove click, rebuilt larder/object transition, and either bank or servant refill evidence.
- Preserve event ordering and timestamps so downstream menu-latency and build-cadence analysis stays auditable.
- Refresh the fixture notes whenever POH widget IDs, menu text, or item requirements change in RuneLite-visible evidence.
