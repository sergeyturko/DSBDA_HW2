package ru.turko.mephi;

import org.apache.ignite.*;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskSplitAdapter;
import org.apache.ignite.configuration.CacheConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import javax.cache.Cache;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;


/**
 * Main class
 */
public class Main {
	 /**
     * Address for Flume
     */
	public static final String HOST = "localhost";
    public static final int PORT = 12345;

    /**
     * Main function
     * @param args List of arguments
     */
    public static void main(String[] args) throws IgniteException, IOException, ParseException {
        if (args.length > 0 && ("store".equals(args[0]) || "compute".equals(args[0]))) {
            Ignition.setClientMode(true);
            try (Ignite ignite = Ignition.start(args[1])) {
                ignite.active(true);
                CacheConfiguration<Integer, String> cacheCfg = new CacheConfiguration<>("myCache");
                cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
                cacheCfg.setBackups(1);
                cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
                cacheCfg.setIndexedTypes(Integer.class, String.class);
				
                // Put values in cache.
                IgniteCache<Integer, String> cache = ignite.getOrCreateCache(cacheCfg);
                if(args[0].equals("store")) {
                    System.out.println("Storing logs from " + args[2]);
                    Path filePath = Paths.get(args[2]);
                    try (Scanner scaner = new Scanner(filePath);
                         IgniteDataStreamer<Integer, String> streamer = ignite.dataStreamer("myCache")) {
                        int i = cache.size()+1;
                        while (scaner.hasNextLine()) {
                            streamer.addData(i, scaner.nextLine());
                            i++;
                        }
                    }
                    System.out.println("Storing completed successfully!");
                }
                else {
                    List<String> strlist = new ArrayList<>();
                    System.out.println("Start computing task...");
                    if(cache.size()==0)
                        System.out.println("No logs found!");
                    else {
                        IgniteCompute compute = ignite.compute();
                        Map<HourPriority, Integer> res = compute.execute(LogCountTask.class, cache);
                        for (Map.Entry<HourPriority, Integer> val : res.entrySet()) {
							String out = "priority - " + val.getKey().getPriority() + " hour - " + val.getKey().getHour() + " count - " + val.getValue();
                            strlist.add(out);
                            System.out.println(out);
                        }
                    }
                    System.out.println("Computing finished successfully!");
					
					/**
					* Write output with Flume
					*/			
					FlumeClient client = new FlumeClient(HOST, PORT);					
					String sendData = String.join("\n", strlist);
					client.sendData(sendData);
					client.clean();					
                }
            }

        } else {
            System.out.println("Incorrect parameters!");
			prinHelp();
        }
    }

	/**
     * Help
     */
	public static void prinHelp() {
		System.out.println("USAGE:");
		System.out.println(" First param:");
		System.out.println("	store - to storing!");
		System.out.println("	compute - to comouting!");
		System.out.println(" Second param: config-file for Ignite");
		System.out.println(" Third param: input log-file");
	}
	
    /**
     * Class for ignite compute task
     */
    static class LogCountTask extends ComputeTaskSplitAdapter<IgniteCache<Integer, String>, Map<HourPriority, Integer>> {

        @Override
        protected Collection<? extends ComputeJob> split(int i, IgniteCache<Integer, String> entries) throws IgniteException {
            List<ComputeJob> jobs = new ArrayList<>(entries.size());
            for (final Cache.Entry<Integer, String> logRecord : entries) {
                jobs.add(new ComputeJobAdapter() {
                    @Override
                    public Object execute() throws IgniteException {
                        RecordSyslog logRec = new RecordSyslog(logRecord.getValue());
                        return logRec.getHourPriorityPair();
                    }
                });
            }
            return jobs;
        }

        @Nullable
        @Override
        public Map<HourPriority, Integer> reduce(List<ComputeJobResult> list) throws IgniteException {
            Map<HourPriority, Integer> logStats = new HashMap<>();
            for (ComputeJobResult res : list) {
                HourPriority hp = res.<HourPriority>getData();
                Integer count = logStats.get(hp);
                if (count == null)
                    count = 0;
                logStats.put(hp, count + 1);

            }
            return logStats;
        }
    }
}
