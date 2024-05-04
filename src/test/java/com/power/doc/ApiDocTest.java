package com.power.doc;

import com.power.common.util.DateTimeUtil;
import com.power.doc.builder.ApiDocBuilder;
import com.power.doc.builder.HtmlApiDocBuilder;
import com.power.doc.builder.openapi.ThriftIDLBuilder;
import com.power.doc.constants.FrameworkEnum;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.SourceCodePath;
import org.junit.jupiter.api.Test;

/**
 * Description:
 * ApiDoc测试
 *
 * @author yu 2018/06/11.
 */
public class ApiDocTest {

    /**
     * 包括设置请求头，缺失注释的字段批量在文档生成期使用定义好的注释
     */
    @Test
    public void testBuilderControllersApi() {
        ApiConfig config = new ApiConfig();
        config.setServerUrl("http://127.0.0.1:8899");
        // config.setStrict(true);
        config.setOpenUrl("http://localhost:7700/api");
        config.setAppToken("be4211613a734b45888c075741680e49");
        // config.setAppToken("7b0935531d1144e58a86d7b4f2ad23c6");

        config.setDebugEnvName("测试环境");
        config.setInlineEnum(true);
        config.setStyle("randomLight");
        config.setCreateDebugPage(true);
        // config.setAuthor("test");
        config.setDebugEnvUrl("http://127.0.0.1");
        // config.setTornaDebug(true);
        config.setAllInOne(false);
        config.setCoverOld(true);
        config.setOutPath("./");
        // config.setMd5EncryptedHtmlName(true);
        config.setFramework(FrameworkEnum.SPRING.getFramework());
        // 不指定SourcePaths默认加载代码为项目src/main/java下的
        config.setSourceCodePaths(
                SourceCodePath.builder().setDesc("本项目代码")
                        .setPath("/Users/didi/open_src/manhattan-fatman-vehicle-center")
        );

        config.setJarSourcePaths(SourceCodePath.builder()
                .setPath("/Users/didi/.gradle/caches/modules-2/files-2.1/com.xiaoju.manhattan.insurance/base/2.0.25.RELEASE/e746a45896e9c6d18f4577c8955e049525e68402/base-2.0.25.RELEASE-sources.jar")
        );
        long start = System.currentTimeMillis();
        ThriftIDLBuilder.buildOpenApi(config);
        // HtmlApiDocBuilder.buildApiDoc(config);
        long end = System.currentTimeMillis();
        DateTimeUtil.printRunTime(end, start);
    }


}
