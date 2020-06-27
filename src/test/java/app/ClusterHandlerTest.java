package app;

import model.HostDetails;
import model.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

/**
 * @author i312458
 */
@RunWith(MockitoJUnitRunner.class)
public class ClusterHandlerTest {

    //    @InjectMocks
    ClusterHandler clusterHandler;

    @Before
    public void setup() {
        clusterHandler = new ClusterHandler(4);
        clusterHandler.preprocessAndLoadCache();
    }

    @Test(expected = IllegalArgumentException.class)
    public void hostDown() {
        clusterHandler.hostDown(null);
    }

    @Test
    public void hostDown2() {
        List<Result> results = clusterHandler.hostDown(new String[] { "host2" });
        Assert.assertEquals(2, results.size());
    }

    @Test
    public void getAvailableClusters() {
        clusterHandler.hostDown(new String[] { "host2" });
        List<HostDetails> availableClusters = clusterHandler.getAvailableClusters();
        Assert.assertEquals(3, availableClusters.size());
    }

    @Test
    public void getAvailableClustersAfterHostDown() {

        clusterHandler.hostDown(new String[] { "host2", "host3" });
        List<HostDetails> availableClusters = clusterHandler.getAvailableClusters();
        Assert.assertEquals(2, availableClusters.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void hostDownMoreThanTwo() {

        clusterHandler.hostDown(new String[] { "host2", "host3", "host4" });
    }

}