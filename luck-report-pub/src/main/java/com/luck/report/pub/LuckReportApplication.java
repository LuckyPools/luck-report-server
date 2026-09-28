package com.luck.report.pub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;

/**
 * Luck Report 独立应用启动类
 * 
 * @author luck
 */
@SpringBootApplication(exclude = {ThymeleafAutoConfiguration.class})
public class LuckReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuckReportApplication.class, args);
        System.out.println("Luck-Report 后台启动成功！");
    }

}
