package com.spring.gateway.persistent.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by Steven on 2017/2/14.
 */
@Data
public class GroovyScript {

    private String script;
    private Date last_update_time;
    private int id;
    private String script_name;
    private String type;
    private boolean is_active;
    private String hash_code;


}
