package com.universe.models;

import java.time.LocalDate;

//sponsorship deal
public class SponsorshipDeal {
    private String dealID;
    private String sponsorID;
    private String eventID;
    private String proposalMessage;
    private LocalDate dateSubmitted;
    private String status; //pending,accepted,rejected
    private String responseMessage;

    //constructor
    public SponsorshipDeal(String dealID, String sponsorID, String eventID,
                           String proposalMessage, LocalDate dateSubmitted,
                           String status, String responseMessage) {
        this.dealID = dealID;
        this.sponsorID = sponsorID;
        this.eventID = eventID;
        this.proposalMessage = proposalMessage;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
        this.responseMessage = responseMessage;
    }

    //status checks
    public boolean isPending()  { return "Pending".equals(status); }
    public boolean isAccepted() { return "Accepted".equals(status); }
    public boolean isRejected() { return "Rejected".equals(status); }

    //accept deal
    public void accept(String responseMessage) {
        if (!isPending())
            throw new IllegalStateException("Only Pending deals can be accepted.");
        this.status = "Accepted";
        this.responseMessage = responseMessage;
    }

    //reject deal
    public void reject(String responseMessage) {
        if (!isPending())
            throw new IllegalStateException("Only Pending deals can be rejected.");
        this.status = "Rejected";
        this.responseMessage = responseMessage;
    }

    //status badge
    public String getStatusBadgeStyle() {
        return switch (status) {
            case "Accepted" -> "status-badge-green";
            case "Rejected" -> "status-badge-red";
            default         -> "status-badge-yellow";
        };
    }

    //getters setters
    public String getDealID()           { return dealID; }
    public void setDealID(String dealID) { this.dealID = dealID; }

    public String getSponsorID()        { return sponsorID; }
    public void setSponsorID(String sponsorID) { this.sponsorID = sponsorID; }

    public String getEventID()          { return eventID; }
    public void setEventID(String eventID) { this.eventID = eventID; }

    public String getProposalMessage()  { return proposalMessage; }
    public void setProposalMessage(String proposalMessage) { this.proposalMessage = proposalMessage; }

    public LocalDate getDateSubmitted() { return dateSubmitted; }
    public void setDateSubmitted(LocalDate dateSubmitted) { this.dateSubmitted = dateSubmitted; }

    public String getStatus()           { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponseMessage()  { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }

    //to string
    @Override
    public String toString() {
        return "SponsorshipDeal{dealID='" + dealID + "', status='" + status + "'}";
    }
}