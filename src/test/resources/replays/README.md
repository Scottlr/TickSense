# Replay Fixtures

`gem-mining-basic.jsonl` is intentionally a placeholder until we capture a real normalized gem mining session.

Capture rules for the eventual fixture:

- Record normalized TickSense telemetry only; do not commit raw RuneLite objects or screenshots.
- Remove or replace player names, account identifiers, and any unrelated chat text before commit.
- Keep only the evidence needed for verification: object snapshots showing gem rock availability/depletion, mine click interactions, mining animation changes, mining XP changes, inventory deltas for uncut gems, and any region/instance context that proves the location.
- Preserve event ordering and timestamps because later replay tasks depend on timing fidelity.
- Re-verify the committed object, animation, item, and region IDs against current RuneLite-visible evidence before promoting gem mining from partially verified to verified.

An acceptable real fixture should prove:

- gem rock availability at the start of the session;
- at least one mine click on a verified gem rock object;
- mining confirmation through animation, XP, or inventory gain;
- rock depletion and respawn evidence from normalized object snapshots.
