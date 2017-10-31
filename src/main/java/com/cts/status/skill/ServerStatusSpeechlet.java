/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 * <p>
 * http://aws.amazon.com/apache2.0/
 * <p>
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cts.status.skill;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import com.cts.status.data.HostStatus;
import com.cts.status.data.HostStatusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Simple speechlet to return service status based on status data.
 */
public class ServerStatusSpeechlet implements SpeechletV2
{
    private static final Logger log = LoggerFactory.getLogger(ServerStatusSpeechlet.class);
    private static final String CARD_TITLE = "ServiceStatus";

    private HostStatusClient hostStatusClient = new HostStatusClient().init();

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
    {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(), requestEnvelope.getSession().getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
    {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(), requestEnvelope.getSession().getSessionId());
        return getAskResponse(CARD_TITLE, "Welcome to Canary - a server monitor Alexa Skill. "
            +"You can ask me to report status, simulate a failure, list failures, clear failures, or list servers. "
            +"What would you like me to do? "
        );
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope)
    {
        IntentRequest request = requestEnvelope.getRequest();
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        log.info("onIntent intent={}, requestId={}, sessionId={}", intentName, request.getRequestId(), requestEnvelope.getSession().getSessionId());

        if ("WhatIsServiceStatus".equals(intentName))
        {
            return getServerStatusResponse();
        }
        else if ("SimulateNodeFailure".equals(intentName))
        {
            return getNodeFailureSimulatedResponse(simulateFailure());
        }
        else if ("ClearErrors".equals(intentName))
        {
            clearErrors();
            return getClearErrorsResponse();
        }
        else if ("ListServers".equals(intentName))
        {
            return getListServersResponse();
        }
        else if ("AMAZON.HelpIntent".equals(intentName))
        {
            return getHelpResponse();
        }
        else
        {
            return getAskResponse(CARD_TITLE, "That intent is not supported.  Please try something else.");
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope)
    {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(), requestEnvelope.getSession().getSessionId());
    }

