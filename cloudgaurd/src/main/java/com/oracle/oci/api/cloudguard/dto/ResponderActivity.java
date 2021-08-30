package com.oracle.oci.api.cloudguard.dto;

public class ResponderActivity {
    private String id;
    private String responderRuleId;
    private String responderRuleName;
    private String message;
    private String responderActivityType;
    private String responderExecutionStatus;
    private String responderType;
    private String timeCreated;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getResponderRuleName() {
        return responderRuleName;
    }
    public String getResponderRuleId() {
        return responderRuleId;
    }
    public void setResponderRuleId(String responderRuleId) {
        this.responderRuleId = responderRuleId;
    }
    public void setResponderRuleName(String responderRuleName) {
        this.responderRuleName = responderRuleName;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getResponderActivityType() {
        return responderActivityType;
    }
    public void setResponderActivityType(String responderActivityType) {
        this.responderActivityType = responderActivityType;
    }
    public String getResponderExecutionStatus() {
        return responderExecutionStatus;
    }
    public void setResponderExecutionStatus(String responderExecutionStatus) {
        this.responderExecutionStatus = responderExecutionStatus;
    }
    public String getResponderType() {
        return responderType;
    }
    public void setResponderType(String responderType) {
        this.responderType = responderType;
    }
    public String getTimeCreated() {
        return timeCreated;
    }
    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }
}