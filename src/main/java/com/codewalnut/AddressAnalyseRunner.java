package com.codewalnut;

import com.codewalnut.service.AddressAnalyseService;
import com.codewalnut.utils.LevelDBUtils;
import com.saysth.commons.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by Weway-RG001 on 2018-02-06 18:35.
 */
@Component
public class AddressAnalyseRunner implements ApplicationRunner {
    private static Logger log = LoggerFactory.getLogger(AddressAnalyseRunner.class);

    @Autowired
    private AddressAnalyseService service;

    /**
     * action: parseAddr; parseAddrPath, savePath
     * action: removeDuplicate; srcPath, targetPath
     * action: addrToLevelDb; dbPath, filePath, heightRange
     * action: getSummary; dbPath, heightRange
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 参数处理
        String action = getSingle(args, "action");
        Assert.isTrue(StringUtils.isNotBlank(action), "--action must be assigned!");

        if (StringUtils.equals(action, "addrToLevelDb")) {
            // 把区块.json文件的地址余额和最后高度信息放到levelDB
            String dbPath = getSingle(args, "dbPath", null);
            Assert.isTrue(StringUtils.isNotBlank(dbPath), "Argument --dbPath is required!");
            String filePath = getSingle(args, "filePath", null);
            Assert.isTrue(StringUtils.isNotBlank(filePath), "Argument --filePath is required!");
            String heightRange = getSingle(args, "heightRange", null);
            String withTotalSum = getSingle(args, "withTotalSum", "false");
            String[] ss = StringUtils.split(heightRange, '-');
            Assert.isTrue(ss.length == 2, "Invalid arguments: --heightRange, must in 'from-to'pattern!");
            int from = Integer.valueOf(ss[0]);
            int to = Integer.valueOf(ss[1]);
            service.saveAddressToLevelDB(dbPath, filePath, from, to, withTotalSum);

        } else if (StringUtils.equals(action, "getSummary")) {
            // 计算统计区间内的地址余额总数和个数
            String dbPath = getSingle(args, "dbPath", null);
            Assert.isTrue(StringUtils.isNotBlank(dbPath), "Argument --dbPath is required!");
            String heightRange = getSingle(args, "heightRange", null);
            String[] ss = StringUtils.split(heightRange, '-');
            Assert.isTrue(ss.length == 2, "Invalid arguments: --heightRange, must in 'from-to'pattern!");
            int from = Integer.valueOf(ss[0]);
            int to = Integer.valueOf(ss[1]);
            DB db = LevelDBUtils.openLevelDB(dbPath);
            service.calculateSum(db, from, to);
            db.close();

        } else if (StringUtils.equals(action, "mergeLevelDB")) {
            // 将后续库的内容整合进基础库
            String baseDbPath = getSingle(args, "baseDb", null);
            String accumulateDbPath = getSingle(args, "accumulateDb", null);
            Assert.isTrue(StringUtils.isNotBlank(baseDbPath), "Arugment --baseDb is required!");
            Assert.isTrue(StringUtils.isNotBlank(accumulateDbPath), "Arugment --accumulateDb is required!");
            service.mergeLevelDB(baseDbPath, accumulateDbPath);

        } else if (StringUtils.equals(action, "addrInfo")) {
            // 显示指定地址列表的余额和高度信息
            String[] addrs = StringUtils.split(getSingle(args, "addrs"), ',');
            Assert.isTrue(addrs.length > 0, "Argument --addrs is required!");
            String dbPath = getSingle(args, "dbPath", null);
            Assert.isTrue(StringUtils.isNotBlank(dbPath), "Argument --dbPath is required!");
            service.showAddrsInfo(dbPath, addrs);
        }
    }

    // 解析命令行参数
    private String getSingle(ApplicationArguments arguments, String name) {
        return getSingle(arguments, name, null);
    }

    // 解析命令行参数
    private String getSingle(ApplicationArguments arguments, String name, String defaultValue) {
        List<String> list = arguments.getOptionValues(name);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return defaultValue;
        }
    }

}
