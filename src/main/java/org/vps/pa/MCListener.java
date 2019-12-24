package org.vps.pa;

import java.net.ServerSocket;
import java.net.Socket;

public class MCListener extends Thread {

    public void run() {

        try {

            ServerSocket ss = new ServerSocket(MethodCountAgent.listenPort);

            //noinspection InfiniteLoopStatement
            while (true) {

                Socket slave = ss.accept();
                new MCTalk(slave).start();

            }

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

}
