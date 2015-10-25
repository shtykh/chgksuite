package shtykh.rest;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import shtykh.util.html.HtmlHelper;

/**
 * Created by shtykh on 08/04/15.
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackages = "shtykh.rest")
public class MainApplication /*extends WebMvcConfigurationSupport*/ {
	private static final Logger log = Logger.getLogger(MainApplication.class);

	public static void main(String[] args) throws Exception {
		try {
			Object[] classes = new Object[]{
					HtmlHelper.class,
					AuthorsCatalogue.class,
					PackController.class,
					MainApplication.class
			};
			SpringApplication app = new SpringApplicationBuilder()
					.sources(classes)
					.build();
			app.run(args);
			log.info("Application launched successfully, check it out on your localhost");
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
		return factory;
	}

//	@Override
//	@Bean
//	public RequestMappingHandlerMapping defaultServletHandlerMapping() {
//		RequestMappingHandlerMapping rmh = super.requestMappingHandlerMapping();
//		rmh.setUseTrailingSlashMatch(true);
//		return rmh;
//	}
}
