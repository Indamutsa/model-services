package com.arsene.modelservices;

import com.arsene.modelservices.services.EpsilonService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.OutputStream;

@SpringBootApplication
public class ModelServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelServicesApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startupTest() {
        EpsilonService epsilonService = new EpsilonService();
        System.out.println("\n\n*******\nPrinting inside the main program: \n******\n");

        try {
            epsilonService.eclEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
