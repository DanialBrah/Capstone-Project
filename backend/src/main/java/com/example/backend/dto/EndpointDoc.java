package com.example.backend.dto;

public class EndpointDoc {

    private String method;
    private String path;
    private String summary;
    private String auth;

    public EndpointDoc(String method, String path, String summary, String auth) {
        this.method = method;
        this.path = path;
        this.summary = summary;
        this.auth = auth;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getSummary() { return summary; }
    public String getAuth() { return auth; }
}
