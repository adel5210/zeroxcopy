package com.adel;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Compile JAR with
 * mvn clean compile package
 * mvn -Pnative -Dagent exec:exec@java-agent
 * mvn -Pnative -Dagent package
 */
public class RunZC {
    public static void main(String[] args) {
        System.out.println("--- ZeroCopy initialized ...");
        final long startTime = System.currentTimeMillis();
        if (args.length < 2) {
            System.out.println("usage: RunZC <source> <destination> \n");
            return;
        }
        final String fromFile = args[0];
        final String toFile = args[1];
        try (final ZeroCopyChannel zcc = new ZeroCopyChannel(fromFile, toFile)) {
            System.out.println("--- ZeroCopy Success...");

            //Generate checksum
            byte[] data = Files.readAllBytes(Paths.get(fromFile));
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            final String fromCheckSum = new BigInteger(1, hash).toString(16);

            data = Files.readAllBytes(Paths.get(toFile));
            hash = MessageDigest.getInstance("MD5").digest(data);
            final String toCheckSum = new BigInteger(1, hash).toString(16);

            System.out.println("fromFile: " + fromCheckSum);
            System.out.println("toFile:   " + toCheckSum);
            System.out.println(fromCheckSum.equals(toCheckSum) ? "Valid" : "Invalid");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("--- ZeroCopy process time(ms): " + (System.currentTimeMillis() - startTime));
    }
}