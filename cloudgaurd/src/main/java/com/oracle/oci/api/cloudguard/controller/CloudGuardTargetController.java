package com.oracle.oci.api.cloudguard.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.cloudguard.CloudGuardClient;
import com.oracle.bmc.cloudguard.model.SortOrders;
import com.oracle.bmc.cloudguard.model.TargetSummary;
import com.oracle.bmc.cloudguard.requests.GetTargetRequest;
import com.oracle.bmc.cloudguard.requests.ListTargetsRequest;
import com.oracle.bmc.cloudguard.responses.ListTargetsResponse;
import com.oracle.oci.api.cloudguard.dto.Target;
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
public class CloudGuardTargetController {

    @Autowired AuthentificationProvider authentificationProvider;
    
    Logger logger = LoggerFactory.getLogger(CloudGuardTargetController.class);

    @RequestMapping(value = "/target/get/{id}", method = RequestMethod.GET)
    public Target getTarget(@PathVariable("id") String id) throws Exception {

        // Sample Target ID: ocid1.cloudguardtarget.oc1.iad.amaaaaaavsea7yiaurv7qrkgknntbsa6ecf54em4n3kgj2oyyq2rnyrulijq
        CloudGuardClient client = new CloudGuardClient(authentificationProvider.getAuthenticationDetailsProvider());
        
        GetTargetRequest getTargetRequest = GetTargetRequest.builder().targetId(id).build();

        /* Send request to the Client */
        //GetTargetResponse target = client.getTarget(getTargetRequest);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Target target = new Target();
        target.setId(client.getTarget(getTargetRequest).getTarget().getId());
        target.setDisplayName(client.getTarget(getTargetRequest).getTarget().getDisplayName());
        target.setCompartmentId(client.getTarget(getTargetRequest).getTarget().getCompartmentId());
        target.setLifecycleState(client.getTarget(getTargetRequest).getTarget().getLifecycleState().getValue());
        target.setTargetResourceType(client.getTarget(getTargetRequest).getTarget().getTargetResourceType().getValue());
        target.setTimeCreated(df.format(client.getTarget(getTargetRequest).getTarget().getTimeCreated()));
        target.setTimeUpdated(df.format(client.getTarget(getTargetRequest).getTarget().getTimeUpdated()));
        
        return target;
    }

    @RequestMapping(value = "/target/list", method = RequestMethod.GET)
    public List<Target> listTargets(@RequestParam("compartment_id") String compartment_id) throws Exception {

        // sample compartment id: ocid1.compartment.oc1..aaaaaaaa2ps2cj6joosvscmkvmwtfabassubsiku6qf3yoy3hkchofmzdytq (gambas)
        // ocid1.compartment.oc1..aaaaaaaalxrd2j5jpkf7axvksisyjhvsl2wy3nifdnqitjssgz64z22jx3oa (PROJECT)
        List<Target> targetList = new ArrayList<Target>();

        CloudGuardClient client = new CloudGuardClient(authentificationProvider.getAuthenticationDetailsProvider());
        
        ListTargetsRequest listTargetsRequest = ListTargetsRequest.builder().compartmentId(compartment_id).compartmentIdInSubtree(true).accessLevel(ListTargetsRequest.AccessLevel.Restricted).sortOrder(SortOrders.Asc).sortBy(ListTargetsRequest.SortBy.DisplayName).build();

        //ListTargetsRequest listTargetsRequest = ListTargetsRequest.builder().compartmentId(compartment_ocid).build();

        // /* Send request to the Client */
        ListTargetsResponse listTargets = client.listTargets(listTargetsRequest);
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (TargetSummary targetSummary : listTargets.getTargetCollection().getItems()) {
            Target target = new Target();

            target.setDisplayName(targetSummary.getDisplayName());

            target.setId(targetSummary.getId());
            target.setDisplayName(targetSummary.getDisplayName());
            target.setCompartmentId(targetSummary.getCompartmentId());
            target.setLifecycleState(targetSummary.getLifecycleState().getValue());
            target.setTargetResourceType(targetSummary.getTargetResourceType().getValue());
            target.setTimeCreated(df.format(targetSummary.getTimeCreated()));
            target.setTimeUpdated(df.format(targetSummary.getTimeUpdated()));

            targetList.add(target);
        }

        return targetList;
    }
}