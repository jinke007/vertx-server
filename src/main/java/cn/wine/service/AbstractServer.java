package cn.wine.service;

import com.alibaba.fastjson.JSON;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ChenJinKe
 */
public class AbstractServer extends AbstractVerticle {
	public static final Map<String, NetSocket> SOCKET_MAP = new ConcurrentHashMap<>();

	static void printf(MyMessage log) {
		System.out.println(String.format("[%s]：", SOCKET_MAP.get(log.getClientId()).writeHandlerID())
				+ JSON.toJSONString(log));
	}

	static void printf(String log) {
		System.out.println(log);
	}
}
