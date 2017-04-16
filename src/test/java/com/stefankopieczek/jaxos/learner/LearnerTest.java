package com.stefankopieczek.jaxos.learner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.junit.rules.Timeout;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import com.stefankopieczek.jaxos.core.Proposal;


public class LearnerTest {
    private static final int TIMEOUT_MS = 1000;

    // Crazy black magic to turn JUnit TestTimedOutExceptions into
    // TimeoutExceptions so that we can expect them in tests.
	@Rule
    public Timeout timeout = new Timeout(TIMEOUT_MS) {
        public Statement apply(Statement base, Description description) {
            return new FailOnTimeout(base, TIMEOUT_MS) {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        super.evaluate();
                        throw new TimeoutException();
                    } catch (Exception e) {}
                }
            };
        }
    };

    private static int FUTURE_TIMEOUT_SECS = 1;

    private static final Proposal<String> p1a = new Proposal<>(1, "foo");
    private static final Proposal<String> p1b = new Proposal<>(1, "bar");
    private static final Proposal<String> p1c = new Proposal<>(1, "baz");
    private static final Proposal<String> p1d = new Proposal<>(1, "baff");
    private static final Proposal<String> p2a = new Proposal<>(2, "foo");
    private static final Proposal<String> p2b = new Proposal<>(2, "bar");
    private static final Proposal<String> p2c = new Proposal<>(2, "baz");
    private static final Proposal<String> p2d = new Proposal<>(2, "baff");
    private static final Proposal<String> p3a = new Proposal<>(3, "foo");
    private static final Proposal<String> p3b = new Proposal<>(3, "bar");
    private static final Proposal<String> p3c = new Proposal<>(3, "baz");
    private static final Proposal<String> p3d = new Proposal<>(3, "baff");

    @Test
    public void learnSingleValue() throws TimeoutException {
        Learner<String> learner = new LearnerImpl<>(1);
        learner.teach(1, p1a);
        assertEquals(p1a.getValue(), learner.getValue());
    }

    @Test
    public void learnSubsequentValue() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(1);
        learner.teach(1, p1a);
        learner.teach(1, p2b);
        assertEquals(p2b.getValue(), learner.getValue());
    }

    @Test
    public void rejectPriorValue() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(1);
        learner.teach(1, p2b);
        learner.teach(1, p1a);
        assertEquals(p2b.getValue(), learner.getValue());
    }

    @Test(expected=TimeoutException.class)
    public void tooFewResponses1() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(2);
        learner.teach(1, p1a);
        learner.getValue();
    }

    @Test(expected=TimeoutException.class)
    public void tooFewResponses2() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(3);
        learner.teach(1, p1a);
        learner.getValue();
    }

    @Test(expected=TimeoutException.class)
    public void tooFewResponses3() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(4);
        learner.teach(1, p1a);
        learner.teach(2, p1a);
        learner.teach(1, p2b);
        learner.teach(2, p2b);
        learner.getValue();
    }

    @Test(expected=TimeoutException.class)
    public void noConsensus1() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(3);
        learner.teach(1, p1a);
        learner.teach(2, p1b);
        learner.teach(3, p1c);
        learner.getValue();
    }

    @Test(expected=TimeoutException.class)
    public void noConsensus2() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(4);
        learner.teach(1, p1a);
        learner.teach(2, p1a);
        learner.teach(3, p2b);
        learner.teach(4, p2b);
        learner.getValue();
    }

    @Test
    public void eventualConsensus1() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(3);
        learner.teach(1, p1a);
        learner.teach(2, p1b);
        learner.teach(3, p1c);
        learner.teach(1, p2b);
        assertEquals(p1b.getValue(), learner.getValue());
    }

    @Test
    public void eventualConsensus2() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(3);
        learner.teach(1, p1a);
        learner.teach(2, p1b);
        learner.teach(3, p1c);
        learner.teach(1, p2c);
        assertEquals(p1c.getValue(), learner.getValue());
    }

    @Test
    public void eventualConsensus3() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(3);
        learner.teach(1, p1a);
        learner.teach(2, p1b);
        learner.teach(3, p1c);
        learner.teach(3, p2a);
        assertEquals(p1a.getValue(), learner.getValue());
    }

    @Test
    public void eventualConsensus4() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(3);
        learner.teach(1, p1a);
        learner.teach(2, p1b);
        learner.teach(3, p1c);
        learner.teach(1, p2d);
        learner.teach(1, p3b);
        assertEquals(p1b.getValue(), learner.getValue());
    }

    @Test
    public void eventualConsensus5() throws TimeoutException  {
        Learner<String> learner = new LearnerImpl<>(4);
        learner.teach(1, p1a);
        learner.teach(2, p1a);
        learner.teach(3, p1b);
        learner.teach(4, p1b);
        learner.teach(1, p2c);
        learner.teach(2, p2c);
        learner.teach(4, p2c);
        learner.getValue();
    }

    private <V> V timedGet(Future<V> future) throws TimeoutException {
        try {
            return future.get(FUTURE_TIMEOUT_SECS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
