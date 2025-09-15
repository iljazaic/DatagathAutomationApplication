package com.example.datagath.controller;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.datagath.dto.SchEvCreationForm;
import com.example.datagath.dto.SchEvCreationResponse;
import com.example.datagath.model.User;
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

    public final UserService userService;
    public final ScheduledEventsService scheduledEventsService;
    public final EmailServiceImpl emailService;

    public ScheduledEventController(UserService userService, ScheduledEventsService scheduledEventsService,
            EmailServiceImpl emailService) {
        this.userService = userService;
        this.scheduledEventsService = scheduledEventsService;
        this.emailService = emailService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createScheduledEvent(
            @CookieValue(value = "sessionToken", required = false) String sessionToken,
            @RequestBody SchEvCreationForm eventCreationForm)
            throws FileNotFoundException, DocumentException, MessagingException {

        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;
        if (user != null) {
            eventCreationForm.setUserId(user.getId());
            Map<String, String> eventActionBody = new HashMap<>();
            switch (eventCreationForm.getAction()) {
                case "PING":
                    eventActionBody.put("pingAddress", eventCreationForm.getPingAddress());
                    eventActionBody.put("sendAddress", eventCreationForm.getSendAddress());
                    break;
                case "REPORT":
                    eventActionBody.put("sendAddress", eventCreationForm.getSendAddress());
                    eventActionBody.put("dataset", eventCreationForm.getDataset());
                    break;
                case "AI/LLM":
                    eventActionBody.put("sendAddress", eventCreationForm.getSendAddress());
                    eventActionBody.put("model", eventCreationForm.getModel());
                    eventActionBody.put("apikey", eventCreationForm.getApikey());
                    eventActionBody.put("prompt", eventCreationForm.getPrompt());
                    break;
                case "VISUALISTION":
                    eventActionBody.put("dataset", eventCreationForm.getDataset());
                    eventActionBody.put("sendAddress", eventCreationForm.getSendAddress());
                    eventActionBody.put("visualisationType", eventCreationForm.getVisualisationType());
                    break;
                default:
                    break;

            }

            // TESTING NEW EMAIL CREATION SYSTEM
            NetworkActionsService networkActionsService = new NetworkActionsService(emailService);
            networkActionsService.sendResponse(FileGenerationService.generatePDFReportOnCollectionTable(null),
                    sessionToken);

            SchEvCreationResponse creationResponse = scheduledEventsService.createNewScheduledEvent(eventCreationForm,
                    eventActionBody);
            if (creationResponse.getSuccess()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .header(HttpHeaders.LOCATION, "/schedules/" + creationResponse.getEventId())
                        .body("success");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("there was an error");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in to create the event");
        }
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
