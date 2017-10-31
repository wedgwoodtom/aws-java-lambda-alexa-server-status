package com.cts;

import com.cts.status.data.HostStatus;
import com.google.gson.Gson;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class HostStatusUpdateHandlerIntegrationTest
{
    String LAMBDA_URL = "https://r8katr3xak.execute-api.us-west-2.amazonaws.com/prod/setStatus";

    @Test
    public void testUpdateStatus() throws Exception
    {
        HostStatus request = HostStatus.hostStatusFrom("testtp01", "");
        Gson gson = new Gson();
        String requestStr = gson.toJson(request);

        String responseStr = Request.Post(LAMBDA_URL)
            .bodyString(requestStr, ContentType.APPLICATION_JSON)
            .execute()
            .returnContent()
            .asString();

        Assert.assertEquals(responseStr, "null");

//        Response response = gson.fromJson(responseStr, Response.class);

    }


    @Test
    public void monitorAgentTest()
    {
        List<Host> hosts = Arrays.asList(
            new Host("testtplicws03", "http://testtplicws03:10504/license/management/alive"),
            new Host("testtplicws04", "http://testtplicws04:10504/license/management/alive"),
            new Host("testtplicws05", "http://testtplicws05:10504/license/management/alive")
        );

        do {
            hosts.forEach((host) -> {
                postStatus(host.name, isAlive(host.aliveCheck) ? "" : "failing alive check");
            });

            // sleep
            try
            {
                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        } while (true);
    }

    private boolean isAlive(String aliveCheckUrl)
    {
        try
        {
            String contents = Request.Get(aliveCheckUrl)
                .execute()
                .returnContent()
                .asString();

            return contents.contains("Web Service is Ok");
        }
        catch(Exception error)
        {
            error.printStackTrace();
            return false;
        }
    }

    private void postStatus(String host, String status)
    {
        try
        {
            HostStatus request = HostStatus.hostStatusFrom(host, status);
            Gson gson = new Gson();
            String requestStr = gson.toJson(request);

            String responseStr = Request.Post(LAMBDA_URL)
                .bodyString(requestStr, ContentType.APPLICATION_JSON)
                .execute()
                .returnContent()
                .asString();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    class Host
    {
        String name;
        String aliveCheck;

        Host(String name, String aliveCheck)
        {
            this.name = name;
            this.aliveCheck = aliveCheck;
        }
    }

}
