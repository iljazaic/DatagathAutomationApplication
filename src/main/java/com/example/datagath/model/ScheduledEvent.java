package com.example.datagath.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ScheduledEvent {
    @ManyToOne // or @OneToOne depending on your model
    @JoinColumn(name = "owner")
    User owner;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String cronString;
    String description;
    String action;
    String actionBody;

    // action information will depend on what kind of action type was selected
    //
    // if the action is prompt_ai_model, the its structured as follows:
    // AI_URL:{url};AI_API:{api/default if paid
    // user};PROMPT:{user_prompt};CONTEXT_ACCESS:{list of datasets to access};
    //
    // if the action is send_email:
    // EMAIL_BODY:{...{tableName.latestValue/cumulativeValue/average/median/graph}...}
    //
    // if action is prepare_report_from_dataset
    // DATASET_NAME:{datasetname};SEND_AS:{};ADDRESS:{};
    //
    // if action is healthcheck
    // SOURCE_URL:{url}

    // why do i have to document this

    public ScheduledEvent() {
    }

    public ScheduledEvent(String name, String cronString, User owner, String action) {
        this.name = name;
        this.cronString = cronString;
        this.owner = owner;
        this.action = action;
    }

    public void generateActionBody(Map<String, String> actionBodyMap) {
        String action = actionBodyMap.containsKey("action") ? actionBodyMap.get("action") : null;
        if (action == null) {
            return;
        }
        
        Map<String,String> requiredKeys = new HashMap<String,String>(){
            {
                put("PING", "");
            }
        }


        String body = new String();
        switch (action) {
            case "PING":
                //Boolean containsRequiredKeys = Arrays.stream(requiredKeys[0].split(",")).filter(p->actionBodyMap.containsKey(p)).toArray(String[]::new).length==requiredKeys[0].split(",").length;
                Boolean containsKeys = Arrays.stream(requiredKeys.get(action).split(",")).allMatch(actionBodyMap::containsKey);
                if (actionBodyMap.containsKey("sendAddress") && actionBodyMap.containsKey("pingAddress")) {
                    body = "SEND_ADDRESS:%s;PING_ADDRESS:%s".formatted(actionBodyMap.get("sendAddress"),
                            actionBodyMap.get("pingAddress"));
                    this.actionBody = body;
                }
                break;
            case "REPORT":
                if (actionBodyMap.containsKey("sendAddress") && actionBodyMap.containsKey("dataset")) {
                    body = "SEND_ADDRESS:%s;DATASET:%s".formatted(actionBodyMap.get("sendAddress"),
                            actionBodyMap.get("dataset"));
                    this.actionBody = body;
                }
                break;
            case "AI/LLM":
                if (actionBodyMap.containsKey("sendAddress") && actionBodyMap.containsKey("model"))&& {
                    body = "SEND_ADDRESS:%s;DATASET:%s".formatted(actionBodyMap.get("sendAddress"),
                            actionBodyMap.get("dataset"));
                    this.actionBody = body;
                }
                break;
            default:
                break;
        }
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronString() {
        return cronString;
    }

    public void setCronString(String cronString) {
        this.cronString = cronString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setActionBody(String actionBody) {
        this.actionBody = actionBody;
    }

    public String getActionBody() {
        return actionBody;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
