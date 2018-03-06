package com.codewalnut.service;

import com.codewalnut.domain.Block;
import com.codewalnut.domain.Input;
import com.codewalnut.domain.Output;
import com.codewalnut.domain.Transaction;
import com.codewalnut.utils.LevelDBUtils;
import com.codewalnut.utils.NotifyUtils;
import com.saysth.commons.utils.LogUtils;
import com.saysth.commons.utils.json.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.codewalnut.utils.Constants.BITCOIN_2_CONG;
import static com.codewalnut.utils.Constants.UTF8;

/**
 * 地址分析服务
 * <p>
 * Created by Weway-RG001 on 2018-02-06 22:34.
 */
@Service
public class AddressAnalyseService {
    private Logger log = LoggerFactory.getLogger(AddressAnalyseService.class);

    /* levelDB的key前缀 */
    private static final String KeyBalance = "B."; // B.1KVcqebRwgwRK6PMCrn34KoSRbm7gfXv8B = 120000000
    private static final String KeyHeight = "H."; // H.1KVcqebRwgwRK6PMCrn34KoSRbm7gfXv8B = 370002
    /* levelDB的key前缀 */

    private static final String kelvinOpenId = "oc9byv_N1SwunMRXCN9K13aCIv3w";

    /**
     * 把指定路径下的区块信息文件遍历，然后写入LevelDB，区块文件全部都是统一成以区块高度来命名，比如500000.json是500000高度的区块信息
     *
     * @param dbPath       数据库
     * @param path         区块文件所在路径
     * @param from         区块高度编号起始值（包含）
     * @param to           区块高度编号结束值（包含）
     * @param withTotalSum 是否打印总额信息
     */
    public void saveAddressToLevelDB(String dbPath, String path, int from, int to, String withTotalSum) throws IOException {
        log.info("saveAddressToLevelDB({}, {}, {}, {}, {})", dbPath, path, from, to, withTotalSum);
        long bgn = System.currentTimeMillis();

        DB db = LevelDBUtils.openLevelDB(dbPath);

        if (!StringUtils.endsWith(path, File.separator)) {
            path = path + File.separator;
        }

        int total = 0; // 计算处理的文件总数
        for (int i = from; i <= to; i++) {
            File file = new File(path + i + ".json");
            saveOneAddressFileToLevelDB(db, file);
            total++;
        }
        long end = System.currentTimeMillis();

        log.info("扫描了{}个区块高度, {}", String.format("%,d", total), LogUtils.getElapse(bgn, end));

        if (StringUtils.equals(withTotalSum, "true")) {
            try {
                calculateSum(db, 0, to);
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        db.close();
        NotifyUtils.sendWechatMsg("", "完成" + path + ":" + from + "-》" + to + ", " + LogUtils.getElapse(bgn, end), kelvinOpenId);
    }

    /**
     * 扫描一个区块文件，更新levelDB中保存的addr:balance和addr:height信息
     *
     * @param db
     * @param file
     */
    public void saveOneAddressFileToLevelDB(DB db, File file) {
        // log.info("saveOneAddressFileToLevelDB({})", file.getName());
        long bgn = System.currentTimeMillis();
        Block block = null;
        try {
            String json = FileUtils.readFileToString(file, UTF8);
            List<Block> blocks = JsonUtils.parseArray(json, "blocks", Block.class);
            for (Block blk : blocks) {
                if (blk.isMainChain()) {
                    block = blk;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("错误的区块文件: " + file.getName(), ex);
        }

        List<Transaction> txs = block.getTx();

        // 处理所有交易
        long fee = block.getFee();
        long height = block.getHeight();
        String heightStr = String.valueOf(height);
        boolean coinbaseAppeared = false; // 标记一个区块应该只有一个coinbase交易
        long totalIn = 0L;
        long totalOut = 0L;

        for (Transaction tx : txs) {
            // 处理input
            boolean isMining = false; // 标记是否是挖矿交易
            for (Input input : tx.getInputs()) { // 循环所有的交易进行账号余额计算
                Output prevOut = input.getPrevOut();
                if (prevOut == null) { // 没有交易币的来源的就是挖矿收入
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
                    if (!StringUtils.equals(prevHeightStr, heightStr)) {
                        db.put(keyHeight.getBytes(), heightStr.getBytes()); // 更新最后高度
                    }
                } else {
                    // log.error("区块[{}]出现了无余额支付账户:{}", height, addr);
                    // throw new RuntimeException("出现了无余额的账户进行支付");
                    long newBal = 0 - val;
                    db.put(keyBal.getBytes(), String.valueOf(newBal).getBytes()); // 更新累加余额
                    if (!StringUtils.equals(prevHeightStr, heightStr)) {
                        db.put(keyHeight.getBytes(), heightStr.getBytes()); // 更新最后高度
                    }
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
                    if (!StringUtils.equals(prevHeightStr, heightStr)) {
                        db.put(keyHeight.getBytes(), String.valueOf(height).getBytes()); // 更新最后高度
                    }
                } else {
                    db.put(keyBal.getBytes(), String.valueOf(val).getBytes()); // 新增累加余额
                    db.put(keyHeight.getBytes(), heightStr.getBytes()); // 新增最后高度
                }
            }
        }
        if (totalIn != totalOut + fee) {
            log.error("区块[{}]存在非法交易数据: totalIn:{} 不等于 totalOut:{} + fee:{}, 交易数:{}, 差额:{}", height, totalIn, totalOut, fee, txs.size(), totalIn - totalOut - fee);
        }
        long end = System.currentTimeMillis();
        log.info("完成区块[{}], tx: {}, {}", height, txs.size(), LogUtils.getElapse(bgn, end));
    }

    /**
     * 为了加快速度，增加了区间处理，这样可以分片计算地址的余额，到最后再汇总，比如开一个进程算0-399999，然后再开一个进程算400000-499999，最后将余额值进行汇总即可
     *
     * @param baseDbPath       基础数据库数据文件所在路径，最终该库会因为合并发生更新
     * @param accumulateDbPath 增量数据库，该库不会发生变化
     * @throws IOException
     */
    public void mergeLevelDB(String baseDbPath, String accumulateDbPath) throws IOException {
        log.info("mergeLevelDB({}, {})", baseDbPath, accumulateDbPath);
        long bgn = System.currentTimeMillis();

        DB baseDB = LevelDBUtils.openLevelDB(baseDbPath);
        DB accumulateDB = LevelDBUtils.openLevelDB(accumulateDbPath);

        //读取当前snapshot，快照，读取期间数据的变更，不会反应出来
        Snapshot snapshot = accumulateDB.getSnapshot();
        //读选项
        ReadOptions readOptions = new ReadOptions();
        readOptions.fillCache(false);//遍历中swap出来的数据，不应该保存在memtable中。
        readOptions.snapshot(snapshot);//默认snapshot为当前。
        DBIterator iterator = accumulateDB.iterator(readOptions);

        long totalCnt = 0L;
        long addressCnt = 0L;
        WriteBatch writeBatch = baseDB.createWriteBatch();
        while (iterator.hasNext()) {
            totalCnt++;
            if (totalCnt % 100000 == 0) {
                baseDB.write(writeBatch);
                log.info("totalCnt:{}", String.format("%,d", totalCnt));
                writeBatch = baseDB.createWriteBatch();
            }
            Map.Entry<byte[], byte[]> item = iterator.next();
            String key = new String(item.getKey(), UTF8);
            String value = new String(item.getValue(), UTF8);//null,check.

            if (StringUtils.startsWith(key, KeyHeight)) { //是高度记录
                int height = Integer.parseInt(value);
                String addr = key.substring(2);
                String keyBal = KeyBalance + addr;
                long newBal = Long.parseLong(new String(accumulateDB.get(keyBal.getBytes())));
                byte[] baseBalBytes = baseDB.get(keyBal.getBytes());
                long baseBal = (baseBalBytes != null) ? Long.parseLong(new String(baseBalBytes)) : 0L;
                baseBal += newBal; // 算出合并后的值
                writeBatch.put(keyBal.getBytes(), String.valueOf(baseBal).getBytes());
                writeBatch.put((KeyHeight + addr).getBytes(), String.valueOf(height).getBytes());
                addressCnt++;
            }
        }
        baseDB.write(writeBatch);
        log.info("totalCnt:{}", String.format("%,d", totalCnt));
        writeBatch.close();
        iterator.close();//must be
        long end = System.currentTimeMillis();
        baseDB.close();
        accumulateDB.close();

        log.info("共合并了{}条数据记录，共扫描了{}条数据记录，{}", String.format("%,d", addressCnt), String.format("%,d", totalCnt), LogUtils.getElapse(bgn, end));
        NotifyUtils.sendWechatMsg("合并完成", "合并" + accumulateDbPath + "到" + baseDbPath + ", " + LogUtils.getElapse(bgn, end), kelvinOpenId);
    }

    /**
     * 计算总额
     *
     * @param db   统计数据库的路径
     * @param from 起始高度
     * @param to   结束高度
     * @return
     * @throws IOException
     */
    public long calculateSum(DB db, Integer from, Integer to) throws IOException {
        from = (from == null) ? 0 : from;
        to = (to == null) ? Integer.MAX_VALUE : to;
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
            if (totalCnt % 1000000 == 0) {
                log.info("totalCnt:{}", String.format("%,d", totalCnt));
            }
            Map.Entry<byte[], byte[]> item = iterator.next();
            String key = new String(item.getKey(), UTF8);
            String value = new String(item.getValue(), UTF8);//null,check.
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
        log.info("totalCnt:{}", String.format("%,d", totalCnt));
        iterator.close();//must be
        long end = System.currentTimeMillis();

        log.info("区块高度[{} - {}]，总币额为：{}比特币，共扫描到：{}个钱包地址， 共扫描了{}条数据记录，{}", from, to, toBTC(sum), String.format("%,d", addressCnt), String.format("%,d", totalCnt), LogUtils.getElapse(bgn, end));
        NotifyUtils.sendWechatMsg("计总完成", "完成统计" + from + "-》" + to + "计总:" + sum + ", " + LogUtils.getElapse(bgn, end), kelvinOpenId);
        return sum;
    }

    /**
     * 显示指定地址的余额信息
     *
     * @param dbPath levelDB数据库路径
     * @param addrs  地址列表
     * @throws IOException
     */
    public void showAddrsInfo(String dbPath, String... addrs) throws IOException {
        DB db = LevelDBUtils.openLevelDB(dbPath);
        for (String addr : addrs) {
            byte[] heightBytes = db.get((KeyHeight + addr).getBytes());
            boolean exists = heightBytes != null;

            if (exists) {
                byte[] balBytes = db.get((KeyBalance + addr).getBytes());
                String balStr = getString(balBytes, "");
                BigDecimal addrBal = StringUtils.isNotBlank(balStr) ? toBTC(Long.valueOf(balStr)) : new BigDecimal(0);
                log.info("==========={}===========", addr);
                log.info("高度: {}", new String(db.get((KeyHeight + addr).getBytes())));
                log.info("余额: {}", addrBal);
            } else {
                log.info("==========={}===========", addr);
                log.info("高度: {}", db.get((KeyHeight + addr).getBytes()));
                log.info("余额: {}", db.get((KeyBalance + addr).getBytes()));
            }
        }
        db.close();
    }

    /**
     * 将‘聪’转换成‘比特币’
     *
     * @param value
     * @return
     */
    private BigDecimal toBTC(long value) {
        return BigDecimal.valueOf(value / BITCOIN_2_CONG);
    }

    /**
     * 获得byte数组的对应字符串，可以指定默认值
     *
     * @param bytes
     * @param defaultValue
     * @return
     */
    private String getString(byte[] bytes, String defaultValue) {
        return bytes != null ? new String(bytes) : defaultValue;
    }

}
