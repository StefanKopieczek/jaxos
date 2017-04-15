package com.stefankopieczek.jaxos.learner;

import com.stefankopieczek.jaxos.core.Proposal;
import java.util.concurrent.Future;

public interface Learner<V> {
    public Future<V> getValue();
    public void teach(int acceptorId, Proposal<V> proposal);
}
