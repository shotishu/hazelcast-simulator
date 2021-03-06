package com.hazelcast.simulator.probes.probes.impl;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperationsPerSecProbeTest extends AbstractProbeTest {

    private OperationsPerSecProbe operationsPerSecProbe = new OperationsPerSecProbe();

    @Test
    public void testStarted() {
        operationsPerSecProbe.started();
    }

    @Test
    public void testInvocations() {
        operationsPerSecProbe.done();
        operationsPerSecProbe.done();
        operationsPerSecProbe.done();

        assertEquals(3, operationsPerSecProbe.getInvocationCount());
    }

    @Test(expected = IllegalStateException.class)
    public void testResultWithInitialization() {
        operationsPerSecProbe.getResult();
    }

    @Test
    public void testResult() {
        long started = System.currentTimeMillis();
        operationsPerSecProbe.startProbing(started);
        operationsPerSecProbe.done();
        operationsPerSecProbe.done();
        operationsPerSecProbe.done();
        operationsPerSecProbe.done();
        operationsPerSecProbe.stopProbing(started + TimeUnit.SECONDS.toMillis(5));

        OperationsPerSecResult result = operationsPerSecProbe.getResult();
        assertTrue(result != null);

        Long invocations = getObjectFromField(result, "invocations");
        assertEqualsStringFormat("Expected %d invocations, but was %d", 4L, invocations);

        Double operationsPerSecond = getObjectFromField(result, "operationsPerSecond");
        assertEqualsStringFormat("Expected %.2f op/s, but was %.2f", 0.8, operationsPerSecond, 0.01);
    }

    @Test
    public void testResultToHumanString() {
        long started = System.currentTimeMillis();
        operationsPerSecProbe.startProbing(started);
        operationsPerSecProbe.done();
        operationsPerSecProbe.stopProbing(started + TimeUnit.SECONDS.toMillis(5));

        OperationsPerSecResult result = operationsPerSecProbe.getResult();
        assertTrue(result != null);
        assertTrue(result.toHumanString() != null);
    }

    @Test
    public void testResultCombine() {
        long started = System.currentTimeMillis();
        operationsPerSecProbe.startProbing(started);
        operationsPerSecProbe.done();
        operationsPerSecProbe.done();
        operationsPerSecProbe.stopProbing(started + TimeUnit.SECONDS.toMillis(5));

        OperationsPerSecResult result1 = operationsPerSecProbe.getResult();
        assertTrue(result1 != null);

        operationsPerSecProbe.startProbing(started + TimeUnit.SECONDS.toMillis(10));
        operationsPerSecProbe.done();
        operationsPerSecProbe.done();
        operationsPerSecProbe.stopProbing(started + TimeUnit.SECONDS.toMillis(15));

        OperationsPerSecResult result2 = operationsPerSecProbe.getResult();
        assertTrue(result2 != null);

        OperationsPerSecResult combined = result1.combine(result2);
        assertTrue(combined != null);

        Long invocations = getObjectFromField(combined, "invocations");
        assertEqualsStringFormat("Expected %d invocations, but was %d", 6L, invocations);

        Double operationsPerSecond = getObjectFromField(combined, "operationsPerSecond");
        assertEqualsStringFormat("Expected %.2f op/s, but was %.2f", 1.2, operationsPerSecond, 0.01);
    }
}