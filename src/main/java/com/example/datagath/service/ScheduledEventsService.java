package com.example.datagath.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import com.example.datagath.dto.SchEvCreationForm;
import com.example.datagath.dto.SchEvCreationResponse;
import com.example.datagath.model.CollectionTable;
import com.example.datagath.model.ScheduledEvent;
import com.example.datagath.model.User;
import com.example.datagath.repository.ScheduledEventsRepository;
import com.example.datagath.repository.UserRepository;

@Service
public class ScheduledEventsService {

    private final ScheduledEventsRepository scheduledEventsRepository;
    private final TaskScheduler taskScheduler;
    private final UserRepository userRepository;

    public ScheduledEventsService(ScheduledEventsRepository scheduledEventsRepository, TaskScheduler taskScheduler,
            UserRepository userRepository) {
        this.scheduledEventsRepository = scheduledEventsRepository;
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        this.userRepository = userRepository;
        scheduler.setPoolSize(5);
        scheduler.initialize();
        this.taskScheduler = taskScheduler;
    }

    public void executeEvent(ScheduledEvent event) {
        System.out.println("Executing event: " + event.getName());
    }

    public void executeEventById(Long id) {
        scheduledEventsRepository.findById(id)
                .ifPresent(event -> System.out.println("Executing event: " + event.getName()));
    }

    public void startPayloadExecution(String action, Map<String, String> actionParams)
            throws IOException, InterruptedException {
        switch (action) {
            case "REPORT":
                break;
            case "PING":
                break;
            case "AI/LLM":
                String response = NetworkActionsService.llm_response(actionParams.get("model"),
                        actionParams.get("apikey"), actionParams.get("prompt"));
                // TODO:send the response
                NetworkActionsService.sendResponse(response, actionParams.get("sendAddress"));
                break;
            case "VISUALISTION":
                break;
            default:
                break;
        }
    }

    public ScheduledEvent insertIntoSchedule(ScheduledEvent event, Map<String, String> eventBody) {
        ScheduledEvent saved = scheduledEventsRepository.save(event);

        // uhhhh maybe this works
        // chatgpt kinda explained it to me but i think ill just implement it myself
        BackgroundJob.scheduleRecurrently(
                "event-" + saved.getId(),
                saved.getCronString(),
                () -> startPayloadExecution(saved.getAction(), eventBody));

        return saved;
    }

    public SchEvCreationResponse createNewScheduledEvent(SchEvCreationForm creationForm,
            Map<String, String> eventBody) {
        String name = creationForm.getEventName();
        User owner = userRepository.findById(creationForm.getUserId()).orElse(null);
        String cronString = creationForm.getCronString();

        SchEvCreationResponse response = new SchEvCreationResponse();
        response.setSuccess(false);
        response.setEventId(null);
        if (name != null && owner != null && cronString != null) {
            ScheduledEvent scheduledEvent = insertIntoSchedule(new ScheduledEvent(name, cronString, owner), eventBody);
            if (scheduledEvent != null) {
                response.setSuccess(true);
                response.setEventId(scheduledEvent.getId());
            }
        }
        return response;
    }

    public String getAllTableNamesForUser(User user) {
        if (user != null) {
            List<ScheduledEvent> eventList = scheduledEventsRepository.findByOwner(user);
            String toRespond = "";
            for (ScheduledEvent event : eventList) {
                ///System.out.println(event.getName());
                toRespond = String.join(",", toRespond, event.getName());
            }
            return toRespond;
        }
        return null;
    }
}
