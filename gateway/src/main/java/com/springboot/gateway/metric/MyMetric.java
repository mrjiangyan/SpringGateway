package com.springboot.gateway.metric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Component;

/**
 * Created by Steven on 2017/2/16.
 */
@Component
public class MyMetric {
    private final CounterService counterService;
    private final GaugeService gaugeService;
    @Autowired
    public MyMetric(CounterService counterService, GaugeService gaugeService) {
        this.counterService = counterService;
        this.gaugeService = gaugeService;
    }
    public void exampleCounterMethod() {
        this.counterService.increment("login.count");
        // reset each minute
    }
    public void exampleGaugeMethod() {
        this.gaugeService.submit("cache.hit", 80.0);
    }
}