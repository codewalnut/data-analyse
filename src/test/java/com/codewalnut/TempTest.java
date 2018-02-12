package com.codewalnut;

import com.alibaba.fastjson.JSONObject;
import com.codewalnut.domain.Block;
import com.codewalnut.utils.Constants;
import com.saysth.commons.utils.json.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
public class TempTest {
	private static Logger log = LoggerFactory.getLogger(TempTest.class);

	@Test
	public void test() throws Exception {
		long l = 234645645234l;
		System.out.println(String.valueOf(l));

		log.debug("begin");
		File file = new File("D:\\bitcoin_data\\507739.json");
		log.debug("file");
		String json = FileUtils.readFileToString(file, Constants.UTF8);
		log.debug("read");
		List<Block> blocks = JsonUtils.parseArray(json, "blocks", Block.class);
		log.debug("parse");
		System.out.println(blocks.get(0).toJson());
	}

}
