package fr.an.jvm.thread.tool.threaddumps.parser.sourcereader;

import java.io.File;
import java.io.IOException;

/**
 * 
 */
public class SourceReaderFactory {

    static final int MARK_BUFFER_SIZE = 30000;

    public SourceReaderFactory() {
   }

    public static SourceReader getSourceReader(String s) throws IOException {
        if(s == null)
            throw new IllegalArgumentException("Null parentDirectory is not allowed");
        File file = new File(s);
        if (!file.exists())
           throw new IllegalArgumentException("File not found: " + file.getCanonicalPath());
        if (file.isDirectory())
            return new DirectoryLNReader(file, null);
        int i = s.lastIndexOf('.');
        String s1 = "";
        if (i >= 0)
           s1 = s.substring(i).toLowerCase();
        if (s1.equals(".gz"))
            return new GZipFileSourceReader(new File(s), 30000);
        if (s1.equals(".zip"))
            return new ZipLNReader(new File(s), 30000);
        else
            return new PlainFileSourceReader(new File(s), 30000);
    }

}