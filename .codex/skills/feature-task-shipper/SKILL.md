---
name: feature-task-shipper
description: Choose, implement, and ship the next logical task from a large-feature-planning style feature bundle. Use when the user asks Codex to take the next feature/spec task through the full engineering workflow: select the next unblocked task, create a branch, implement only that task, validate, commit, push, open a GitHub PR, wait for checks, merge, and delete the branch.
---

# Feature Task Shipper

## Purpose

Use this skill to turn a planned feature task bundle into one merged PR. It assumes a repo contains `features/<feature-name>/tasks.md` plus `features/<feature-name>/tasks/T###.md` task files.

This skill is intentionally narrow: complete one task, keep the PR scoped, and leave the repo on the updated base branch after merge.

## Inputs

- Feature bundle path, if the user names one. Otherwise find `features/*/tasks.md`.
- Optional explicit task ID. If absent, choose the next unblocked incomplete task.
- GitHub remote with authenticated `gh`.

If multiple feature bundles exist and the user did not identify one, ask which bundle to use.

## Next Task Selection

Prefer the bundled helper:

```powershell
python "$env:CODEX_HOME\skills\feature-task-shipper\scripts\select_next_task.py" features\some-feature\tasks.md --json
```

When `CODEX_HOME` is unset, use `C:\Users\<user>\.codex\skills\feature-task-shipper\scripts\select_next_task.py`.

Selection rule:

1. Read the task table in `tasks.md` order.
2. Treat `[x]` as complete and `[ ]` as incomplete.
3. A task is unblocked when `Blocked By` is empty, `None`, or every listed task is complete.
4. Pick the first incomplete unblocked task.
5. If the user names a task, verify its blockers are complete before implementing.

If no task is unblocked, report the blocker chain and stop.

## Workflow

### 1. Preflight

- Read the selected task file completely.
- Read any repo instructions and relevant docs named by the task.
- Run `git status --short --branch`, `git remote -v`, `gh --version`, and `gh auth status`.
- Confirm the repo has a usable base branch. If the repo has no commits/default branch yet, create and push a baseline commit containing only pre-existing planning/docs before branching for the task.
- If the worktree has unrelated changes, do not stage or revert them. Either work around them or ask the user when scope is ambiguous.

### 2. Branch

- From the base branch, create `codex/<task-id-lower>-<short-title-slug>`, for example `codex/t001-plugin-hub-gradle-skeleton`.
- Emit the app git branch directive in the final response only after branch creation succeeds.

### 3. Implement One Task

- Make only the changes required by the selected task.
- Follow the task's out-of-scope section strictly.
- Do not opportunistically implement downstream tasks.
- If the selected task is too broad or hidden prerequisites appear, stop and explain the split rather than smuggling extra work into the PR.
- Update the feature bundle only for task status/completion notes that belong to this task.

Mark the task complete only after implementation and validation are good enough for merge. Add completion notes to the task file with branch, commit/PR, and validation.

### 4. Validate

- Run the task's required checks first.
- Run the narrowest meaningful project checks after that.
- If a required local tool is unavailable, look for an existing CI path. State the local limitation in the PR body and final response.
- Do not merge a PR with failing checks. Fix failures on the same branch, push, and wait again.
- If no automated checks exist, do not pretend validation exists; use static checks and state residual risk.

### 5. Commit

- Inspect `git diff --stat` and `git diff --check`.
- Stage only intended files. Use explicit paths unless the whole worktree is known to belong to the task.
- Commit with a terse message, usually the task title without the ID prefix.
- Emit the app git stage/commit directives in the final response only after those actions succeed.

### 6. PR

- Push with tracking:

```powershell
git push -u origin (git branch --show-current)
```

- Open a PR against the remote default branch with `gh pr create`.
- Use a ready PR only when the user requested merge; otherwise default to draft.
- PR body must include: what changed, why, validation, known local limitations, and issue/task ID.
- Emit the app PR directive in the final response only after PR creation succeeds.

### 7. Checks, Merge, Delete Branch

- Wait for checks:

```powershell
gh pr checks <pr-number> --watch --fail-fast
```

- If checks pass and the user requested merge, run:

```powershell
gh pr merge <pr-number> --merge --delete-branch
git switch <base-branch>
git pull --ff-only
git fetch --prune
```

- Delete the local branch if it still exists:

```powershell
git branch -d <branch>
```

- Verify `git status --short --branch` is clean on the base branch.

## Safety Rules

- Never use `git reset --hard`, `git checkout -- <file>`, or destructive cleanup unless the user explicitly requests it.
- Never stage unrelated user changes.
- Never merge with failing CI.
- Never edit task completion status for tasks not implemented in the PR.
- Never delete the remote branch before the PR is merged or closed.
- If merge conflicts occur, resolve only files in the selected task scope unless the user approves broader work.

## Final Response

Keep the final response short and include:

- selected task ID/title;
- branch name;
- commit hash;
- PR URL and merge commit;
- validation results;
- branch cleanup status;
- any local validation gaps.

Also emit applicable Codex app directives for successful branch, stage, commit, push, and PR actions.
