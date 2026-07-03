# Execution Tracker PR Slices

This feature area tracks reusable execution behavior that can run inside many activities without owning the activity lifecycle.

## Naming

- Activity lifecycle owner: `ActivityStrategy`.
- One detected run: `ActivitySession`.
- Reusable execution behavior: `ExecutionTracker`.
- Measurable emitted window: `Opportunity`.
- Shared opportunity state machine: `OpportunityLifecycle`.

## Package Layout

- `com.ticksense.activities.execution`: contracts, base lifecycle plumbing, sets, and presets.
- `com.ticksense.activities.execution.recovery`: food/potion/resource recovery trackers.
- `com.ticksense.activities.execution.equipment`: gear and equipment-switch trackers.
- `com.ticksense.activities.execution.prayer`: prayer cue/switch trackers.
- `com.ticksense.activities.execution.movement`: target re-engagement and movement response trackers.

## PR Order

| PR | Scope | Status | Notes |
|---|---|---|---|
| ET001 | Execution tracker foundation | Started | Adds `ExecutionTracker`, `AbstractExecutionTracker`, `ExecutionTrackerSet`, and `CommonExecutionTrackers` in the root execution package. |
| ET002 | Food recovery tracker | Started | Emits `food-recovery.FOOD_RECOVERY` from `execution.recovery` using local-player damage followed by known food consumption. |
| ET003 | Potion recovery tracker | Started | Emits `potion-recovery.POTION_RECOVERY` from `execution.recovery` using known potion consumption or dose-down inventory changes. |
| ET004 | Gear switch tracker | Started | Emits `gear-switch.GEAR_SWITCH` from `execution.equipment` using equipment-container item replacements. |
| ET005 | Gear switch attack tracker | Started | Emits `gear-switch-attack.GEAR_SWITCH_ATTACK` from `execution.equipment` using equipment switch followed by NPC attack. |
| ET006 | Target re-engagement tracker | Started | Emits `target-reengagement.TARGET_REENGAGEMENT` from `execution.movement` using target loss followed by NPC interaction/action. |
| ET007 | Movement response tracker | Started | Provides an explicit `openResponse` hook from `execution.movement` and completes on local-player movement. |
| ET008 | Prayer switch tracker | Stubbed | No-op in `execution.prayer` until normalized prayer widget/varbit telemetry exists. |
| ET009 | Activity adoption pass | Started | Araxxor wires reusable food/potion/gear/prayer/movement; Vardorvis and Inferno wire reusable food/potion/gear/prayer; Gem Mining wires skilling gear/movement while keeping its domain-specific movement-to-rock opportunity. |

## Current TODOs

- Food recovery: decide whether proactive eating is a separate opportunity or a completed unattributed recovery.
- Food recovery: use stronger health context before opening a recovery window.
- Potion recovery: split restoration, brew, offensive buff, and defensive buff semantics once item taxonomy is verified.
- Potion recovery: expand default IDs to include every dose variant from source-owned RuneLite ItemID fixtures.
- Gear switch: verify normalized equipment container IDs from real RuneLite fixtures.
- Gear switch attack: carry changed slot/item details once equipment deltas are normalized and stable across activities.
- Gear switch attack: treat special-attack clicks, spell casts, and activity-specific target actions as valid follow-ups.
- Prayer switch: add normalized prayer-state telemetry before emitting opportunities.
- Movement response: standardize who opens movement response windows for mechanics such as blood splats or tile dodges.
- Activity adoption: avoid double-emitting target or movement opportunities where an activity already has richer domain-specific trackers.

## Review Guidance

Each tracker PR should include focused tests for its own opportunity emissions plus one activity adoption test when the tracker first becomes used by an activity.
