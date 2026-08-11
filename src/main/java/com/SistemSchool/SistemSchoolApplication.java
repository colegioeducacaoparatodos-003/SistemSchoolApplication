package com.SistemSchool;

import org.apache.myfaces.webapp.StartupServletContextListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SistemSchoolApplication {

    static {
        System.setProperty("org.jboss.weld.environment.servlet.archive.isolation", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(SistemSchoolApplication.class, args);
    }

    @Bean
    public ServletListenerRegistrationBean<StartupServletContextListener> myFacesStartupListener() {
        return new ServletListenerRegistrationBean<>(new StartupServletContextListener());
    }

    @Bean
    public ServletRegistrationBean<FacesServlet> facesServletRegistration() {
        ServletRegistrationBean<FacesServlet> registration = new ServletRegistrationBean<>(new FacesServlet(), "*.xhtml");
        registration.setName("Faces Servlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    public ServletContextInitializer initializer() {
        return servletContext -> {
            // Configurações ESSENCIAIS do JSF
            servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", "Production");
            servletContext.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", "true");

            // Configurações para DESATIVAR CDI
            servletContext.setInitParameter("jakarta.faces.ENABLE_CDI_RESOLVER", "true");

            // servletContext.setInitParameter("org.apache.myfaces.CDI_MANAGER", "none");
            servletContext.setInitParameter("org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE", "true");
            servletContext.setInitParameter("primefaces.UPLOADER", "commons");

            // Configuração do PrimeFaces
            servletContext.setInitParameter("primefaces.THEME", "saga");
            servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");
            servletContext.setInitParameter("primefaces.UPLOAD_MAX_FILE_SIZE", "10485760");
        };
    }

}
