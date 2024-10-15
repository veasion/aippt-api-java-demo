package cn.veasion.aippt;

import com.alibaba.fastjson.JSONObject;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * AiPptDemo3 通过文件直接生成 PPT
 *
 * @author veasion
 * @date 2024/7/12
 */
public class AiPptDemo3 {

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

        // 通过文件直接生成PPT
        System.out.println("\n正在生成PPT...\n");
        JSONObject pptInfo = Api.directGeneratePptx(apiToken, true, null, null, dataUrl, null, false);

        String pptId = pptInfo.getString("id");
        String fileUrl = pptInfo.getString("fileUrl");
        System.out.println("\n\n===============");
        System.out.println("pptId: " + pptId);
        System.out.println("ppt主题：" + pptInfo.getString("subject"));
        System.out.println("ppt封面：" + pptInfo.getString("coverUrl"));
        System.out.println("ppt链接：" + fileUrl);

        // 下载PPT到桌面
        String savePath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + pptId + ".pptx";
        HttpUtils.download(fileUrl, new File(savePath));
        System.out.println("ppt下载完成，保存路径：" + savePath);
    }

}
