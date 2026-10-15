package com.keytech.modules.ems.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Webhook服务实现类
 * @author yepeng
 * @since 2025.05.24
 */
@Slf4j
public class WebhookSendHelper {

    private WebhookSendHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 企业微信Webhook的URL前缀
     */
    private static final String WECHAT_WEBHOOK_URL_PREFIX = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=";

    /**
     * 格式化为Text消息
     * @param content 内容
     */
    public static String formatText(String content) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgtype", "text");
        JSONObject cotentObj = new JSONObject();
        cotentObj.put("content", content);
        jsonObject.put("text", cotentObj);
        return jsonObject.toJSONString();
    }

    /**
     * 格式化为markdown消息
     * @param content 内容
     */
    public static String formatMarkdown(String content) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgtype", "markdown");
        JSONObject cotentObj = new JSONObject();
        cotentObj.put("content", content);
        jsonObject.put("markdown", cotentObj);
        return jsonObject.toJSONString();
    }

    /**
     * 发送企业微信Webhook消息
     * @param webhookKey Webhook的key
     * @param content 消息内容
     */
    public static boolean sendMsg(String webhookKey, String content) {
        try {
            String webhookUrl = WECHAT_WEBHOOK_URL_PREFIX + webhookKey;
            HttpResponse response = HttpRequest.post(webhookUrl).body(content).header("Content-Type", "application/json").execute();
            if (response.isOk()) {
                JSONObject jsonObject = JSONObject.parseObject(response.body());
                if (jsonObject.getInteger("errcode") != 0) {
                    log.warn("推送企业微信Webhook消息失败: {}", jsonObject.getString("errmsg"));
                    return false;
                }else {
                    log.info("发送企业微信Webhook消息成功");
                    return true;
                }
            } else {
                log.warn("发送企业微信Webhook消息失败: {}", response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("发送企业微信Webhook消息异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送markdown消息
     * @param webhookKey Webhook的key
     * @param content 消息内容
     */
    public static boolean sendMarkdownMsg(String webhookKey, String content) {
        return sendMsg(webhookKey, formatMarkdown(content));
    }

    /**
     * 发送markdown消息(只要有一个成功就算成功)
     * @param webhookKeys Webhook的keys
     * @param content 消息内容
     */
    public static boolean sendMarkdownMsg(List<String> webhookKeys, String content) {
        boolean hasSuccess = false;
        for (String webhookKey : webhookKeys) {
            boolean success = sendMarkdownMsg(webhookKey, content);
            hasSuccess = hasSuccess || success;
        }
        return hasSuccess;
    }

}