package fr.an.jvm.thread.tool.threaddumps.parser.sourcereader;

import java.io.BufferedReader;
import java.io.IOException;

public class BufferedSourceReader implements SourceReader {
    
    private BufferedReader lineReader;
    private int currLineNumber;
    private int currPos;
    
    // ------------------------------------------------------------------------
    
    public BufferedSourceReader(BufferedReader lineReader) {
        this.lineReader = lineReader;
    }

    // ------------------------------------------------------------------------

    public int getCurrLineNumber() {
        return currLineNumber;
    }

    public int getCurrPos() {
        return currPos;
    }
    
    @Override
    public String readLine() throws IOException {
        String res = lineReader.readLine();
        if (res != null) {
            currLineNumber++;
            currPos += res.length();
        }
        return res;
    }

    @Override
    public void close() {
        if (lineReader != null) {
            try {
                lineReader.close();
            } catch (IOException e) {
            }
            lineReader = null;
        }
    }

    @Override
    public String getResourceName() {
        return null;
    }

    @Override
    public long getPosition() {
        return currLineNumber;
    }

    @Override
    public void markPosition() throws IOException {
        lineReader.mark(100000);
    }

    @Override
    public void resetPosition() throws IOException {
        lineReader.reset();
    }

    @Override
    public void resetPositionLine(String line) {
        throw new UnsupportedOperationException();
    }
    
}
