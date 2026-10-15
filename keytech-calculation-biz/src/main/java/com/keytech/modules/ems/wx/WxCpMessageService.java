package com.keytech.modules.ems.wx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * 微信企业号消息服务
 * @author yepeng
 * @since 2025.05.23
 */
@Service
@RequiredArgsConstructor
public class WxCpMessageService {
    private final WxCpTokenService tokenService;
    private final WxCpProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送文本消息
     * @param toUsers 用户ID列表
     * @param toPartys 部门ID列表
     * @param toTags 标签ID列表
     * @param content 消息内容
     */
    public String sendTextMessage(List<String> toUsers, List<String> toPartys, List<String> toTags, String content) {
        String token = tokenService.getToken();
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;

        ObjectNode body = objectMapper.createObjectNode();
        if (toUsers != null && !toUsers.isEmpty()) {
            body.put("touser", String.join("|", toUsers));
        }
        if (toPartys != null && !toPartys.isEmpty()) {
            body.put("toparty", String.join("|", toPartys));
        }
        if (toTags != null && !toTags.isEmpty()) {
            body.put("totag", String.join("|", toTags));
        }

        body.put("msgtype", "text");
        body.put("agentid", properties.getAgentId());
        body.putObject("text").put("content", content);
        body.put("safe", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        return restTemplate.postForObject(url, request, String.class);
    }

}
