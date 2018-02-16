package com.codewalnut.domain;

import java.util.List;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
//@Entity
public class Transaction implements BaseEntity {
	//	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
//	private int id;
//	@Column
//	private int blockId;
//    @Column
//    @JSONField(name = "lock_time")
//    private int lockTime;
//    @Column
//    private int ver;
//    @Column
//    private int size;
//	@Transient
	private List<Input> inputs;
	//    @Column
//    private int weight;
//	@Column
//	private long time;
//    @Column
//    @JSONField(name = "tx_index")
//    private int txIndex;
//    @Column
//    @JSONField(name = "vin_sz")
//    private int vinSz;
//    @Column
//    private String hash;
//    @Column
//    @JSONField(name = "vout_sz")
//    private int voutSz;
//    @Column
//    @JSONField(name = "relayed_by")
//    private String relayedBy;
//	@Transient
	private List<Output> out;

	//	public int getId() {
//		return id;
//	}
//
//	public void setId(int id) {
//		this.id = id;
//	}
//
//	public int getBlockId() {
//		return blockId;
//	}
//
//	public void setBlockId(int blockId) {
//		this.blockId = blockId;
//	}
//

//	public int getTxIndex() {
//		return txIndex;
//	}
//
//	public void setTxIndex(int txIndex) {
//		this.txIndex = txIndex;
//	}

	public List<Input> getInputs() {
		return inputs;
	}

	public void setInputs(List<Input> inputs) {
		this.inputs = inputs;
	}

	public List<Output> getOut() {
		return out;
	}

	public void setOut(List<Output> out) {
		this.out = out;
	}
//
//	public long getTime() {
//		return time;
//	}
//
//	public void setTime(long time) {
//		this.time = time;
//	}
}
