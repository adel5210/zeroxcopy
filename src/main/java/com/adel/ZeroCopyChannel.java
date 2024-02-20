package com.adel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZeroCopyChannel implements AutoCloseable {

    private final FileChannel source;
    private final FileChannel dest;
    private final ExecutorService displayService;

    public ZeroCopyChannel(final String fromFile, final String toFile) throws IOException {
        this.source = new FileInputStream(fromFile).getChannel();
        this.dest = new FileOutputStream(toFile).getChannel();
        this.displayService = Executors.newVirtualThreadPerTaskExecutor();

        CompletableFuture.runAsync(() -> {
            while (true) {
                try {

                    double ratio = (double) dest.size() / source.size();
                    int percent = (int) (ratio * 100);
                    System.out.println("File transferred: " + percent + "% ");

                    if (source.size() == dest.size()) break;

                    Thread.sleep(3000);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, displayService);

        source.transferTo(0, source.size(), dest);
    }

    @Override
    public void close() throws Exception {
        if (null != source) source.close();
        if (null != dest) dest.close();
        if (null != displayService) displayService.close();

    }
}
