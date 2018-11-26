package net.kazuhiro1982.graphdb.config;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class Config {

    @Autowired
    private Environment env;

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/message");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver(
            @Qualifier("templateEngine") SpringTemplateEngine templateEngine) {
        ThymeleafViewResolver thymeleafViewResolver = new ThymeleafViewResolver();
        thymeleafViewResolver.setTemplateEngine(templateEngine);
        thymeleafViewResolver.setCharacterEncoding("UTF-8");
        return thymeleafViewResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(@Qualifier("webTemplateResolver") ITemplateResolver webTemplateResolver,
            @Qualifier("messageSource") MessageSource messageSource) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(webTemplateResolver);
        templateEngine.setMessageSource(messageSource);
        return templateEngine;
    }

    @Bean(name = "webTemplateResolver")
    public ITemplateResolver webTemplateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setOrder(3);

        return templateResolver;
    }

    @Bean
    public Cluster cluster() {
        Cluster.Builder builder = Cluster.build();
        builder.addContactPoint(env.getProperty("spring.datasource.endpoint"));
        builder.port(env.getProperty("spring.datasource.port", Integer.class));

        return builder.create();
    }

    @Bean
    public GraphTraversalSource graphTraversalSource(Cluster cluster) {
        return EmptyGraph.instance().traversal().withRemote(DriverRemoteConnection.using(cluster));
    }
}
