import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        // 1. Get Secrets from Environment
        String playitSecret = System.getenv("PLAYIT_SECRET");
        String zrokSecret = System.getenv("ZROK_SECRET");

        if (playitSecret == null || playitSecret.isEmpty()) {
            System.err.println("[Wrapper] ERROR: PLAYIT_SECRET environment variable is not set!");
            System.exit(1);
        }

        // 2. Playit Configuration
        ProcessBuilder playitBuilder = new ProcessBuilder(
            "./playit-linux-amd64", 
            "--secret", playitSecret
        );

        // 3. Zrok Configuration (Using the exact reserved name)
        // Command: zrok share reserved dhposserver --headless
        ProcessBuilder zrokBuilder = new ProcessBuilder(
            "zrok", "share", "reserved", "dhposserver", "--headless"
        );

        // 4. Spigot Configuration (Aikar's Flags for performance)
        ProcessBuilder spigotBuilder = new ProcessBuilder(
            "java", "-Xms4G", "-Xmx7G",
            "-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled", "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions", "-XX:+DisableExplicitGC", "-XX:+AlwaysPreTouch",
            "-XX:G1NewSizePercent=30", "-XX:G1MaxNewSizePercent=40", "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20", "-XX:G1HeapWastePercent=5", "-XX:G1MixedGCCountTarget=4",
            "-XX:InitiatingHeapOccupancyPercent=15", "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5", "-XX:SurvivorRatio=32", "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1", "-Dusing.aikars.flags=https://mcflags.emc.gs",
            "-Daikars.new.flags=true", "-jar", "spigot.jar", "nogui"
        );

        Process playitProcess = null;
        Process zrokProcess = null;
        Process spigotProcess = null;

        try {
            // Start Playit Tunnel
            System.out.println("[Wrapper] Starting Playit...");
            playitProcess = playitBuilder.start();
            startLoggingThread(playitProcess, "Playit");

            // Enable Zrok and Start Tunnel
            if (zrokSecret != null && !zrokSecret.isEmpty()) {
                System.out.println("[Wrapper] Authenticating Zrok...");
                executeSimpleCommand("zrok", "enable", zrokSecret);
                
                System.out.println("[Wrapper] Starting Zrok (dhposserver) -> https://dhposserver.share.zrok.io");
                zrokProcess = zrokBuilder.start();
                startLoggingThread(zrokProcess, "Zrok");
            } else {
                System.err.println("[Wrapper] WARNING: ZROK_SECRET not found. HTTP tunnel will not start.");
            }

            // Start Minecraft Server
            System.out.println("[Wrapper] Starting Spigot...");
            spigotProcess = spigotBuilder.start();
            startLoggingThread(spigotProcess, "Server");

            // 5. Automated Sync Loop (Every 56 seconds)
            final Process finalSpigot = spigotProcess;
            Thread syncThread = new Thread(() -> {
                while (finalSpigot.isAlive()) {
                    try {
                        Thread.sleep(56000); // 56 second interval
                        if (finalSpigot.isAlive()) {
                            runSync();
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            syncThread.setDaemon(true);
            syncThread.start();

            // Shutdown Hook for Graceful Exit
            Process pProc = playitProcess;
            Process zProc = zrokProcess;
            Process sProc = spigotProcess;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[Wrapper] Shutdown signal detected. Cleaning up processes...");
                if (pProc != null) pProc.destroy();
                if (zProc != null) zProc.destroy();
                if (sProc != null) sProc.destroy();
            }));

            // Block until Spigot closes
            spigotProcess.waitFor();
            System.out.println("[Wrapper] Spigot has stopped.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Final cleanup in case of crash
            if (playitProcess != null && playitProcess.isAlive()) playitProcess.destroy();
            if (zrokProcess != null && zrokProcess.isAlive()) zrokProcess.destroy();
            System.out.println("[Wrapper] Cleanup finished. Goodbye.");
            System.exit(0);
        }
    }

    private static void runSync() {
        System.out.println("[Sync] Pushing updates to GitHub...");
        try {
            executeSimpleCommand("git", "config", "--local", "user.name", "github-actions");
            executeSimpleCommand("git", "config", "--local", "user.email", "github-actions@github.com");
            executeSimpleCommand("git", "add", ".");
            executeSimpleCommand("git", "commit", "-m", "Automated sync: " + new Date());
            executeSimpleCommand("git", "push", "origin", "playit-only", "--force");
        } catch (Exception e) {
            System.out.println("[Sync] Failed or nothing to commit: " + e.getMessage());
        }
    }

    private static void executeSimpleCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Filter out noise from Git
                if (!line.toLowerCase().contains("nothing to commit") && 
                    !line.toLowerCase().contains("working tree clean")) {
                    System.out.println("[Cmd-Log] " + line);
                }
            }
        }
        p.waitFor();
    }

    private static void startLoggingThread(Process process, String prefix) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[" + prefix + "] " + line);
                }
            } catch (IOException e) {
                // Stream closed
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
