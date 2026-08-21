package com.example.backend.dto;

import java.util.List;

public class ApiDocsResponse {

    private String title;
    private String version;
    private String description;
    private String authentication;
    private List<EndpointGroupDoc> groups;

    public ApiDocsResponse(String title, String version, String description, String authentication,
                            List<EndpointGroupDoc> groups) {
        this.title = title;
        this.version = version;
        this.description = description;
        this.authentication = authentication;
        this.groups = groups;
    }

    public String getTitle() { return title; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public String getAuthentication() { return authentication; }
    public List<EndpointGroupDoc> getGroups() { return groups; }
}
