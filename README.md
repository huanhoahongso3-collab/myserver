# Minecraft Server Auto-Deploy

This repository automates the setup and deployment of a Minecraft server environment, ensuring a clean state and secure connectivity on every run.
Do not accept pull request. Feel free to use it

---

## 🛠 Prerequisites

To use this automation, you must add the following **GitHub Secrets** to your repository (**Settings > Secrets and variables > Actions**):

* **`PLAYIT_SECRET`**: Your [playit.gg](https://playit.gg) agent key to enable the global network tunnel.
* **`FINE_GRAINED_PAT`**: A GitHub Personal Access Token with repository permissions for handling builds and assets.

---

## 🚀 Deployment Process

The automation script performs the following tasks:

1.  **Dependency Management**:
    * Downloads the latest `spigot.jar`.
    * **Keeps** `server.jar` (the primary launcher).
    * **Keeps** the existing build artifacts.
2.  **Environment Cleanup**:
    * **Deletes** all world-related data (`world`, `world_nether`, `world_the_end`) to ensure a fresh start and prevent corruption from previous sessions.
3.  **Tunneling**:
    * Initializes the `playit` agent using your secret key to map your local port to a public IP.

---

## 🎮 Getting Started

1.  Push your changes or manually trigger the GitHub Action.
2.  Wait for the "Cleanup and Download" steps to complete.
3.  Once the server is live, use your Playit.gg address to connect.

**Enjoy your fresh server build!**

---

> [!NOTE]
> Ensure you have accepted the Minecraft EULA by setting `eula=true` in your configuration files before launching.
