package com.adel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ZeroCopyChannel {
    protected void process(final String from,
                           final String to) throws IOException {
        FileChannel source = null;
        FileChannel dest = null;
        try {
            source = new FileInputStream(from).getChannel();
            dest = new FileOutputStream(to).getChannel();
            source.transferTo(0, source.size(), dest);
        } finally {
            if (null != source) source.close();
            if (null != dest) dest.close();
        }
    }
}
