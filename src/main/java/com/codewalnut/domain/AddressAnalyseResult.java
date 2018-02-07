package com.codewalnut.domain;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Weway-RG001 on 2018-02-07 14:50.
 */
public class AddressAnalyseResult implements Serializable {
    private boolean success;
    private int height;
    private long time;
    private Set<String> addrSet;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Set<String> getAddrSet() {
        return addrSet;
    }

    public void setAddrSet(Set<String> addrSet) {
        this.addrSet = addrSet;
    }

}
