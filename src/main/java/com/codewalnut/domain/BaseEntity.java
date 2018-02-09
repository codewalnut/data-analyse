package com.codewalnut.domain;

import com.saysth.commons.utils.json.JsonUtils;

import java.io.Serializable;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
public interface BaseEntity extends Serializable {
    default String toJson() {
        return JsonUtils.toJson(this);
    }
}
