package com.adel;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * Compile JAR with
 * mvn -Pnative clean compile package
 * OR
 * mvn -Pnative -Dagent exec:exec@java-agent
 * mvn -Pnative -Dagent package
 */
public class RunZC {

    public static void main(String[] args) {
        clearTerminalScreen();
        System.out.println("--- ZeroCopy initialized ...");
        final long startTime = System.currentTimeMillis();

        try {
            run(args);
        } finally {
            System.out.println("--- ZeroCopy process time(ms): " + (System.currentTimeMillis() - startTime));
        }
    }

    private static void run(String[] args) {
        if (args.length < 2) {
            System.out.println("usage: RunZC <source> <destination> \n");
            return;
        }

        System.out.println("Handled thread: " + Thread.currentThread());
        final String fromFile = args[0];
        final String toFile = args[1];
        try (final ZeroCopyChannel zcc = new ZeroCopyChannel(fromFile, toFile)) {
            System.out.println("--- ZeroCopy Success...");
            zcc.close();
            validateChecksum(fromFile, toFile);
        } catch (Exception e) {
            System.out.println("--- ZeroCopy Failed...");
            e.printStackTrace();
        } finally {
            System.out.println("--- ZeroCopy Completed");
        }

    }

    private static void validateChecksum(String fromFile, String toFile) throws ExecutionException, InterruptedException {
        final CompletableFuture<String> processFromCS = CompletableFuture.supplyAsync(() -> checkSumOf(fromFile),
                Executors.newVirtualThreadPerTaskExecutor());
        final CompletableFuture<String> processToCS = CompletableFuture.supplyAsync(() -> checkSumOf(toFile),
                Executors.newVirtualThreadPerTaskExecutor());

        final String fromCS = processFromCS.get();
        final String toCS = processToCS.get();

        System.out.println("Source file: " + fromCS);
        System.out.println("Dest. File:   " + toCS);
        System.out.println(fromCS.equals(toCS) ? "File valid" : "File invalid");
    }

    private static String checkSumOf(final String filePath) {
        try (final InputStream is = Files.newInputStream(Paths.get(filePath))) {
            return DigestUtils.md5Hex(is);
        } catch (IOException e) {
            System.out.println("Failed to validate checksum, cause: " + e.getMessage());
        }
        return "";
    }

    private static void clearTerminalScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}