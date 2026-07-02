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
