package configuration.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class Redis {
	
	@Bean
	public JedisPool jedisPool(){
		JedisPoolConfig poolConfig=new JedisPoolConfig();
		poolConfig.setMaxIdle(200);
		poolConfig.setMaxWaitMillis(10000);
		JedisPool jedisPool=new JedisPool(poolConfig,"localhost",6379);
		return jedisPool;
		//return jedisPool;
		//jedisPool.initPool(poolConfig, factory);
	}
}
