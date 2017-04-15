package com.stefankopieczek.jaxos.acceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.Optional;

import com.stefankopieczek.jaxos.core.Proposal;
import com.stefankopieczek.jaxos.core.Promise;
import com.stefankopieczek.jaxos.learner.Learner;

public class AcceptorTest {
    private static final Proposal<String> p1 = new Proposal<>(1, "foo");
    private static final Proposal<String> p2 = new Proposal<>(2, "bar");
    @Test
    public void initialProposal() {
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.emptyList());
        Optional<Promise<String>> result = acceptor.prepare(p1);
        assertTrue(result.isPresent());
        Proposal<String> proposal = result.get().getAcceptableProposal();
        assertEquals(p1, proposal);
    }

    @Test
    public void greaterProposalBeforeAccept() {
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.emptyList());
        acceptor.prepare(p1);
        Optional<Promise<String>> result = acceptor.prepare(p2);
        assertTrue(result.isPresent());
        Proposal<String> proposal = result.get().getAcceptableProposal();
        assertEquals(p2, proposal);
    }

    @Test
    public void lesserProposalBeforeAccept() {
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.emptyList());
        acceptor.prepare(p2);
        Optional<Promise<String>> result = acceptor.prepare(p1);
        assertFalse(result.isPresent());
    }

    @Test
    public void acceptRequest() {
        DummyLearner<String> learner = new DummyLearner<>();
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.singletonList(learner));
        acceptor.prepare(p1);
        assertTrue(acceptor.accept(p1));
        assertTrue(learner.hasLearnt);
        assertEquals(p1, learner.proposal);
    }

    @Test
    public void greaterProposalAfterAccept() {
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.emptyList());
        acceptor.prepare(p1);
        acceptor.accept(p1);
        Optional<Promise<String>> result = acceptor.prepare(p2);
        assertTrue(result.isPresent());
        assertEquals(p1, result.get().getAcceptableProposal());
    }

    @Test
    public void lesserProposalAfterAccept() {
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.emptyList());
        acceptor.prepare(p2);
        acceptor.accept(p2);
        Optional<Promise<String>> result = acceptor.prepare(p1);
        assertFalse(result.isPresent());
    }

    @Test
    public void lesserAcceptRequestAfterProposal() {
        DummyLearner<String> learner = new DummyLearner<>();
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.singletonList(learner));
        acceptor.prepare(p1);
        acceptor.prepare(p2);
        assertFalse(acceptor.accept(p1));
        assertFalse(learner.hasLearnt);
    }

    @Test
    public void greaterAcceptRequestAfterProposal() {
        DummyLearner<String> learner = new DummyLearner<>();
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.singletonList(learner));
        acceptor.prepare(p1);
        acceptor.prepare(p2);
        assertTrue(acceptor.accept(p2));
        assertTrue(learner.hasLearnt);
        assertEquals(p2, learner.proposal);
    }

    @Test
    public void greaterAcceptRequestAfterAccept() {
        DummyLearner<String> learner = new DummyLearner<>();
        Acceptor<String> acceptor = new AcceptorImpl<>(Collections.singletonList(learner));
        acceptor.prepare(p1);
        acceptor.accept(p1);
        acceptor.prepare(p2);
        assertTrue(acceptor.accept(p2));
        assertTrue(learner.hasLearnt);
        assertEquals(p2, learner.proposal);
    }

    private static class DummyLearner<V> implements Learner<V> {
        public int teacherId = -1;
        public Proposal<V> proposal = null;
        public boolean hasLearnt = false;

        @Override
        public Future<V> getValue() {
            // Unused
            return null;
        }

        @Override
        public void teach(int acceptorId, Proposal<V> proposal) {
            hasLearnt = true;
            teacherId = acceptorId;
            this.proposal = proposal;
        }
    }
}
