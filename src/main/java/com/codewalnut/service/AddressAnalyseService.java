package com.codewalnut.service;

import com.carrotsearch.sizeof.RamUsageEstimator;
import com.codewalnut.domain.*;
import com.codewalnut.utils.Constants;
import com.codewalnut.utils.NotifyUtils;
import com.saysth.commons.utils.LogUtils;
import com.saysth.commons.utils.json.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Weway-RG001 on 2018-02-06 22:34.
 */
@Service
public class AddressAnalyseService {
	private Logger log = LoggerFactory.getLogger(AddressAnalyseService.class);
	private static final String TIME = "\"time\":";
	private static final String HEIGHT = "\"height\":";
	private static final String ADDR = "\"addr\":";

	private static final String KeyBalance = "B.";
	private static final String KeyHeight = "H.";


	// 把路径下的地址文件遍历，然后写入LevelDB
	public void saveAddressToLevelDB(DB db, String path, int from, int to) {
		log.info("saveAddressToLevelDB path {}, {}, {}", path, from, to);

		long bgn = System.currentTimeMillis();
		if (!StringUtils.endsWith(path, File.separator)) {
			path = path + File.separator;
		}

		int total = 0;
		for (int i = from; i < to; i++) {
			File file = new File(path + i + ".json");
			saveOneAddressFileToLevelDB(db, file);
			total++;
		}
		long end = System.currentTimeMillis();

		log.info("saved {} files, {}", total, LogUtils.getElapse(bgn, end));
		NotifyUtils.sendWechatMsg("", "完成" + path + ":" + from + "-》" + to + ", " + LogUtils.getElapse(bgn, end), "oc9byv_N1SwunMRXCN9K13aCIv3w");
	}

	// 把一个文件的内容写入levelDB
	public void saveOneAddressFileToLevelDB(DB db, File file) {
//        log.info("saveOneAddressFileToLevelDB({})", file.getName());
		Block block = null;
		try {
			String json = FileUtils.readFileToString(file, Constants.UTF8);
			List<Block> blocks = JsonUtils.parseArray(json, "blocks", Block.class);
			block = blocks.get(0);
		} catch (Exception ex) {
			log.error("Wrong Block File: {}", file.getName());
			return;
		}

		List<Transaction> txs = block.getTx();

		// 处理所有交易
		long height = block.getHeight();
		String heightStr = String.valueOf(height);
		for (Transaction tx : txs) {
			for (Transaction transaction : txs) {
				// 处理input
				for (Input input : transaction.getInputs()) {
					Output prevOut = input.getPrevOut();
					if (prevOut == null) {
						continue;
					}
					String addr = prevOut.getAddr();
					long val = prevOut.getValue();
					String keyBal = KeyBalance + addr;
					String keyHeight = KeyHeight + addr;
					byte[] balBytes = db.get(keyBal.getBytes());
					byte[] heightBytes = db.get(keyHeight.getBytes());
					String prevBalStr = balBytes != null ? new String(balBytes) : null;
					String prevHeightStr = heightBytes != null ? new String(heightBytes) : null;

					if (prevBalStr != null) { // 账户已经存在，则做扣减处理
						long newBal = Long.parseLong(prevBalStr) - val;
						db.put(keyBal.getBytes(), String.valueOf(newBal).getBytes()); // 更新累加余额
						if (!StringUtils.equals(prevHeightStr, heightStr)) {
							db.put(keyHeight.getBytes(), String.valueOf(height).getBytes()); // 更新最后高度
						}
					} else {
						log.error("出现了无余额支付账户:{}", addr);
					}
				}

				// 处理output
				for (Output output : transaction.getOut()) {
					String addr = output.getAddr();
					long val = output.getValue();
					String keyBal = KeyBalance + addr;
					String keyHeight = KeyHeight + addr;
					byte[] balBytes = db.get(keyBal.getBytes());
					byte[] heightBytes = db.get(keyHeight.getBytes());
					String prevBalStr = balBytes != null ? new String(balBytes) : null;
					String prevHeightStr = heightBytes != null ? new String(heightBytes) : null;

					if (prevBalStr != null) { // 账户已经存在，则做累加处理
						long newBal = Long.parseLong(prevBalStr) + val;
						db.put(keyBal.getBytes(), String.valueOf(newBal).getBytes()); // 更新累加余额
						if (!StringUtils.equals(prevHeightStr, heightStr)) {
							db.put(keyHeight.getBytes(), String.valueOf(height).getBytes()); // 更新最后高度
						}
					} else {
						db.put(keyBal.getBytes(), String.valueOf(val).getBytes()); // 新增累加余额
						db.put(keyHeight.getBytes(), heightStr.getBytes()); // 新增最后高度
					}
				}
			}
		}
	}

	// 抽取需要的内容
	private String getFirstFromJson(String s, String searchStr, int startPos) {
		int start = StringUtils.indexOf(s, searchStr, startPos) + searchStr.length();
		int end = StringUtils.indexOf(s, ",", start);
		return StringUtils.substring(s, start, end);
	}

