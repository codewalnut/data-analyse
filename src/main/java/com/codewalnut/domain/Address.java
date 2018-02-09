package com.codewalnut.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
@Entity
public class Address extends BlockchainEntity {
    @Id
    private String addr;
    @Column
    private int height;
    @Column
    private int nTx;
    @Column
    private long totalReceived;
    @Column
    private long totalSent;
    @Column
    private long balance;
    @Column
    private Date lastIn;
    @Column
    private Date lastOut;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getnTx() {
        return nTx;
    }

    public void setnTx(int nTx) {
        this.nTx = nTx;
    }

    public long getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(long totalReceived) {
        this.totalReceived = totalReceived;
    }

    public long getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(long totalSent) {
        this.totalSent = totalSent;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public Date getLastIn() {
        return lastIn;
    }

    public void setLastIn(Date lastIn) {
        this.lastIn = lastIn;
    }

    public Date getLastOut() {
        return lastOut;
    }

    public void setLastOut(Date lastOut) {
        this.lastOut = lastOut;
    }

}
