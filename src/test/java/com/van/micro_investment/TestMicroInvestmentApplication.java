package com.van.micro_investment;

import org.springframework.boot.SpringApplication;

public class TestMicroInvestmentApplication {

    public static void main(String[] args) {
        SpringApplication.from(MicroInvestmentApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
