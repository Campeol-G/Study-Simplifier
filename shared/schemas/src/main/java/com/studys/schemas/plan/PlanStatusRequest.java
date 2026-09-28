package com.studys.schemas.plan;

public class PlanStatusRequest {

    private String status;

    protected PlanStatusRequest() {
    }

    public PlanStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
