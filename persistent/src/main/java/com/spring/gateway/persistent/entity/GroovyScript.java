package com.spring.gateway.persistent.entity;

import java.util.Date;

/**
 * Created by Steven on 2017/2/14.
 */
public class GroovyScript {

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Date getLastUpdateTime() {
        return last_update_time;
    }

    public void setLastUpdateTime(Date last_update_time) {
        this.last_update_time = last_update_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String script;

    private Date last_update_time;

    private int id;

    public String getScriptName() {
        return script_name;
    }

    public void setScriptName(String script_name) {
        this.script_name = script_name;
    }


    private String script_name;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;


    public boolean isActive() {
        return is_active;
    }

    public void setIsActive(boolean is_active) {
        this.is_active = is_active;

    }

    public String getHashCode() {
        return hash_code;
    }



    private boolean is_active;

    private String hash_code;




}
