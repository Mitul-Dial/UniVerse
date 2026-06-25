package com.universe.models;

import java.time.LocalDate;

//sponsor model
public class Sponsor extends User {
    private String organization;
    private String contactInfo;

    //constructor
    public Sponsor(String sponsorID, String name, String organization,
                   String email, String contactInfo, String password) {
        super(sponsorID, name, email, password);
        this.organization = organization;
        this.contactInfo = contactInfo;
    }

    //login logic
    @Override
    public boolean login() {
        System.out.println("Sponsor logged in: " + email);
        return true;
    }

    //logout logic
    @Override
    public void logout() {
        System.out.println("Sponsor logged out: " + email);
    }

    //submit proposal
    public SponsorshipDeal submitProposal(String dealID, String eventID,
                                          String proposalMessage) {
        if (eventID == null || eventID.isBlank())
            throw new IllegalArgumentException("Event ID is required.");

        return new SponsorshipDeal(
                dealID,
                this.userID,
                eventID,
                proposalMessage,
                LocalDate.now(),
                "Pending",
                null
        );
    }

    //active proposal check
    public boolean hasActiveProposalFor(String eventID,
                                        java.util.List<SponsorshipDeal> existingDeals) {
        for (SponsorshipDeal deal : existingDeals) {
            if (deal.getEventID().equals(eventID) &&
                    !"Rejected".equals(deal.getStatus())) {
                return true;
            }
        }
        return false;
    }

    //sponsor id
    public String getSponsorID() { return userID; }
    public void setSponsorID(String id) { this.userID = id; }

    //getters setters
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    //to string
    @Override
    public String toString() {
        return "Sponsor{org='" + organization + "', " + super.toString() + "}";
    }
}