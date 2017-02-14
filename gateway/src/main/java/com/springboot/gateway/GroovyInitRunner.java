package com.springboot.gateway;

import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;
import com.netflix.zuul.monitoring.MonitoringHelper;
import com.spring.gateway.persistent.entity.GroovyScript;
import com.spring.gateway.persistent.mapper.GroovyScriptMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Steven on 2017/2/14.
 */
@Component
public class GroovyInitRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(GroovyInitRunner.class);


    Thread poller;
    boolean bRunning = true;
    int pollingIntervalSeconds = 20;

    @Override
    public void run(String... args) throws Exception {
        log.info("starting GroovyInitRunner");
        MonitoringHelper.initMocks();
        initGroovyFilterManagerFromDB();
    }

    void stopPoller() {
        bRunning = false;
    }

    void startPoller() {
        poller = new Thread("GroovyFilterFileManagerPoller") {
            public void run() {
                while (bRunning) {
                    try {
                        sleep(pollingIntervalSeconds * 1000);
                        manageFiles();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        poller.setDaemon(true);
        poller.start();
    }

    @Autowired
    GroovyScriptMapper scriptMapper;

    private void initGroovyFilterManager() {
        FilterLoader.getInstance().setCompiler(new GroovyCompiler());

        String scriptRoot = System.getProperty("zuul.filter.root", "gateway/src/main/groovy/filters");
        if (scriptRoot.length() > 0) scriptRoot = scriptRoot + File.separator;
        try {
            FilterFileManager.setFilenameFilter(new GroovyFileFilter());
            FilterFileManager.init(5, scriptRoot + "pre", scriptRoot + "route", scriptRoot + "post");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initGroovyFilterManagerFromDB() {
        FilterLoader instance = FilterLoader.getInstance();
        instance.setCompiler(new GroovyCompiler());
        manageFiles();
        startPoller();
        //启动一个线程去动态的刷新文件

    }

    private void manageFiles() {
        String scriptRoot = "";
        File root = new File("script");
        if (!root.exists())
            root.mkdir();
        scriptRoot = root.getName() + File.separator;

        for (GroovyScript script : scriptMapper.getAll()) {
            try {
                File temp = new File(scriptRoot, script.getScriptName() + ".groovy");
                log.info(temp.getAbsolutePath());
                //在程序退出时删除临时文件
                temp.deleteOnExit();
                // 向临时文件中写入内容
                BufferedWriter out = new BufferedWriter(new FileWriter(temp));
                out.write(script.getScript());
                out.close();
                //instance.putFilter(temp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (scriptRoot.length() > 0) scriptRoot = scriptRoot + File.separator;
        try {
            FilterFileManager.setFilenameFilter(new GroovyFileFilter());
            FilterFileManager.init(pollingIntervalSeconds, scriptRoot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}