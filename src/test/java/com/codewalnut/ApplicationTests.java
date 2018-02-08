package com.codewalnut;

import com.codewalnut.service.BlockService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {
    private Logger log = LoggerFactory.getLogger(ApplicationTests.class);

    @Autowired
    private BlockService blockService;

    @Test
    public void contextLoads() {
    }

//    @Test
//    public void handleOneFile() {
//        while (true) {
//            long bgn = System.currentTimeMillis();
//            FileTask fileTask = blockService.getMaxFileTask();
//
//            if (fileTask == null) {
//                ThreadUtils.sleep(1000 * 5);
//            }
//            blockService.handleFileTask(fileTask);
//            long end = System.currentTimeMillis();
//            String gap = LogUtils.getElapse(bgn, end);
//            log.info("Import height:{} into DB {}", fileTask.getHeight(), gap);
//        }
//    }

//    @Test
//    public void batchFileTask() {
//        for (int i = 507739; i > 506459; i--) {
//            FileTask fileTask = new FileTask();
//            fileTask.setHeight(i);
//            blockService.save(fileTask);
//        }
//    }

}
