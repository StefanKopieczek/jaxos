package com.stefankopieczek.jaxos.learner;

import com.stefankopieczek.jaxos.core.Proposal;

public interface Learner<V> {
    public V getValue();
    public void teach(int acceptorId, Proposal<V> proposal);
}
