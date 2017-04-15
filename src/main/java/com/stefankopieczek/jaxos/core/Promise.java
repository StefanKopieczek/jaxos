package com.stefankopieczek.jaxos.core;

public final class Promise<V> {
    private final Proposal<V> acceptableProposal;

    public Promise(Proposal<V> acceptableProposal) {
        this.acceptableProposal = acceptableProposal;
    }

    public Proposal<V> getAcceptableProposal() {
        return this.acceptableProposal;
    }
}
