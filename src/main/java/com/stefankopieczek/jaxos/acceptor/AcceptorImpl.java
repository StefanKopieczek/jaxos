package com.stefankopieczek.jaxos.acceptor;

import com.stefankopieczek.jaxos.core.Promise;
import com.stefankopieczek.jaxos.core.Proposal;
import com.stefankopieczek.jaxos.learner.Learner;
import java.util.Collection;
import java.util.Optional;

public class AcceptorImpl<V> implements Acceptor<V> {
    private final int id;
    private final Collection<Learner<V>> learners;
    private int maxProposalNumber = -1;
    private Proposal<V> acceptedProposal;

    public AcceptorImpl(int id, Collection<Learner<V>> learners) {
        this.id = id;
        this.learners = learners;
        this.acceptedProposal = null;
    }

    @Override
    public synchronized Optional<Promise<V>> prepare(Proposal<V> proposal) {
        if (proposal.getProposalNumber() < maxProposalNumber) {
            return Optional.empty();
        } else {
            if (maxProposalNumber < proposal.getProposalNumber()) {
                maxProposalNumber = proposal.getProposalNumber();
            }

            if (acceptedProposal != null) {
                return Optional.of(Promise.withProposal(acceptedProposal));
            } else {
                return Optional.of(Promise.withoutProposal());
            }
        }
    }

    @Override
    public synchronized boolean accept(Proposal<V> p) {
        if (p.getProposalNumber() < maxProposalNumber) {
            return false;
        } else {
            for (Learner learner : learners) {
                learner.teach(id, p);
            }

            acceptedProposal = p;
            return true;
        }
    }
}
