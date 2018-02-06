package com.codewalnut.domain;

import javax.persistence.Column;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
//@Entity
public class Output implements BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private int id;
//    @Column
//    private int transactionId;
//    @Column
//    @JSONField(name = "tx_index")
//    private int txIndex;
//    @Column
//    private boolean spent;
//    @Column
//    private int type;
    @Column(length = 255)
    private String addr;
//    @Column
//    private long value;
//    @Column
//    private int n;
//    @Column
//    private String script;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
