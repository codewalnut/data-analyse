package com.codewalnut;

import com.codewalnut.service.AddressAnalyseService;
import com.saysth.commons.utils.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
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
     * heightRange
     * threadPoolSize
     * savePath
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 参数处理
        String action = getSingle(args, "action", FileUtils.getUserDirectoryPath() + File.separatorChar + "bitcoin_data" + File.separatorChar);

        if (StringUtils.equals(action, "parseAddr")) { // 处理解析地址
            String folderPath = getSingle(args, "parseAddrPath", null);
            String savePath = getSingle(args, "savePath", null);
            Assert.isTrue(StringUtils.isNotBlank(folderPath), "parseAddr mast have argument: --parseAddrPath");
            service.handleOneFolder(folderPath, savePath);
        } else if (StringUtils.equals(action, "something else")) {
            // to be implement
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
