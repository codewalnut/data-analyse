package com.codewalnut.service;

import com.codewalnut.domain.Address;
import com.codewalnut.domain.AddressRepository;
import com.codewalnut.domain.FileTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Set;

/**
 * Created by Weway-RG001 on 2018-02-06 22:34.
 */
@Service
public class AddressAnalyseService {
    private Logger log = LoggerFactory.getLogger(AddressAnalyseService.class);

    @Autowired
    private AddressRepository addressRepository;

    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Set<Address> addressses) {
        log.info("Saving {} addresses...", addressses.size());
        addressRepository.save(addressses);
    }

    public boolean handleOneHeight(FileTask fileTask) {
        File file = new File("D:/bitcoin_data/" + fileTask.getHeight() + ".json");
        Assert.isTrue(file.exists() && file.canRead() && file.length() > 0, "file not exists");

        return false;
    }

}
