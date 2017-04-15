package com.stefankopieczek.jaxos.proposer;

import com.stefankopieczek.jaxos.acceptor.Acceptor;
import com.stefankopieczek.jaxos.core.Proposal;
import com.stefankopieczek.jaxos.core.Promise;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

public class ProposerImpl<V> implements Proposer<V> {
    private static final int INITIAL_PROPOSAL_NUMBER = 1;
    private final int id;
    private final Collection<Acceptor<V>> acceptors;
    private Proposal<V> proposal;

    public ProposerImpl(int id, Collection<Acceptor<V>> acceptors) {
        this.id = id;
        this.acceptors = acceptors;
        this.proposal = null;
    }

    @Override
    public synchronized void propose(V valueHint) {
        if (proposal == null) {
            proposal = new Proposal<>(INITIAL_PROPOSAL_NUMBER, valueHint);
        }

        boolean success = false;
        while (!success) {
            phase1();
            success = phase2();
        }
    }

    private void phase1() {
        while (true) {
            List<Optional<Promise<V>>> prepareResults = acceptors.stream()
                    .map(acceptor -> acceptor.prepare(proposal)).collect(Collectors.toList());

            if (hasPrepareQuorum(prepareResults)) {
                // We received a quorum of promises back from the acceptors, so phase 1 is complete.
                // If any promises contained already-accepted proposals, replace our proposal with the highest of them.
                Optional<Proposal<V>> bestExistingAcceptedProposal = getMaximalProposal(prepareResults);
                proposal = bestExistingAcceptedProposal
                        .map(p -> new Proposal<>(proposal.getProposalNumber(), p.getValue()))
                        .orElse(proposal);
                break;
            } else {
                // We failed to receive a quorum of responses. Increment our proposal number and try again.
                proposal = new Proposal<>(proposal.getProposalNumber() + 1, proposal.getValue());
            }
        }
    }

    private boolean phase2() {
        List<Boolean> acceptResults = acceptors.stream().map(acceptor -> acceptor.accept(proposal))
                    .collect(Collectors.toList());

        return hasAcceptQuorum(acceptResults);
    }

    private boolean hasPrepareQuorum(Collection<Optional<Promise<V>>> prepareResults) {
        long numPromises = prepareResults.stream().filter(opt -> opt.isPresent()).count();
        return (numPromises >= (prepareResults.size() / 2) + 1);
    }

    private boolean hasAcceptQuorum(Collection<Boolean> acceptResults) {
        long numAccepted = acceptResults.stream().filter(isAccepted -> isAccepted).count();
        return (numAccepted >= (acceptResults.size() / 2) + 1);
    }

    private Optional<Proposal<V>> getMaximalProposal(Collection<Optional<Promise<V>>> prepareResults) {
        return prepareResults.stream()
            .filter(opt -> opt.isPresent())            // Ignore non-responses.
            .map(opt -> opt.get())                     // Extract the promises from the responses.
            .filter(promise -> promise.hasProposal())  // Ignore promises without a previously-accepted proposal.
            .map(promise -> promise.getProposal())     // Extract the proposals.
            .max(new Comparator<Proposal<V>>() {       // Choose the proposal with the highest proposal number.
                @Override
                public int compare(Proposal<V> p1, Proposal<V> p2) {
                    return p1.getProposalNumber() - p2.getProposalNumber();
                };
            });
    }
}
