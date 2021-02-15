package fr.an.jvm.thread.tool.threaddumps.parser.sourcereader;

import java.io.*;

public class PlainFileSourceReader extends LineSourceReader implements SourceReader {

    public PlainFileSourceReader(File file, int i) throws FileNotFoundException {
        source = file;
        bufferSize = i;
        realReader = new LineNumberReader(new FileReader(file));
    }
}