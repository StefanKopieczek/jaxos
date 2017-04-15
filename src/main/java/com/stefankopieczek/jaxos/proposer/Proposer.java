package com.stefankopieczek.jaxos.proposer;

public interface Proposer<V> {
    void propose(V valueHint);
}
