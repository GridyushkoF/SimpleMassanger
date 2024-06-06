package net.study.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Value("${avatars-path}")
    public String avatarsFolderPath;
    @Value("${pinned-images-path}")
    public String pinnedImagesFolderPath;
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/main").setViewName("main-page");
        registry.addViewController("/sign-up-page").setViewName("sign-up-page");
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/avatars/**")
                .addResourceLocations("file:///" + avatarsFolderPath + "/");
        registry.addResourceHandler("/pinned-images/**")
                .addResourceLocations("file:///" + pinnedImagesFolderPath + "/");
    }
}
