package com.ModSync;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ModSyncLogger {

    private final JavaPlugin plugin;
    private final File logFile;
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    public ModSyncLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        this.logFile = new File(logsFolder, "modsync.log");
    }

    public void log(String message) {
        checkAndRotate();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message + "\n";

        try (FileWriter fw = new FileWriter(logFile, true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logEntry);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not write to modsync.log", e);
        }
    }

    private void checkAndRotate() {
        if (logFile.exists() && logFile.length() > MAX_SIZE_BYTES) {
            rotateLog();
        }
    }

    private void rotateLog() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        File zipFile = new File(logFile.getParent(), "modsync-" + timestamp + ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos);
                FileInputStream fis = new FileInputStream(logFile)) {

            ZipEntry zipEntry = new ZipEntry(logFile.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();

            // Close streams before deleting
            fis.close();
            zos.close();
            fos.close();

            // Clear the original log file
            // We can delete and recreate, or just truncate.
            // Truncating is safer if we want to keep the file object valid, but deleting is
            // fine here.
            Files.delete(logFile.toPath());
            logFile.createNewFile();

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not rotate modsync.log", e);
        }
    }
}
