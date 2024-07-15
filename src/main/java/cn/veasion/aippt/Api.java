package cn.veasion.aippt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Objects;

/**
 * Api
 *
 * @author luozhuowei
 * @date 2024/7/15
 */
public class Api {

    public static final String BASE_URL = "https://chatmee.cn";

    public static String createApiToken(String apiKey, String uid, Integer limit) {
        String url = BASE_URL + "/api/user/createApiToken";
        JSONObject body = new JSONObject();
        body.put("uid", uid);
        body.put("limit", limit);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("Api-Key", apiKey);
        HttpUtils.HttpResponse response = HttpUtils.request(httpRequest);
        if (response.getStatus() != 200) {
            throw new RuntimeException("创建apiToken失败，httpStatus=" + response.getStatus());
        }
        JSONObject result = response.getResponseToJson();
        if (result.getIntValue("code") != 0) {
            throw new RuntimeException("创建apiToken异常，" + result.getString("message"));
        }
        return result.getJSONObject("data").getString("token");
    }

    public static String generateOutline(String apiToken, String subject, String prompt, String dataUrl) {
        String url = BASE_URL + "/api/ppt/generateOutline";
        JSONObject body = new JSONObject();
        body.put("subject", subject);
        body.put("prompt", prompt);
        body.put("dataUrl", dataUrl);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("token", apiToken);
        StringBuilder sb = new StringBuilder();
        HttpUtils.HttpResponse response = HttpUtils.requestWithEventStream(httpRequest, data -> {
            if (data == null || data.isEmpty()) {
                return;
            }
            JSONObject json = JSONObject.parseObject(data);
            if (Objects.equals(json.getInteger("status"), -1)) {
                throw new RuntimeException(json.getString("error"));
            }
            String text = json.getString("text");
            sb.append(text);
            // 打印输出
            System.out.print(text);
        });
        if (response.getStatus() != 200) {
            throw new RuntimeException("生成大纲失败，httpStatus=" + response.getStatus());
        }
        if (response.getHeaders().getOrDefault("Content-Type", response.getHeaders().get("content-type")).contains("application/json")) {
            JSONObject result = response.getResponseToJson();
            throw new RuntimeException("生成大纲失败：" + result.getString("message"));
        }
        return sb.toString();
    }

    public static String generateContent(String apiToken, String outlineMarkdown, String prompt, String dataUrl) {
        String url = BASE_URL + "/api/ppt/generateContent";
        JSONObject body = new JSONObject();
        body.put("outlineMarkdown", outlineMarkdown);
        body.put("prompt", prompt);
        body.put("dataUrl", dataUrl);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("token", apiToken);
        StringBuilder sb = new StringBuilder();
        HttpUtils.HttpResponse response = HttpUtils.requestWithEventStream(httpRequest, data -> {
            if (data == null || data.isEmpty()) {
                return;
            }
            JSONObject json = JSONObject.parseObject(data);
            if (Objects.equals(json.getInteger("status"), -1)) {
                throw new RuntimeException(json.getString("error"));
            }
            String text = json.getString("text");
            sb.append(text);
            // 打印输出
            System.out.print(text);
        });
        if (response.getStatus() != 200) {
            throw new RuntimeException("生成大纲内容失败，httpStatus=" + response.getStatus());
        }
        if (response.getHeaders().getOrDefault("Content-Type", response.getHeaders().get("content-type")).contains("application/json")) {
            JSONObject result = response.getResponseToJson();
            throw new RuntimeException("生成大纲内容失败：" + result.getString("message"));
        }
        return sb.toString();
    }

    public static String randomOneTemplateId(String apiToken) {
        String url = BASE_URL + "/api/ppt/randomTemplates";
        JSONObject body = new JSONObject();
        body.put("size", 1);
        JSONObject filters = new JSONObject();
        filters.put("type", 1);
        body.put("filters", filters);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("token", apiToken);
        HttpUtils.HttpResponse response = HttpUtils.request(httpRequest);
        if (response.getStatus() != 200) {
            throw new RuntimeException("获取模板失败，httpStatus=" + response.getStatus());
        }
        JSONObject result = response.getResponseToJson();
        if (result.getIntValue("code") != 0) {
            throw new RuntimeException("获取模板异常，" + result.getString("message"));
        }
        JSONArray data = result.getJSONArray("data");
        JSONObject template = data.getJSONObject(0);
        return template.getString("id");
    }

