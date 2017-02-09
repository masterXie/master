package configuration.hibernate;

import java.util.Properties;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

@SuppressWarnings("deprecation")
@Configuration
public class Config {
	
	@Bean
	public SessionFactory sessionFactory(BasicDataSource dataSource) throws Exception{
		AnnotationSessionFactoryBean sfb=new AnnotationSessionFactoryBean();
		sfb.setDataSource(dataSource);
		sfb.setPackagesToScan("master.entity");
		Properties props=new Properties();
		props.setProperty("dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
		sfb.setDataSource(dataSource);
		sfb.afterPropertiesSet();
		return sfb.getObject();
	}
	
	@Bean
	public BasicDataSource dataSource(){
		BasicDataSource ds=new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost:3306/master?useUnicode=true&characterEncoding=UTF-8");
		ds.setUsername("root");
		ds.setPassword("tiger");
		ds.setInitialSize(20);
		ds.setMaxActive(50);
		ds.setMaxIdle(30);
		ds.setDefaultAutoCommit(false);
		return ds;
	}
}
