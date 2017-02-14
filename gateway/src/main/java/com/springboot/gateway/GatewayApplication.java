package com.springboot.gateway;

import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;
import com.netflix.zuul.monitoring.MonitoringHelper;
import com.spring.gateway.persistent.entity.GroovyScript;
import com.spring.gateway.persistent.mapper.GroovyScriptMapper;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Steven on 2017/2/13.
 */
@SpringBootApplication
@MapperScan("com.spring.gateway.persistent")
@ComponentScan
@Configuration
public class GatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(GatewayApplication.class);



    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     * @throws UnknownHostException if the local host name could not be resolved into an address
     */
    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        //DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:{}\n\t" +
                        "External: \thttp://{}:{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());

        String configServerStatus = env.getProperty("configserver.status");
        log.info("\n----------------------------------------------------------\n\t" +
                        "Config Server: \t{}\n----------------------------------------------------------",
                configServerStatus == null ? "Not found or not setup for this application" : configServerStatus);
    }

    @Component
    public static class GroovyInitRunner implements CommandLineRunner {
        @Override
        public void run(String... args) throws Exception {
            log.info("starting GroovyInitRunner");
            MonitoringHelper.initMocks();
            initGroovyFilterManagerFromDB();
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
            FilterLoader instance= FilterLoader.getInstance();
            instance.setCompiler(new GroovyCompiler());
            for(GroovyScript script :scriptMapper.getAll())
            {
                File temp =null ;

                try {
                    temp = new File(script.getScriptName()+".groovy");
                    log.info(temp.getAbsolutePath());
                    //在程序退出时删除临时文件
                    temp.deleteOnExit();
                    // 向临时文件中写入内容
                    BufferedWriter out = new BufferedWriter(new FileWriter(temp));
                    out.write(script.getScript());
                    out.close();
                    instance.putFilter(temp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
