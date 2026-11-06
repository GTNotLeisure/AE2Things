package com.asdflj.ae2thing.coremod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public enum OptionalModules {

    APPBOT("ae2thing_ab", "Botania");

    private static final String MOD_STATES_PROPERTY = "fml.modStates";
    private static final String MC_MOD_INFO = "mcmod.info";
    private static final Pattern MOD_ID_PATTERN = Pattern.compile("\"modid\"\\s*:\\s*\"([^\"]+)\"");

    private static File gameDir;
    private static List<File> candidates;

    private final String module;
    private final String[] dependencies;

    OptionalModules(String module, String... dependencies) {
        this.module = module;
        this.dependencies = dependencies;
    }

    public static void init(File mcDir) {
        gameDir = mcDir;
        candidates = null;
        applyModStates();
    }

    public static boolean hasMod(String modId) {
        if (modId == null || modId.isEmpty()) {
            return false;
        }
        for (File file : collectCandidates()) {
            if (declaresMod(file, modId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAvailable() {
        for (String dependency : dependencies) {
            if (!hasMod(dependency)) {
                return false;
            }
        }
        return true;
    }

    private static void applyModStates() {
        final String forced = System.getProperty(MOD_STATES_PROPERTY, "");
        final StringBuilder merged = new StringBuilder();
        for (String entry : forced.split("[;:]")) {
            final String trimmed = entry.trim();
            final int separator = trimmed.indexOf('=');
            if (separator <= 0 || isManaged(trimmed.substring(0, separator))) {
                continue;
            }
            if (!merged.isEmpty()) {
                merged.append(';');
            }
            merged.append(trimmed);
        }
        for (OptionalModules mod : values()) {
            if (mod.isAvailable()) {
                continue;
            }
            if (!merged.isEmpty()) {
                merged.append(';');
            }
            merged.append(mod.module)
                .append("=false");
        }
        if (!forced.contentEquals(merged)) {
            System.setProperty(MOD_STATES_PROPERTY, merged.toString());
        }
    }

    private static boolean isManaged(String modId) {
        for (OptionalModules mod : values()) {
            if (mod.module.equals(modId)) {
                return true;
            }
        }
        return false;
    }

    private static List<File> collectCandidates() {
        if (candidates != null) {
            return candidates;
        }
        final List<File> found = new ObjectArrayList<>();
        final String classPath = System.getProperty("java.class.path", "");
        for (String entry : classPath.split(File.pathSeparator)) {
            if (!entry.isEmpty()) {
                found.add(new File(entry));
            }
        }
        if (gameDir != null) {
            final File[] modDirs = { new File(gameDir, "mods"), new File(gameDir, "mods/1.7.10") };
            for (File dir : modDirs) {
                final File[] files = dir.listFiles();
                if (files != null) {
                    Collections.addAll(found, files);
                }
            }
        }
        candidates = found;
        return candidates;
    }

    private static boolean declaresMod(File candidate, String modId) {
        if (candidate.isDirectory()) {
            return declaresModInFile(new File(candidate, MC_MOD_INFO), modId);
        }
        if (!candidate.isFile() || !candidate.getName()
            .toLowerCase(Locale.ROOT)
            .endsWith(".jar")) {
            return false;
        }
        try (ZipFile zip = new ZipFile(candidate)) {
            final ZipEntry entry = zip.getEntry(MC_MOD_INFO);
            if (entry == null) {
                return false;
            }
            try (InputStream stream = zip.getInputStream(entry)) {
                return declaresMod(readFully(stream), modId);
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean declaresModInFile(File infoFile, String modId) {
        if (!infoFile.isFile()) {
            return false;
        }
        try (InputStream stream = new FileInputStream(infoFile)) {
            return declaresMod(readFully(stream), modId);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean declaresMod(String info, String modId) {
        final Matcher matcher = MOD_ID_PATTERN.matcher(info);
        while (matcher.find()) {
            if (modId.equalsIgnoreCase(matcher.group(1))) {
                return true;
            }
        }
        return false;
    }

    private static String readFully(InputStream stream) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }
        return out.toString(StandardCharsets.UTF_8);
    }
}
