package cn.veasion.aippt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * AiPptDemo
 *
 * @author veasion
 * @date 2024/7/12
 */
public class AiPptDemo {

    private static final String BASE_URL = "https://chatmee.cn/api";

    public static void main(String[] args) throws Exception {
        // 官网 https://docmee.cn
        // 开放平台 https://docmee.cn/open-platform/api

        // 填写你的API-KEY
        String apiKey = "{{ YOUR API KEY }}";

        // 第三方用户ID（数据隔离）
        String userId = "test";
        String subject = "AI未来的发展";

        // 创建 apiToken (有效期2小时，建议缓存到redis)
        String apiToken = createApiToken(apiKey, userId);
        System.out.println("apiToken: " + apiToken);

        // 生成大纲
        System.out.println("\n\n========== 正在生成大纲 ==========");
        String outline = generateOutline(apiToken, subject, null, null);

        // 生成大纲内容
        System.out.println("\n\n========== 正在生成大纲内容 ==========");
        String markdown = generateContent(apiToken, outline, null, null);

        // 随机一个模板
        System.out.println("\n\n========== 随机选择模板 ==========");
        String templateId = randomOneTemplateId(apiToken);
        System.out.println(templateId);

        // 生成PPT
        System.out.println("\n\n========== 正在生成PPT ==========");
        JSONObject pptInfo = generatePptx(apiToken, templateId, markdown, false);
        String pptId = pptInfo.getString("id");
        System.out.println("pptId: " + pptId);
        System.out.println("ppt主题：" + pptInfo.getString("subject"));
        System.out.println("ppt封面：" + pptInfo.getString("coverUrl") + "?token=" + apiToken);

        // 下载PPT到桌面
        System.out.println("\n\n========== 正在下载PPT ==========");
        String url = downloadPptx(apiToken, pptId);
        System.out.println("ppt链接：" + url);
        String savePath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + pptId + ".pptx";
        HttpUtils.download(url, new File(savePath));
        System.out.println("ppt下载完成，保存路径：" + savePath);
    }

    public static String createApiToken(String apiKey, String userId) {
        String url = BASE_URL + "/user/createApiToken";
        JSONObject body = new JSONObject();
        body.put("uid", userId);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("Api-Key", apiKey);
        HttpUtils.HttpResponse response = HttpUtils.request(httpRequest);
        if (response.getStatus() != 200) {
            throw new AiPptException("创建apiToken失败，httpStatus=" + response.getStatus());
        }
        JSONObject result = response.getResponseToJson();
        if (result.getIntValue("code") != 0) {
            throw new AiPptException("创建apiToken异常，" + result.getString("message"));
        }
        return result.getJSONObject("data").getString("token");
    }

    public static String generateOutline(String apiToken, String subject, String prompt, String dataUrl) {
        String url = BASE_URL + "/ppt/generateOutline";
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
            String text = json.getString("text");
            sb.append(text);
            // 打印输出
            System.out.print(text);
        });
        if (response.getStatus() != 200) {
            throw new AiPptException("生成大纲失败，httpStatus=" + response.getStatus());
        }
        if (response.getHeaders().getOrDefault("Content-Type", response.getHeaders().get("content-type")).contains("application/json")) {
            JSONObject result = response.getResponseToJson();
            throw new AiPptException("生成大纲失败：" + result.getString("message"));
        }
        return sb.toString();
    }

    public static String generateContent(String apiToken, String outlineMarkdown, String prompt, String dataUrl) {
        String url = BASE_URL + "/ppt/generateContent";
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
            String text = json.getString("text");
            sb.append(text);
            // 打印输出
            System.out.print(text);
        });
        if (response.getStatus() != 200) {
            throw new AiPptException("生成大纲内容失败，httpStatus=" + response.getStatus());
        }
        if (response.getHeaders().getOrDefault("Content-Type", response.getHeaders().get("content-type")).contains("application/json")) {
            JSONObject result = response.getResponseToJson();
            throw new AiPptException("生成大纲内容失败：" + result.getString("message"));
        }
        return sb.toString();
    }

    public static String randomOneTemplateId(String apiToken) {
        String url = BASE_URL + "/ppt/randomTemplates";
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
            throw new AiPptException("获取模板失败，httpStatus=" + response.getStatus());
        }
        JSONObject result = response.getResponseToJson();
        if (result.getIntValue("code") != 0) {
            throw new AiPptException("获取模板异常，" + result.getString("message"));
        }
        JSONArray data = result.getJSONArray("data");
        JSONObject template = data.getJSONObject(0);
        return template.getString("id");
    }

    public static JSONObject generatePptx(String apiToken, String templateId, String markdown, boolean pptxProperty) {
        String url = BASE_URL + "/ppt/generatePptx";
        JSONObject body = new JSONObject();
        body.put("templateId", templateId);
        body.put("outlineContentMarkdown", markdown);
        body.put("pptxProperty", pptxProperty);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("token", apiToken);
        HttpUtils.HttpResponse response = HttpUtils.request(httpRequest);
        if (response.getStatus() != 200) {
            throw new AiPptException("生成PPT失败，httpStatus=" + response.getStatus());
        }
        JSONObject result = response.getResponseToJson();
        if (result.getIntValue("code") != 0) {
            throw new AiPptException("生成PPT异常，" + result.getString("message"));
        }
        return result.getJSONObject("data").getJSONObject("pptInfo");
    }

    public static String downloadPptx(String apiToken, String id) {
        String url = BASE_URL + "/ppt/downloadPptx";
        JSONObject body = new JSONObject();
        body.put("id", id);
        HttpUtils.HttpRequest httpRequest = HttpUtils.HttpRequest.postJson(url);
        httpRequest.setBody(body.toJSONString());
        httpRequest.addHeaders("token", apiToken);
        HttpUtils.HttpResponse response = HttpUtils.request(httpRequest);
        if (response.getStatus() != 200) {
            throw new AiPptException("下载PPT失败，httpStatus=" + response.getStatus());
        }
        JSONObject result = response.getResponseToJson();
        if (result.getIntValue("code") != 0) {
            throw new AiPptException("下载PPT异常，" + result.getString("message"));
        }
        return result.getJSONObject("data").getString("fileUrl");
    }

}
