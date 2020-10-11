package cn.wine.service;

import cn.wine.zookeeper.ZookeeperClusterManager;
import com.alibaba.fastjson.JSON;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.spi.cluster.ClusterManager;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Resource;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author ChenJinKe
 */
@Service
public class Server extends AbstractServer implements InitializingBean {
	private static final AtomicReference<Vertx> vertx = new AtomicReference<>();

	@Resource
	private CuratorFramework curatorFramework;

	@Override
	public void start() {
		vertx.get().eventBus().consumer("msg.test", handler -> {
			String msg = (String) handler.body();
			System.out.println(msg);
//			msg.reply("success");
//			handler.reply("success");
		});
		// 创建TCP服务器
		NetServer server = vertx.get().createNetServer();
		// 处理连接请求
		server.connectHandler(socket -> {
			socket.handler(buffer -> {
				// TODO 这边try catch必须加,socket的异常处理器是可以的，但是处理器的异常处理器貌似
				// TODO 只能自己处理
				// TODO 每个都可以设置处理器，vertx也可以，vertx的还没测试
				vertx.get().executeBlocking((Handler<Future<Boolean>>) event -> {
					try {
						// 在这里应该解析报文，封装为协议对象，并找到响应的处理类，得到处理结果，并响应
						String msg = buffer.toString();
						MyMessage message = JSON.parseObject(msg, MyMessage.class);
						SOCKET_MAP.put(message.getClientId(), socket);
						// TODO 这边异常是通过事件机制抛出的
						printf(message);
						// 按照协议响应给客户端
						socket.write(Buffer.buffer("cn.wine.service.Server Received"));
//						System.out.println(Collections.singletonList("2").get(3));
						// TODO 这儿就代表结束了，后面的就算抛异常也没用
						event.complete(Boolean.TRUE);
					} catch (Exception e) {
						event.fail(e);
//						printf(ExceptionUtils.getMessage(e));
					}
				}, event -> {
					printf(String.format("执行结果[%s]", event.result()));
					if (Objects.nonNull(event.cause())) {
						event.cause().printStackTrace();
					}
				});

			});
			socket.exceptionHandler(throwable -> {
				printf("异常");
				throwable.printStackTrace();
			});
			// 监听客户端的退出连接
			socket.closeHandler(close -> {
				printf("客户端退出连接" + socket.writeHandlerID());
			});
		});
//		server.exceptionHandler(throwable -> {
//			throwable.printStackTrace();
//		});
		vertx.get().exceptionHandler(throwable -> {
			throwable.printStackTrace();
		});

		// 监听端口
		server.listen(5555, res -> {
			if (res.succeeded()) {
				printf("服务器启动成功");
			}
		});
	}

	@Override
	public void afterPropertiesSet() {
		ClusterManager mgr = new ZookeeperClusterManager(curatorFramework);
		// TODO 数据量大了后会粘包

		VertxOptions options = new VertxOptions().setClusterManager(mgr);
		options.getEventBusOptions()
				.setReceiveBufferSize(4194304)
				.setSendBufferSize(4194304);
		options.setClusterPort(8066);
		options.setClusterHost("192.168.2.205");
		MetricsOptions metrics = new MetricsOptions();
		options.setMetricsOptions(metrics);
		Vertx.clusteredVertx(options, res -> {
			if (res.succeeded()) {
				vertx.set(res.result());
				vertx.get().deployVerticle(this);
				EventBus eventBus = vertx.get().eventBus();
				System.out.println("We now have a clustered event bus: " + eventBus);
			} else {
				System.out.println("Failed: " + res.cause());
			}
		});
	}

}
