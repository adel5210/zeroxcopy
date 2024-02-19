package com.adel;

import java.io.IOException;

public class RunZC {
    public static void main(String[] args) {
        System.out.println("--- ZeroCopy initialized ...");
        final long startTime = System.currentTimeMillis();
        try {
            if (args.length < 2) {
                System.out.println("usage: RunZC <source> <destination> \n");
                return;
            }

            new ZeroCopyChannel().process(args[0], args[1]);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("--- ZeroCopy process time(ms): " + (System.currentTimeMillis() - startTime));
    }
}