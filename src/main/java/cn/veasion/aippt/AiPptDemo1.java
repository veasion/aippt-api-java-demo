package cn.veasion.aippt;

import com.alibaba.fastjson.JSONObject;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * AiPptDemo1 流式生成 PPT
 *
 * @author veasion
 * @date 2024/7/12
 */
public class AiPptDemo1 {

    public static void main(String[] args) throws Exception {
        // 官网 https://docmee.cn
        // 开放平台 https://docmee.cn/open-platform/api

        // 填写你的API-KEY
        String apiKey = "YOUR API KEY";

        // 第三方用户ID（数据隔离）
        String uid = "test";
        String subject = "AI未来的发展";

        // 创建 api token (有效期2小时，建议缓存到redis，同一个 uid 创建时之前的 token 会在10秒内失效)
        String apiToken = Api.createApiToken(apiKey, uid, null);
        System.out.println("api token: " + apiToken);

        // 生成大纲
        System.out.println("\n\n========== 正在生成大纲 ==========");
        String outline = Api.generateOutline(apiToken, subject, null, null);

        // 生成大纲内容
        System.out.println("\n\n========== 正在生成大纲内容 ==========");
        String markdown = Api.generateContent(apiToken, outline, null, null);

        // 随机一个模板
        System.out.println("\n\n========== 随机选择模板 ==========");
        String templateId = Api.randomOneTemplateId(apiToken);
        System.out.println(templateId);

        // 生成PPT
        System.out.println("\n\n========== 正在生成PPT ==========");
        JSONObject pptInfo = Api.generatePptx(apiToken, templateId, markdown, false);
        String pptId = pptInfo.getString("id");
        System.out.println("pptId: " + pptId);
        System.out.println("ppt主题：" + pptInfo.getString("subject"));
        System.out.println("ppt封面：" + pptInfo.getString("coverUrl") + "?token=" + apiToken);

        // 下载PPT到桌面
        System.out.println("\n\n========== 正在下载PPT ==========");
        String url = Api.downloadPptx(apiToken, pptId);
        System.out.println("ppt链接：" + url);
        String savePath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + pptId + ".pptx";
        HttpUtils.download(url, new File(savePath));
        System.out.println("ppt下载完成，保存路径：" + savePath);
    }

}
