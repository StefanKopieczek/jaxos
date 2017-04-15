package com.stefankopieczek.jaxos.proposer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.stefankopieczek.jaxos.acceptor.Acceptor;
import com.stefankopieczek.jaxos.core.Proposal;
import com.stefankopieczek.jaxos.core.Promise;

public class ProposerTest {
    private static final Proposal<String> proposal1 = new Proposal<>(20, "bar");
    private static final Proposal<String> proposal2 = new Proposal<>(30, "baz");
    private static final Proposal<String> proposal3 = new Proposal<>(35, "jounce");

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    @Test
    public void phase1() {
        DummyAcceptor<String> acceptor1 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor2 = new DummyAcceptor<>();
        Proposer<String> proposer = new ProposerImpl<>(1, Arrays.asList(acceptor1, acceptor2));
        proposer.propose("foo");
        assertEquals("foo", acceptor1.lastPrepare.getValue());
        assertEquals("foo", acceptor2.lastPrepare.getValue());
        assertEquals(1, acceptor1.lastPrepare.getProposalNumber());
        assertEquals(1, acceptor2.lastPrepare.getProposalNumber());
    }

    @Test
    public void phase1Retries() {
        DummyAcceptor<String> acceptor = new DummyAcceptor<>();
        acceptor.prepare(proposal1);
        Proposer<String> proposer = new ProposerImpl<>(1, Collections.singletonList(acceptor));
        proposer.propose("foo");
        assertEquals("foo", acceptor.lastPrepare.getValue());
        assertEquals(proposal1.getProposalNumber() + 1, acceptor.lastPrepare.getProposalNumber());
    }

    @Test
    public void phase1RetryStopsAtQuorum() {
        DummyAcceptor<String> acceptor1 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor2 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor3 = new DummyAcceptor<>();
        acceptor1.prepare(proposal1);
        acceptor2.prepare(proposal2);
        acceptor3.prepare(proposal3);
        Proposer<String> proposer = new ProposerImpl<>(1, Arrays.asList(acceptor1, acceptor2, acceptor3));
        proposer.propose("foo");
        assertEquals(proposal2.getProposalNumber() + 1, acceptor1.lastPrepare.getProposalNumber());
        assertEquals(proposal2.getProposalNumber() + 1, acceptor2.lastPrepare.getProposalNumber());
        assertEquals(proposal3.getProposalNumber(), acceptor3.lastPrepare.getProposalNumber());
    }

    @Test
    public void phase2() {
        DummyAcceptor<String> acceptor1 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor2 = new DummyAcceptor<>();
        Proposer<String> proposer = new ProposerImpl<>(1, Arrays.asList(acceptor1, acceptor2));
        proposer.propose("foo");
        assertEquals("foo", acceptor1.lastAccept.getValue());
        assertEquals("foo", acceptor2.lastAccept.getValue());
        assertEquals(1, acceptor1.lastAccept.getProposalNumber());
        assertEquals(1, acceptor2.lastAccept.getProposalNumber());
    }

    @Test
    public void phase2RespectsPreviouslyAccepted() {
        DummyAcceptor<String> acceptor = new DummyAcceptor<>();
        acceptor.prepare(proposal1);
        acceptor.accept(proposal1);
        Proposer<String> proposer = new ProposerImpl<>(1, Collections.singletonList(acceptor));
        proposer.propose("foo");
        assertEquals(proposal1.getValue(), acceptor.lastAccept.getValue());
        assertEquals(proposal1.getProposalNumber() + 1, acceptor.lastAccept.getProposalNumber());
    }

