/*******************************************************************************
 * Copyright (c) 2015-2016 Apcera Inc. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the MIT License (MIT) which accompanies this
 * distribution, and is available at http://opensource.org/licenses/MIT
 *******************************************************************************/

package io.nats.client.examples;

import io.nats.client.Connection;
import io.nats.client.ConnectionFactory;
import io.nats.client.Message;
import io.nats.client.Statistics;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Requestor {
    Map<String, String> parsedArgs = new HashMap<String, String>();

    int count = 20000;
    String url = ConnectionFactory.DEFAULT_URL;
    String subject = "foo";
    byte[] payload = null;
    long start;
    long end;
    long elapsed;

    public void run(String[] args) {
        parseArgs(args);
        banner();

        try (Connection c = new ConnectionFactory(url).createConnection()) {
            start = System.nanoTime();
            int received = 0;

            Message msg = null;
            byte[] reply = null;
            try {
                for (int i = 0; i < count; i++) {
                    msg = c.request(subject, payload, 10000);
                    if (msg == null) {
                        break;
                    }

                    received++;
                    reply = msg.getData();
                    if (reply != null) {
                        System.out.println("Got reply: " + new String(reply));
                    } else {
                        System.out.println("Got reply with null payload");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            end = System.nanoTime();
            elapsed = TimeUnit.NANOSECONDS.toSeconds(end - start);

            System.out.printf("Completed %d requests in %d seconds ", received, elapsed);
            if (elapsed > 0) {
                System.out.printf("(%d msgs/second).\n", (received / elapsed));
            } else {
                System.out.println();
                System.out.println("Test not long enough to produce meaningful stats. "
                        + "Please increase the message count (-count n)");
            }
            printStats(c);
        } catch (IOException | TimeoutException e) {
            System.err.println("Couldn't connect: " + e.getMessage());
            System.exit(-1);
        }

    }

    private void printStats(Connection conn) {
        Statistics stats = conn.getStats();
        System.out.printf("Statistics:  ");
        System.out.printf("   Incoming Payload Bytes: %d\n", stats.getInBytes());
        System.out.printf("   Incoming Messages: %d\n", stats.getInMsgs());
        System.out.printf("   Outgoing Payload Bytes: %d\n", stats.getOutBytes());
        System.out.printf("   Outgoing Messages: %d\n", stats.getOutMsgs());
    }

    private void usage() {
        System.err.println("Usage:  Requestor [-url url] [-subject subject] "
                + "[-count count] [-payload payload]");

        System.exit(-1);
    }

    private void parseArgs(String[] args) {
        if (args == null) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if (i + 1 == args.length) {
                usage();
            }

            parsedArgs.put(args[i], args[i + 1]);
            i++;
        }

        if (parsedArgs.containsKey("-count")) {
            count = Integer.parseInt(parsedArgs.get("-count"));
        }

        if (parsedArgs.containsKey("-url")) {
            url = parsedArgs.get("-url");
        }

        if (parsedArgs.containsKey("-subject")) {
            subject = parsedArgs.get("-subject");
        }

        if (parsedArgs.containsKey("-payload")) {
            payload = parsedArgs.get("-payload").getBytes(Charset.forName("UTF-8"));
        }
    }

    private void banner() {
        System.out.printf("Sending %d requests on subject %s\n", count, subject);
        System.out.printf("  URL: %s\n", url);
        System.out.printf("  Payload is %d bytes.\n", payload != null ? payload.length : 0);
    }

    /**
     * Runs Requestor.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Requestor().run(args);
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }

    }
}
