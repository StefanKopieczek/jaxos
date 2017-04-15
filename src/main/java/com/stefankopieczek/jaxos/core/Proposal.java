package com.stefankopieczek.jaxos.core;

public final class Proposal<V> {
    private final int proposalNumber;
    private final V value;

    public Proposal(int proposalNumber, V value) {
        this.proposalNumber = proposalNumber;
        this.value = value;
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public V getValue() {
        return value;
    }
}
