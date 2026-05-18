# Sys Status Project Plan

## Goal
Design a Spring Boot + Vue web tool for monitoring three shared lab servers, including online/total users, CPU/GPU utilization, and per-user/per-process resource attribution. Produce development documentation, API documentation, and pre-development preparation steps before implementation.

## Phases

| Phase | Status | Notes |
|---|---|---|
| 1. Explore project context | complete | Workspace is empty and not yet a Git repository. |
| 2. Clarify product scope | complete | Confirmed mixed Linux/Windows environment, all-member visibility, manual LAN server add, and lightweight Agent direction. |
| 3. Propose approaches | complete | Documented remote polling, Agent, and hybrid options; recommended Agent. |
| 4. Present design for approval | complete | User asked to完善文档 after accepting lightweight Agent direction. |
| 5. Write project docs | complete | Created Chinese requirements, architecture, Agent, API, development prep, database, and GitHub docs. |
| 6. Initialize Git/GitHub workflow | in_progress | Local Git repo initialized and pushed to GitHub; adding docs index and encoding/editor safeguards. |

## Decisions

| Decision | Status | Rationale |
|---|---|---|
| Backend stack: Spring Boot | requested | User prefers Spring Boot. |
| Frontend stack: Vue | requested | User prefers Vue. |
| Documentation language: Chinese | requested | User explicitly requested Chinese documentation. |
| Collection model: lightweight Agent | accepted | User agreed that a lightweight collector is acceptable. |
| All lab members can view resource details | accepted | User wants everyone to see concrete server resource usage. |
| Implementation before design approval | deferred | Current turn focuses on documentation and GitHub preparation. |

## Errors Encountered

| Error | Attempt | Resolution |
|---|---|---|
| `session-catchup.py` failed with `SyntaxError` on walrus operator | Ran planning-with-files catchup using system `python` | Logged issue. Current `python` is 3.7.0; use a newer runtime for future Python tooling if needed. |
| `git status` failed: not a git repository | Checked Git state in workspace | Treat as new project directory; initialize later after agreed documentation scope. |
| `rg` failed with Access denied | Tried placeholder scan using `rg` | Switched to PowerShell `Select-String`. |
| GitHub push failed with `schannel` TLS handshake error | Ran `git push -u origin main` with global Git TLS backend | Set repository-local `http.sslBackend=openssl`, then push succeeded. |
