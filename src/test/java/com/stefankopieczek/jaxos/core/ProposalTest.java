package com.stefankopieczek.jaxos.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ProposalTest {
    @Test
    public void returnsProposalNumber() {
        Proposal<String> p = new Proposal<>(13, "foo");
        assertEquals(13, p.getProposalNumber());
    }

    @Test
    public void returnsValue() {
        Proposal<String> p = new Proposal<>(13, "foo");
        assertEquals("foo", p.getValue());
    }
}

