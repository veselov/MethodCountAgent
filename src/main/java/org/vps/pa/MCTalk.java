package org.vps.pa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class MCTalk extends Thread {

    public static final String C_PRINT = "print";
    public static final String C_RESET = "reset";
    public static final String C_FLUSH = "flush";


    private Socket s;

    public MCTalk(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {

        try {
            run0();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void run0() {

        Socket s = this.s;


        try {

            BufferedReader input = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));

            PrintWriter w = new PrintWriter(
                    new OutputStreamWriter(s.getOutputStream()));

            w.println("ready>");

            while (true) {

                w.flush();

                String line = input.readLine();
                if (line == null) { break; }

                if ("".equals(line = line.trim())) {
                    w.println("Yes, I'm still here");
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line);
                String cmd = st.nextToken().toLowerCase();

                boolean flush = false;

                if (C_FLUSH.equals(cmd)) {
                    cmd = C_PRINT;
                    flush = true;
                }

                boolean print = true;
                if (C_RESET.equals(cmd)) {
                    cmd = C_PRINT;
                    print = false;
                    flush = true;
                }

                if (C_PRINT.equals(cmd)) {
                    int max;

                    if (print) {
                        try {
                            max = Integer.parseInt(st.nextToken());
                        } catch (Exception ignored) {
                            max = Integer.MAX_VALUE;
                        }
                    } else {
                        max = -1;
                    }

                    print(w, max, flush);

                } else {

                    w.println("I don't speak goo-goo");

                }

            }

        } catch (Exception ignored) {
            // ignore and die.
        }

    }

    private void print(PrintWriter pw, int max,
                       boolean reset) {

        TreeMap<Long, MethodInfo> map;

        if (max > 0) {
            map = new TreeMap<>(new Comparator<Long>() {
                @Override
                public int compare(Long o1, Long o2) {
                    if (o1 < o2) { return 1; }
                    //noinspection ComparatorMethodParameterNotUsed
                    return -1;
                }
            });
        } else {
            map = null;
        }

        for (MethodInfo mi : MethodCountAgent.methodInfos) {

            if (map != null) {
                long count = reset?mi.counter.getAndSet(0L):mi.counter.get();
                if (count == 0) { continue; }
                map.put(count, mi);
            } else if (reset) {
                mi.counter.set(0);
            }

        }

        if (map == null || map.isEmpty()) {
            pw.println("executed");
            return;
        }

        Iterator<Map.Entry<Long, MethodInfo>> i = map.entrySet().iterator();
        int l = 0;
        while (max-- > 0 && i.hasNext()) {

            Map.Entry<Long, MethodInfo> me = i.next();
            String s = String.valueOf(me.getKey());
            if (l == 0) {
                l = s.length();
            } else {
                int cl = s.length();
                while (cl++ < l) {
                    pw.print(' ');
                }
            }
            pw.print(s);
            MethodInfo mi = me.getValue();
            if (mi.isConstructor) {
                pw.print(" * ");
            } else {
                pw.print("   ");
            }
            pw.println(mi.name);

        }

        pw.println("--");

    }

}
