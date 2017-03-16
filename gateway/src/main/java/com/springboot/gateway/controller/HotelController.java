package com.springboot.gateway.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Steven on 2017/2/16.
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {

    @RequestMapping(method = RequestMethod.GET, name = "register")
    @HystrixCommand
    public String Register()
    {
        return "123";
    }
}
