package com.keytech.modules.lims.util;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.keytech.modules.lims.dto.LimsResultDto;

import java.util.Map;

/**
 * 数据推送
 * 平台推送LIMS
 */
public class LimsPush {

    /**
     * 超时
     */
    public final static int TIMEOUT = 20000;
    public final static String HEADER_PASSWORD = "password";

    /**
     * POST请求传入header，json url等
     */
    public static LimsResultDto sendPost(String url, String jsonStr, Map<String, String> headers) throws StatefulException {

        LimsResultDto limsResult = new LimsResultDto();

        HttpRequest request = HttpRequest.post(url).body(jsonStr, "application/json").timeout(TIMEOUT);
        if (headers != null) {
            headers.forEach(request::header);
        }

        try (HttpResponse httpResponse = request.execute()) {
            int status = httpResponse.getStatus();
            String result = httpResponse.body();
            if(status == 200) {
                if (StrUtil.isNotBlank(result)) {
                    limsResult = JSONUtil.toBean(result, LimsResultDto.class);
                } else {
                    throw new StatefulException("post请求status状态码：{}", status);
                }
            }else{
                throw new StatefulException("post请求status状态码：{},token({})", status,result);
            }

        } catch (Exception e) {
            throw new StatefulException("请求接口sendPostHasToken异常：url({}),token({}),jsonStr({})", url, jsonStr);
        }
        return limsResult;
    }

}
