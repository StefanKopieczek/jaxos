package com.stefankopieczek.jaxos.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.util.Optional;

public class PromiseTest {
    @Test
    public void withProposal() {
        Proposal<String> proposal = new Proposal<>(99, "skidoo");
        Promise<String> promise = Promise.withProposal(proposal);
        assertTrue(promise.hasProposal());
        assertEquals(proposal, promise.getProposal());
    }

    @Test
    public void withoutProposal() {
        Promise<String> promise = Promise.withoutProposal();
        assertFalse(promise.hasProposal());
    }
}
