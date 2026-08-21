package com.example.backend.dto;

import java.util.List;

public class EndpointGroupDoc {

    private String name;
    private String basePath;
    private String description;
    private List<EndpointDoc> endpoints;

    public EndpointGroupDoc(String name, String basePath, String description, List<EndpointDoc> endpoints) {
        this.name = name;
        this.basePath = basePath;
        this.description = description;
        this.endpoints = endpoints;
    }

    public String getName() { return name; }
    public String getBasePath() { return basePath; }
    public String getDescription() { return description; }
    public List<EndpointDoc> getEndpoints() { return endpoints; }
}
