package com.codewalnut.domain;

import java.util.List;

/**
 * 区块
 * Created by Weway-RG001 on 2018-02-05.
 */
//@Entity
public class Block implements BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private int id;
//    @Column(length = 64, unique = true)
//    private String hash;
//    @Column
//    private int ver;
//    @JSONField(name = "prev_block")
//    @Column(length = 64)
//    private String prevBlock;
//    @JSONField(name = "mrkl_root")
//    @Column(length = 64)
//    private String mrklRoot;
////    @Column
//    @Transient
//    private long time;
//    @Column
//    private Date datetime;
//    @Column
//    private long bits;
//    @Column
//    private long fee;
//    @Column
//    private long nonce;
//    @JSONField(name = "n_tx")
//    @Column
//    private int nTx;
//    @Column
//    private int size;
//    @JSONField(name = "block_index")
//    @Column
//    private long blockIndex;
//    @JSONField(name = "main_chain")
//    @Column
//    private boolean mainChain;
//    @Column
    private long height;
//    @JSONField(name = "received_time")
////    @Column
//    @Transient
//    private long receivedTime;
//    @Column
//    private Date receivedDateTime;
//    @JSONField(name = "relayed_by")
//    @Column(length = 64)
//    private String relayedBy;
//    @Transient
    private List<Transaction> tx;
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public String getHash() {
//        return hash;
//    }
//
//    public void setHash(String hash) {
//        this.hash = hash;
//    }
//
//    public int getVer() {
//        return ver;
//    }
//
//    public void setVer(int ver) {
//        this.ver = ver;
//    }
//
//    public String getPrevBlock() {
//        return prevBlock;
//    }
//
//    public void setPrevBlock(String prevBlock) {
//        this.prevBlock = prevBlock;
//    }
//
//    public String getMrklRoot() {
//        return mrklRoot;
//    }
//
//    public void setMrklRoot(String mrklRoot) {
//        this.mrklRoot = mrklRoot;
//    }
//
//    public long getTime() {
//        return time;
//    }
//
//    public void setTime(long time) {
//        this.time = time;
//    }
//
//    public long getBits() {
//        return bits;
//    }
//
//    public void setBits(long bits) {
//        this.bits = bits;
//    }
//
//    public long getFee() {
//        return fee;
//    }
//
//    public void setFee(long fee) {
//        this.fee = fee;
//    }
//
//    public long getNonce() {
//        return nonce;
//    }
//
//    public void setNonce(long nonce) {
//        this.nonce = nonce;
//    }
//
//    public int getnTx() {
//        return nTx;
//    }
//
//    public void setnTx(int nTx) {
//        this.nTx = nTx;
//    }
//
//    public int getSize() {
//        return size;
//    }
//
//    public void setSize(int size) {
//        this.size = size;
//    }
//
//    public long getBlockIndex() {
//        return blockIndex;
//    }
//
//    public void setBlockIndex(long blockIndex) {
//        this.blockIndex = blockIndex;
//    }
//
//    public boolean isMainChain() {
//        return mainChain;
//    }
//
//    public void setMainChain(boolean mainChain) {
//        this.mainChain = mainChain;
//    }
//
    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }
//
//    public long getReceivedTime() {
//        return receivedTime;
//    }
//
//    public void setReceivedTime(long receivedTime) {
//        this.receivedTime = receivedTime;
//    }
//
//    public String getRelayedBy() {
//        return relayedBy;
//    }
//
//    public void setRelayedBy(String relayedBy) {
//        this.relayedBy = relayedBy;
//    }
//
    public List<Transaction> getTx() {
        return tx;
    }

    public void setTx(List<Transaction> tx) {
        this.tx = tx;
    }
//
//    public Date getDatetime() {
//        return datetime;
//    }
//
//    public void setDatetime(Date datetime) {
//        this.datetime = datetime;
//    }
//
//    public Date getReceivedDateTime() {
//        return receivedDateTime;
//    }
//
//    public void setReceivedDateTime(Date receivedDateTime) {
//        this.receivedDateTime = receivedDateTime;
//    }
}
