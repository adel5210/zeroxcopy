package com.adel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ZeroCopyChannel implements AutoCloseable {

    private final FileChannel source;
    private final FileChannel dest;

    public ZeroCopyChannel(final String fromFile, final String toFile) throws IOException {
        this.source = new FileInputStream(fromFile).getChannel();
        this.dest = new FileOutputStream(toFile).getChannel();
        source.transferTo(0, source.size(), dest);
    }

    @Override
    public void close() throws Exception {
        if (null != source) source.close();
        if (null != dest) dest.close();
    }
}
