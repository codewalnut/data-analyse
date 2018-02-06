package com.codewalnut.service;

import com.carrotsearch.sizeof.RamUsageEstimator;
import com.codewalnut.domain.Address;
import com.codewalnut.domain.AddressRepository;
import com.codewalnut.domain.FileTask;
import com.saysth.commons.utils.LogUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Weway-RG001 on 2018-02-06 22:34.
 */
//@Service
public class AddressAnalyseService {
	private Logger log = LoggerFactory.getLogger(AddressAnalyseService.class);
	private static final String TIME = "\"time\":";
	private static final String HEIGHT = "\"height\":";
	private static final String ADDR = "\"addr\":";

	@Autowired
	private AddressRepository addressRepository;

	@Transactional(rollbackFor = Exception.class)
	public void batchSave(Set<Address> addressses) {
		log.info("Saving {} addresses...", addressses.size());
		addressRepository.save(addressses);
	}

	private String getFirstFromJson(String s, String searchStr, int startPos) {
		int start = StringUtils.indexOf(s, searchStr, startPos) + searchStr.length();
		int end = StringUtils.indexOf(s, ",", start);
		return StringUtils.substring(s, start, end);
	}

	public boolean handleOneHeight(FileTask fileTask) throws Exception {
		log.info("handleOneHeight {}", fileTask.getHeight());
		File file = new File("D:/bitcoin_data/" + fileTask.getHeight() + ".json");

		log.info("Open file as String");
		String s = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
		log.info("time:[{}]", getFirstFromJson(s, TIME, 0));


		log.info("height:[{}]", getFirstFromJson(s, HEIGHT, 0));

		Set<String> addrSet = new HashSet<>();
		int start = StringUtils.indexOf(s, ADDR);
		long bgn = System.currentTimeMillis();
		while (start > 0) {
			int end = StringUtils.indexOf(s, ",", start);
			String addr = StringUtils.substring(s, start + ADDR.length(), end);
			addrSet.add(addr);
			start = StringUtils.indexOf(s, ADDR, start + ADDR.length());
		}
		long end = System.currentTimeMillis();
		log.info("found {} about {} {}", addrSet.size(), RamUsageEstimator.humanSizeOf(addrSet), LogUtils.getElapse(bgn, end));

		return false;
	}

}
