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
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Weway-RG001 on 2018-02-06 22:34.
 */
@Service
public class AddressAnalyseService {
    private Logger log = LoggerFactory.getLogger(AddressAnalyseService.class);
    private static final String TIME = "\"time\":";
    private static final String HEIGHT = "\"height\":";
    private static final String ADDR = "\"addr\":";

    private static final String KeyBalance = "B."; // B.1KVcqebRwgwRK6PMCrn34KoSRbm7gfXv8B = 120000000
    private static final String KeyHeight = "H."; // H.1KVcqebRwgwRK6PMCrn34KoSRbm7gfXv8B = 370002
    private static final String kelvinOpenId = "oc9byv_N1SwunMRXCN9K13aCIv3w";

    // 计算总额
    public long calculateSum(DB db, Integer from, Integer to) throws IOException {
        if (from == null) {
            from = Integer.MIN_VALUE;
        }
        if (to == null) {
            to = Integer.MAX_VALUE;
        }
        log.info("calculateSum({}, {})", from, to);
        Assert.isTrue(from < to, "Invalid arguments! Must ensure from < to");
        long bgn = System.currentTimeMillis();

        //读取当前snapshot，快照，读取期间数据的变更，不会反应出来
        Snapshot snapshot = db.getSnapshot();
        //读选项
        ReadOptions readOptions = new ReadOptions();
        readOptions.fillCache(false);//遍历中swap出来的数据，不应该保存在memtable中。
        readOptions.snapshot(snapshot);//默认snapshot为当前。
        DBIterator iterator = db.iterator(readOptions);

        long totalCnt = 0L;
        long addressCnt = 0L;
        long sum = 0L;
        while (iterator.hasNext()) {
            totalCnt++;
            Map.Entry<byte[], byte[]> item = iterator.next();
            String key = new String(item.getKey(), Constants.UTF8);
            String value = new String(item.getValue(), Constants.UTF8);//null,check.
            if (StringUtils.startsWith(key, KeyHeight)) { //是高度记录
                int height = Integer.parseInt(value);
                if (height >= from && height <= to) {
                    String addr = key.substring(2);
                    String keyBal = KeyBalance + addr;
                    sum += Long.parseLong(new String(db.get(keyBal.getBytes())));
                    addressCnt++;
                }
            }
        }
        sum = sum / 100000000L;
        iterator.close();//must be
        long end = System.currentTimeMillis();

        log.info("计算总额为：{}比特币，共扫描到：{}个钱包地址， 共扫描了{}条数据记录，{}", sum, addressCnt, totalCnt, LogUtils.getElapse(bgn, end));
//        NotifyUtils.sendWechatMsg("计总完成", "完成" + from + "-》" + to + "计总:" + sum + ", " + LogUtils.getElapse(bgn, end), kelvinOpenId);
        return sum;
    }

