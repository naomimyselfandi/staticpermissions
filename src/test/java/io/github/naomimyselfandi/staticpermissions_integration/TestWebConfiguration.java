package io.github.naomimyselfandi.staticpermissions_integration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@Configuration
@ComponentScan
@Import(TestConfiguration.class)
class TestWebConfiguration {}
