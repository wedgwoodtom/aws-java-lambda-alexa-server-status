package com.cts.status.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.cts.status.data.HostStatus;
import com.cts.status.data.HostStatusClient;

import java.util.List;

/**
 * AWS Lambda Handler
 */
public class HostStatusUpdateHandler
{
    private HostStatusClient hostStatusClient = new HostStatusClient().init();

    public void updateStatus(HostStatus hostStatus, Context context)
    {
        hostStatusClient.save(hostStatus);
    }

    public void deleteStatus(HostStatus hostStatus, Context context)
    {
        hostStatusClient.delete(hostStatus.getHostKeyId());
    }

    public List<HostStatus> allHostStatus(Context context)
    {
        return hostStatusClient.getAll();
    }

}
