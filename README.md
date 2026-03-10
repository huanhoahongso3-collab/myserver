# Minecraft Server Auto-Deploy

This repository automates the setup and deployment of a Minecraft server environment, ensuring a clean state and secure connectivity on every run.

> [!WARNING]
> Do not accept pull request. Feel free to use it

> [!NOTE]
> This only can be run in 5 hours. To continue, trigger a new workflow to play. Game is automatically saved after 1 min and after stopping the server


---

## 🛠 Prerequisites

To use this automation, you must add the following **GitHub Secrets** to your repository (**Settings > Secrets and variables > Actions**):

* **`PLAYIT_SECRET`**: Your [playit.gg](https://playit.gg) agent secret key to enable the global network tunnel. Usually be found after using playit-gg client.
* **`FINE_GRAINED_PAT`**: A GitHub Personal Access Token with repository permissions for handling builds and assets.

---

## 🚀 How to make it work:

Please do these tasks to make sure it works perfectly

1.  **Dependency Management**:
    * Fork this repo
    * Downloads the latest server file and save it as `spigot.jar`.
    * **Keeps** `server.jar` (the primary launcher).
    * **Keeps** the existing new.yaml (workflow) file
2.  **Environment Cleanup**:
    * **Deletes** all my world-related data (`world`, `world_nether`, `world_the_end`) to ensure a fresh start and prevent corruption from previous sessions.
3.  **Starting**:
    * Now you can start your server.

---

## 🎮 Getting Started

1.  Manually trigger the GitHub Action using workflow_dispatch
2.  Once the server is live, use your Playit.gg address to connect.
3.  Enjoy!

**Enjoy your fresh server build!**

---

> [!NOTE]
> Ensure you have accepted the Minecraft EULA by setting `eula=true` in your configuration files before launching.
