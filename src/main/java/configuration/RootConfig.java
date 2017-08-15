package configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author xielei
 * @description
 * @createDate 2017-03-29 15:58
 */
@Configuration
@ComponentScan(basePackages={"master","configuration.hibernate","configuration.redis"},
		excludeFilters={
				@ComponentScan.Filter(type= FilterType.ANNOTATION,value=EnableWebMvc.class)
		})
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class RootConfig {
}