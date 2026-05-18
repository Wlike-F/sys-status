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
| 7. Update technical constraints | complete | Updated docs and server config for Java 17, MySQL 5.7.35, and local passwordless database login. |
| 8. Create project skeleton | complete | Created Maven multi-module Java skeleton and Vue skeleton; `mvn test` and `npm run build --prefix web` passed. |
| 9. Backend basic APIs | complete | Implemented server management APIs, schema initialization, and integration tests. |
| 10. Agent communication loop | complete | Implemented Agent register, heartbeat, minimal CPU/memory snapshot loop, and verified against local MySQL. |

## Decisions

| Decision | Status | Rationale |
|---|---|---|
| Backend stack: Spring Boot | requested | User prefers Spring Boot. |
| Frontend stack: Vue | requested | User prefers Vue. |
| Documentation language: Chinese | requested | User explicitly requested Chinese documentation. |
| Collection model: lightweight Agent | accepted | User agreed that a lightweight collector is acceptable. |
| All lab members can view resource details | accepted | User wants everyone to see concrete server resource usage. |
| Java version: 17 | requested | User updated project runtime requirement. |
| MySQL version: 5.7.35 | requested | User updated database version. |
| Local MySQL passwordless login | verified | MySQL 5.7.35 root login without password works locally. |
| Database name: `sys-status` | requested | User explicitly asked to create this database; SQL must quote it because of the hyphen. |
| Implementation scope for this turn | accepted | Only update constraints and create skeleton; do not implement business APIs yet. |

## Errors Encountered

| Error | Attempt | Resolution |
|---|---|---|
| `session-catchup.py` failed with `SyntaxError` on walrus operator | Ran planning-with-files catchup using system `python` | Logged issue. Current `python` is 3.7.0; use a newer runtime for future Python tooling if needed. |
| `git status` failed: not a git repository | Checked Git state in workspace | Treat as new project directory; initialize later after agreed documentation scope. |
| `rg` failed with Access denied | Tried placeholder scan using `rg` | Switched to PowerShell `Select-String`. |
| GitHub push failed with `schannel` TLS handshake error | Ran `git push -u origin main` with global Git TLS backend | Set repository-local `http.sslBackend=openssl`, then push succeeded. |
| `mvn test` failed because `maven-compiler-plugin 3.13.0` requires Maven 3.6.3 | Ran initial Maven verification on local Maven 3.6.2 | Downgraded compiler plugin to 3.11.0 for local compatibility. |
| MySQL database creation failed because PowerShell consumed backticks around `sys-status` | Ran `mysql -u root -e 'CREATE DATABASE ... `sys-status` ...'` | Re-ran with escaped PowerShell backticks: ````sys-status````. |
| `mvn spring-boot:run` from root failed to find main class | Started Spring Boot plugin from parent POM | Installed multi-module artifacts and ran Spring Boot from `server/` module. |
| `mvn -f agent/pom.xml exec:java -Dexec.args=...` was misparsed on PowerShell | Tried to run Agent with URL-containing args directly in PowerShell | Ran the Maven exec command through `cmd.exe /c` with quoted `-Dexec.args`. |
