package com.codewalnut.domain;

import com.saysth.commons.utils.json.JsonUtils;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
public interface BaseEntity {
    default String toJson() {
        return JsonUtils.toJson(this);
    }
}
