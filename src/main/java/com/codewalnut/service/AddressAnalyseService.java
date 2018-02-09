package com.codewalnut.service;

import com.carrotsearch.sizeof.RamUsageEstimator;
import com.codewalnut.domain.Address;
import com.codewalnut.domain.AddressAnalyseResult;
import com.codewalnut.domain.AddressRepository;
import com.saysth.commons.utils.LogUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    @Autowired
    private AddressRepository addressRepository;

    /**
     * 保存数据库
     *
     * @param addressses
     */
    public void batchSave(Set<Address> addressses) {
        log.info("Saving {} addresses...", addressses.size());

        // addressRepository.save(addressses); // 看了源码，对内存占用有要求，因为要保存返回值
        for (Address addr : addressses) {
            addressRepository.save(addr);
        }
    }

    public void save(Address address) {
        addressRepository.save(address);
    }

    public void saveAddressToDB(String path) {
        log.info("saveAddressToDB path {}", path);

        long bgn = System.currentTimeMillis();
        if (!StringUtils.endsWith(path, File.separator)) {
            path = path + File.separator;
        }
        File dir = new File(path);
        Collection<File> files = FileUtils.listFiles(dir, null, true);

        int total = 0;
        for (File file : files) {
            total += saveOneAddressFileToDB(file);
        }
        long end = System.currentTimeMillis();

        log.debug("saved {} rows, {}", total, LogUtils.getElapse(bgn, end));
    }

    public int saveOneAddressFileToDB(File file) {
        log.info("saveOneAddressFileToDB({})", file.getName());
        int count = 0;
        String[] heightTime = StringUtils.split(file.getName(), '-');
        Assert.isTrue(heightTime.length == 2, "Expect filename in height-timestamp format!!");
        int height = Integer.valueOf(heightTime[0]);
        long time = Long.valueOf(heightTime[0]);
        try {
            LineIterator lineIterator = FileUtils.lineIterator(file, "UTF-8");
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                Address addr = new Address();
                addr.setHeight(height);
                addr.setAddr(line);
                save(addr);
                count++;
            }
            return count;
        } catch (IOException ex) {
            log.error("Wrong file", ex);
            return count;
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
     * 将某目录下的地址json文件的内容去重复
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
