package com.universe;

import com.universe.models.Society;
import com.universe.services.SocietyService;

import java.util.List;

public class TestSociety {
    public static void main(String[] args) {
        SocietyService service = new SocietyService();
        List<Society> societies = service.getActiveSocieties();
        System.out.println("Found " + societies.size() + " active societies:");
        for (Society s : societies) {
            System.out.println(" - " + s.getName() + " (" + s.getStatus() + ")");
        }
    }
}
