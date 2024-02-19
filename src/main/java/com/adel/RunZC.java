package com.adel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
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
            final CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> run(args),
                    Executors.newVirtualThreadPerTaskExecutor());
            try {
                runAsync.get();
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Failed to process zerocopy, cause: "+e.getMessage());
            }
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

        System.out.println("fromFile: " + fromCS);
        System.out.println("toFile:   " + toCS);
        System.out.println(fromCS.equals(toCS) ? "File valid" : "File invalid");
    }

    private static String checkSumOf(final String filePath) {
        try (final SeekableByteChannel ch = Files.newByteChannel(Paths.get(filePath), StandardOpenOption.READ)) {

            final List<byte[]> dataStream = new LinkedList<>();
            final ByteBuffer bf = ByteBuffer.allocate(1000);
            while (ch.read(bf) > 0) {
                bf.flip();
                dataStream.add(bf.array());
                bf.clear();
            }

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (final byte[] byteArr : dataStream) {
                byteArrayOutputStream.write(byteArr);
            }

            byte[] hash = MessageDigest.getInstance("MD5").digest(byteArrayOutputStream.toByteArray());
            return new BigInteger(1, hash).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Failed to validate checksum, cause: " + e.getMessage());
        }
        return "";
    }


    private static void clearTerminalScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}