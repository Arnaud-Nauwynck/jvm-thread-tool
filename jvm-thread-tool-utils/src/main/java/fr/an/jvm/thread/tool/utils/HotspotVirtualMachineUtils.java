package fr.an.jvm.thread.tool.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import com.sun.tools.attach.VirtualMachine;

import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import fr.an.jvm.thread.tool.threaddumps.parser.ThreadDumpParser;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.BufferedSourceReader;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.SourceReader;
import sun.tools.attach.HotSpotVirtualMachine;

public class HotspotVirtualMachineUtils {


    public static HotSpotVirtualMachine attachVM(int pid) {
        VirtualMachine vm;
        try {
            vm = VirtualMachine.attach(Integer.toString(pid));
        } catch (Exception ex) {
            throw new RuntimeException("Failed attachVM(pid:" + pid + ")", ex);
        }
        return (HotSpotVirtualMachine) vm;
    }

	public static void detachVm(HotSpotVirtualMachine vm) throws IOException {
        vm.detach();
	}

    
    public static String getThreadDumpText(HotSpotVirtualMachine vm) {
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
            throw new RuntimeException("Failed to vm.remoteDataDump", e);
        }
        return threadDump.toString();
    }

    public static ThreadDumpInfo getThreadDump(HotSpotVirtualMachine vm) {
	    String threadDumpText = getThreadDumpText(vm);
	
	    BufferedReader reader = new BufferedReader(new StringReader(threadDumpText));
	    SourceReader sourceReader = new BufferedSourceReader(reader);
	    ThreadDumpParser threadDumpParser = new ThreadDumpParser(sourceReader);
	
	    ThreadDumpInfo res;
		try {
			res = threadDumpParser.readThreadDump(0, "dump from attachVM");
		} catch (IOException e) {
            throw new RuntimeException("Failed to parse ThreadDump", e);
		}
	    return res;
    }

}
