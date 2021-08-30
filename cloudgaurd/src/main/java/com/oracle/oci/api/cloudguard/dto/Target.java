package com.oracle.oci.api.cloudguard.dto;

public class Target {
    private String id;
    private String displayName;
    private String description;
    private String compartmentId;
    private String lifecycleState;
    private String targetResourceType;
    private String timeCreated;
    private String timeUpdated;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getCompartmentId() {
        return compartmentId;
    }
    public void setCompartmentId(String compartmentId) {
        this.compartmentId = compartmentId;
    }
    public String getLifecycleState() {
        return lifecycleState;
    }
    public void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }
    public String getTargetResourceType() {
        return targetResourceType;
    }
    public void setTargetResourceType(String targetResourceType) {
        this.targetResourceType = targetResourceType;
    }
    public String getTimeCreated() {
        return timeCreated;
    }
    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }
    public String getTimeUpdated() {
        return timeUpdated;
    }
    public void setTimeUpdated(String timeUpdated) {
        this.timeUpdated = timeUpdated;
    }
}
