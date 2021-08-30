package com.oracle.oci.api.cloudguard.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.cloudguard.CloudGuardClient;
import com.oracle.bmc.cloudguard.model.ProblemLifecycleDetail;
import com.oracle.bmc.cloudguard.model.ProblemSummary;
import com.oracle.bmc.cloudguard.model.ResponderActivitySummary;
import com.oracle.bmc.cloudguard.model.ResponderExecutionStates;
import com.oracle.bmc.cloudguard.model.SortOrders;
import com.oracle.bmc.cloudguard.model.TriggerResponderDetails;
import com.oracle.bmc.cloudguard.model.UpdateProblemStatusDetails;
import com.oracle.bmc.cloudguard.requests.GetProblemRequest;
import com.oracle.bmc.cloudguard.requests.ListProblemsRequest;
import com.oracle.bmc.cloudguard.requests.ListProblemsRequest.AccessLevel;
import com.oracle.bmc.cloudguard.requests.ListResponderActivitiesRequest;
import com.oracle.bmc.cloudguard.requests.ListResponderExecutionsRequest;
import com.oracle.bmc.cloudguard.requests.TriggerResponderRequest;
import com.oracle.bmc.cloudguard.requests.UpdateProblemStatusRequest;
import com.oracle.bmc.cloudguard.responses.GetProblemResponse;
import com.oracle.bmc.cloudguard.responses.ListProblemsResponse;
import com.oracle.bmc.cloudguard.responses.ListResponderActivitiesResponse;
import com.oracle.bmc.resourcesearch.ResourceSearchClient;
import com.oracle.bmc.resourcesearch.model.ResourceSummary;
import com.oracle.bmc.resourcesearch.model.SearchDetails;
import com.oracle.bmc.resourcesearch.model.StructuredSearchDetails;
import com.oracle.bmc.resourcesearch.requests.SearchResourcesRequest;
import com.oracle.bmc.resourcesearch.responses.SearchResourcesResponse;
import com.oracle.oci.api.cloudguard.dto.Problem;
import com.oracle.oci.api.cloudguard.util.AuthentificationProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class CloudGuardProblemController {

    @Autowired AuthentificationProvider authentificationProvider;
    
    Logger logger = LoggerFactory.getLogger(CloudGuardProblemController.class);

    @RequestMapping(value = "/problem/get/{id}", method = RequestMethod.GET)
    public Problem getProblem(@PathVariable("id") String id) throws Exception {
        CloudGuardClient client = new CloudGuardClient(authentificationProvider.getAuthenticationDetailsProvider());

        GetProblemRequest getProblemRequest = GetProblemRequest.builder().problemId(id).build();

        /* Send request to the Client */
        GetProblemResponse problemResponse = client.getProblem(getProblemRequest);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Problem problem = new Problem();
  
        problem.setId(problemResponse.getProblem().getId());
        problem.setDescription(problemResponse.getProblem().getDescription());
        problem.setRecommendation(problemResponse.getProblem().getRecommendation());
        problem.setLifecycleDetail(problemResponse.getProblem().getLifecycleDetail().getValue());    // OPEN, RESOLVED, DISMISSED
        problem.setLifecycleState(problemResponse.getProblem().getLifecycleState().getValue());    // ACTIVE, INACTIVE
        problem.setResourceName(problemResponse.getProblem().getResourceName());
        problem.setResourceType(problemResponse.getProblem().getResourceType());
        problem.setRiskLevel(problemResponse.getProblem().getRiskLevel().getValue());
        problem.setTimeFirstDetected(df.format(problemResponse.getProblem().getTimeFirstDetected()));
        problem.setTimeLastDetected(df.format(problemResponse.getProblem().getTimeLastDetected()));

        if(problemResponse.getProblem().getResourceType().equals("User")) {
            problem.setResourceCreator(problemResponse.getProblem().getResourceName());
        }

        // For resource search
        ResourceSearchClient resourceSearchClient = new ResourceSearchClient(authentificationProvider.getAuthenticationDetailsProvider());

        // SearchDetails searchDetails = StructuredSearchDetails.builder()
		// .query("query " + problemResponse.getProblem().getResourceType().replace(" ", "").toLowerCase() + " resources where displayName = '"+problemResponse.getProblem().getResourceName()+"'")
		// .matchingContextType(SearchDetails.MatchingContextType.Highlights).build();

        SearchDetails searchDetails = StructuredSearchDetails.builder()
		.query("query all resources where displayName = '"+problemResponse.getProblem().getResourceName()+"'")
		.matchingContextType(SearchDetails.MatchingContextType.Highlights).build();

	    SearchResourcesRequest searchResourcesRequest = SearchResourcesRequest.builder()
		.searchDetails(searchDetails).build();

        /* Send request to the Client */
        SearchResourcesResponse searchResourcesResponse = resourceSearchClient.searchResources(searchResourcesRequest);
        logger.info(Integer.toString(searchResourcesResponse.getResourceSummaryCollection().getItems().size()));
        
        if(searchResourcesResponse.getResourceSummaryCollection().getItems().size() == 0) {
            problem.setResourceCreator("");
        } else {
            logger.info("2222");
            for(ResourceSummary resourceSummary: searchResourcesResponse.getResourceSummaryCollection().getItems()) {
                problem.setResourceCreator(resourceSummary.getDefinedTags().get("Oracle-Tags").get("CreatedBy").toString().replace("oracleidentitycloudservice/", ""));
            }
        }

        ListResponderActivitiesRequest listResponderActivitiesRequest = ListResponderActivitiesRequest.builder()
		.problemId(problem.getId())
		.limit(10)
		.sortOrder(SortOrders.Desc)
		.sortBy(ListResponderActivitiesRequest.SortBy.TimeCreated).build();

        /* Send request to the Client */
        ListResponderActivitiesResponse listResponderActivitiesResponse = client.listResponderActivities(listResponderActivitiesRequest);

        for (ResponderActivitySummary responderActivitySummary : listResponderActivitiesResponse.getResponderActivityCollection().getItems()) {
            if(responderActivitySummary.getResponderExecutionStatus().getValue().equals(ResponderExecutionStates.AwaitingConfirmation.getValue()) && responderActivitySummary.getResponderType().getValue().equals(ListResponderExecutionsRequest.ResponderType.Remediation.getValue())) {
                problem.setResponderRuleId(responderActivitySummary.getResponderRuleId());
                problem.setResponderRuleName(responderActivitySummary.getResponderRuleName());
                problem.setCanRemediation(true);

                break;
            } else {
                problem.setCanRemediation(false);
            }
        }
        
        return problem;
    }

    @RequestMapping(value = "/problem/list", method = RequestMethod.GET)
    public List<Problem> listProblems(@RequestParam("compartment_id") String compartment_id, @RequestParam("target_id") String target_id, @RequestParam("lifecycleDetail") String lifecycleDetail, @RequestParam("riskLevel") String riskLevel) throws Exception {

        // lifecycleDetail: OPEN, RESOLVED, DISMISSED
        // riskLevel: MINOR, CRITICAL...
        // sample compartment id: ocid1.compartment.oc1..aaaaaaaa2ps2cj6joosvscmkvmwtfabassubsiku6qf3yoy3hkchofmzdytq (gambas)
        // ocid1.compartment.oc1..aaaaaaaalxrd2j5jpkf7axvksisyjhvsl2wy3nifdnqitjssgz64z22jx3oa (PROJECT)
        List<Problem> problemList = new ArrayList<Problem>();

        CloudGuardClient client = new CloudGuardClient(authentificationProvider.getAuthenticationDetailsProvider());
        
        // ProblemLifecycleDetail.Dismissed
        // ProblemLifecycleDetail.Open
        // ProblemLifecycleDetail.Resolved

        ListProblemsRequest listProblemsRequest = ListProblemsRequest.builder()
		.compartmentId(compartment_id)
		.lifecycleDetail((lifecycleDetail.equals("DISMISSED") ? ProblemLifecycleDetail.Dismissed : (lifecycleDetail.equals("OPEN") ? ProblemLifecycleDetail.Open : ProblemLifecycleDetail.Resolved)))
		.riskLevel(riskLevel)
		.targetId(target_id)
        .accessLevel(AccessLevel.Restricted)
        .compartmentIdInSubtree(true)
		.sortOrder(SortOrders.Desc)
		.sortBy(ListProblemsRequest.SortBy.ResourceName).build();

        /* Send request to the Client */
        ListProblemsResponse problemsResponse = client.listProblems(listProblemsRequest);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (ProblemSummary problemSummary : problemsResponse.getProblemCollection().getItems()) {
            Problem problem = new Problem();

            problem.setId(problemSummary.getId());
            problem.setLifecycleDetail(problemSummary.getLifecycleDetail().getValue());    // OPEN, RESOLVED, DISMISSED
            problem.setLifecycleState(problemSummary.getLifecycleState().getValue());    // ACTIVE, INACTIVE
            problem.setResourceName(problemSummary.getResourceName());
            problem.setResourceType(problemSummary.getResourceType());
            problem.setRiskLevel(problemSummary.getRiskLevel().getValue());
            problem.setTimeFirstDetected(df.format(problemSummary.getTimeFirstDetected()));
            problem.setTimeLastDetected(df.format(problemSummary.getTimeLastDetected()));

            problemList.add(problem);
        }
        
        return problemList;
    }

    @RequestMapping(value = "/problem/actions/remediate/{id}", method = RequestMethod.POST)
    public void remediate(@PathVariable("id") String id, @RequestParam("responder_rule_id") String responder_rule_id) throws Exception {
        CloudGuardClient client = new CloudGuardClient(authentificationProvider.getAuthenticationDetailsProvider());

        TriggerResponderDetails triggerResponderDetails = TriggerResponderDetails.builder()
		.responderRuleId(responder_rule_id).build();

        TriggerResponderRequest triggerResponderRequest = TriggerResponderRequest.builder()
		.problemId(id)
		.triggerResponderDetails(triggerResponderDetails).build();

        /* Send request to the Client */
        client.triggerResponder(triggerResponderRequest);
    }

    @RequestMapping(value = "/problem/actions/update/{id}", method = RequestMethod.POST)
    public void update(@PathVariable("id") String id, @RequestParam("status") String status) throws Exception {
        CloudGuardClient client = new CloudGuardClient(authentificationProvider.getAuthenticationDetailsProvider());

        /* Create a request and dependent object(s). */
	    UpdateProblemStatusDetails updateProblemStatusDetails = UpdateProblemStatusDetails.builder()
            .status(status.equals("dismissed") ? ProblemLifecycleDetail.Dismissed : ProblemLifecycleDetail.Resolved).build();

        UpdateProblemStatusRequest updateProblemStatusRequest = UpdateProblemStatusRequest.builder()
            .problemId(id)
            .updateProblemStatusDetails(updateProblemStatusDetails).build();

        client.updateProblemStatus(updateProblemStatusRequest);
    }
}