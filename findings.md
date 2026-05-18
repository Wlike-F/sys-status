# Findings

## Project Context

- Workspace: `C:\Users\19577\Desktop\sys-status`
- Current directory is empty.
- No existing Git repository is present.
- System `python` reports `Python 3.7.0`; the `py` launcher is not available.

## Initial Product Feasibility

- The idea is feasible and useful for a shared lab environment.
- The hard parts are not basic charts, but reliable multi-server collection, mapping resource usage to Linux users and processes, GPU attribution, refresh latency, and access control.
- A Spring Boot + Vue stack is reasonable for the management portal. The resource collector can be implemented as a lightweight Linux-side agent, SSH polling module, or hybrid collector.

## Open Requirements

- All project documents should be written in Chinese.
- Whether installing a collector service on each server is allowed.
- Users should be able to manually add servers in the LAN.
- Whether the page is only read-only monitoring or should support alerts/process kill/accounting later.
- Authentication source for lab users/admins.
- Refresh interval and historical retention expectations.

## Confirmed Server Inventory

| Server | OS | Hardware Focus | Notes |
|---|---|---|---|
| Server 1 | Linux | 8 x NVIDIA A100 | Need Linux user/session/process collection and GPU process attribution. |
| Server 2 | Windows | CPU-focused, ordinary GPU | Need Windows user/session/process collection; GPU detail may be basic or optional depending driver visibility. |
| Server 3 | Windows | 8 x NVIDIA P100 | Need Windows user/session/process collection and NVIDIA GPU process attribution. |

## Design Implications

- The collector layer must support both Linux and Windows.
- GPU collection should normalize NVIDIA A100/P100 metrics into one data model.
- Windows process/user attribution needs a different implementation path from Linux; likely PowerShell/WMI/Performance Counters plus `nvidia-smi` where NVIDIA GPUs exist.
- Linux collection can use `/proc`, `who`/`w`, `ps`, and `nvidia-smi`/NVML.
- Server management should include a manual "add server" flow for LAN hosts, including server name, IP/host, OS type, GPU type, connection mode, and collector status.
