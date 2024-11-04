package cn.veasion.aippt;

import com.alibaba.fastjson.JSONObject;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Map;

/**
 * 异步流式生成 PPT
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
        // 文档文件，支持 word/excel/ppt/md/txt/pdf 等类型
        File file = new File("README.md");

        // 创建 api token (有效期2小时，建议缓存到redis，同一个 uid 创建时之前的 token 会在10秒内失效)
        String apiToken = Api.createApiToken(apiKey, uid, null);
        System.out.println("apiToken: " + apiToken);

        // 解析文件
        String dataUrl = Api.parseFileData(apiToken, file, null, null);

        // 生成大纲
        System.out.println("\n\n========== 正在生成大纲 ==========");
        String outline = Api.generateOutline(apiToken, null, dataUrl, null);

        // 异步生成大纲内容
        System.out.println("\n\n========== 正在异步生成大纲内容 ==========");
        Map<String, String> pptInfo = Api.asyncGenerateContent(apiToken, outline, dataUrl, null, null);

        String pptId = pptInfo.get("id");

        // 下载PPT到桌面
        System.out.println("\n\n========== 正在下载PPT ==========");
        System.out.println("pptId: " + pptId);
        String url = null;
        for (int i = 0; i < 30; i++) {
            // 等待PPT文件可下载
            JSONObject result = Api.downloadPptx(apiToken, pptId);
            if (result != null) {
                url = result.getString("fileUrl");
                if (url != null) {
                    break;
                }
            }
            Thread.sleep(1000);
        }
        System.out.println("ppt链接：" + url);
        String savePath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + pptId + ".pptx";
        HttpUtils.download(url, new File(savePath));
        System.out.println("ppt下载完成，保存路径：" + savePath);
    }

}
