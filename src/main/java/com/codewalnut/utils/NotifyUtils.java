package com.codewalnut.utils;

import com.saysth.commons.http.HttpHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhqpe on 2018-02-09.
 */
public class NotifyUtils {
	private static Logger log = LoggerFactory.getLogger(NotifyUtils.class);

	public static void sendWechatMsg(String title, String content, String openId) {
		Assert.isTrue(StringUtils.isNotBlank(openId), "Missing openId");
		title = StringUtils.defaultIfBlank(title, "消息通知");
		content = StringUtils.defaultIfBlank(content, "完成");
		String url = "https://wx.vibeac.com/bizcardServer/notice/sendText.do";
		final Map<String, String> data = new HashMap<>(3);
		data.put("title", title);
		data.put("content", content);
		data.put("openId", openId);
		try {
			HttpHelper.connect(url).data(data).get().html();
		} catch (Exception ex) {
			log.error("sendWechartMsg failed", ex);
		}
	}

}
