package com.codewalnut.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
//@Service
public class BlockService {
//    private Logger log = LoggerFactory.getLogger(BlockService.class);

//    @Autowired
//    private BlockRepository blockRepository;
//    //    @Autowired
////    private TransactionRepository transactionRepository;
////    @Autowired
////    private InputRepository inputRepository;
////    @Autowired
////    private OutputRepository outputRepository;
////    @Autowired
////    private PrevOutRepository prevOutRepository;
//    @Autowired
//    private FileTaskRepository fileTaskRepository;
//    @Autowired
//    private AddressRepository addressRepository;
//
//    private Set<Address> addressSet = new HashSet<>();
//
//    @Transactional(rollbackFor = Exception.class)
//    public void save(Block block) {
//        block.setDatetime(new Date(block.getTime() * 1000l));
//        block.setReceivedDateTime(new Date(block.getReceivedTime() * 1000l));
//        blockRepository.save(block);
//        log.info("区块:{} 包含 {}个交易, 区块时间: {}", block.getHeight(), block.getTx().size(), DateUtils.formatDatetime(block.getDatetime()));
//        for (Transaction transaction : block.getTx()) {
//            for (Input input : transaction.getInputs()) {
//                PrevOut prevOut = input.getPrevOut();
//                if (prevOut != null) {
//                    String addr = prevOut.getAddr();
//                    if (StrUtils.isNotBlank(addr)) {
//                        Address address = new Address();
//                        address.setAddr(addr);
////                        address.setHeight(block.getHeight());
////                        address.setLastIn(new Date(transaction.getTime() * 1000l));
//                        addressSet.add(address);
//                    }
//                }
//            }
//
//            List<Output> outputs = transaction.getOut();
//            for (Output output : outputs) {
//                String addr = output.getAddr();
//                if (StrUtils.isNotBlank(addr)) {
//                    Address address = new Address();
//                    address.setAddr(addr);
////                    address.setHeight(block.getHeight());
////                    address.setLastOut(new Date(transaction.getTime() * 1000l));
//                    addressSet.add(address);
//                }
//            }
//        }
//        batchSave(addressSet);
//        addressSet.clear();
//    }
//
////    public void save(Transaction transaction) {
////        transactionRepository.save(transaction);
////
////        int transactionId = transaction.getId();
////        for (Input input : transaction.getInputs()) {
////            PrevOut prevOut = input.getPrevOut();
////            input.setTransactionId(transactionId);
////            inputRepository.save(input);
////            int inputId = input.getId();
////
////            if (prevOut != null) {
////                prevOut.setTransactionId(transactionId);
////                prevOut.setInputId(inputId);
////                prevOutRepository.save(prevOut);
////            }
////        }
////        for (Output output : transaction.getOut()) {
////            output.setTransactionId(transaction.getId());
////            outputRepository.save(output);
////        }
////    }
//
//    public void save(FileTask fileTask) {
//        if (fileTask.getCtime() == null) {
//            fileTask.setCtime(new Date());
//        }
//        fileTaskRepository.save(fileTask);
//        if (fileTask != null) {
//            unhandleFileTask(fileTask);
//        }
//    }
//
//    public void save(Address address) {
//        addressRepository.save(address);
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    public void batchSave(Set<Address> addressses) {
//        log.info("Saving {} addresses...", addressses.size());
//        addressRepository.save(addressses);
////        Iterator<Address> addressIterable = addressses.iterator();
////        while (addressIterable.hasNext()) {
////            boolean needSave = false;
////            Address newAddress = addressIterable.next();
////            Address dbAddress = addressRepository.findOne(newAddress.getAddr());
////            if (dbAddress == null) {
////                dbAddress = newAddress;
////                needSave = true;
////            } else {
////                long dbHeight = dbAddress.getHeight() != null ? dbAddress.getHeight().longValue() : 0;
////                long newHeight = newAddress.getHeight() != null ? newAddress.getHeight().longValue() : 0;
////                if (dbHeight < newHeight) {
////                    dbAddress.setHeight(newHeight);
////                    needSave = true;
////                }
////                long dbLastIn = dbAddress.getLastIn() != null ? dbAddress.getLastIn().getTime() : 0;
////                long dbLastOut = dbAddress.getLastOut() != null ? dbAddress.getLastOut().getTime() : 0;
////                long newLastIn = newAddress.getLastIn() != null ? newAddress.getLastIn().getTime() : 0;
////                long newLastOut = newAddress.getLastOut() != null ? newAddress.getLastOut().getTime() : 0;
////
////                if (dbLastIn < newLastIn) {
////                    dbAddress.setLastIn(newAddress.getLastIn());
////                    needSave = true;
////                }
////                if (dbLastOut < newLastOut) {
////                    dbAddress.setLastOut(newAddress.getLastOut());
////                    needSave = true;
////                }
////            }
////
////            if (needSave) {
////                addressRepository.save(dbAddress);
////            }
////        }
//    }
//
//    public void handleFileTask(FileTask fileTask) {
//        fileTask.setHtime(new Date());
//        fileTaskRepository.save(fileTask);
//
//        File file = new File("D:/bitcoin_data/" + fileTask.getHeight() + ".json");
//        Assert.isTrue(file.exists() && file.canRead() && file.length() > 0, "file not exists");
//        if (!readFromFile(fileTask, file)) {
//            unhandleFileTask(fileTask);
//        } else {
//            finishFileTask(fileTask);
//        }
//    }
//
//    public void unhandleFileTask(FileTask fileTask) {
//        fileTask.setHtime(null);
//        fileTaskRepository.save(fileTask);
//    }
//
//    public void finishFileTask(FileTask fileTask) {
//        fileTask.setFtime(new Date());
//        fileTaskRepository.save(fileTask);
//    }
//
//    public FileTask getMaxFileTask() {
//        return fileTaskRepository.getMaxFileTask();
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    private boolean readFromFile(FileTask fileTask, File file) {
//        Assert.isTrue(file.exists() && file.canRead() && file.length() > 0, "file not exists");
//        try {
//            String json = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
//            List<Block> blocks = JsonUtils.parseArray(json, "blocks", Block.class);
//            if (blocks.size() > 0) {
//                Block block = blocks.get(0);
//                save(block);
//            }
//            return true;
//        } catch (Exception e) {
//            log.error("Failed to read file: " + file.getName(), e);
//        }
//        return false;
//    }

}
