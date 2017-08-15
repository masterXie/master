package configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xielei
 * @description
 * @createDate 2017-03-29 15:59
 */

@Configuration
@EnableWebMvc
@ComponentScan("master.controller")
public class WebConfig extends WebMvcConfigurerAdapter {
	@Bean
	public ViewResolver viewResolver(){
		InternalResourceViewResolver resolver=new InternalResourceViewResolver();
		resolver.setCache(true);
		resolver.setSuffix(".jsp");
		resolver.setPrefix("/");
		resolver.setContentType("text/html;charset=UTF-8");
		return resolver;
	}

/*    @Bean
    public FreeMarkerConfigurer freemarkerConfig(){
        FreeMarkerConfigurer f=new FreeMarkerConfigurer();
        f.setTemplateLoaderPath("/");
        f.setDefaultEncoding("UTF-8");
        return f;
    }*/

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry){
		registry.addResourceHandler("/**")
				.addResourceLocations("/");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry){
		//registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters){
		StringHttpMessageConverter c=new StringHttpMessageConverter();
		List<MediaType> types=new ArrayList<>();
		types.add(MediaType.APPLICATION_JSON_UTF8);
		//types.add(new MediaType("text/html;charset=UTF-8"));
		c.setSupportedMediaTypes(types);
		converters.add(c);
	}
}