    @Test
    public void phase2RespectsMaximumAccepted() {
        DummyAcceptor<String> acceptor1 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor2 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor3 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor4 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor5 = new DummyAcceptor<>();
        acceptor1.prepare(proposal1);
        acceptor2.prepare(proposal2);
        acceptor3.prepare(proposal3);
        acceptor4.prepare(proposal3);
        acceptor5.prepare(proposal3);
        acceptor5.accept(proposal3);
        acceptor1.accept(proposal1);
        acceptor2.accept(proposal2);
        acceptor3.accept(proposal3);
        acceptor4.accept(proposal3);

        // Intentionally pass the acceptors in out of order to ensure we don't just trust the last
        // one in the list.
        Proposer<String> proposer = new ProposerImpl<>(1, Arrays.asList(acceptor1, acceptor3, acceptor4, acceptor5, acceptor2));
        proposer.propose("foo");

        assertEquals(proposal3.getValue(), acceptor1.lastAccept.getValue());
        assertEquals(proposal3.getProposalNumber() + 1, acceptor1.lastAccept.getProposalNumber());
        assertEquals(proposal3.getValue(), acceptor2.lastAccept.getValue());
        assertEquals(proposal3.getProposalNumber() + 1, acceptor2.lastAccept.getProposalNumber());
        assertEquals(proposal3.getValue(), acceptor3.lastAccept.getValue());
        assertEquals(proposal3.getProposalNumber() + 1, acceptor3.lastAccept.getProposalNumber());
        assertEquals(proposal3.getValue(), acceptor4.lastAccept.getValue());
        assertEquals(proposal3.getProposalNumber() + 1, acceptor4.lastAccept.getProposalNumber());
        assertEquals(proposal3.getValue(), acceptor5.lastAccept.getValue());
        assertEquals(proposal3.getProposalNumber() + 1, acceptor5.lastAccept.getProposalNumber());
    }

    @Test
    public void phase2Retries() {
        // Define a custom dummy acceptor that will allow the initial prepare, but on accept
        // will initially act as if another prepare arrived with a higher sequence number before
        // the accept call was made.
        // Expected flow:
        // (1) Test Proposer proposes.
        // (2) Fictional Proposer proposes with proposal number 20.
        // (3) Test Proposer sends accept request and is rejected.
        // (4) Test Proposer ramps up its proposal number.
        // (5) Test Proposer sends accept request and is accepted.
        DummyAcceptor<String> acceptor = new DummyAcceptor<String>() {
            @Override
            public boolean accept(Proposal<String> p) {
                if (p.getProposalNumber() <= 20) {
                    lastPrepare = proposal1;
                    return false;

                } else {
                    lastAccept = p;
                    return true;
                }
            }
        };

        Proposer<String> proposer = new ProposerImpl<>(1, Collections.singletonList(acceptor));
        proposer.propose("foo");
        assertEquals(proposal1.getProposalNumber() + 1, acceptor.lastAccept.getProposalNumber());
        assertEquals("foo", acceptor.lastAccept.getValue());
    }

    @Test
    public void phase2RetryStopsAtQuorum() {
        DummyAcceptor<String> acceptor1 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor2 = new DummyAcceptor<>();
        DummyAcceptor<String> acceptor3 = new DummyAcceptor<String>() {
            @Override
            public boolean accept(Proposal<String> p) {
                // Accept nothing! Mwahahaha!
                return false;
            }
        };

        // We expect the proposer to complete even though acceptor3 hasn't accepted its request.
        // If there is a bug here, the global test timeout will catch us, rather than letting us loop forever.
        Proposer<String> proposer = new ProposerImpl<>(1, Arrays.asList(acceptor1, acceptor2, acceptor3));
        proposer.propose("foo");
        assertEquals(1, acceptor1.lastAccept.getProposalNumber());
        assertEquals("foo", acceptor1.lastAccept.getValue());
        assertEquals(1, acceptor2.lastAccept.getProposalNumber());
        assertEquals("foo", acceptor2.lastAccept.getValue());
    }

    private static class DummyAcceptor<V> implements Acceptor<V> {
        public Proposal<V> lastPrepare = null;
        public Proposal<V> lastAccept = null;

        @Override
        public Optional<Promise<V>> prepare(Proposal<V> proposal) {
            if (lastPrepare == null || proposal.getProposalNumber() > lastPrepare.getProposalNumber()) {
                lastPrepare = proposal;
                Promise<V> promise;
                if (lastAccept != null) {
                    promise = Promise.withProposal(lastAccept);
                } else {
                    promise = Promise.withoutProposal();
                }
                return Optional.of(promise);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public boolean accept(Proposal<V> p) {
            lastAccept = p;
            return true;
        }
    }
}
