package fr.an.jvm.thread.tool.threaddumps.parser.sourcereader;


import java.io.*;

/**
 * 
 */
public class LineSourceReader implements SourceReader {

    protected File source;
    protected LineNumberReader realReader;
    protected int bufferSize;
    private String readAheadLine;
    
    public LineSourceReader() {
    }

    public String getResourceName() {
        try {
            return source.getCanonicalPath();
        } catch(IOException ioexception) {
        return source.getPath();
        }
    }

    public long getPosition() {
        return realReader.getLineNumber();
    }

    public void markPosition() throws IOException {
        realReader.mark(bufferSize);
    }

    public void resetPosition() throws IOException {
        realReader.reset();
    }

    public void resetPositionLine(String line) {
        this.readAheadLine = line;
    }
    
    public String readLine() throws IOException {
        String res;
        if (readAheadLine != null) {
           res = readAheadLine;
            readAheadLine = null;
        } else {
            res = realReader.readLine();
        }
        return res;
    }

    public void close() {
        try {
            realReader.close();
        } catch(IOException ioexcetion) { 
        }
    }

}