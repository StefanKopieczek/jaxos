package com.stefankopieczek.jaxos.acceptor;

import com.stefankopieczek.jaxos.core.Promise;
import com.stefankopieczek.jaxos.core.Proposal;
import com.stefankopieczek.jaxos.learner.Learner;
import java.util.Collection;
import java.util.Optional;

public class AcceptorImpl<V> implements Acceptor<V> {
    public AcceptorImpl(Collection<Learner<V>> learners) {
        // TODO
    }

    @Override
    public Optional<Promise<V>> prepare(Proposal<V> p) {
        // TODO
        return Optional.empty();
    }

    @Override
    public boolean accept(Proposal<V> p) {
        // TODO
        return false;
    }
}
