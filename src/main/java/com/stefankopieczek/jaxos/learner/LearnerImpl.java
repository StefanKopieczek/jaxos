package com.stefankopieczek.jaxos.learner;

import com.stefankopieczek.jaxos.core.Proposal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class LearnerImpl<V> implements Learner<V> {
    private final Lock lock = new ReentrantLock();
    private final Condition newValueAvailable = lock.newCondition();
    private final int numAcceptors;
    private final HashMap<Integer, Proposal<V>> taughtValues;
    private Optional<V> consensusValue;

    public LearnerImpl(int numAcceptors) {
        this.numAcceptors = numAcceptors;
        this.taughtValues = new HashMap<>();
        this.consensusValue = Optional.empty();
    }

    @Override
    public V getValue() {
        lock.lock();
        try {
            while (!consensusValue.isPresent()) {
                newValueAvailable.awaitUninterruptibly();
            }
            return consensusValue.get();
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void teach(int acceptorId, Proposal<V> proposal) {
        lock.lock();
        try {
            boolean newValue = !taughtValues.containsKey(acceptorId) ||
                (taughtValues.get(acceptorId).getProposalNumber() <
                    proposal.getProposalNumber());

            if (newValue) {
                taughtValues.put(acceptorId, proposal);
                consensusValue = calculateConsensusValue();
                if (consensusValue.isPresent()) {
                    newValueAvailable.signalAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private Optional<V> calculateConsensusValue() {
        if (taughtValues.size() == 0) {
            return Optional.empty();
        }

        // Since V might not be hashable we can't use bucket counting to identify
        // the majority value.
        // Instead we use Moore's Voting Algorithm to identify a consensus
        // in two linear passes.

        // STEP 1 - Identify a possible candidate for the consensus value.
        // If there *is* a consensus value, we will find it in this step.
        // However, if there is no consensus we might end up with a false positive.
        List<V> values = taughtValues.values().stream()
            .map(proposal -> proposal.getValue())
            .collect(Collectors.toList());

        V candidate = values.get(0);
        int score = 1;

        for (V other : values.subList(1, values.size())) {
            if (other.equals(candidate)) {
                score += 1;
            } else {
                score -= 1;
            }

            if (score == 0) {
                candidate = other;
                score = 1;
            }
        }

        // STEP 2 - Count up how many times our candidate occurs to figure out
        // whether it actually holds a majority.
        final V fCandidate = candidate;
        long count = values.stream().filter(v -> v.equals(fCandidate)).count();
        if (count >= (numAcceptors / 2) + 1) {
           return Optional.of(candidate);
        } else {
           return Optional.empty();
        }
    }
}
