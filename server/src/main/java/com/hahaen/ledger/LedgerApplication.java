package com.hahaen.ledger;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hahaen.ledger.**.mapper")
public class LedgerApplication {
    public static void main(String[] args) {
        // Java 24+ 会警告 Netty 对 sun.misc.Unsafe 的内存访问；在应用启动最早阶段关闭该路径，兼容 IDEA、Maven 和 JAR 启动方式。
        System.setProperty("io.netty.noUnsafe", "true");
        SpringApplication.run(LedgerApplication.class, args);
    }
}
