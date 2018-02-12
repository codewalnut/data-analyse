package com.codewalnut.utils;

import org.junit.Test;

/**
 * Created by zhqpe on 2018-02-09.
 */
public class NotifyUtilsTest {

	@Test
	public void testSendWechartMsg() {
		NotifyUtils.sendWechatMsg("测试用例", "运行完毕，共导入155307759条数据, 耗时: 2小时32分26秒652毫秒", "oc9byv_N1SwunMRXCN9K13aCIv3w");
	}

}
