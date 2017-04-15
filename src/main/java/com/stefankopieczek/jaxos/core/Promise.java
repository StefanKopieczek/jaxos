package com.stefankopieczek.jaxos.core;

import java.util.Optional;

public final class Promise<V> {
    private final Optional<Proposal<V>> proposal;

    public static <V> Promise<V> withProposal(Proposal<V> proposal) {
        return new Promise<>(Optional.of(proposal));
    }

    public static <V> Promise<V> withoutProposal() {
        return new Promise<>(Optional.empty());
    }

    private Promise(Optional<Proposal<V>> proposal) {
        this.proposal = proposal;
    }

    public boolean hasProposal() {
        return proposal.isPresent();
    }

    public Proposal<V> getProposal() {
        return this.proposal.get();
    }
}