	/**
	 * 处理一个区块Json文件
	 *
	 * @param file
	 * @param addrSet
	 * @return
	 */
	public AddressAnalyseResult handleOneBlockFile(File file, Set<String> addrSet) {
//        log.info("handleOneHeight {}", fileTask.getHeight());
		AddressAnalyseResult result = new AddressAnalyseResult();
		result.setSuccess(false);

//        log.debug("Open file as String");
		String s = null;
		try {
			s = FileUtils.readFileToString(file, Constants.UTF8);
		} catch (IOException ex) {
			return result;
		}

		long time = Long.valueOf(getFirstFromJson(s, TIME, 0));
		int height = Integer.valueOf(getFirstFromJson(s, HEIGHT, 0));
//        log.debug("height:[{}], time:[{}]", height, time);

		int start = StringUtils.indexOf(s, ADDR);
		long bgn = System.currentTimeMillis();
		if (addrSet == null) {
			addrSet = new HashSet<>(1000);
		}
		while (start > 0) {
			int end = StringUtils.indexOf(s, ",", start);
			String addr = StringUtils.substring(s, start + ADDR.length() + 1, end - 1);
			addrSet.add(addr);
			start = StringUtils.indexOf(s, ADDR, start + ADDR.length());
		}
		long end = System.currentTimeMillis();

		result.setSuccess(true);
		result.setTime(time);
		result.setHeight(height);
		result.setAddrSet(addrSet);
//        log.debug("found {} addrs, {}", addrSet.size(), LogUtils.getElapse(bgn, end));
//        log.debug("Set Size: {}", RamUsageEstimator.humanSizeOf(addrSet));
		return result;
	}

	/**
	 * 处理一个目录下的区块Json文件
	 *
	 * @param path
	 * @param savePath
	 * @return
	 * @throws Exception
	 */
	public AddressAnalyseResult handleOneFolder(String path, String savePath) throws Exception {
		AddressAnalyseResult result = new AddressAnalyseResult();
		log.info("handleOneFolder path {}", path);
		Set<String> addrSet = null;

		long bgn = System.currentTimeMillis();
		if (!StringUtils.endsWith(path, File.separator)) {
			path = path + File.separator;
		}
		if (StringUtils.isBlank(savePath)) {
			savePath = path.substring(0, path.length() - 1) + "-address" + File.separator;
		}
		if (!StringUtils.endsWith(savePath, File.separator)) {
			savePath = savePath + File.separator;
		}
		File dir = new File(path);
		Collection<File> files = FileUtils.listFiles(dir, new String[]{"json"}, true);

		for (File file : files) {
			AddressAnalyseResult oneResult = handleOneBlockFile(file, addrSet);
			File resultFile = new File(savePath + oneResult.getHeight() + "-" + oneResult.getTime() + ".txt");
			FileUtils.writeLines(resultFile, "UTF-8", oneResult.getAddrSet(), false);
		}
		long end = System.currentTimeMillis();

		log.debug("found {} addrs, {}", addrSet != null ? addrSet.size() : 0, LogUtils.getElapse(bgn, end));
		log.debug("Set Size: {}", RamUsageEstimator.humanSizeOf(addrSet));

		result.setSuccess(true);
		result.setAddrSet(addrSet);
		return result;
	}

	/**
	 * 将某目录下的地址地址文件的内容去重复，合并成一个文件
	 *
	 * @param srcPath
	 * @param targetPath
	 * @return
	 * @throws Exception
	 */
	public AddressAnalyseResult removeDuplicateAddrForFolder(String srcPath, String targetPath) throws Exception {
		AddressAnalyseResult result = new AddressAnalyseResult();
		log.info("removeDuplicateAddrForFolder srcPath {}", srcPath);

		long bgn = System.currentTimeMillis();
		if (!StringUtils.endsWith(srcPath, File.separator)) {
			srcPath = srcPath + File.separator;
		}
		if (StringUtils.isBlank(targetPath)) {
			targetPath = srcPath.substring(0, srcPath.length() - 1) + "-distinct" + File.separator;
		}
		if (!StringUtils.endsWith(targetPath, File.separator)) {
			targetPath = targetPath + File.separator;
		}
		File dir = new File(srcPath);
		Collection<File> files = FileUtils.listFiles(dir, new String[]{"txt"}, true);

		Set<String> addrSet = new HashSet<>(files.size());

		for (File file : files) {
			handleOneAddrFile(file, addrSet);
		}
		long end = System.currentTimeMillis();

		log.debug("found {} addrs, {}", addrSet != null ? addrSet.size() : 0, LogUtils.getElapse(bgn, end));
//        log.debug("Set Size: {}", RamUsageEstimator.humanSizeOf(addrSet));

		File resultFile = new File(targetPath + "addrs.txt");
		FileUtils.writeLines(resultFile, "UTF-8", addrSet, false);

		result.setSuccess(true);
		result.setAddrSet(addrSet);
		return result;
	}

	public AddressAnalyseResult handleOneAddrFile(File file, Set<String> addrSet) {
//        log.info("handleOneHeight {}", fileTask.getHeight());
		AddressAnalyseResult result = new AddressAnalyseResult();
		result.setSuccess(false);

//        log.debug("Open file as String");
		String s = null;
		long bgn = System.currentTimeMillis();
		try {
			FileReader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);
			String str = null;

			if (addrSet == null) {
				addrSet = new HashSet<>(1000);
			}

			while ((str = br.readLine()) != null) {
				addrSet.add(str);
			}

			br.close();
			reader.close();
		} catch (IOException ex) {
			return result;
		}
		long end = System.currentTimeMillis();

		result.setSuccess(true);
		result.setAddrSet(addrSet);
		log.debug("found {} addrs in {}, {}", addrSet.size(), file.getName(), LogUtils.getElapse(bgn, end));
//        log.debug("Set Size: {}", RamUsageEstimator.humanSizeOf(addrSet));
		return result;
	}

}
