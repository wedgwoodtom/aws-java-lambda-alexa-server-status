package com.cts.status.data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "HostStatus")
public class HostStatus
{
    private String hostKeyId;
    private boolean isDown = false;
    private String errorMessage;

    public static HostStatus hostStatusFrom(String hostId, String errorMessage)
    {
        HostStatus hostStatus = new HostStatus();
        hostStatus.setHostKeyId(hostId);
        hostStatus.setErrorMessage(errorMessage);
        hostStatus.setIsDown(errorMessage!=null && errorMessage.length()>0);

        return hostStatus;
    }

    @DynamoDBAttribute(attributeName = "ErrorMessage")
    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    @DynamoDBAttribute(attributeName = "IsDown")
    public boolean getIsDown()
    {
        return isDown;
    }

    public void setIsDown(boolean down)
    {
        isDown = down;
    }

    @DynamoDBHashKey(attributeName = "HostKeyId")
    public String getHostKeyId()
    {
        return hostKeyId;
    }

    public void setHostKeyId(String hostKeyId)
    {
        this.hostKeyId = hostKeyId;
    }
}
