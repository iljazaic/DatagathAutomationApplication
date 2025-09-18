package com.example.datagath.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.datagath.model.CollectionTable;
import com.example.datagath.model.ScheduledEvent;
import com.example.datagath.model.User;
import com.example.datagath.repository.CollectionTableRepository;
import com.example.datagath.repository.ScheduledEventsRepository;
import com.example.datagath.service.DynamicTableService;
import com.example.datagath.service.UserService;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;
import com.zaxxer.hikari.util.SuspendResumeLock;

import jakarta.websocket.server.PathParam;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/analyse")
public class AnalisysController {

    private DynamicTableService dynamicTableService;
    private UserService userService;
    private CollectionTableRepository tableRepository;
    private final ScheduledEventsRepository scheduledEventsRepository;

    public AnalisysController(DynamicTableService dynamicTableService,
            UserService userService,
            CollectionTableRepository tableRepository,
            ScheduledEventsRepository scheduledEventsRepository) {
        this.dynamicTableService = dynamicTableService;
        this.userService = userService;
        this.tableRepository = tableRepository;
        this.scheduledEventsRepository = scheduledEventsRepository;
    }

    @GetMapping("/reports/create")
    public String reportCreationPage(@CookieValue(required = false) String sessionToken, Model model) {
        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;
        if (user != null) {

            //List<CollectionTable> userTableList = tableRepository.findByOwnerId(user.getId());

            // model.addAttribute("user", user);
            model.addAttribute("type", user);
            return "report";

        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/visualisations/create")
    public String visualisationCreationPage(@CookieValue(required = false) String sessionToken, Model model) {
        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;
        if (user != null) {

            //List<CollectionTable> userTableList = tableRepository.findByOwnerId(user.getId());

            // model.addAttribute("user", user);
            model.addAttribute("type", user);
            return "visualisation";

        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("/stats")
    @ResponseBody
    public Map<String, Map<String, String>> processTableSummary(@CookieValue(required = false) String sessionToken,
            @RequestParam String tableName) {
        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;
        if (user != null) {
            List<CollectionTable> userTables = tableRepository.findByOwnerId(user.getId());
            for (CollectionTable collectionTable : userTables) {
                if (collectionTable.getName() != null && collectionTable.getName().equals(tableName)) {
                    Map<String, Map<String, String>> tableSummary = dynamicTableService.tableSummary(collectionTable);
                    return tableSummary;
                }
            }
        }
        return Collections.emptyMap();
    }

    @PostMapping("/eventcontext")
    @ResponseBody
    public Map<String, String> eventContextRequest(@CookieValue(required = false) String sessionToken,
            @RequestParam String eventName) {
        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;
        if (user != null) {
            Map<String, String> response = new HashMap<>();
            List<ScheduledEvent> allEvennts = scheduledEventsRepository.findByOwner(user);
            for (ScheduledEvent scheduledEvent : allEvennts) {
                if (scheduledEvent.getName().equals(eventName)) {
                    response.put("cron", scheduledEvent.getCronString());
                    response.put("name", scheduledEvent.getName());
                    response.put("description", scheduledEvent.getDescription());
                    String action = scheduledEvent.getAction();
                    if (!action.equals("NONE")) {
                        System.out.println("BBBBBB " + scheduledEvent.getActionBody());
                        for (String actionPart : scheduledEvent.getActionBody().split(";")) {
                            response.put(actionPart.split(":")[0], actionPart.split(":")[1]);
                        }
                    } else {
                        response.put("Action", "None");
                    }
                    return response;
                }
            }
        }
        return Collections.emptyMap();

    }

    @GetMapping("/{TableName}")
    public String getMethodName(@PathVariable String TableName, @CookieValue(required = false) String sessionToken,
            Model model) {
        User user = sessionToken != null ? userService.validateSessionToken(sessionToken) : null;

        if (user != null) {
            CollectionTable table = dynamicTableService.findTable(user.getId(), TableName);
            if (table != null) {
                // Map<String,String> tableData = new HashMap<>();

                model.addAttribute("table", table);
                System.out.println(table.getUrl());
                return "examine";
            }
        }
        return "redirect:/login";

    }

}
