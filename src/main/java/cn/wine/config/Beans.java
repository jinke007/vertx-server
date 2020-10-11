package cn.wine.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ChenJinKe
 */
@Configuration
public class Beans {

	@Bean
	public CuratorFramework curatorFramework() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE);
		CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
				.connectString("10.248.224.18:2181,10.248.224.25:2181,10.248.224.39:2181")
				.namespace("steelyard")
				.sessionTimeoutMs(3000)
				.connectionTimeoutMs(3000)
				.retryPolicy(retryPolicy)
				.build();
		curatorFramework.start();
		return curatorFramework;
	}

}
