# Execution Feedback Tasks

## Discovery Summary

Execution feedback is the immediate-but-after-the-fact layer for TickSense. It should celebrate confirmed perfect execution and optionally call out conservative correction patterns such as likely misclicks. Discovery inspected `README.md`, `docs/ARCHITECTURE.md`, `TickSenseConfig`, `TickSensePlugin`, `RuneLiteEventAdapter`, `RuneLiteSnapshotter`, `OpportunityLifecycle`, `OpportunityMarker`, execution tracker packages, replay/debug fixtures, and existing report/UI packages.

Current code already models opportunities and emits `OpportunityMarker` records when an opportunity opens or reaches a terminal state. `PlayerActionTelemetryEvent` captures menu action intent and target refs, but it does not currently capture canvas click coordinates. RuneLite exposes current mouse canvas position through `Client.getMouseCanvasPosition()`, and `MenuOptionClicked` fires for left-click actions as well as menu selections. The first implementation task must therefore extend click telemetry with a sanitized click point before likely-misclick inference can be reliable.

Relevant existing paths:

- `src/main/java/com/ticksense/runelite/TickSenseConfig.java` - current config surface.
- `src/main/java/com/ticksense/runelite/TickSensePlugin.java` - RuneLite event subscription and service wiring.
- `src/main/java/com/ticksense/runelite/RuneLiteEventAdapter.java` - maps RuneLite events into normalized telemetry.
- `src/main/java/com/ticksense/telemetry/events/PlayerActionTelemetryEvent.java` - normalized player action event.
- `src/main/java/com/ticksense/activities/OpportunityLifecycle.java` and `OpportunityMarker.java` - terminal opportunity evidence source.
- `src/main/java/com/ticksense/activities/execution/**` - reusable execution trackers for recovery, gear, movement, and prayer stubs.
- `src/main/java/com/ticksense/ui/**` - report panel and player-facing text.
- `src/test/resources/replays/` and `src/test/java/com/ticksense/replay/**` - replay/golden patterns.

## Planning Invariants

- Execution feedback is after-the-fact only. It may appear quickly, but only after TickSense has observed evidence that an action completed, failed, was corrected, or was missed.
- Do not implement prompts, countdowns, predictions, target highlights, tile markers, prayer instructions, or any message that tells the player what to do next.
- `Tick Perfect!` is only for confirmed zero-tick-loss completion. Do not reuse it for recovered, late, double-input, or likely-misclick outcomes.
- Misclick feedback must be phrased as `Likely misclick`, not as certainty.
- Likely-misclick detection requires a correction action and confirmation evidence. A near click alone is not enough.
- Misclick feedback must never tell the player to click again; it can only appear after the successful correction has already been observed.
- Keep inference conservative by default. Prefer missed detections over false accusations.
- Feedback rendering and sound belong in the RuneLite/UI layer. Activity strategies and execution trackers emit domain feedback events only.
- Sound must be separately configurable from visual text and disabled when the master feedback toggle is disabled.
- Mistake/correction feedback must be separately configurable from positive Perfect Tick feedback.
- Rate-limit feedback so repeated perfect or mistake events cannot spam the player.
- Normal reports must include feedback evidence where useful, even if immediate text/sound is disabled.
- Store feedback as local TickSense data only. Do not add networking, cloud sync, external services, or downloaded rules.
- Java remains Java 11 for RuneLite Plugin Hub compatibility.
- Add replay tests before enabling feedback from any real boss mechanic.

## Task Dependency Table

| ID | Completed | Title | Description | Github Issue # | Blocked By | Task File |
|---|---|---|---|---|---|---|
| T001 | [ ] | Feedback Domain Model | Add a core execution feedback event model with types for perfect, likely misclick, double input, late, missed, and recovered outcomes. |  | None | [`tasks/T001.md`](tasks/T001.md) |
| T002 | [ ] | Click Position Telemetry | Extend normalized player-action telemetry to include sanitized canvas click coordinates captured at click time. |  | T001 | [`tasks/T002.md`](tasks/T002.md) |
| T003 | [ ] | Feedback Config Surface | Add player-facing config for master feedback, perfect text, perfect sound, correction text, placement, and cooldown. |  | T001 | [`tasks/T003.md`](tasks/T003.md) |
| T004 | [ ] | Perfect Tick Feedback From Opportunities | Emit `Tick Perfect!` feedback from confirmed zero-tick-loss opportunity completions without rendering or sound. |  | T001, T003 | [`tasks/T004.md`](tasks/T004.md) |
| T005 | [ ] | Conservative Likely Misclick Detection | Detect likely prayer/inventory misclick correction patterns from click position, next intended action, and confirmation evidence. |  | T001, T002, T003 | [`tasks/T005.md`](tasks/T005.md) |
| T006 | [ ] | Feedback Rendering And Sound | Render configurable after-the-fact feedback text and play the tiny optional perfect sound from feedback events. |  | T003, T004 | [`tasks/T006.md`](tasks/T006.md) |
| T007 | [ ] | Reports And Replay Coverage | Add feedback entries to reports/debug replay fixtures and golden assertions for perfect and likely-misclick outcomes. |  | T004, T005 | [`tasks/T007.md`](tasks/T007.md) |

Task details live in separate files under `tasks/`, named by task ID.

## Final Notes

- Recommended implementation order: T001, T002, T003, T004, T005, T006, T007.
- Unresolved questions: exact RuneLite overlay/animation API choice for XP-drop-like text; whether sound uses RuneLite client audio APIs or a tiny bundled Java sound resource; exact prayer widget bounds source; whether inventory item bounds should rely on widget geometry or normalized widget snapshots.
- Risks: false-positive misclicks, feedback spam, accidentally drifting into live guidance, capturing too much pointer data, and brittle UI placement across fixed/resizable layouts.
- Areas that need human review before implementation: final wording of correction feedback, default config values, sound asset choice, and sensitivity thresholds for likely misclick classification.
