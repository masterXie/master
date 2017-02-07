package configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan("master.controller")
public class WebConfig extends WebMvcConfigurerAdapter{
	@Bean
	public ViewResolver viewResolver(){
		FreeMarkerViewResolver resolver=new FreeMarkerViewResolver();
		resolver.setCache(true);
		resolver.setSuffix(".ftl");
		resolver.setPrefix("/views/");
		resolver.setContentType("text/html;charset=UTF-8");
		return resolver;
	}
	
	@Bean
	public FreeMarkerConfigurer freemarkerConfig(){
		FreeMarkerConfigurer f=new FreeMarkerConfigurer();
		f.setTemplateLoaderPath("/");
		f.setDefaultEncoding("UTF-8");
		return f;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry){
		registry
			.addResourceHandler("/views/page_js/**")
			.addResourceLocations("/views/page_js/","classpath:/views/page_js/");
	}
}
