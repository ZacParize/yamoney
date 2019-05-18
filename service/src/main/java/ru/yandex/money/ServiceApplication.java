/*
 * ServiceApplication.java
 *
 * Copyright 2017-2019 BCS-Technologies. All Rights Reserved.
 *
 * This software is the proprietary information of BCS-Technologies.
 * Use is subject to license terms.
 */

package ru.yandex.money;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringCloudApplication
@ComponentScan(basePackages = { "ru.yandex.money" }, excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))
@EntityScan(basePackageClasses = ServiceApplication.class)
public class ServiceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        System.setProperty("user.country", System.getProperty("user.country", "RU"));
        System.setProperty("user.language", System.getProperty("user.language", "ru_RU"));
        new SpringApplicationBuilder(ServiceApplication.class).build().run(args);
    }

    @Override
    public void run(String... args) {}

}