package fr.an.jvm.thread.tool.threaddumps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.an.jvm.thread.tool.threaddumps.analyzer.ThreadDumpUtils;
import fr.an.jvm.thread.tool.threaddumps.model.ClassHistogramInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpList;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadItemInfoVisitor;
import fr.an.jvm.thread.tool.threaddumps.parser.ThreadDumpListParser;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.SourceReader;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.SourceReaderFactory;
import fr.an.jvm.thread.tool.threaddumps.util.ClassHistogramInfoListIncrPrinter;
import fr.an.jvm.thread.tool.threaddumps.util.ClassHistogramInfoListPrinter;
import fr.an.jvm.thread.tool.threaddumps.util.DefaultThreadDumpPrinter;


/**
 * Thread Dump extractor
 */
public class Main {

    private static final String NL = "\n";

    // -------------------------------------------------------------------------
    
    private String inputFile;
    private File outputDir;
    private String outputFileNamePrefix;
    private String outputThreadDumpSuffix = "-threaddump";

    private boolean dumpFull = false;
    private boolean dumpShort = true;

    /**
     * format for output: one of "txt" / "xml"
     */
    private String dumpFormat = "txt";
    
    
    // -------------------------------------------------------------------------

    public Main() {
    }

    // -------------------------------------------------------------------------
    