    public static JSONObject generatePptx(String apiToken, String templateId, String markdown, boolean pptxProperty) {
        String url = BASE_URL + "/api/ppt/generatePptx";
        JSONObject body = new JSONObject();
        body.put("templateId", templateId);
        body.put("outlineContentMarkdown", markdown);
        body.put("pptxProperty", pptxProperty);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("token", apiToken);
        HttpUtils.HttpResponse response = HttpUtils.request(httpRequest);
        if (response.getStatus() != 200) {
            throw new RuntimeException("生成PPT失败，httpStatus=" + response.getStatus());
        }
        JSONObject result = response.getResponseToJson();
        if (result.getIntValue("code") != 0) {
            throw new RuntimeException("生成PPT异常，" + result.getString("message"));
        }
        return result.getJSONObject("data").getJSONObject("pptInfo");
    }

    public static String downloadPptx(String apiToken, String id) {
        String url = BASE_URL + "/api/ppt/downloadPptx";
        JSONObject body = new JSONObject();
        body.put("id", id);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("token", apiToken);
        HttpUtils.HttpResponse response = HttpUtils.request(httpRequest);
        if (response.getStatus() != 200) {
            throw new RuntimeException("下载PPT失败，httpStatus=" + response.getStatus());
        }
        JSONObject result = response.getResponseToJson();
        if (result.getIntValue("code") != 0) {
            throw new RuntimeException("下载PPT异常，" + result.getString("message"));
        }
        return result.getJSONObject("data").getString("fileUrl");
    }

    public static JSONObject directGeneratePptx(String apiToken, boolean stream, String templateId, String subject, String prompt, String dataUrl, boolean pptxProperty) {
        String url = BASE_URL + "/api/ppt/directGeneratePptx";
        JSONObject body = new JSONObject();
        body.put("stream", stream);
        body.put("templateId", templateId);
        body.put("subject", subject);
        body.put("prompt", prompt);
        body.put("dataUrl", dataUrl);
        body.put("pptxProperty", pptxProperty);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("token", apiToken);
        if (stream) {
            // 流式生成
            JSONObject[] pptInfo = new JSONObject[1];
            HttpUtils.HttpResponse response = HttpUtils.requestWithEventStream(httpRequest, data -> {
                if (data == null || data.isEmpty()) {
                    return;
                }
                JSONObject json = JSONObject.parseObject(data);
                if (Objects.equals(json.getInteger("status"), -1)) {
                    throw new RuntimeException(json.getString("error"));
                }
                if (Objects.equals(json.getInteger("status"), 4) && json.containsKey("result")) {
                    pptInfo[0] = json.getJSONObject("result");
                }
                String text = json.getString("text");
                // 打印输出
                System.out.print(text);
            });
            if (response.getStatus() != 200) {
                throw new RuntimeException("生成PPT失败，httpStatus=" + response.getStatus());
            }
            if (response.getHeaders().getOrDefault("Content-Type", response.getHeaders().get("content-type")).contains("application/json")) {
                JSONObject result = response.getResponseToJson();
                throw new RuntimeException("生成PPT失败：" + result.getString("message"));
            }
            return pptInfo[0];
        } else {
            // 非流式生成
            HttpUtils.HttpResponse response = HttpUtils.request(httpRequest);
            if (response.getStatus() != 200) {
                throw new RuntimeException("生成PPT失败，httpStatus=" + response.getStatus());
            }
            JSONObject result = response.getResponseToJson();
            if (result.getIntValue("code") != 0) {
                throw new RuntimeException("生成PPT异常，" + result.getString("message"));
            }
            return result.getJSONObject("data").getJSONObject("pptInfo");
        }
    }

}
