package com.hazelcast.simulator.probes.probes;

import com.hazelcast.simulator.probes.probes.impl.HdrLatencyDistributionProbe;
import com.hazelcast.simulator.probes.probes.impl.HdrLatencyDistributionResult;
import com.hazelcast.simulator.probes.probes.impl.LatencyDistributionResult;
import com.hazelcast.simulator.probes.probes.impl.MaxLatencyResult;
import com.hazelcast.simulator.probes.probes.impl.OperationsPerSecResult;
import org.HdrHistogram.Histogram;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ProbesResultXmlTest {

    private Map<String, Result> resultMap = new HashMap<String, Result>();

    @Test
    public void testHdrLatencyProbeResult() throws Exception {
        HdrLatencyDistributionResult originalResult = createHdrLatencyDistribution();
        resultMap.put("hdrLatency", originalResult);

        Map<String, Result> result = serializeAndDeserializeAgain(resultMap);

        assertEquals(originalResult, result.get("hdrLatency"));
    }

    @Test
    public void testMaxLatencyResult() throws Exception {
        MaxLatencyResult originalResult = new MaxLatencyResult(1);
        resultMap.put("maxLatency", originalResult);

        Map<String, Result> result = serializeAndDeserializeAgain(resultMap);

        assertEquals(originalResult, result.get("maxLatency"));
    }

    @Test
    public void testLatencyDistributionResult() throws Exception {
        LatencyDistributionResult original = createLatencyDistribution();
        resultMap.put("latencyDistribution", original);

        Map<String, Result> read = serializeAndDeserializeAgain(resultMap);

        assertEquals(original, read.get("latencyDistribution"));
    }

    @Test
    public void testOperationsPerSecResult() throws Exception {
        OperationsPerSecResult original = new OperationsPerSecResult(100000, 1234.5);
        resultMap.put("operationsPerSec", original);

        Map<String, Result> read = serializeAndDeserializeAgain(resultMap);

        assertEquals(original, read.get("operationsPerSec"));
    }

    @Test
    public void testMultipleProbes() throws Exception {
        LatencyDistributionResult result1 = createLatencyDistribution();
        resultMap.put("result1", result1);

        LatencyDistributionResult result2 = createLatencyDistribution();
        resultMap.put("result2", result2);

        MaxLatencyResult result3 = new MaxLatencyResult(Integer.MAX_VALUE);
        resultMap.put("result3", result3);

        Map<String, Result> read = serializeAndDeserializeAgain(resultMap);

        assertEquals(result1, read.get("result1"));
        assertEquals(result2, read.get("result2"));
        assertEquals(result3, read.get("result3"));
    }

    private static HdrLatencyDistributionResult createHdrLatencyDistribution() {
        Histogram histogram = new Histogram(HdrLatencyDistributionProbe.MAXIMUM_LATENCY, 4);
        histogram.recordValue(0);
        histogram.recordValue(1);
        histogram.recordValue(2);
        histogram.recordValue(1);
        histogram.recordValue(5);
        histogram.recordValue(80);

        return new HdrLatencyDistributionResult(histogram);
    }

    private static LatencyDistributionResult createLatencyDistribution() {
        LinearHistogram histogram = new LinearHistogram(100, 1);
        histogram.addValue(0);
        histogram.addValue(1);
        histogram.addValue(2);
        histogram.addValue(1);
        histogram.addValue(5);
        histogram.addValue(80);

        return new LatencyDistributionResult(histogram);
    }

    private static Map<String, Result> serializeAndDeserializeAgain(Map<String, Result> resultMap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProbesResultXmlWriter.write(resultMap, outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        return ProbesResultXmlReader.read(inputStream);
    }
}