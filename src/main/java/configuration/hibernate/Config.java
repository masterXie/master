package configuration.hibernate;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import java.beans.PropertyVetoException;
import java.util.Properties;

/**
 * @author xielei
 * @description
 * @createDate 2017-03-31 10:58
 */

@Configuration
@PropertySource(value = { "classpath:data_source.properties" })
public class Config {

    @Autowired
    private Environment environment;

    @Bean
    public SessionFactory sessionFactory(ComboPooledDataSource dataSource) throws Exception {
        LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
        sfb.setDataSource(dataSource);
        sfb.setPackagesToScan("org.master.ixiansheng.entity");
        Properties props = new Properties();
        props.setProperty("dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
        props.setProperty("show_sql", "true");
        props.setProperty("current_session_context_class", "thread");
        sfb.setHibernateProperties(props);
        sfb.afterPropertiesSet();
        System.out.println("sessionFactory创建");
        System.out.println(environment.getProperty("user"));
        return sfb.getObject();
    }

    @Bean
    public ComboPooledDataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setDriverClass("com.mysql.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/master?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useOldAliasMetadataBehavior=true");
        ds.setUser("root");
        ds.setPassword("xie0316lei");
        ds.setMinPoolSize(10);//线程池中保留最小连接数
        ds.setMaxPoolSize(50);//线程池中保留的最大连接数 默认15
        ds.setMaxIdleTime(1800);//最大空闲时间 1800秒内未使用则连接被弃用。若为0则用不丢弃
        ds.setAcquireIncrement(3);//当连接耗尽的时候c3p0一次on更是获取的连接数 默认3
        ds.setMaxStatements(1000);
        ds.setInitialPoolSize(10);
        ds.setIdleConnectionTestPeriod(60);//每60秒检查所有连接池中的空闲衔接
        ds.setAcquireRetryAttempts(30);//定义在从数据库获取新连接失败后重复尝试的次数 默认30
        ds.setBreakAfterAcquireFailure(true);
        ds.setTestConnectionOnCheckout(false);
        ds.setAutoCommitOnClose(false);
        System.out.println("ComboPooledDataSource创建");
        return ds;
    }

    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager htm = new HibernateTransactionManager();
        htm.setSessionFactory(sessionFactory);
        return htm;
    }
}