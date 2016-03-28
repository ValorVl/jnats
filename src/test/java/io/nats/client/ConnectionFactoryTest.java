/*******************************************************************************
 * Copyright (c) 2015-2016 Apcera Inc. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the MIT License (MIT) which accompanies this
 * distribution, and is available at http://opensource.org/licenses/MIT
 *******************************************************************************/

package io.nats.client;

import static io.nats.client.ConnectionFactory.DEFAULT_SSL_PROTOCOL;
import static io.nats.client.Constants.PROP_CLOSED_CB;
import static io.nats.client.Constants.PROP_CONNECTION_NAME;
import static io.nats.client.Constants.PROP_CONNECTION_TIMEOUT;
import static io.nats.client.Constants.PROP_DISCONNECTED_CB;
import static io.nats.client.Constants.PROP_EXCEPTION_HANDLER;
import static io.nats.client.Constants.PROP_HOST;
import static io.nats.client.Constants.PROP_MAX_PENDING_BYTES;
import static io.nats.client.Constants.PROP_MAX_PENDING_MSGS;
import static io.nats.client.Constants.PROP_MAX_PINGS;
import static io.nats.client.Constants.PROP_MAX_RECONNECT;
import static io.nats.client.Constants.PROP_NORANDOMIZE;
import static io.nats.client.Constants.PROP_PASSWORD;
import static io.nats.client.Constants.PROP_PEDANTIC;
import static io.nats.client.Constants.PROP_PING_INTERVAL;
import static io.nats.client.Constants.PROP_PORT;
import static io.nats.client.Constants.PROP_RECONNECTED_CB;
import static io.nats.client.Constants.PROP_RECONNECT_ALLOWED;
import static io.nats.client.Constants.PROP_RECONNECT_BUF_SIZE;
import static io.nats.client.Constants.PROP_RECONNECT_WAIT;
import static io.nats.client.Constants.PROP_SECURE;
import static io.nats.client.Constants.PROP_SERVERS;
import static io.nats.client.Constants.PROP_TLS_DEBUG;
import static io.nats.client.Constants.PROP_URL;
import static io.nats.client.Constants.PROP_USERNAME;
import static io.nats.client.Constants.PROP_VERBOSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import io.nats.client.Constants.ConnState;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class ConnectionFactoryTest
        implements ExceptionHandler, ClosedCallback, DisconnectedCallback, ReconnectedCallback {
    @Rule
    public TestCasePrinterRule pr = new TestCasePrinterRule(System.out);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test(expected = IllegalArgumentException.class)
    public void testConnectionFactoryNullProperties() {
        Properties nullProps = null;
        ConnectionFactory cf = new ConnectionFactory(nullProps);
    }

    static final String url = "nats://foobar:4122";
    static final String hostname = "foobar2";
    static final int port = 2323;
    static final String username = "larry";
    static final String password = "password";
    static final String servers = "nats://cluster-host-1:5151 , nats://cluster-host-2:5252";
    static final String[] serverArray = servers.split(",\\s+");
    static List<URI> sList = null;
    static final Boolean noRandomize = true;
    static final String name = "my_connection";
    static final Boolean verbose = true;
    static final Boolean pedantic = true;
    static final Boolean secure = true;
    static final Boolean reconnectAllowed = false;
    static final int maxReconnect = 14;
    static final int reconnectWait = 100;
    static final int reconnectBufSize = 12 * 1024 * 1024;
    static final int timeout = 2000;
    static final int pingInterval = 5000;
    static final int maxPings = 4;
    static final Boolean tlsDebug = true;
    static final int maxPendingMsgs = 4400;
    static final long maxPendingBytes = maxPendingMsgs * 1024;

    @Test
    public void testConnectionFactoryProperties() {
        final ClosedCallback ccb = this;
        final DisconnectedCallback dcb = this;
        final ReconnectedCallback rcb = this;
        final ExceptionHandler eh = this;

        Properties props = new Properties();
        props.setProperty(PROP_HOST, hostname);
        props.setProperty(PROP_PORT, Integer.toString(port));
        props.setProperty(PROP_URL, url);
        props.setProperty(PROP_USERNAME, username);
        props.setProperty(PROP_PASSWORD, password);
        props.setProperty(PROP_SERVERS, servers);
        props.setProperty(PROP_NORANDOMIZE, Boolean.toString(noRandomize));
        props.setProperty(PROP_CONNECTION_NAME, name);
        props.setProperty(PROP_VERBOSE, Boolean.toString(verbose));
        props.setProperty(PROP_PEDANTIC, Boolean.toString(pedantic));
        props.setProperty(PROP_SECURE, Boolean.toString(secure));
        props.setProperty(PROP_TLS_DEBUG, Boolean.toString(secure));
        props.setProperty(PROP_RECONNECT_ALLOWED, Boolean.toString(reconnectAllowed));
        props.setProperty(PROP_MAX_RECONNECT, Integer.toString(maxReconnect));
        props.setProperty(PROP_RECONNECT_WAIT, Integer.toString(reconnectWait));
        props.setProperty(PROP_RECONNECT_BUF_SIZE, Integer.toString(reconnectBufSize));
        props.setProperty(PROP_CONNECTION_TIMEOUT, Integer.toString(timeout));
        props.setProperty(PROP_PING_INTERVAL, Integer.toString(pingInterval));
        props.setProperty(PROP_MAX_PINGS, Integer.toString(maxPings));
        props.setProperty(PROP_EXCEPTION_HANDLER, eh.getClass().getName());
        props.setProperty(PROP_CLOSED_CB, ccb.getClass().getName());
        props.setProperty(PROP_DISCONNECTED_CB, dcb.getClass().getName());
        props.setProperty(PROP_RECONNECTED_CB, rcb.getClass().getName());
        props.setProperty(PROP_MAX_PENDING_MSGS, Integer.toString(maxPendingMsgs));
        props.setProperty(PROP_MAX_PENDING_BYTES, Long.toString(maxPendingBytes));


        ConnectionFactory cf = new ConnectionFactory(props);
        assertEquals(hostname, cf.getHost());

        assertEquals(port, cf.getPort());

        assertEquals(username, cf.getUsername());
        assertEquals(password, cf.getPassword());
        List<URI> s1 = ConnectionFactoryTest.serverArrayToList(serverArray);
        List<URI> s2 = cf.getServers();
        assertEquals(s1, s2);
        assertEquals(noRandomize, cf.isNoRandomize());
        assertEquals(name, cf.getConnectionName());
        assertEquals(verbose, cf.isVerbose());
        assertEquals(pedantic, cf.isPedantic());
        assertEquals(secure, cf.isSecure());
        assertEquals(reconnectAllowed, cf.isReconnectAllowed());
        assertEquals(maxReconnect, cf.getMaxReconnect());
        assertEquals(reconnectWait, cf.getReconnectWait());
        assertEquals(reconnectBufSize, cf.getReconnectBufSize());
        assertEquals(timeout, cf.getConnectionTimeout());
        assertEquals(pingInterval, cf.getPingInterval());
        assertEquals(maxPings, cf.getMaxPingsOut());
        assertEquals(eh.getClass().getName(), cf.getExceptionHandler().getClass().getName());
        assertEquals(ccb.getClass().getName(), cf.getClosedCallback().getClass().getName());
        assertEquals(dcb.getClass().getName(), cf.getDisconnectedCallback().getClass().getName());
        assertEquals(rcb.getClass().getName(), cf.getReconnectedCallback().getClass().getName());
        assertEquals(maxPendingMsgs, cf.getMaxPendingMsgs());
        assertEquals(maxPendingBytes, cf.getMaxPendingBytes());

        cf.setSecure(false);
        try (TCPConnectionMock mock = new TCPConnectionMock()) {
            try (ConnectionImpl ci = cf.createConnection(mock)) {
                assertFalse(ci.isClosed());

                assertEquals(hostname, ci.opts.getHost());
                assertEquals(password, ci.opts.getPassword());
                List<URI> s3 = ci.opts.getServers();
                assertEquals(s1, s3);
                assertEquals(noRandomize, ci.opts.isNoRandomize());
                assertEquals(name, ci.opts.getConnectionName());
                assertEquals(verbose, ci.opts.isVerbose());
                assertEquals(pedantic, ci.opts.isPedantic());
                // Setting to default just to ensure we can connect
                assertEquals(false, ci.opts.isSecure());
                assertEquals(reconnectAllowed, ci.opts.isReconnectAllowed());
                assertEquals(maxReconnect, ci.opts.getMaxReconnect());
                assertEquals(reconnectWait, ci.opts.getReconnectWait());
                assertEquals(reconnectBufSize, ci.opts.getReconnectBufSize());
                assertEquals(timeout, ci.opts.getConnectionTimeout());
                assertEquals(pingInterval, ci.opts.getPingInterval());
                assertEquals(maxPings, ci.opts.getMaxPingsOut());
                assertEquals(eh.getClass().getName(),
                        ci.opts.getExceptionHandler().getClass().getName());
                assertEquals(ccb.getClass().getName(),
                        ci.opts.getClosedCallback().getClass().getName());
                assertEquals(dcb.getClass().getName(),
                        ci.opts.getDisconnectedCallback().getClass().getName());
                assertEquals(rcb.getClass().getName(),
                        ci.opts.getReconnectedCallback().getClass().getName());
            } catch (IOException | TimeoutException e) {
                fail(e.getMessage());
            }
        } catch (Exception e1) {
            fail("Unexpected exception: " + e1.getMessage());
        }
    }

    @Test
    public void testConnectionFactoryEmptyServersProp() {
        Properties props = new Properties();
        props.setProperty(PROP_SERVERS, "");
        boolean exThrown = false;
        try {
            ConnectionFactory cf = new ConnectionFactory(props);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        } finally {
            assertTrue(exThrown);
        }
    }

    @Test
    public void testConnectionFactoryBadCallbackProps() {
        String[] propNames = { PROP_EXCEPTION_HANDLER, PROP_CLOSED_CB, PROP_DISCONNECTED_CB,
                PROP_RECONNECTED_CB };


        for (String p : propNames) {
            Properties props = new Properties();
            props.setProperty(p, "foo.bar.baz");

            boolean exThrown = false;
            try {
                ConnectionFactory cf = new ConnectionFactory(props);
            } catch (IllegalArgumentException e) {
                exThrown = true;
            } finally {
                assertTrue(exThrown);
            }
        }

    }

    @Test
    public void testConnectionFactory() {
        try {
            new ConnectionFactory();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectionFactoryString() {
        final String theUrl = "localhost:1234";
        IOException e;
        ConnectionFactory cf = new ConnectionFactory(theUrl);
        assertNotNull(cf);

        assertFalse(theUrl.equals(cf.getUrlString()));

        ConnectionFactory cf2 = new ConnectionFactory("nota:wellformed/uri");
        fail("Should have thrown exception");
    }

    public static List<URI> serverArrayToList(String[] sarray) {
        List<URI> rv = new ArrayList<URI>();
        for (String s : serverArray) {
            rv.add(URI.create(s.trim()));
        }
        return rv;
    }

    @Test
    public void testConnectionFactoryStringArray() {
        sList = serverArrayToList(serverArray);
        ConnectionFactory cf = new ConnectionFactory(serverArray);
        assertEquals(sList, cf.getServers());
        assertEquals(null, cf.getUrlString());
        // try (ConnectionImpl c = cf.createConnection()) {
        // } catch (IOException | TimeoutException e) {
        // fail(e.getMessage());
        // }
    }

    @Test
    public void testConnectionFactoryStringStringArray() {
        String url = "nats://localhost:1222";
        String[] servers = { "nats://localhost:1234", "nats://localhost:5678" };
        ArrayList<URI> s1 = new ArrayList<URI>(10);
        ArrayList<URI> s2 = new ArrayList<URI>(10);

        try (TCPConnectionMock mock = new TCPConnectionMock()) {
            for (String s : servers) {
                s1.add(URI.create(s));
                s2.add(URI.create(s));
            }
            // test null, servers
            ConnectionFactory cf = new ConnectionFactory(null, servers);
            assertNull(cf.getUrlString());
            assertNotNull(cf.getServers());
            assertEquals(s1, cf.getServers());
            cf.setNoRandomize(true);
            try (ConnectionImpl c = cf.createConnection(mock)) {
                // test passed-in options
                List<URI> serverList = c.opts.getServers();
                assertEquals(s1, serverList);

                // Test the URLS produced by setupServerPool
                List<URI> connServerPool = new ArrayList<URI>();
                List<ConnectionImpl.Srv> srvPool = c.srvPool;
                c.close();
                assertTrue(c.getState() == ConnState.CLOSED);
                for (ConnectionImpl.Srv srv : srvPool) {
                    connServerPool.add(srv.url);
                }
                assertEquals(s1, connServerPool);
            } catch (IOException | TimeoutException e) {
                fail(e.getMessage());
            }

            mock.bounce();

            // test url, null
            cf = new ConnectionFactory(url, null);
            cf.setNoRandomize(true);
            assertEquals(url, cf.getUrlString());
            // System.err.println("Connecting to: " + url);
            try (ConnectionImpl c = cf.createConnection(mock)) {
                // test passed-in options
                assertNull(c.opts.getServers());

                // Test the URLS produced by setupServerPool
                List<URI> connServerPool = new ArrayList<URI>();
                List<ConnectionImpl.Srv> srvPool = c.srvPool;
                for (ConnectionImpl.Srv srv : srvPool) {
                    connServerPool.add(srv.url);
                }
                s1.clear();
                s1.add(URI.create(url));
                assertEquals(s1, connServerPool);
            } catch (IOException | TimeoutException e) {
                fail(e.getMessage());
            }

            s1.clear();
            s1.addAll(s2);
            assertEquals(s1, s2);

            mock.bounce();

            // test url, servers
            cf = new ConnectionFactory(url, servers);
            cf.setNoRandomize(true);
            assertNotNull(cf.getUrlString());
            assertEquals(url, cf.getUrlString());
            try (ConnectionImpl c = cf.createConnection(mock)) {
                // test passed-in options
                List<URI> serverList = c.opts.getServers();
                assertEquals(s1, serverList);
                assertEquals(url, c.opts.getUrl().toString());

                // Test the URLS produced by setupServerPool
                List<URI> connServerPool = new ArrayList<URI>();
                List<ConnectionImpl.Srv> srvPool = c.srvPool;
                for (ConnectionImpl.Srv srv : srvPool) {
                    connServerPool.add(srv.url);
                }
                s1.add(0, URI.create(url));
                assertEquals(s1, connServerPool);
            } catch (IOException | TimeoutException e) {
                fail(e.getMessage());
            }
        }
    }

    // @Test
    // public void testCreateConnection() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testCreateConnectionTCPConnection() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetSubChanLen() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetSubChanLen() {
    // fail("Not yet implemented"); // TODO
    // }
    //

    @Test
    public void testClone() {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setUri(URI.create("nats://localhost:7272"));
        cf.setHost("localhost");
        cf.setPort(7272);
        cf.setServers(new String[] { "nats://somehost:1010", "nats://somehost2:1020" });
        cf.setUsername("foo");
        cf.setPassword("bar");
        cf.setNoRandomize(true);
        cf.setConnectionName("BAR");
        cf.setVerbose(true);
        cf.setPedantic(true);
        cf.setSecure(true);
        cf.setReconnectAllowed(false);
        cf.setMaxReconnect(14);
        cf.setReconnectWait(55000);
        cf.setConnectionTimeout(99);
        cf.setPingInterval(9900);
        cf.setMaxPingsOut(11);
        try {
            cf.setSSLContext(SSLContext.getInstance(DEFAULT_SSL_PROTOCOL));
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        cf.setMaxPendingMsgs(49);
        cf.setTlsDebug(true);

        ConnectionFactory cf2 = null;
        cf2 = cf.clone();
        assertEquals(cf.getUrlString(), cf2.getUrlString());
        assertEquals(cf.getHost(), cf2.getHost());
        assertEquals(cf.getPort(), cf2.getPort());
        assertEquals(cf.getUsername(), cf2.getUsername());
        assertEquals(cf.getPassword(), cf2.getPassword());
        assertEquals(cf.getServers(), cf2.getServers());
        assertEquals(cf.isNoRandomize(), cf2.isNoRandomize());
        assertEquals(cf.getConnectionName(), cf2.getConnectionName());
        assertEquals(cf.isVerbose(), cf2.isVerbose());
        assertEquals(cf.isPedantic(), cf2.isPedantic());
        assertEquals(cf.isSecure(), cf2.isSecure());
        assertEquals(cf.isReconnectAllowed(), cf2.isReconnectAllowed());
        assertEquals(cf.getMaxReconnect(), cf2.getMaxReconnect());
        assertEquals(cf.getReconnectWait(), cf2.getReconnectWait());
        assertEquals(cf.getConnectionTimeout(), cf2.getConnectionTimeout());
        assertEquals(cf.getPingInterval(), cf2.getPingInterval());
        assertEquals(cf.getMaxPingsOut(), cf2.getMaxPingsOut());
        assertEquals(cf.getSSLContext(), cf2.getSSLContext());
        assertEquals(cf.getExceptionHandler(), cf2.getExceptionHandler());
        assertEquals(cf.getClosedCallback(), cf2.getClosedCallback());
        assertEquals(cf.getDisconnectedCallback(), cf2.getDisconnectedCallback());
        assertEquals(cf.getReconnectedCallback(), cf2.getReconnectedCallback());
        assertEquals(cf.getUrlString(), cf2.getUrlString());
        assertEquals(cf.getMaxPendingMsgs(), cf2.getMaxPendingMsgs());
        assertEquals(cf.isTlsDebug(), cf2.isTlsDebug());
    }

    @Test
    public void testSetUriBadScheme() {
        URI uri = URI.create("file:///etc/fstab");

        ConnectionFactory cf = new ConnectionFactory();
        boolean exThrown = false;
        try {
            cf.setUri(uri);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue("Should have thrown exception.", exThrown);
        exThrown = false;

        uri = URI.create("nats:///etc/fstab");
        cf.setUri(uri);
        assertNull("Host should be null.", cf.getHost());
        assertEquals(-1, cf.getPort());

        uri = URI.create("tcp://localhost:4222");
        try {
            cf.setUri(uri);
        } catch (Exception e) {
            fail("Should not have thrown exception.");
        }
    }

    @Test
    public void testSetUriWithToken() {
        String secret = "$2a$11$3kIDaCxw.Glsl1.u5nKa6eUnNDLV5HV9tIuUp7EHhMt6Nm9myW1aS";
        String urlString = String.format("nats://%s@natshost:2222", secret);

        try {
            ConnectionFactory cf = new ConnectionFactory(urlString);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        URI goodUri = URI.create(urlString);
        // System.err.println(goodUri.toString());
        assertEquals(secret, goodUri.getRawUserInfo());
        ConnectionFactory cf = new ConnectionFactory();
        try {
            cf.setUri(goodUri);
            assertEquals(secret, cf.getUsername());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetUriBadUserInfo() {
        String urlString = "nats://foo:bar:baz@natshost:2222";
        String urlString2 = "nats://foo:@natshost:2222";
        String urlString3 = "nats://:pass@natshost:2222";
        ConnectionFactory cf = new ConnectionFactory();

        boolean exThrown = false;
        URI uri = URI.create(urlString);
        try {
            cf.setUri(uri);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue(exThrown);

        cf.setUsername(null);
        cf.setPassword(null);

        exThrown = false;
        uri = URI.create(urlString2);
        try {
            cf.setUri(uri);
            assertEquals("foo", cf.getUsername());
            assertNull(cf.getPassword());
        } catch (IllegalArgumentException e) {
            exThrown = true;
            fail(e.getMessage());
        }
        assertFalse(exThrown);

        cf.setUsername(null);
        cf.setPassword(null);

        exThrown = false;
        uri = URI.create(urlString3);
        try {
            cf.setUri(uri);
            assertNull(cf.getUsername());
            assertNull(cf.getPassword());
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertFalse(exThrown);
    }

    @Test
    public void testGetUrlString() {
        String urlString = "nats://natshost:2222";
        ConnectionFactory cf = new ConnectionFactory(urlString);
        assertEquals(urlString, cf.getUrlString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMalformedUrl() {
        ConnectionFactory cf = new ConnectionFactory("this is a : badly formed url");
        assertNotNull(cf);
    }

    @Test
    public void testGetHost() {
        String url = "nats://foobar:1234";
        ConnectionFactory cf = new ConnectionFactory(url);
        try (TCPConnectionMock mock = new TCPConnectionMock()) {
            try (Connection c = cf.createConnection(mock)) {
                assertEquals("foobar", cf.getHost());
            } catch (IOException | TimeoutException e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testSetHost() {
        String url = "nats://foobar:1234";
        ConnectionFactory cf = new ConnectionFactory(url);
        try (TCPConnectionMock mock = new TCPConnectionMock()) {
            try (Connection c = cf.createConnection(mock)) {
                assertEquals("foobar", cf.getHost());
            } catch (IOException | TimeoutException e) {
                fail(e.getMessage());
            }
        }
    }


    @Test
    public void testGetPort() {
        String url = "nats://foobar:1234";
        ConnectionFactory cf = new ConnectionFactory(url);
        try (TCPConnectionMock mock = new TCPConnectionMock()) {
            try (Connection c = cf.createConnection(mock)) {
                assertEquals(1234, cf.getPort());
            } catch (IOException | TimeoutException e) {
                fail(e.getMessage());
            }
        }
    }

    // @Test
    // public void testSetPort() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetUsername() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetUsername() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetPassword() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetPassword() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetServers() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    @Test
    public void testSetServersListOfURI() {
        String[] servers = { "nats://localhost:1234", "nats://localhost:5678" };
        List<URI> s1 = new ArrayList<URI>();

        for (String s : servers) {
            s1.add(URI.create(s));
        }

        ConnectionFactory cf = new ConnectionFactory();
        cf.setServers(s1);
        assertEquals(s1, cf.getServers());
    }

    @Test
    public void testSetServersStringArray() {
        String[] servers = { "nats://localhost:1234", "nats://localhost:5678" };
        List<URI> s1 = new ArrayList<URI>();
        ConnectionFactory cf = new ConnectionFactory(servers);
        cf.setServers(servers);
        cf.setNoRandomize(true);

        assertNull(cf.getUrlString());

        try (TCPConnectionMock mock = new TCPConnectionMock()) {
            for (String s : servers) {
                s1.add(URI.create(s));
            }
            try (ConnectionImpl c = cf.createConnection(mock)) {
                List<URI> serverList = c.opts.getServers();
                assertEquals(s1, serverList);
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }

        try {
            new ConnectionFactory((String[]) null);
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }

        try {
            ConnectionFactory cf2 = new ConnectionFactory();
            cf2.setServers((String[]) null);
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }

        String[] badServers = { "foo bar", "bar" };
        boolean exThrown = false;
        try {
            cf = new ConnectionFactory(badServers);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        } finally {
            assertTrue(exThrown);
        }

    }

    @Test
    public void testConnectionFactoryCommaDelimitedString() {
        String servers = "nats://localhost:1234, nats://localhost:5678";
        List<URI> s1 = new ArrayList<URI>();
        ConnectionFactory cf = new ConnectionFactory(servers);
        cf.setNoRandomize(true);

        for (String s : servers.trim().split(",")) {
            s1.add(URI.create(s.trim()));
        }

        assertNull(cf.getUrlString());
        assertEquals(s1, cf.getServers());


    }

    @Test
    public void testSetUrl() {
        String servers = "nats://localhost:1234, nats://localhost:5678";
        List<URI> s1 = new ArrayList<URI>();
        ConnectionFactory cf = new ConnectionFactory();
        cf.setNoRandomize(true);

        cf.setUrl(servers);

        for (String s : servers.trim().split(",")) {
            s1.add(URI.create(s.trim()));
        }

        assertNull(cf.getUrlString());
        assertEquals(s1, cf.getServers());

    }
    // @Test
    // public void testIsNoRandomize() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetNoRandomize() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetConnectionName() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetConnectionName() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testIsVerbose() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetVerbose() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testIsPedantic() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetPedantic() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testIsSecure() {
    // fail("Not yet implemented"); // TODO
    // }

    @Test
    public void testIsTlsDebug() {
        try (TCPConnectionMock mock = new TCPConnectionMock()) {
            ConnectionFactory cf = new ConnectionFactory();
            cf.setTlsDebug(true);
            assertTrue(cf.isTlsDebug());
            try (ConnectionImpl c = cf.createConnection(mock)) {
                assertTrue(c.opts.isTlsDebug());
            } catch (IOException | TimeoutException e) {
                fail(e.getMessage());
            }
        }
    }

    // @Test
    // public void testSetSecure() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testIsReconnectAllowed() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetReconnectAllowed() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetMaxReconnect() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetMaxReconnect() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetReconnectWait() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetReconnectWait() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetConnectionTimeout() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetConnectionTimeout() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetPingInterval() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetPingInterval() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetMaxPingsOut() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetClosedCallback() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetClosedCallback() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetDisconnectedCallback() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetDisconnectedCallback() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetReconnectedCallback() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetReconnectedCallback() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetMaxPingsOut() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetExceptionHandler() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    @Test
    public void testSetExceptionHandler() {
        ConnectionFactory cf = new ConnectionFactory();
        boolean exThrown = false;
        try {
            cf.setExceptionHandler(null);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        } finally {
            assertTrue(exThrown);
        }
    }

    @Test
    public void testGetSSLContext() {
        ConnectionFactory cf = new ConnectionFactory();
        try {
            SSLContext c = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
            cf.setSSLContext(c);
            assertEquals(c, cf.getSSLContext());
            assertEquals(cf.getSSLContext(), cf.getSslContext());
            cf.setSslContext(c);
            assertEquals(c, cf.getSSLContext());
            assertEquals(cf.getSSLContext(), cf.getSslContext());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetSSLContext() {
        ConnectionFactory cf = new ConnectionFactory();
        try {
            SSLContext c = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
            cf.setSSLContext(c);
            assertEquals(c, cf.getSSLContext());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
    }

    @Override
    public void onDisconnect(ConnectionEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReconnect(ConnectionEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClose(ConnectionEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onException(NATSException e) {
        // TODO Auto-generated method stub

    }

    @Test
    public void testConstructURI() {
        final String HOST = "foobar";
        final int PORT = 7272;
        final String USER = "derek";
        final String PASS = "derek";
        final String expectedURL =
                String.format("nats://%s:%d", HOST, ConnectionFactory.DEFAULT_PORT);
        final String expectedURL1 = String.format("nats://%s:%d", HOST, PORT);
        final String expectedURL2 = String.format("nats://%s@%s:%d", USER, HOST, PORT);
        final String expectedURL3 = String.format("nats://%s:%s@%s:%d", USER, PASS, HOST, PORT);

        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost(HOST);
        assertEquals(HOST, cf.getHost());
        assertEquals(-1, cf.getPort());
        assertEquals(expectedURL, cf.constructURI().toString());

        cf.setPort(PORT);
        assertEquals(PORT, cf.getPort());
        assertNull(cf.getUrlString());
        assertEquals(expectedURL1, cf.constructURI().toString());

        assertNull(cf.getUsername());
        assertNull(cf.getPassword());
        cf.setUsername(USER);
        assertEquals(USER, cf.getUsername());
        assertEquals(expectedURL2, cf.constructURI().toString());

        cf.setPassword(PASS);
        assertEquals(PASS, cf.getPassword());
        assertEquals(expectedURL3, cf.constructURI().toString());

    }

    @Test
    public void testSetReconnectBufSizeNegative() {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setReconnectBufSize(-14);
        assertEquals(ConnectionFactory.DEFAULT_RECONNECT_BUF_SIZE, cf.getReconnectBufSize());
    }
}
