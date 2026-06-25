package com.universe.services;

import com.universe.db.DBConnection;
import com.universe.models.SponsorshipDeal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//sponsorship service
public class SponsorshipService {

    private Connection conn;

    //constructor
    public SponsorshipService() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    //submit proposal
    public void submitProposal(SponsorshipDeal deal) {
        String sql = "INSERT INTO SponsorshipDeal VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, deal.getDealID());
            ps.setString(2, deal.getSponsorID());
            ps.setString(3, deal.getEventID());
            ps.setString(4, deal.getProposalMessage());
            ps.setDate(5, Date.valueOf(deal.getDateSubmitted()));
            ps.setString(6, "Pending");
            ps.setString(7, "");
            ps.executeUpdate();
            System.out.println("Proposal submitted successfully!");

            //fetch names
            String sName = deal.getSponsorID();
            String eTitle = deal.getEventID();
            try (Statement st = conn.createStatement()) {
                ResultSet rs1 = st.executeQuery("SELECT name FROM Sponsor WHERE sponsorID='" + sName + "'");
                if (rs1.next()) sName = rs1.getString("name");
                ResultSet rs2 = st.executeQuery("SELECT title FROM Event WHERE eventID='" + eTitle + "'");
                if (rs2.next()) eTitle = rs2.getString("title");
            } catch (SQLException ignored) {}
            new NotificationService().sendNotification("ALL", "ADMIN", "Sponsor " + sName + " applied to sponsor event '" + eTitle + "'", "System");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //sponsor deals
    public List<SponsorshipDeal> getDealsBySponsor(String sponsorID) {
        List<SponsorshipDeal> list = new ArrayList<>();
        String sql = "SELECT * FROM SponsorshipDeal WHERE sponsorID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, sponsorID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new SponsorshipDeal(
                        rs.getString("dealID"),
                        rs.getString("sponsorID"),
                        rs.getString("eventID"),
                        rs.getString("proposalMessage"),
                        rs.getDate("dateSubmitted").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("responseMessage")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //society deals
    public List<SponsorshipDeal> getDealsBySociety(String societyID) {
        List<SponsorshipDeal> list = new ArrayList<>();
        String sql = "SELECT d.* FROM SponsorshipDeal d JOIN Event e ON d.eventID = e.eventID WHERE e.societyID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, societyID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new SponsorshipDeal(
                        rs.getString("dealID"),
                        rs.getString("sponsorID"),
                        rs.getString("eventID"),
                        rs.getString("proposalMessage"),
                        rs.getDate("dateSubmitted").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("responseMessage")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //update status
    public void updateDealStatus(String dealID, String status, String response) {
        String sql = "UPDATE SponsorshipDeal SET status = ?, responseMessage = ? WHERE dealID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, response);
            ps.setString(3, dealID);
            ps.executeUpdate();
            System.out.println("Deal " + dealID + " updated to: " + status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}