    // 把路径下的地址文件遍历，然后写入LevelDB
    public void saveAddressToLevelDB(DB db, String path, int from, int to, String withTotalSum) {
        log.info("saveAddressToLevelDB path {}, {}, {}", path, from, to);

        long bgn = System.currentTimeMillis();
        if (!StringUtils.endsWith(path, File.separator)) {
            path = path + File.separator;
        }

        int total = 0;
        for (int i = from; i <= to; i++) {
            File file = new File(path + i + ".json");
            saveOneAddressFileToLevelDB(db, file);
            total++;
        }
        long end = System.currentTimeMillis();

        log.info("扫描了{}个文档, {}", total, LogUtils.getElapse(bgn, end));
        if (StringUtils.equals(withTotalSum, "true")) {
            try {
                calculateSum(db, 0, to);
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        NotifyUtils.sendWechatMsg("", "完成" + path + ":" + from + "-》" + to + ", " + LogUtils.getElapse(bgn, end), kelvinOpenId);
    }

    // 把一个文件的内容写入levelDB
    public void saveOneAddressFileToLevelDB(DB db, File file) {
//        log.info("saveOneAddressFileToLevelDB({})", file.getName());
        long bgn = System.currentTimeMillis();
        Block block = null;
        try {
            String json = FileUtils.readFileToString(file, Constants.UTF8);
            List<Block> blocks = JsonUtils.parseArray(json, "blocks", Block.class);
            for (Block blk : blocks) {
                if (blk.isMainChain()) {
                    block = blk;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("错误的区块文件: " + file.getName(), ex);
        }

        List<Transaction> txs = block.getTx();

        // 处理所有交易
        long fee = block.getFee();
        long height = block.getHeight();
        String heightStr = String.valueOf(height);
        boolean coinbaseAppeared = false;
        long totalIn = 0L;
        long totalOut = 0L;
        for (Transaction tx : txs) {
            // 处理input
            boolean isMining = false;
            for (Input input : tx.getInputs()) {
                Output prevOut = input.getPrevOut();
                if (prevOut == null) {
                    // 一个区块内部应该有且仅有一个coinbase交易
                    Assert.isTrue(!coinbaseAppeared, "wrong transaction(2 coinbase) in block:" + height);
                    coinbaseAppeared = true;
                    isMining = true;
                    continue;
                }
                String addr = prevOut.getAddr();
                long val = prevOut.getValue();
                totalIn += val;
                String keyBal = KeyBalance + addr;
                String keyHeight = KeyHeight + addr;
                byte[] balBytes = db.get(keyBal.getBytes());
                byte[] heightBytes = db.get(keyHeight.getBytes());
                String prevBalStr = balBytes != null ? new String(balBytes) : null;
                String prevHeightStr = heightBytes != null ? new String(heightBytes) : null;

                if (prevBalStr != null) { // 账户已经存在，则做扣减处理
                    long newBal = Long.parseLong(prevBalStr) - val;
                    db.put(keyBal.getBytes(), String.valueOf(newBal).getBytes()); // 更新累加余额
					if (StringUtils.equals(addr, "12JLCkkRKKKevxpSmqK58gZKfzTHRRW77K")) {
						log.error("{}: {} - {} = {}", heightStr, prevBalStr, val, newBal);
					}
                    if (!StringUtils.equals(prevHeightStr, heightStr)) {
                        db.put(keyHeight.getBytes(), heightStr.getBytes()); // 更新最后高度
                    }
                } else {
                    //log.error("区块[{}]出现了无余额支付账户:{}", height, addr);
//                    throw new RuntimeException("出现了无余额的账户进行支付");
                }
            }

            // 处理output
            for (Output output : tx.getOut()) {
                String addr = output.getAddr();
                long val = output.getValue();
                if (!isMining) {
                    totalOut += val;
                }
                String keyBal = KeyBalance + addr;
                String keyHeight = KeyHeight + addr;
                byte[] balBytes = db.get(keyBal.getBytes());
                byte[] heightBytes = db.get(keyHeight.getBytes());
                String prevBalStr = balBytes != null ? new String(balBytes) : null;
                String prevHeightStr = heightBytes != null ? new String(heightBytes) : null;

                if (prevBalStr != null) { // 账户已经存在，则做累加处理
                    long newBal = Long.parseLong(prevBalStr) + val;
                    db.put(keyBal.getBytes(), String.valueOf(newBal).getBytes()); // 更新累加余额
					if (StringUtils.equals(addr, "12JLCkkRKKKevxpSmqK58gZKfzTHRRW77K")) {
						log.error("{}: {} + {} = {}", heightStr, prevBalStr, val, newBal);
					}
                    if (!StringUtils.equals(prevHeightStr, heightStr)) {
                        db.put(keyHeight.getBytes(), String.valueOf(height).getBytes()); // 更新最后高度
                    }
                } else {
					if (StringUtils.equals(addr, "12JLCkkRKKKevxpSmqK58gZKfzTHRRW77K")) {
						log.error("{}: received {}", heightStr, val);
					}
                    db.put(keyBal.getBytes(), String.valueOf(val).getBytes()); // 新增累加余额
                    db.put(keyHeight.getBytes(), heightStr.getBytes()); // 新增最后高度
                }
            }
        }
        if (totalIn != totalOut + fee) {
            log.error("区块[{}]存在非法交易数据: totalIn:{} != totalOut:{} + fee:{}, 交易数:{}, 差额:{}", height, totalIn, totalOut, fee, txs.size(), totalIn - totalOut - fee);
        }
        long end = System.currentTimeMillis();
        log.info("完成区块[{}], tx: {}, {}", height, txs.size(), LogUtils.getElapse(bgn, end));
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