    private SpeechletResponse getClearErrorsResponse()
    {
        clearErrors();

        List<String> INSPIRATION = Arrays.asList(
            "Working together, we can make a better tomorrow.",
            "Alone we can do so little, together we can do so much.",
            "None of us is as smart as all of us.",
            "If everyone is moving forward together, then success takes care of itself.",
            "It takes two flints to make a fire.",
            "Teamwork makes the dream work.",
            "It is literally true that you can succeed best and quickest by helping others to succeed.",
            "To me, teamwork is the beauty of our endeavors, where you have five acting as one. You become selfless."
        );

        String speechText = "All Errors have now been cleared. "+randomItem(INSPIRATION);
        SimpleCard card = getSimpleCard(CARD_TITLE, speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getListServersResponse()
    {
        String hostList = hostStatusClient.getAll().stream()
            .map((host) -> {return speechFormattedName(host.getHostKeyId());})
            .collect(Collectors.joining(" and "));

        String speechText = "<speak>I am currently monitoring the following servers. "+hostList+"</speak>";
        SimpleCard card = getSimpleCard(CARD_TITLE, speechText);

        // Create the plain text output.
        OutputSpeech speech = getSSMLTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getNodeFailureSimulatedResponse(HostStatus failedHost)
    {
        String suggestion = "Uh-oh, something has happened, you should probably ask me to list failures. ";
        String speechText = "<speak>"
            + "A Failure has been simulated for node "
            + speechFormattedName(failedHost.getHostKeyId())
            + " <audio src='https://s3-us-west-2.amazonaws.com/hosted-files-23737372732/Red+Alert.mp3'/>"
            + suggestion
            + "What would you like me to do? "
            + "</speak>";

        SimpleCard card = getSimpleCard(CARD_TITLE, speechText);

        // Create the plain text output.
        OutputSpeech speech = getSSMLTextOutputSpeech(speechText);

//        return SpeechletResponse.newTellResponse(speech, card);
        return SpeechletResponse.newAskResponse(speech, getReprompt(getPlainTextOutputSpeech(suggestion)), card);
    }

    private HostStatus simulateFailure()
    {
        List<String> ERRORS = Arrays.asList(
            "failing alive checks",
            "out of memory",
            "out of disk space",
            "experiencing open circuit breakers",
            "unreachable",
            "performing very poorly",
            "experiencing null pointer errors"
        );

        // TODO: Cause some chaos
        List<HostStatus> hostStatuses = hostStatusClient.getAll();
        if (hostStatuses.isEmpty())
        {
            HostStatus nullHost = new HostStatus();
            return nullHost;
        }

        Collections.shuffle(hostStatuses);

        // Set a node to fail
        HostStatus unluckyHost = hostStatuses.remove(0);
        unluckyHost.setErrorMessage(randomItem(ERRORS));
        hostStatusClient.save(unluckyHost);

        return unluckyHost;
    }

    private void clearErrors()
    {
        hostStatusClient.getAll().stream().forEach((HostStatus hostStatus) -> {
            hostStatus.setErrorMessage("");
            hostStatusClient.save(hostStatus);
        });
    }

    private String randomItem(List<String> items)
    {
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }

    private SpeechletResponse getServerStatusResponse()
    {
        // TODO: Implement dynamic status builder

        List<String> POSITIVE = Arrays.asList(
            "Congratulations",
            "Relax",
            "Excellent",
            "Fantastic",
            "Thank goodness"
        );

        List<String> ALL_NODES_OK = Arrays.asList(
            "All servers are operating normally. Way to go end user services team!",
            "All servers are fully operational.",
            "All servers are currently as right as rain.",
            "Nothing but blue skys for all monitored servers.",
            "All servers are completely functional."
        );

        List<String> NEGATIVE = Arrays.asList(
            "Yo",
            "Uh-oh",
            "Yikes",
            "Darn",
            "Alert",
            "Roll up your sleeves"
        );

        List<String> TROUBLE = Arrays.asList(
            "There is a problem with the production cluster.",
            "The following servers are experiencing anomalies.",
            "We have work to do.",
            "We need to look into the following issues."
        );

        String speechText = "<speak>" + randomItem(POSITIVE)+"! "+randomItem(ALL_NODES_OK) + "</speak>";
        List<HostStatus> badHosts = hostStatusClient.getAll().stream()
            .filter(hostStatus -> hostStatus.getIsDown())
            .collect(Collectors.toList());

        if (!badHosts.isEmpty())
        {
            StringBuilder nodeErrors = new StringBuilder();
            badHosts.forEach((host) -> {
                nodeErrors.append(" Node "+ speechFormattedName(host.getHostKeyId())+ " is "+host.getErrorMessage()+". ");
            });

            String numberHavingIssues = "One node is having trouble.";
            if (badHosts.size()>1)
            {
                numberHavingIssues = badHosts.size()+ " nodes are having trouble.";
            }

            speechText = "<speak>"
                +randomItem(NEGATIVE)+"! "
                +randomItem(TROUBLE)+" "
                +numberHavingIssues+" "
                +nodeErrors.toString()
                + "</speak>";
        }

        // Create the Simple card content.
        SimpleCard card = getSimpleCard(CARD_TITLE, speechText);

        // Create the plain text output.
        OutputSpeech speech = getSSMLTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private String speechFormattedName(String hostName)
    {
        // sea1tpentds09
        if (hostName == null)
        {
            return "NULL";
        }
        if (hostName.length()<11)
        {
            return "<say-as interpret-as='spell-out'>"+hostName+"</say-as> ";
        }

        return hostName.substring(0,4) +", "
            +"<say-as interpret-as='spell-out'>"+hostName.substring(4, 11) +"</say-as> "
            +", "
            + hostName.substring(11)+", ";
    }

    private SpeechletResponse getHelpResponse()
    {
        String speechText = "You can ask me the server status.";
        return getAskResponse(CARD_TITLE, speechText);
    }

    // TODO: Replace all this stuff with a SpeechletResponseBuilder

    private SimpleCard getSimpleCard(String title, String content)
    {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText)
    {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    private OutputSpeech getSSMLTextOutputSpeech(String speechText)
    {
        SsmlOutputSpeech speech =  new SsmlOutputSpeech();
        speech.setSsml(speechText);

        return speech;
    }

    private Reprompt getReprompt(OutputSpeech outputSpeech)
    {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    private SpeechletResponse getAskResponse(String cardTitle, String speechText)
    {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}
