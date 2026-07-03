# Execution Tracker PR Slices

This feature area tracks reusable execution behavior that can run inside many activities without owning the activity lifecycle.

## Naming

- Activity lifecycle owner: `ActivityStrategy`.
- One detected run: `ActivitySession`.
- Reusable execution behavior: `ExecutionTracker`.
- Measurable emitted window: `Opportunity`.
- Shared opportunity state machine: `OpportunityLifecycle`.

## PR Order

| PR | Scope | Status | Notes |
|---|---|---|---|
| ET001 | Execution tracker foundation | Started | Adds `ExecutionTracker`, `AbstractExecutionTracker`, `ExecutionTrackerSet`, and `CommonExecutionTrackers`. |
| ET002 | Food recovery tracker | Started | Emits `food-recovery.FOOD_RECOVERY` from local-player damage followed by known food consumption. |
| ET003 | Gear switch tracker | Started | Emits `gear-switch.GEAR_SWITCH` from equipment-style inventory replacements. |
| ET004 | Target re-engagement tracker | Started | Emits `target-reengagement.TARGET_REENGAGEMENT` from target loss followed by NPC interaction/action. |
| ET005 | Movement response tracker | Started | Provides an explicit `openResponse` hook and completes on local-player movement. |
| ET006 | Prayer switch tracker | Stubbed | No-op until normalized prayer widget/varbit telemetry exists. |
| ET007 | Activity adoption pass | Started | Araxxor wires reusable food/gear/prayer/movement; Vardorvis wires reusable food/gear/prayer; Gem Mining wires skilling gear/movement while keeping its domain-specific movement-to-rock opportunity. |

## Current TODOs

- Food recovery: decide whether proactive eating is a separate opportunity or a completed unattributed recovery.
- Food recovery: use stronger health context before opening a recovery window.
- Gear switch: replace quantity/item replacement heuristic with verified equipment container IDs.
- Prayer switch: add normalized prayer-state telemetry before emitting opportunities.
- Movement response: standardize who opens movement response windows for mechanics such as blood splats or tile dodges.
- Activity adoption: avoid double-emitting target or movement opportunities where an activity already has richer domain-specific trackers.

## Review Guidance

Each tracker PR should include focused tests for its own opportunity emissions plus one activity adoption test when the tracker first becomes used by an activity.
