package com.codewalnut.service;

import com.carrotsearch.sizeof.RamUsageEstimator;
import com.codewalnut.domain.AddressAnalyseResult;
import com.saysth.commons.utils.LogUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
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

//    @Autowired
//    private AddressRepository addressRepository;

    /**
     * 保存数据库
     *
     * @param addressses
     */
//    @Transactional(rollbackFor = Exception.class)
//    public void batchSave(Set<Address> addressses) {
//        log.info("Saving {} addresses...", addressses.size());
//        addressRepository.save(addressses);
//    }

    /**
     * 抽取需要的内容
     *
     * @param s
     * @param searchStr
     * @param startPos
     * @return
     */
    private String getFirstFromJson(String s, String searchStr, int startPos) {
        int start = StringUtils.indexOf(s, searchStr, startPos) + searchStr.length();
        int end = StringUtils.indexOf(s, ",", start);
        return StringUtils.substring(s, start, end);
    }

    /**
     * 处理一个区块文件
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
            s = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
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
     * 处理一个目录下的区块文件
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

}
