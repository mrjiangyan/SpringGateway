package com.springboot.gateway.Controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
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
    @HystrixCommand(fallbackMethod = "stubMyService",
    commandProperties = {
        @HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE")
    }
    )
    public String Register()
    {
        return "123";
    }
}
