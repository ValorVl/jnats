/*******************************************************************************
 * Copyright (c) 2015-2016 Apcera Inc. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the MIT License (MIT) which accompanies this
 * distribution, and is available at http://opensource.org/licenses/MIT
 *******************************************************************************/

package io.nats.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class MessageTest {
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

    @Test
    public void testMessage() {
        byte[] payload = "This is a message payload.".getBytes();
        String subj = "foo";
        String reply = "bar";
        Message m = new Message();
        m.setData(null, 0, payload.length);
        m.setData(payload, 0, payload.length);
        m.setReplyTo(reply);
        m.setSubject(subj);
    }

    @Test
    public void testMessageStringStringByteArray() {
        Message m = new Message("foo", "bar", "baz".getBytes());
        assertEquals("foo", m.getSubject());
        assertEquals("bar", m.getReplyTo());
        assertTrue(Arrays.equals("baz".getBytes(), m.getData()));
    }

    @Test(expected = NullPointerException.class)
    public void testNullSubject() {
        new Message(null, "bar", "baz".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhitespaceSubject() {
        new Message(" 	", "bar", "baz".getBytes());
    }

    // @Test
    // public void testMessageMsgArgSubscriptionImplByteArrayLong() {
    // fail("Not yet implemented"); // TODO
    // }

    @Test
    public void testMessageByteArrayLongStringStringSubscriptionImpl() {
        byte[] payload = "This is a message payload.".getBytes();
        String subj = "foo";
        String reply = "bar";

        Message m = new Message(payload, payload.length, subj, reply, null);
        assertEquals(subj, m.getSubject());
        assertEquals(reply, m.getReplyTo());
        assertTrue(Arrays.equals(payload, m.getData()));
    }

    // @Test
    // public void testGetData() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetSubject() {
    // fail("Not yet implemented"); // TODO
    // }

    // @Test
    // public void testSetSubject() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetReplyTo() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testSetReplyTo() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // @Test
    // public void testGetSubscription() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    @Test
    public void testSetData() {
        byte[] data = "hello".getBytes();
        Message m = new Message();
        m.setData(data);
    }

    @Test
    public void testToString() {
        byte[] data = "hello".getBytes();
        Message m = new Message();
        m.setData(data);
        m.setSubject("foo");
        m.setReplyTo("bar");
        assertEquals("{Subject=foo;Reply=bar;Payload=<hello>}", m.toString());

        String longPayload = "this is a really long message that's easily over 32 "
                + "characters long so it will be truncated.";
        data = longPayload.getBytes();
        m.setData(data);
        assertEquals("{Subject=foo;Reply=bar;Payload="
                + "<this is a really long message th60 more bytes>}", m.toString());
    }

}
