package com.example.datagath.controller;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.datagath.dto.SchEvCreationForm;
import com.example.datagath.dto.SchEvCreationResponse;
import com.example.datagath.model.ScheduledEvent;
import com.example.datagath.model.User;
import com.example.datagath.repository.ScheduledEventsRepository;
import com.example.datagath.service.EmailServiceImpl;
import com.example.datagath.service.FileGenerationService;
import com.example.datagath.service.NetworkActionsService;
import com.example.datagath.service.ScheduledEventsService;
import com.example.datagath.service.UserService;
import com.itextpdf.text.DocumentException;

import ch.qos.logback.core.model.Model;
import jakarta.mail.MessagingException;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/schedules")
public class ScheduledEventController {

    private final ScheduledEventsRepository scheduledEventsRepository;
    public final UserService userService;
    public final ScheduledEventsService scheduledEventsService;
    public final EmailServiceImpl emailService;

    public ScheduledEventController(UserService userService, ScheduledEventsService scheduledEventsService,
            EmailServiceImpl emailService, ScheduledEventsRepository scheduledEventsRepository) {
        this.userService = userService;
        this.scheduledEventsService = scheduledEventsService;
        this.emailService = emailService;
        this.scheduledEventsRepository = scheduledEventsRepository;
    }

    // TESTING NEW EMAIL CREATION SYSTEM
    // NetworkActionsService networkActionsService = new
    // NetworkActionsService(emailService);
    // networkActionsService.sendResponse(FileGenerationService.generatePDFReportOnCollectionTable(null),
    // sessionToken);
    @PostMapping("/create")
    public ResponseEntity<?> createScheduledEvent(
            @CookieValue(value = "sessionToken", required = false) String sessionToken,
            @RequestBody SchEvCreationForm eventCreationForm)
            throws FileNotFoundException, DocumentException, MessagingException {

        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;
        if (user != null) {
            eventCreationForm.setUserId(user.getId());
            Map<String, String> eventActionBody = eventCreationForm.setupActionBody();
            if (eventActionBody == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some or all paramaters missing");
            }

            SchEvCreationResponse creationResponse = scheduledEventsService.createNewScheduledEvent(eventCreationForm,
                    eventActionBody);
            if (creationResponse.getSuccess()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .header(HttpHeaders.LOCATION, "/schedules/" + creationResponse.getEventId())
                        .body("success");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Server error crating event. Try agai later or report the issue.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in to create the event");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateScheduledEvent(
            @CookieValue(value = "sessionToken", required = false) String sessionToken,
            @RequestBody SchEvCreationForm eventCreationForm)
            throws FileNotFoundException, DocumentException, MessagingException {

        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;
        if (user != null) {
            Map<String, String> eventActionBody = eventCreationForm.setupActionBody();
            if (eventActionBody == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Some or all parameters are missing. Please fill out all the fields.");
            }

            // update event in jpa
            if (eventCreationForm != null && eventCreationForm.getAction() != null) {
                ScheduledEvent existingEvent = scheduledEventsService.findEvent(user, eventCreationForm.getEventName());

                if (existingEvent == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Non Existent Event");
                } else {
                    existingEvent.setAction(eventCreationForm.getAction());
                    existingEvent = scheduledEventsRepository.save(existingEvent);
                    SchEvCreationResponse response = scheduledEventsService.updateScheduledEvent(existingEvent,
                            eventActionBody);
                    if (response.getSuccess()) {
                        return ResponseEntity.ok().header(HttpHeaders.LOCATION, "./manage/dashboard")
                                .body("Updated Event Successfully");
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Internal error updating event. Try again later.");
                    }
                }

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No new action detected!");

            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Login detected");
    }

    @GetMapping("/create")
    public String getMethodName(@CookieValue(required = false) String sessionToken) {
        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;
        if (user != null) {
            return "eventCreator";
        } else {
            return "redirect://login";
        }
    }

    @GetMapping("/userschedules")
    public ResponseEntity<?> getUserTables(@CookieValue(value = "sessionToken", required = false) String token) {
        User user = token != null ? userService.validateSessionToken(token) : null;
        String results = scheduledEventsService.getAllTableNamesForUser(user);
        System.out.println(results);
        ResponseEntity<?> response = results != null ? ResponseEntity.status(HttpStatus.FOUND).body(results)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No User Specified");
        return response;
    }

    @GetMapping("/{id}")
    public String getMethodName(@CookieValue(value = "sessionToken", required = false) String sessionToken,
            @PathVariable("id") Long eventId, Model model) {

        return "eventCreator";
    }

}
