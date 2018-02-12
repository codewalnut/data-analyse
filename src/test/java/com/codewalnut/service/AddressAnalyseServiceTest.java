package com.codewalnut.service;

import com.codewalnut.service.AddressAnalyseService;
import org.junit.Test;

/**
 * Created by zhqpe on 2018-02-07.
 */
public class AddressAnalyseServiceTest {
	private AddressAnalyseService service = new AddressAnalyseService();

//	@Test
//	public void testHandleOneHeight() throws Exception {
//		FileTask fileTask = new FileTask();
//		fileTask.setHeight(507739);
//		Set<String> set = new HashSet<>();
//		service.handleOneHeight(fileTask, set);
//	}

	@Test
	public void testHandleOneFolder() throws Exception {
		service.handleOneFolder("D:\\bitcoin_data\\test", null);
	}

}
