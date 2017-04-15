package com.stefankopieczek.jaxos.proposer;

import com.stefankopieczek.jaxos.acceptor.Acceptor;
import java.util.Collection;

public class ProposerImpl<V> implements Proposer<V> {
    private static final int INITIAL_PROPOSAL_NUMBER = 1;

    public ProposerImpl(int proposerId, Collection<Acceptor> acceptors) {
        // TODO
    }

    @Override
    public void propose(V valueHint) {
        // TODO
    }
}
