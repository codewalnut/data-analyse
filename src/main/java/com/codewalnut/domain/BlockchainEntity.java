package com.codewalnut.domain;

/**
 * Created by Weway-RG001 on 2018-02-09 17:02.
 */
public class BlockchainEntity implements BaseEntity {
    @Override
    public String toString() {
        return toJson();
    }
}
