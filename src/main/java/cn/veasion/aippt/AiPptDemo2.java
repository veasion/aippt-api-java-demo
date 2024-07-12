package cn.veasion.aippt;

import com.alibaba.fastjson.JSONObject;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Objects;

/**
 * AiPptDemo2 直接生成 PPT
 *
 * @author veasion
 * @date 2024/7/12
 */
public class AiPptDemo2 {

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

        // 通过主题直接生成PPT
        System.out.println("\n正在生成PPT...\n");
        JSONObject pptInfo = directGeneratePptx(apiToken, true, null, subject, null, null, false);

        String pptId = pptInfo.getString("id");
        String fileUrl = pptInfo.getString("fileUrl");
        System.out.println("\n\n===============");
        System.out.println("pptId: " + pptId);
        System.out.println("ppt主题：" + pptInfo.getString("subject"));
        System.out.println("ppt封面：" + pptInfo.getString("coverUrl") + "?token=" + apiToken);
        System.out.println("ppt链接：" + fileUrl);

        // 下载PPT到桌面
        String savePath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + pptId + ".pptx";
        HttpUtils.download(fileUrl, new File(savePath));
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

    public static JSONObject directGeneratePptx(String apiToken, boolean stream, String templateId, String subject, String prompt, String dataUrl, boolean pptxProperty) {
        String url = BASE_URL + "/ppt/directGeneratePptx";
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
                if (Objects.equals(json.getInteger("status"), 4) && json.containsKey("result")) {
                    pptInfo[0] = json.getJSONObject("result");
                }
                String text = json.getString("text");
                // 打印输出
                System.out.print(text);
            });
            if (response.getStatus() != 200) {
                throw new AiPptException("生成PPT失败，httpStatus=" + response.getStatus());
            }
            if (response.getHeaders().getOrDefault("Content-Type", response.getHeaders().get("content-type")).contains("application/json")) {
                JSONObject result = response.getResponseToJson();
                throw new AiPptException("生成PPT失败：" + result.getString("message"));
            }
            return pptInfo[0];
        } else {
            // 非流式生成
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
    }

}