    public static void main(String args[]) throws IOException {
        try {
            System.out.println(NL + "ThreadDumpAnalyzerMain Started at: " + new Date());

           new Main().run(args);

            System.out.println(NL + "ThreadDumpAnalyzerMain Finished at: " + new Date());
            System.exit(0);
        } catch(Exception ex) {
            System.err.println(NL + "ERROR - ThreadDumpAnalyzerMain Finished at: " + new Date());
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
    
    public void run(String args[]) throws IOException {
        
        if(args.length < 1)
        {
            // System.err.println("Article: http://dev2dev.bea.com/produts/wlplatform81/articles/thread_dumps.jsp");
            System.err.println("Usage: java -jar <<threaddump.jar>> <input_dumpfile> [--out <output_fileprefix>][--dump-full][--no-dump-short][--format txt/xml]");
            System.exit(1);
        }

        inputFile = args[0];

        for (int i = 1; i < args.length; i++) {
            String opt = args[i];
            if (opt.equals("--out")) {
                if (i+1>=args.length) throw new IllegalArgumentException("missing arg");
                String arg = args[++i];
                outputFileNamePrefix = arg;
            } else if (opt.equals("--dump-full")) {
                dumpFull = true;
            } else if (opt.equals("--no-dump-short")) {
                dumpShort = true;
            } else if (opt.equals("--format")) {
                if (i+1>=args.length)throw new IllegalArgumentException("missing arg");
                String arg = args[++i];
                dumpFormat = arg;
            } else {
                throw new IllegalArgumentException("unrecognized option '" + opt + "'");
            }
        }
        
           if (outputFileNamePrefix == null) {
//            println("Output fileName prefix is missing. Assuming 'dump'");
//            if (new File(inputFile).isDir()) {
//                outputFilePrefix = args[1] + "dump";
//            }
            outputFileNamePrefix = new File(inputFile).getName();
            int indexLastDot = outputFileNamePrefix.lastIndexOf('.');
            if (indexLastDot != -1) {
                outputFileNamePrefix = outputFileNamePrefix.substring(0, indexLastDot);
            }
           }

        outputDir = new File(inputFile).getParentFile();
        
        
        ThreadDumpList threadDumpList = new ThreadDumpList();
        
        // Parsing
        println("Parsing ThreadDumps started at: " + new Date());
        println("Extracting from: " + inputFile);
        println("Output dir is:" + outputDir);
        println("Output file name prefix is: " + outputFileNamePrefix);
        
        SourceReader sourcereader = SourceReaderFactory.getSourceReader(inputFile);
        ThreadDumpListParser parser = new ThreadDumpListParser(threadDumpList, sourcereader); 
        try {
            parser.parse();
        } finally {
            if (sourcereader != null) sourcereader.close();
        }
        
        println(NL + "Parsing ThreadDumps finished at: " + new Date());
        if(threadDumpList.getThreadDumps().size() == 0) {
            println(NL + "No Dumps found");
        }
        
        // TODO analyse ThreadDump to print summary ...
        
        
        // Dump Full (List<ThreadDump> before simplification) 
        if (dumpFull) {
            dump(threadDumpList, outputDir, outputFileNamePrefix, inputFile);
        }

        ThreadDumpUtils.simplifyThreadDumps(threadDumpList);
        
        
        // Dump Short (List<ThreadDump> after simplification) 
       if (dumpShort) {
            dump(threadDumpList, outputDir, outputFileNamePrefix+"-short", inputFile);
        }
        
        if (threadDumpList.getThreadDumps().size() != 0 && threadDumpList.get(0).getClassHistogramInfo() != null) {
            // dump clasHistograms in csv file
            List<ClassHistogramInfo> ls = new ArrayList<ClassHistogramInfo>();
            for(ThreadDumpInfo td : threadDumpList.getThreadDumps()) {
                if (td.getClassHistogramInfo() != null) {
                    ls.add(td.getClassHistogramInfo());
                }
            }
            File fileClassHistoByteSize = new File(outputDir, outputFileNamePrefix+ "-classHisto-size.csv");
            ClassHistogramInfoListPrinter.dumpClassHistoCsvFile(fileClassHistoByteSize, ls, true, false);

//            File fileClasHistoByteSizeIncr = new File(outputDir, outputFileNamePrefix+ "-classHisto-size-incr.csv");
//            ClassHistogramInfoListPrinter.dumpClassHistoCsvFile(fileClassHistoByteSizeIncr, ls, true, true);

            File fileClassHistoInstanceCount = new File(outputDir, outputFileNamePrefix+ "-classHisto-count.csv");
            ClassHistogramInfoListPrinter.dumpClassHistoCsvFile(fileClassHistoInstanceCount, ls, false, false);
            
//            File fileClassHistoInstanceCountIncr = new File(outputDir, outputFileNamePrefix+ "-cassHisto-count-incr.csv");
//            ClassHistogramInfoListPrinter.dumpClassHistoCsvFile(fileClassHistoInstanceCountIncr, ls, false, true);

            
            File fileClassHistoByteSizeIncr2 = new File(outputDir, outputFileNamePrefix+ "-classHisto-size-incr-cass.csv");
            ClassHistogramInfoListIncrPrinter.dumpClassHistoCsvFile(fileClassHistoByteSizeIncr2, ls, true);

            File fileClassHistoInstanceCountIncr2 = new File(outputDir, outputFileNamePrefix+ "-classHisto-count-incr-class.csv");
            ClassHistogramInfoListIncrPrinter.dumpClassHistoCsvFile(fileClassHistoInstanceCountIncr2, ls, true);

        }
    }

    private void dump(ThreadDumpList threadDumpList, File outputDir, String outputFileName, String sourceName) throws IOException {
        if (dumpFormat.equals("txt")) {
            dumpTxt(threadDumpList, outputDir, outputFileName, sourceName);
        } else {
            System.err.println("unrecognized print format '" + dumpFormat + "', expecting 'txt' ... using default : 'txt'");
            dumpTxt(threadDumpList, outputDir, outputFileName, sourceName);
        }
    }
    
    private void dumpTxt(ThreadDumpList threadDumpList, File outputDir, String outputFileName, String sourceName) throws IOException {
        File file = new File(outputDir, outputFileName + outputThreadDumpSuffix + ".txt");
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(file));

            ThreadItemInfoVisitor printVisitor = new DefaultThreadDumpPrinter(out);
            printVisitor.caseThreadDumpList(threadDumpList);

            out.flush();
            
        } finally {
            out.close();
        }
    }

    private void println(String s) {
        System.out.println(s);
    }

}
    


