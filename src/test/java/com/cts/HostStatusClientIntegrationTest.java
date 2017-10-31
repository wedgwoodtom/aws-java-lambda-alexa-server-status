package com.cts;

import com.cts.status.data.HostStatus;
import com.cts.status.data.HostStatusClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HostStatusClientIntegrationTest
{
    private static HostStatusClient hostStatusClient;
    private static final String HOST_KEY = "testtp01";

    @BeforeClass
    public static void setup()
    {
        hostStatusClient = new HostStatusClient().init();
    }

    @Test
    public void test10Save() throws Exception
    {
        HostStatus host = HostStatus.hostStatusFrom(HOST_KEY, "Java Out of Memory Exception");
        hostStatusClient.save(host);

        HostStatus fromDb = hostStatusClient.load(HOST_KEY);
        Assert.assertNotNull(fromDb);
        Assert.assertEquals(host.getHostKeyId(), fromDb.getHostKeyId());
        Assert.assertEquals(host.getErrorMessage(), fromDb.getErrorMessage());
        Assert.assertEquals(host.getIsDown(), fromDb.getIsDown());
    }

    @Test
    public void test20Update() throws Exception
    {
        HostStatus host = hostStatusClient.load(HOST_KEY);
        Assert.assertNotNull(host);
        host.setErrorMessage("Another Error Message");
        hostStatusClient.save(host);

        HostStatus fromDb = hostStatusClient.load(HOST_KEY);
        Assert.assertEquals(host.getHostKeyId(), fromDb.getHostKeyId());
        Assert.assertEquals(host.getErrorMessage(), fromDb.getErrorMessage());
        Assert.assertEquals(host.getIsDown(), fromDb.getIsDown());
    }

    @Test
    public void test30GetAll() throws Exception
    {
        List<HostStatus> allHostStatus = hostStatusClient.getAll();
        Assert.assertTrue(!allHostStatus.isEmpty());
    }

    @Test
    public void test40Delete() throws Exception
    {
        hostStatusClient.delete(HOST_KEY);
        HostStatus host = hostStatusClient.load(HOST_KEY);
        Assert.assertNull(host);
    }

    @Test
    public void test50CreateDemoHosts() throws Exception
    {
        hostStatusClient.getAll().stream().forEach((HostStatus hostStatus) -> {
            hostStatusClient.delete(hostStatus.getHostKeyId());
        });

        hostStatusClient.save(HostStatus.hostStatusFrom("sea1tpentds09", ""));
        hostStatusClient.save(HostStatus.hostStatusFrom("sea1tpentds10", ""));
        hostStatusClient.save(HostStatus.hostStatusFrom("sea1tpentds11", ""));
        hostStatusClient.save(HostStatus.hostStatusFrom("sea1tpentds12", ""));

        hostStatusClient.save(HostStatus.hostStatusFrom("phl1tpentds09", ""));
        hostStatusClient.save(HostStatus.hostStatusFrom("phl1tpentds10", ""));
        hostStatusClient.save(HostStatus.hostStatusFrom("phl1tpentds11", ""));
        hostStatusClient.save(HostStatus.hostStatusFrom("phl1tpentds12", ""));

    }

}
