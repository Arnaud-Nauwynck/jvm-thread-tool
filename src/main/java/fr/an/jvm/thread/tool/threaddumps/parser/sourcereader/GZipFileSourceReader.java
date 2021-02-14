package fr.an.jvm.thread.tool.threaddumps.parser.sourcereader;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * 
 */
public class GZipFileSourceReader extends LineSourceReader implements SourceReader {

    public GZipFileSourceReader(File file, int i) {
        source = file;
        bufferSize = i;
        try {
            realReader = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
        } catch(IOException ioexception) {
           IllegalArgumentException illegalargumentexception = new IllegalArgumentException("Could not create GZipFileSourceReader");
            illegalargumentexception.initCause(ioexception);
            throw illegalargumentexception;
        }
    }
}
