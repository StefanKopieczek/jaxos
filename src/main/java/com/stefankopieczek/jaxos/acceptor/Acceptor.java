package com.stefankopieczek.jaxos.acceptor;

import com.stefankopieczek.jaxos.core.Promise;
import com.stefankopieczek.jaxos.core.Proposal;
import java.util.Optional;

public interface Acceptor<V> {
    public Optional<Promise<V>> prepare(Proposal<V> p);
    public boolean accept(Proposal<V> p);
}
