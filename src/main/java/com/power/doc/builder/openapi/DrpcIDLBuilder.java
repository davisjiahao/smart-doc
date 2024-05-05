/*
 * smart-doc https://github.com/smart-doc-group/smart-doc
 *
 * Copyright (C) 2018-2023 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.power.doc.builder.openapi;

import com.power.common.util.CollectionUtil;
import com.power.common.util.FileUtil;
import com.power.common.util.StringUtil;
import com.power.doc.helper.JavaProjectBuilderHelper;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.ApiDoc;
import com.power.doc.model.ApiMethodDoc;
import com.power.doc.model.ApiParam;
import com.power.doc.model.openapi.OpenApiTag;
import com.power.doc.utils.BeetlTemplateUtil;
import com.power.doc.utils.DocUtil;
import com.power.doc.utils.OpenApiSchemaUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.beetl.core.Template;

import java.util.*;

import static com.power.doc.constants.DocGlobalConstants.COMPONENT_REQUEST_SUFFIX;
import static com.power.doc.constants.DocGlobalConstants.COMPONENT_RESPONSE_SUFFIX;


/**
 * @author xingzi
 * Date 2022/9/17 15:16
 */
@SuppressWarnings("all")
public class DrpcIDLBuilder extends ThriftIDLBuilder {

    private static final DrpcIDLBuilder INSTANCE = new DrpcIDLBuilder();

    /**
     * For unit testing
     *
     * @param config Configuration of smart-doc
     */
    public static void buildOpenApi(ApiConfig config) {
        JavaProjectBuilder javaProjectBuilder = JavaProjectBuilderHelper.create();
        buildOpenApi(config, javaProjectBuilder);
    }

    /**
     * Only for smart-doc maven plugin and gradle plugin.
     *
     * @param config         Configuration of smart-doc
     * @param projectBuilder JavaDocBuilder of QDox
     */
    public static void buildOpenApi(ApiConfig config, JavaProjectBuilder projectBuilder) {
        List<ApiDoc> apiDocList = INSTANCE.getOpenApiDocs(config, projectBuilder);
        INSTANCE.openApiCreate(config, apiDocList);
    }

    protected void buildFile(ApiConfig config, List<ApiDoc> apiDocList, String fileName) {
        Map<String, Object> json = new HashMap<>(8);
        json.put("openapi", "3.0.3");
        json.put("info", buildInfo(config, fileName, apiDocList.get(0).getPackageName().substring(0, apiDocList.get(0).getPackageName().lastIndexOf('.'))));
        json.put("servers", buildServers(config));
        Set<OpenApiTag> tags = new HashSet<>();
        json.put("tags", tags);
        json.put("paths", buildPaths(config, apiDocList, tags));
        json.put("components", buildComponentsSchema(apiDocList));
        // 这里开始使用模板引擎
        Template byName = BeetlTemplateUtil.getByName("/idl/template.drpc.thrift");
        byName.binding(json);
        String filePath = config.getOutPath();
        filePath = filePath + fileName + ".drpc.thrift";
        FileUtil.nioWriteFile(byName.render(), filePath);
    }
}
