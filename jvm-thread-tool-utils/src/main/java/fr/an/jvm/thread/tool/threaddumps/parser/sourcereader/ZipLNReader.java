package fr.an.jvm.thread.tool.threaddumps.parser.sourcereader;


import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 
 */
public class ZipLNReader implements SourceReader {

    File source;
    int bufferSize;
    LineNumberReader currentReader;
    private int baseOffsetPos;
    private ZipFile zipSource;
    private Enumeration<? extends ZipEntry> zipEntries;
    private boolean stillInSameFile;
    private String readAheadLine;
    
    public ZipLNReader(File file, int i) {
        stillInSameFile = false;
        source = file;
        bufferSize = i;
        try {
            zipSource = new ZipFile(file);
        } catch(IOException ioexception) {
            IllegalArgumentException iex = new IllegalArgumentException("Could not create a ZipFile");
            iex.initCause(ioexception);
            throw iex;
        }
        zipEntries = zipSource.entries();
    }

    public String getResourceName() {
        try {
            return source.getCanonicalPath();
        } catch(IOException ioexception) {
            return source.getPath();
        }
    }

    public long getPosition() {
        return currentReader != null ? baseOffsetPos + currentReader.getLineNumber() : -1L;
    }

    public void markPosition() throws IOException {
        if(currentReader != null) {
            currentReader.mark(bufferSize);
            stillInSameFile = true;
        }
    }

    public void resetPosition() throws IOException {
        if(currentReader == null)
            throw new IOException("No current file");
        if(!stillInSameFile) {
            throw new IOException("File cannot be reset. The mark is in the old file.");
        } else {
            currentReader.reset();
        }
    }

    public void resetPositionLine(String line) {
        this.readAheadLine = line;
    }
    
    public String readLine()throws IOException {
        if (readAheadLine != null) {
            String res = readAheadLine;
            readAheadLine = null;
            return res;
        }
        
        if(currentReader == null) {
            while(findNextReader()) {
                String s = currentReader.readLine();
                if (s != null)
                    return s;
            }
            return null;
        } else {
            String s1;
            for(s1 = currentReader.readLine(); s1 == null && findNextReader(); s1 = currentReader.readLine()) {
            }
            return s1;
        }
    }

    private boolean findNextReader() {
        for (;;) {
            if(!zipEntries.hasMoreElements())
                break; /* Loop/switch isn't completed */
            ZipEntry zipentry = (ZipEntry)zipEntries.nextElement();
            try {
                if(zipentry.isDirectory())
                    continue; /* Loop/switch isn't completed */
                if(zipentry.getName().lastIndexOf('/') != -1)
                    break;
                System.out.println("Acepted entry: " + zipentry.getName());
                currentReader = new LineNumberReader(new InputStreamReader(zipSource.getInputStream(zipentry)));
                return true;
            } catch(IOException ioexception) {
                System.out.println("Accepted but failed");
                ioexception.printStackTrace();
                System.out.println("Skipping non-root entry: " + zipentry.getName());
                continue; /* Loop/switch isn't completed */
            }
        }
        return false;
    }

    public void close() {
       if(currentReader != null) {
            baseOffsetPos += currentReader.getLineNumber();
            try {
                currentReader.close();
            } catch(IOException ioexception) {
                ioexception.printStackTrace();
            }
       }
        currentReader = null;
        stillInSameFile = false;
    }

}