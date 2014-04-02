package com.softmotions.ncms.asm;

import java.io.Serializable;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmCore implements Serializable {

    Long id;

    String location;

    String name;

    String templateEngine;

    public AsmCore() {
    }

    public AsmCore(Long id) {
        this.id = id;
    }

    public AsmCore(String location, String name) {
        this.location = location;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("AsmCore{");
        sb.append("id=").append(id);
        sb.append(", location='").append(location).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", templateEngine='").append(templateEngine).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
