package com.stefankopieczek.jaxos.learner;

import com.stefankopieczek.jaxos.core.Proposal;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class LearnerImpl<V> implements Learner<V> {
    public LearnerImpl(int numAcceptors) {
        // TODO
    }

    public Future<V> getValue() {
        // TODO
        return new FutureTask<V>(new Callable<V>() {
            @Override
            public V call() {
                return null;
            }
        });
    }

    public void teach(int acceptorId, Proposal<V> proposal) {
        // TODO
    }
}
