package fr.an.jvm.thread.tool.flamegraph;


import com.sun.tools.attach.VirtualMachine;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import fr.an.jvm.thread.tool.threaddumps.parser.ThreadDumpListParser;
import fr.an.jvm.thread.tool.threaddumps.parser.ThreadDumpParser;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.BufferedSourceReader;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.SourceReader;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

// @Slf4j
public class FlameGraphApp {
    public static void main(String[] args) {
        new FlameGraphApp().run(args);
    }


    public  void run(String[] args) {
        String pid = args[0];

        VirtualMachine vm = attachVM(pid);
        HotSpotVirtualMachine hotspotVm = (HotSpotVirtualMachine) vm;

        long prevThreadDumpTime = System.currentTimeMillis();

        for(int i = 0; i < 100; i++) {
            long threadDumpTime = System.currentTimeMillis();

            long start = System.nanoTime();
            String threadDumpText = getThreadDump(hotspotVm);
            long threadNanos =  System.nanoTime() - start;


            // System.out.println(threadDump);
            System.out.println("..Thread dump took " + (threadNanos /1000) + " µ-sec");

            BufferedReader reader = new BufferedReader(new StringReader(threadDumpText));
            SourceReader sourceReader = new BufferedSourceReader(reader);
            ThreadDumpParser threadDumpParser = new ThreadDumpParser(sourceReader);

            ThreadDumpInfo threadDump;
            try {
                threadDump = threadDumpParser.readThreadDump(i, "dump from attachVM");
            } catch (IOException ex) {
                System.err.println("Failed to parse threadDump " + ex.getMessage());
                ex.printStackTrace(System.err);
                continue;
            }
            if (threadDump == null) {
                continue;
            }
            System.out.println(" .. " + threadDump.getThreads().size() + " threads");

            long threadDumpWeightMillis = threadDumpTime - prevThreadDumpTime;
            // TODO merge add Threads in FlameGraphNode, with weight=



            try {
                Thread.sleep(100);
            } catch(Exception ex) {
            }
            prevThreadDumpTime = threadDumpTime;
        }

        try {
            vm.detach();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VirtualMachine attachVM(String pid) {
        VirtualMachine vm;
        try {
            vm = VirtualMachine.attach(pid);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get attach(pid:" + pid + ")", ex);
        }
        return vm;
    }


    private String getThreadDump(HotSpotVirtualMachine vm) {
        StringBuilder threadDump = new StringBuilder();
        try (InputStream in = vm.remoteDataDump()) {
            byte[] b = new byte[4096];
            int n;
            do {
                n = in.read(b);
                if (n > 0) {
                    String s = new String(b, 0, n, StandardCharsets.UTF_8);
                    threadDump.append(s);
                }
            } while (n > 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get threadDump", e);
        }
        return threadDump.toString();
    }

}

