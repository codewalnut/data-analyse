package com.codewalnut.domain;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
//@Entity
public class Input implements BaseEntity {
	//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private int id;
//    @Column
//    private int transactionId;
//    @Column
//    private long sequence;
//    @Column(length = 1024)
//    private String witness;
//    @Transient
	@JSONField(name = "prev_out")
	private Output prevOut;
//    @Column(length = 1024)
//    private String script;


	public Output getPrevOut() {
		return prevOut;
	}

	public void setPrevOut(Output prevOut) {
		this.prevOut = prevOut;
	}
}
