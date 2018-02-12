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
     * action: saveBlockAddr; addrPath
     * action: addrToLevelDb; dbPath, filePath
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 参数处理
        String action = getSingle(args, "action");
        Assert.isTrue(StringUtils.isNotBlank(action), "--action must be assigned!");

        if (StringUtils.equals(action, "parseAddr")) { // 处理解析地址
            String folderPath = getSingle(args, "parseAddrPath");
            String savePath = getSingle(args, "savePath", null);
            Assert.isTrue(StringUtils.isNotBlank(folderPath), "parseAddr mast have argument: --parseAddrPath");
            service.handleOneFolder(folderPath, savePath);
        } else if (StringUtils.equals(action, "removeDuplicate")) {
            String srcPath = getSingle(args, "srcPath", null);
            String targetPath = getSingle(args, "targetPath", null);
            Assert.isTrue(StringUtils.isNotBlank(srcPath), "parseAddr mast have argument: --srcPath");
            service.removeDuplicateAddrForFolder(srcPath, targetPath);
        } else if (StringUtils.equals(action, "saveBlockAddr")) {
            String folderPath = getSingle(args, "addrPath", null);
            service.saveAddressToDB(folderPath);
        } else if (StringUtils.equals(action, "addrToLevelDb")) {
            String dbPath = getSingle(args, "dbPath", null);
            String filePath = getSingle(args, "filePath", null);
            DB db = LevelDBUtils.openLevelDB(dbPath);
            service.saveAddressToLevelDB(db, filePath);
            db.close();
        }
    }

    //    private ThreadPoolTaskExecutor getAsyncExecutor(int poolSize) {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(poolSize); // 线程池最小
//        executor.setMaxPoolSize(poolSize); // 线程池最大
//        executor.setQueueCapacity(200000); // 线程等待队列长度
//        executor.setKeepAliveSeconds(30); // 空闲线程释放等待时间
//        executor.setAwaitTerminationSeconds(10);
//        executor.setWaitForTasksToCompleteOnShutdown(true);
////        executor.setDaemon(true);
//        executor.initialize();
//        return executor;
//    }
//

    private String getSingle(ApplicationArguments arguments, String name) {
        return getSingle(arguments, name, null);
    }

    // 读取唯一的一个还这么麻烦
    private String getSingle(ApplicationArguments arguments, String name, String defaultValue) {
        List<String> list = arguments.getOptionValues(name);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return defaultValue;
        }
    }

}
