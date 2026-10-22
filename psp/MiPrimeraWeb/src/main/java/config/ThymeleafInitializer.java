package config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@WebListener
public class ThymeleafInitializer implements ServletContextListener {



    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);

        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
        templateResolver.setPrefix(UrlConstants.TEMPLATE_PREFIX);
        templateResolver.setSuffix(UrlConstants.TEMPLATE_SUFFIX);
        templateResolver.setTemplateMode(ThymeleafConstants.TEMPLATE_MODE);
        templateResolver.setCharacterEncoding(ThymeleafConstants.CHARACTER_ENCODING);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        servletContext.setAttribute(ThymeleafConstants.TEMPLATE_ENGINE_ATTR, templateEngine);
    }
}
