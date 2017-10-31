package com.cts.status.data;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

import java.util.*;
import java.util.stream.Collectors;

public class HostStatusClient
{
    private DynamoDBMapper mapper;
    private Regions REGION = Regions.US_WEST_2;

    public HostStatusClient init()
    {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        mapper = new DynamoDBMapper(client);

        return this;
    }

    public void save(HostStatus hostStatus)
    {
        hostStatus.setIsDown(hostStatus.getErrorMessage()!=null && !hostStatus.getErrorMessage().isEmpty());
        mapper.save(hostStatus);
    }

    public HostStatus load(String keyId)
    {
        return mapper.load(HostStatus.class, keyId);
    }

    public void delete(String keyId)
    {
        HostStatus toDelete = new HostStatus();
        toDelete.setHostKeyId(keyId);
        mapper.delete(toDelete);
    }

    public List<HostStatus> getAll()
    {
        List<HostStatus> hostStatuses = new ArrayList<>();

        hostStatuses.addAll(
            mapper.scan(HostStatus.class, new DynamoDBScanExpression())
                .stream()
                .collect(Collectors.toList())
        );
        return hostStatuses;
    }

}
