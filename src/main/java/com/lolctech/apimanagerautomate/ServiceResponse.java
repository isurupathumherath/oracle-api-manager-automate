package com.lolctech.apimanagerautomate;

public class ServiceResponse {
    private Object implementation;
    private String type;
    private String version;
    private String name;
    private String state;

    public Object getImplementation() {
        return implementation;
    }

    public void setImplementation(Object implementation) {
        this.implementation = implementation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
