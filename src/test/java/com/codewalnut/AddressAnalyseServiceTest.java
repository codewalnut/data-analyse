package com.codewalnut;

import com.codewalnut.domain.FileTask;
import com.codewalnut.service.AddressAnalyseService;
import org.junit.Test;

/**
 * Created by zhqpe on 2018-02-07.
 */
public class AddressAnalyseServiceTest {
	private AddressAnalyseService service = new AddressAnalyseService();

	@Test
	public void testHandleOneHeight() throws Exception {
		FileTask fileTask = new FileTask();
		fileTask.setHeight(507738);
		service.handleOneHeight(fileTask);
	}

}
