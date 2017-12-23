package ru.turko.mephi;

import org.apache.ignite.*;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;


/**
 * Class for testing single input
 */
public class LogCounterTest {
    private Ignite ignite = null;
    private IgniteCompute compute = null;
    private IgniteCache<Integer, String> cache = null;

    /**
     * Test initialization
     */
    @Before
    public void initialize() {
        try {
            Ignition.setClientMode(true);
            ignite = Ignition.start("config.xml");
            ignite.active(true);
            compute = ignite.compute();
            CacheConfiguration<Integer, String> cacheCfg = new CacheConfiguration<>("testCache");
            cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
            cacheCfg.setBackups(1);
            cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
            cacheCfg.setIndexedTypes(Integer.class, String.class);
            cache = ignite.getOrCreateCache(cacheCfg);
            cache.clear();
        } catch (Exception e) {
            System.out.println(e);
        }

    }
	
	/**
     * Utility method to generate single log in cache
     */
    private void generateSingleCache() {
        try (IgniteDataStreamer<Integer, String> streamer = ignite.dataStreamer("testCache")) {
            streamer.addData(1, "<0> Dec 02 01:21:39 127.0.0.1 service: emerg message");
        }
    }

	private void generateDoubleDifferentCache() {
        try(IgniteDataStreamer<Integer,String> streamer = ignite.dataStreamer("testCache")) {
            streamer.addData(1, "<0> Dec 02 01:21:39 127.0.0.1 service: emerg message");
            streamer.addData(2, "<6> Dec 02 01:21:39 127.0.0.1 service: info message");
        }
    }

    /**
     * Test single Log
     */
    @Test
    public void testSingleLog() {
        generateSingleCache();

        // Execute task on the cluster and wait for its completion.
        Map<HourPriority, Integer> res = compute.execute(Main.LogCountTask.class, cache); //?
        assert res.containsKey(new HourPriority(1, 0));
        assert res.get(new HourPriority(1, 0)) == 1;
    }


	/**
     * Test Double Log
     */
    @Test
    public void testDoubleDifferentLog() {
        generateDoubleDifferentCache();

        // Execute task on the cluster and wait for its completion.
        // List of Surveys are passed to CalculateSurveyTask
        Map<HourPriority, Integer> res = compute.execute(Main.LogCountTask.class, cache);
        assert res.containsKey(new HourPriority(1, 0));
        assert res.get(new HourPriority(1, 0)) == 1;
        assert res.containsKey(new HourPriority(1, 6));
        assert res.get(new HourPriority(1, 6)) == 1;
    }
	
    /**
     * Clean up resources
     */
    @After
    public void tearDown() {
        ignite.close();
    }

}
