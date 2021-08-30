package com.oracle.oci.api.cloudguard.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.oracle.bmc.cloudguard.CloudGuardClient;
import com.oracle.bmc.cloudguard.model.ProblemAggregation;
import com.oracle.bmc.cloudguard.model.ProblemDimension;
import com.oracle.bmc.cloudguard.model.RiskScoreAggregation;
import com.oracle.bmc.cloudguard.model.SecurityScoreAggregation;
import com.oracle.bmc.cloudguard.requests.RequestRiskScoresRequest;
import com.oracle.bmc.cloudguard.requests.RequestSecurityScoresRequest;
import com.oracle.bmc.cloudguard.requests.RequestSummarizedProblemsRequest;
import com.oracle.bmc.cloudguard.responses.RequestRiskScoresResponse;
import com.oracle.bmc.cloudguard.responses.RequestSecurityScoresResponse;
import com.oracle.bmc.cloudguard.responses.RequestSummarizedProblemsResponse;
import com.oracle.oci.api.cloudguard.util.AuthentificationProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class AggregationController {
    @Autowired AuthentificationProvider authentificationProvider;
    
    Logger logger = LoggerFactory.getLogger(CloudGuardProblemController.class);

    @RequestMapping(value = "/aggregation/securityScores", method = RequestMethod.GET)
    public Map<String, String> securityScores(@RequestParam("tenancy_id") String tenancy_id) throws Exception {
        CloudGuardClient client = new CloudGuardClient(authentificationProvider.getAuthenticationDetailsProvider());

        RequestSecurityScoresRequest requestSecurityScoresRequest = RequestSecurityScoresRequest.builder()
		.compartmentId(tenancy_id).build();

        /* Send request to the Client */
        RequestSecurityScoresResponse requestSecurityScoresResponse = client.requestSecurityScores(requestSecurityScoresRequest);
        Map<String, String> resultMap = new HashMap<String, String>();

        for(SecurityScoreAggregation securityScoreAggregation : requestSecurityScoresResponse.getSecurityScoreAggregationCollection().getItems()) {
            resultMap.put("securityRating", securityScoreAggregation.getSecurityRating().getValue());
            resultMap.put("securityScore", Integer.toString(securityScoreAggregation.getSecurityScore()));
        }

        RequestRiskScoresRequest requestRiskScoresRequest = RequestRiskScoresRequest.builder()
		.compartmentId(tenancy_id).build();

        /* Send request to the Client */
        RequestRiskScoresResponse requestRiskScoresResponse = client.requestRiskScores(requestRiskScoresRequest);

        for(RiskScoreAggregation riskScoreAggregation : requestRiskScoresResponse.getRiskScoreAggregationCollection().getItems()) {
            resultMap.put("riskScore", Integer.toString(riskScoreAggregation.getRiskScore()));
        }

        return resultMap;
    }

    @RequestMapping(value = "/aggregation/problems", method = RequestMethod.GET)
    public Map<String, String> aggregatedProblems(@RequestParam("compartment_id") String compartment_id) throws Exception {
        CloudGuardClient client = new CloudGuardClient(authentificationProvider.getAuthenticationDetailsProvider());

        RequestSummarizedProblemsRequest requestSummarizedProblemsRequest = RequestSummarizedProblemsRequest.builder()
		.listDimensions(new ArrayList<>(Arrays.asList(ProblemDimension.RiskLevel)))
		.compartmentId(compartment_id)
		.compartmentIdInSubtree(true)
		.accessLevel(RequestSummarizedProblemsRequest.AccessLevel.Restricted)
		.limit(3).build();

        /* Send request to the Client */
        RequestSummarizedProblemsResponse requestSummarizedProblemsResponse = client.requestSummarizedProblems(requestSummarizedProblemsRequest);
        Map<String, String> resultMap = new HashMap<String, String>();

        for(ProblemAggregation problemAggregation : requestSummarizedProblemsResponse.getProblemAggregationCollection().getItems()) {
            resultMap.put(problemAggregation.getDimensionsMap().get("RISK_LEVEL"), Integer.toString(problemAggregation.getCount()));

        }

        return resultMap;
    }
}
