package com.example.datagath.dto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchEvCreationForm {
    private String eventName;
    private Long userId;
    private String cronString;
    private String description;
    private String actionBody;
    private String action;
    private String pingAddress;
    private String sendAddress;
    private String model;
    private String apikey;
    private String prompt;
    private String dataset;
    private String visualisationType;
    private String customcode;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getActionBody() {
        return actionBody;
    }

    public void setActionBody(String actionBody) {
        this.actionBody = actionBody;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPingAddress() {
        return pingAddress;
    }

    public void setPingAddress(String pingAddress) {
        this.pingAddress = pingAddress;
    }

    public String getSendAddress() {
        return sendAddress;
    }

    public void setSendAddress(String sendAddress) {
        this.sendAddress = sendAddress;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getVisualisationType() {
        return visualisationType;
    }

    public void setVisualisationType(String visualisationType) {
        this.visualisationType = visualisationType;
    }

    public String getCustomcode() {
        return customcode;
    }

    public void setCustomcode(String customcode) {
        this.customcode = customcode;
    }

    public Map<String, String> setupActionBody() {
        Map<String, String> eventActionBody = new HashMap<>();
        String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        String URL_REGEX = "^(https?:\\/\\/)?((([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}|(\\d{1,3}\\.){3}\\d{1,3}))(:\\d{1,5})?(\\/[^\\s]*)?$";
        String[] modelList = {
                "OPENAI",
                "GOOGLE",
                "ANTHROPIC",
                "META"
        };
        System.out.println(getAction());
        if (getSendAddress() == null
                || !getSendAddress().matches(URL_REGEX) && !getSendAddress().matches(EMAIL_REGEX)) {
            return null;
        }
        ;
        switch (getAction()) {
            case "PING":
                System.out.println("processing ping");
                if (getPingAddress() == null || !getPingAddress().matches(URL_REGEX)) {
                    return null;
                }
                //System.out.println(getPi);
                eventActionBody.put("pingAddress", getPingAddress());
                eventActionBody.put("sendAddress", getSendAddress());
                break;
            case "REPORT":
                if (getDataset() == null) {
                    return null;
                }
                ;
                eventActionBody.put("sendAddress", getSendAddress());
                eventActionBody.put("dataset", getDataset());
                break;
            case "AI/LLM":
                if (getPrompt() == null || getApikey() == null || getModel() == null
                        || !Arrays.asList(modelList).contains(getModel())) {
                    return null;
                }
                eventActionBody.put("sendAddress", getSendAddress());
                eventActionBody.put("model", getModel());
                eventActionBody.put("apikey", getApikey());
                eventActionBody.put("prompt", getPrompt());
                break;
            case "VISUALISTION":
                if (getDataset() == null || getVisualisationType() == null) {
                    return null;
                }
                eventActionBody.put("dataset", getDataset());
                eventActionBody.put("sendAddress", getSendAddress());
                eventActionBody.put("visualisationType", getVisualisationType());
                break;
            default:
                break;
        }
        eventActionBody.put("action", getAction());
        return eventActionBody;
    }
}
