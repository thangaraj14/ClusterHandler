package app;

import model.HostDetails;
import model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class ClusterHandler {

    private int size;
    private List<HostDetails> hostDetailsList;
    private ConcurrentHashMap<String, Integer> mappingTable;
    private Logger logger = LoggerFactory.getLogger(ClusterHandler.class);
    private ReentrantReadWriteLock lock = null;

    public ClusterHandler(int size) {
        this.size = size;
        hostDetailsList = new ArrayList<>();
        mappingTable = new ConcurrentHashMap<>();
        lock = new ReentrantReadWriteLock(true);
    }

    public static void main(String[] args) {

        ClusterHandler clusterAvailability = new ClusterHandler(4);
        clusterAvailability.preprocessAndLoadCache();
        String[] arr = { "host2", "host3" };

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        Callable callable = () -> {
            String[] params = { "host2", "host3" };
            return clusterAvailability.hostDown(params);
        };

        Callable callable1 = () -> {
            String[] params = { "host2", "host1" };
            return clusterAvailability.hostDown(params);
        };

        Future future = executorService.submit(callable);
        Future submit = executorService.submit(callable1);
        Object o = null;
        try {
            List<Result> list = (List<Result>) future.get();
            System.out.println("******---------******");
            System.out.println("Deleted cluster details: ");
            if (list == null) {
                System.out.println("The given cluster doesn't exist");
            } else if (list.isEmpty()) {
                System.out.println("Max of 2 hosts can go down at a time.");
            } else {
                list.forEach(System.out::println);
            }

            List<Result> list1 = (List<Result>) submit.get();
            System.out.println("******---------******");
            System.out.println("Deleted cluster details: ");
            if (list1 == null) {
                System.out.println("The given cluster doesn't exist");
            } else if (list1.isEmpty()) {
                System.out.println("Max of 2 hosts can go down at a time.");
            } else {
                list1.forEach(System.out::println);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executorService.shutdown();

/*                List<Result> list = clusterAvailability.hostDown(arr);

        System.out.println("******---------******");
        System.out.println("Available cluster details: ");
        if (clusterAvailability.hostDetailsList.size() == 0) {
            System.out.println("There is no clusters available");
        } else {
            clusterAvailability.hostDetailsList.forEach(System.out::println);
        }*/
    }

    /**
     * It validates the hosts and perform the required operations for the same.
     *
     * @param hostName
     *
     * @return
     */
    public List<Result> hostDown(String[] hostName) {

        logger.debug("hostDown execution started");
        if (hostName == null || hostName.length == 0) {
            logger.error("Please give valid host name of a cluster");
            return null;
        }
        try {
            lock.writeLock().lock();
            if (hostName.length > 2) {
                logger.error("Max of 2 hosts can go down at a time.");
                return Collections.emptyList();
            }
            List<Result> results = new ArrayList<>();
            for (String host : hostName) {
                HostDetails deletedCluster = removeCluster(host);
                if (deletedCluster == null) {
                    logger.error("The given cluster doesn't exist");
                    return null;
                }
                assignFileToOtherClusters(results, deletedCluster);
            }
            logger.debug("hostDown execution ended");
            return results;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * It copies the files to the available cluster based on the size.
     *
     * @param results
     * @param deletedCluster
     */
    private void assignFileToOtherClusters(List<Result> results, HostDetails deletedCluster) {
        PriorityQueue<HostDetails> priorityQueue = new PriorityQueue<>(
                Comparator.comparingInt(hostDetails -> hostDetails.getHostSize()));

        hostDetailsList.forEach(hostDetails -> priorityQueue.add(hostDetails));

        HashMap<String, String> values = deletedCluster.getValues();

        for (Map.Entry<String, String> map : values.entrySet()) {
            HostDetails primaryHostDetail = priorityQueue.remove();
            HostDetails backupHostDetail = priorityQueue.remove();
            primaryHostDetail = map.getValue().equals(primaryHostDetail) ? backupHostDetail : primaryHostDetail;
            primaryHostDetail.getValues().put(map.getKey(), map.getValue());
            primaryHostDetail.setHostSize(primaryHostDetail.getHostSize() + 1);
            results.add(new Result(map.getKey(), map.getValue(), primaryHostDetail.getHostName()));
            priorityQueue.offer(primaryHostDetail);
            priorityQueue.offer(backupHostDetail);
        }
    }

    /**
     * it removes the existing cluster from the cache
     *
     * @param hostName
     *
     * @return
     */
    private HostDetails removeCluster(String hostName) {

        logger.debug("removeCluster execution started");

        Integer index = mappingTable.get(hostName);
        if (index == null) {
            return null;
        }
        HostDetails hostDetails = hostDetailsList.get(index);

        int size = hostDetailsList.size();
        HostDetails hostDetail = hostDetailsList.get(size - 1);

        if (size != 1) {
            hostDetailsList.set(index, hostDetail);
        }
        hostDetailsList.remove(size - 1);

        logger.debug("removeCluster execution ended");
        return hostDetails;
    }

    public List<HostDetails> getAvailableClusters() {
        logger.debug("getAvailableClusters execution started");
        try {
            lock.readLock().lock();
            return hostDetailsList;
        } finally {
            lock.readLock().unlock();
        }

    }

    /**
     * It preprocesses the data and loads into the cache
     */
    public void preprocessAndLoadCache() {

        logger.debug("preprocessAndLoadCache execution started");

        HashMap<String, String> map1 = new HashMap<>();
        map1.put("file1", "host3");
        map1.put("file2", "host2");
        map1.put("file4", "host3");
        map1.put("file5", "host4");
        mappingTable.put("host1", hostDetailsList.size());
        hostDetailsList.add(new HostDetails("host1", map1.size(), map1));

        HashMap<String, String> map2 = new HashMap<>();
        map2.put("file2", "host1");
        map2.put("file3", "host3");
        mappingTable.put("host2", hostDetailsList.size());
        hostDetailsList.add(new HostDetails("host2", map2.size(), map2));

        HashMap<String, String> map3 = new HashMap<>();
        map3.put("file1", "host1");
        map3.put("file3", "host2");
        map3.put("file4", "host1");
        mappingTable.put("host3", hostDetailsList.size());
        hostDetailsList.add(new HostDetails("host3", map3.size(), map3));

        HashMap<String, String> map4 = new HashMap<>();
        map4.put("file5", "host1");
        mappingTable.put("host4", hostDetailsList.size());
        hostDetailsList.add(new HostDetails("host4", map4.size(), map4));

        logger.debug("preprocessAndLoadCache execution ended");
    }
}




