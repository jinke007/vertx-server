package cn.wine.service;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ChenJinKe
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyMessage {
	private String clientId;
	private String sessionId;
	private String content;
	private String type;

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
