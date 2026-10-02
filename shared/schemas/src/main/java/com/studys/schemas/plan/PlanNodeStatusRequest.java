package com.studys.schemas.plan;

public class PlanNodeStatusRequest {

    private String status;

    protected PlanNodeStatusRequest() {
    }

    public PlanNodeStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
