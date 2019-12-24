package org.vps.pa;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class MethodCountAgent {

    public static final String RX_FILE = "rf";
    public static final String LOG_FILE = "lf";
    public static final String PORT = "lp";

    public static PrintStream log;

    @SuppressWarnings({"MagicNumber"})
    public static int listenPort = 7455;

    @SuppressWarnings({"MagicNumber"})
    public static final List<MethodInfo> methodInfos =
            new ArrayList<>(10000);

    private MethodCountAgent() {
    }

    public static void premain(String agentArguments,
                               Instrumentation ic) {

        String rxFile = null;
        String logFile = null;

        if (agentArguments != null) {
            StringTokenizer st = new StringTokenizer(agentArguments, ",");
            while (st.hasMoreElements()) {

                String line = st.nextToken();

                int i = line.indexOf(':');

                if (i <= 0) {
                    throw new RuntimeException("Invalid argument piece (':'):" +
                            line);
                }

                String p = line.substring(0, i);
                String v = line.substring(i + 1);

                if (p.isEmpty() || v.isEmpty()) {
                    throw new RuntimeException("Invalid argument piece (" +
                            "no key or value):" + line);
                }

                switch (p) {
                    case RX_FILE:
                        rxFile = v;
                        break;
                    case LOG_FILE:
                        logFile = v;
                        break;
                    case PORT:
                        listenPort = Integer.parseInt(v);
                        break;
                    default:
                        throw new RuntimeException("Invalid argument piece " +
                                "(unknown key " + p + "):" + line);
                }

            }
        }

        if (rxFile == null) {
            throw new RuntimeException("I need "+RX_FILE);
        }

        if (logFile == null) {
            throw new RuntimeException("I need "+LOG_FILE);
        }

        try {

            log = new PrintStream(logFile);

        } catch (Exception e) {
            throw new RuntimeException("Failed to open log file? "+logFile, e);
        }

        Collection<Pattern> regexps = new ArrayList<>();

        try {

            BufferedReader br = new BufferedReader(new FileReader(rxFile));
            while (true) {
                String s = br.readLine();
                if (s == null) { break; }
                if ("".equals(s = s.trim()) || s.startsWith("#")) { continue; }
                regexps.add(Pattern.compile(s));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading from "+rxFile, e);
        }

        if (regexps.isEmpty()) {
            throw new RuntimeException("File "+rxFile+" is empty");
        }

        log.println("Initializing transformer");
        // $TODO: canRetransform was true before, but JDK is just throwing
        // unsupported operation exception for using true in there.
        ic.addTransformer(new MethodCountTransformer(regexps), false);
        // System.out.println("*** transformer registered");

        new MCListener().start();

    }

}
