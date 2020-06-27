package app;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author i312458
 */
@RunWith(MockitoJUnitRunner.class)
public class ClusterHandlerTest {

    @InjectMocks
    ClusterHandler clusterHandler;

    public void setUp() {
        clusterHandler.preprocessAndLoadCache();
    }

    @Test
    public void hostDown() {
        Assert.assertEquals(Collections.emptyList(), clusterHandler.hostDown(null));
    }

    @Test
    public void getAvailableClusters() {
    }
}