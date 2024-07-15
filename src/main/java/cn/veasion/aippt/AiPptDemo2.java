package cn.veasion.aippt;

import com.alibaba.fastjson.JSONObject;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * AiPptDemo2 直接生成 PPT
 *
 * @author veasion
 * @date 2024/7/12
 */
public class AiPptDemo2 {

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
        System.out.println("apiToken: " + apiToken);

        // 通过主题直接生成PPT
        System.out.println("\n正在生成PPT...\n");
        JSONObject pptInfo = Api.directGeneratePptx(apiToken, true, null, subject, null, null, false);

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

}
