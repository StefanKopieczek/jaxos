package com.stefankopieczek.jaxos.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PromiseTest {
    @Test
    public void returnsAcceptableProposal() {
        Proposal<String> proposal = new Proposal<>(99, "skidoo");
        Promise<String> promise = new Promise<>(proposal);
        assertEquals(proposal, promise.getAcceptableProposal());
    }
}
