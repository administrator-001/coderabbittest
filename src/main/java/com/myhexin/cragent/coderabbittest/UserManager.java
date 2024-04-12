package com.myhexin.cragent.coderabbittest;

import com.alibaba.fastjson.JSON;

import java.util.Optional;

public class UserManager {

    /**
     * 从客户信息 json 中获取身份证号
     * {
     *     "userInfo": {
     *         "idCard": "xxx"
     *     }
     * }
     *
     * @param json 可能为 {@code null}
     * @return 身份证号
     */
    private String getIdCard(String json) {
        return Optional.ofNullable(json).map(JSON::parseObject).map(o -> o.getJSONObject("userInfo")).map(o -> o.getString("idCard")).orElse(null);
    }
}
