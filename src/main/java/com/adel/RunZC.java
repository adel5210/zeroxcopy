package com.adel;

import java.io.IOException;

public class RunZC {
    public static void main(String[] args) {
        System.out.println("--- ZeroCopy initialized ...");
        final long startTime = System.currentTimeMillis();
        if (args.length < 2) {
            System.out.println("usage: RunZC <source> <destination> \n");
            return;
        }
        try(final ZeroCopyChannel zcc = new ZeroCopyChannel(args[0], args[1])){
            System.out.println("--- ZeroCopy Success...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("--- ZeroCopy process time(ms): " + (System.currentTimeMillis() - startTime));
    }
}