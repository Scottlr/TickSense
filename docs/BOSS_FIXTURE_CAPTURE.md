# Boss Fixture Capture Checklist

Use observe-only boss stubs and the debug event recorder to capture evidence before enabling normal reports.

For each boss fixture, capture:

- Boss spawn/despawn or transform evidence from NPC telemetry.
- Region or instance evidence that constrains the encounter.
- Player attack/menu evidence that proves the encounter start.
- Boss attack animations, graphics, projectiles, and interacting changes.
- Player damage evidence around each suspected mechanic.
- Inventory, equipment, prayer, and movement responses during mechanic windows.
- Kill, despawn, loot, or other finish evidence.

Review the debug JSONL for:

- `NORMALIZED_TELEMETRY` records containing the raw normalized event stream.
- `ACTIVITY_DIAGNOSTIC` records from observe-only boss stubs.
- Evidence lines named `Known boss NPC observed` and `Unverified event ID observed`.
- `ACTIVITY_MARKER` and `OPPORTUNITY_MARKER` records once a boss graduates beyond observe-only.

Store committed debug captures under `src/test/resources/replays/debug/` and assert them with `DebugEventReplayLoader`.

Only move a boss from observe-only diagnostics to normal reports when replay fixtures prove the IDs, timing windows, and finish evidence used by the strategy.
