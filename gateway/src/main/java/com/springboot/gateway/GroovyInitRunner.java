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
    int pollingIntervalSeconds = 30;


    public void run(String... args) throws Exception {
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
    private String scriptRoot;

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
        manageFiles();
        startPoller();
        FilterLoader instance = FilterLoader.getInstance();
        instance.setCompiler(new GroovyCompiler());
        try {
            FilterFileManager.setFilenameFilter(new GroovyFileFilter());
            FilterFileManager.init(pollingIntervalSeconds, scriptRoot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private void manageFiles() {
        File root = new File("script");
        if (!root.exists())
            root.mkdir();
        scriptRoot = root.getName() + File.separator;
        GroovyScript[] array=scriptMapper.getAll();
        for (GroovyScript script : array) {
            try {
                File scriptFile = new File(scriptRoot, script.getScript_name() + ".groovy");
                //如果该脚本被设置为无效，则需要从本地进行清理
                if(!script.is_active())
                {
                    scriptFile.delete();
                    continue;
                }
                //此处的逻辑基于文件的修改日期没有发生过变化则，则不在保存该文件 提升更新效率
                if(scriptFile.exists() && scriptFile.lastModified() == script.getLast_update_time().getTime())
                    continue;
                log.info(scriptFile.getAbsolutePath());

                //向文件中写入内容
                BufferedWriter out = new BufferedWriter(new FileWriter(scriptFile));
                out.write(script.getScript());
                out.flush();
                out.close();
                scriptFile.setLastModified(script.getLast_update_time().getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (scriptRoot.length() > 0) scriptRoot = scriptRoot + File.separator;

    }
}