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
import com.power.doc.constants.DocGlobalConstants;
import com.power.doc.helper.JavaProjectBuilderHelper;
import com.power.doc.model.*;
import com.power.doc.model.openapi.OpenApiTag;
import com.power.doc.utils.BeetlTemplateUtil;
import com.power.doc.utils.DocUtil;
import com.power.doc.utils.JsonUtil;
import com.power.doc.utils.OpenApiSchemaUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Template;

import java.util.*;

import static com.power.doc.constants.DocGlobalConstants.*;


/**
 * @author xingzi
 * Date 2022/9/17 15:16
 */
@SuppressWarnings("all")
public class ThriftIDLBuilder extends OpenApiBuilder {

    private static final ThriftIDLBuilder INSTANCE = new ThriftIDLBuilder();

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


    public void openApiCreate(ApiConfig config, List<ApiDoc> apiDocList) {
        this.setComponentKey(getModuleName());
        if (config.isAllInOne()) {
            buildFile(config, apiDocList, config.getProjectName());
        } else {
            for (ApiDoc apiDoc : apiDocList) {
                List<ApiDoc> one = new ArrayList<ApiDoc>();
                one.add(apiDoc);
                buildFile(config, one, apiDoc.getName());
            }
        }
    }

    /**
     * component schema properties data
     *
     * @param apiParam ApiParam
     */
    protected Map<String, Object> buildPropertiesData(ApiParam apiParam, Map<String, Object> component, boolean isResp) {
        Map<String, Object> propertiesData = new HashMap<>();
        String openApiType = DocUtil.javaTypeToOpenApiTypeConvert(apiParam.getType());
        //array object file map
        propertiesData.put("description", apiParam.getDesc());
        if (StringUtil.isNotEmpty(apiParam.getValue())) {
            propertiesData.put("example", StringUtil.removeDoubleQuotes(apiParam.getValue()));
        }

        if (!"object".equals(openApiType)) {
            propertiesData.put("type", openApiType);
            propertiesData.put("format", "int16".equals(apiParam.getType()) ? "int32" : apiParam.getType());
        }
        if ("map".equals(apiParam.getType())) {
            propertiesData.put("type", "map");
            propertiesData.put("description", apiParam.getDesc() + "(map data)");
            if (CollectionUtil.isNotEmpty(apiParam.getChildren())) {
                if (!apiParam.isSelfReferenceLoop()) {
                    Map<String, Object> arrayRef = new HashMap<>(4);
                    String suffix = isResp ? COMPONENT_RESPONSE_SUFFIX : COMPONENT_REQUEST_SUFFIX;
                    String childSchemaName = OpenApiSchemaUtil.getClassNameFromParams(apiParam.getChildren(), suffix);
                    if (!childSchemaName.contains(OpenApiSchemaUtil.NO_BODY_PARAM)) {
                        component.put(childSchemaName, buildProperties(apiParam.getChildren(), component, isResp));
                        arrayRef.put("$ref", getComponentKey() + childSchemaName);
                        propertiesData.put("items", arrayRef);
                    }
                }
            }
        }
        if ("array".equals(apiParam.getType())) {
            propertiesData.put("type", "array");
            if (CollectionUtil.isNotEmpty(apiParam.getChildren())) {
                if (!apiParam.isSelfReferenceLoop()) {
                    Map<String, Object> arrayRef = new HashMap<>(4);
                    String suffix = isResp ? COMPONENT_RESPONSE_SUFFIX : COMPONENT_REQUEST_SUFFIX;
                    String childSchemaName = OpenApiSchemaUtil.getClassNameFromParams(apiParam.getChildren(), suffix);
                    if (childSchemaName.contains(OpenApiSchemaUtil.NO_BODY_PARAM)) {
                        propertiesData.put("type", "object");
                        propertiesData.put("description", apiParam.getDesc() + "(object)");
                    } else {
                        component.put(childSchemaName, buildProperties(apiParam.getChildren(), component, isResp));
                        arrayRef.put("$ref", getComponentKey() + childSchemaName);
                        propertiesData.put("items", arrayRef);
                    }
                }
            }
            //基础数据类型
            else {
                Map<String, Object> arrayRef = new HashMap<>(4);
                arrayRef.put("type", "string");
                propertiesData.put("items", arrayRef);
            }
        }
        if ("file".equals(apiParam.getType())) {
            propertiesData.put("type", "string");
            propertiesData.put("format", "binary");
        }
        if ("object".equals(apiParam.getType())) {
            if (CollectionUtil.isNotEmpty(apiParam.getChildren())) {
                propertiesData.put("type", "object");
                propertiesData.put("description", apiParam.getDesc() + "(object)");
                String suffix = isResp ? COMPONENT_RESPONSE_SUFFIX : COMPONENT_REQUEST_SUFFIX;
                if (!apiParam.isSelfReferenceLoop()) {
                    String childSchemaName = OpenApiSchemaUtil.getClassNameFromParams(apiParam.getChildren(), suffix);
                    if (childSchemaName.contains(OpenApiSchemaUtil.NO_BODY_PARAM)) {
                        propertiesData.put("type", "object");
                        propertiesData.put("description", apiParam.getDesc() + "(object)");
                    } else {
                        component.put(childSchemaName, buildProperties(apiParam.getChildren(), component, isResp));
                        propertiesData.put("$ref", getComponentKey() + childSchemaName);
                    }
                }
            } else {
                propertiesData.put("type", "object");
                propertiesData.put("description", apiParam.getDesc() + "(object)");
            }
        }

        return propertiesData;
    }

    protected void buildFile(ApiConfig config, List<ApiDoc> apiDocList, String fileName) {
        Map<String, Object> json = new HashMap<>(8);
        json.put("openapi", "3.0.3");
        json.put("info", buildInfo(config, fileName));
        json.put("servers", buildServers(config));
        Set<OpenApiTag> tags = new HashSet<>();
        json.put("tags", tags);
        json.put("paths", buildPaths(config, apiDocList, tags));
        json.put("components", buildComponentsSchema(apiDocList));
        // 这里开始使用模板引擎
        Template byName = BeetlTemplateUtil.getByName("/idl/template.thrift");
        byName.binding(json);
        String filePath = config.getOutPath();
        filePath = filePath + fileName + ".thrift";
        FileUtil.nioWriteFile(byName.render(), filePath);
    }

    @Override
    public Map<String, Object> buildComponentsSchema(List<ApiDoc> apiDocs) {
        Map<String, Object> schemas = new HashMap<>(4);
        Map<String, Object> component = new HashMap<>();
        apiDocs.forEach(
                a -> {
                    List<ApiMethodDoc> apiMethodDocs = a.getList();
                    apiMethodDocs.forEach(
                            method -> {
                                //request components
                                String requestSchema = OpenApiSchemaUtil.getClassNameFromParams(method.getRequestParams(), COMPONENT_REQUEST_SUFFIX);
                                List<ApiParam> requestParams = method.getRequestParams();
                                Map<String, Object> prop = buildProperties(requestParams, component, false);
                                component.put(requestSchema, prop);
                                //response components
                                List<ApiParam> responseParams = method.getResponseParams();
                                String schemaName = OpenApiSchemaUtil.getClassNameFromParams(method.getResponseParams(), COMPONENT_RESPONSE_SUFFIX);
                                component.put(schemaName, buildProperties(responseParams, component, true));
                            }
                    );
                }
        );
        component.remove(OpenApiSchemaUtil.NO_BODY_PARAM);
        schemas.put("schemas", component);
        return schemas;
    }

    public Map<String, Object> buildPaths(ApiConfig apiConfig, List<ApiDoc> apiDocList, Set<OpenApiTag> tags) {
        Map<String, Object> pathMap = new HashMap<>(500);
        for (ApiDoc apiDoc : apiDocList) {
            for (ApiMethodDoc methodDoc : apiDoc.getList()) {
                String path = methodDoc.getPath();
                Map<String, Object> request = buildPathUrls(apiConfig, methodDoc, methodDoc.getClazzDoc());
                if (!pathMap.containsKey(path)) {
                    pathMap.put(path, request);
                } else {
                    Map<String, Object> oldRequest = (Map<String, Object>) pathMap.get(path);
                    oldRequest.putAll(request);
                }
            }
            tags.add(OpenApiTag.of(apiDoc.getDesc(), apiDoc.getDesc()));
        }
        return pathMap;
    }

    protected static Map<String, Object> buildInfo(ApiConfig apiConfig, String fileName) {
        Map<String, Object> infoMap = SwaggerBuilder.buildInfo(apiConfig);
        infoMap.put("name", fileName);
        return infoMap;
    }

    @Override
    public Map<String, Object> buildPathUrlsRequest(ApiConfig apiConfig, ApiMethodDoc apiMethodDoc, ApiDoc apiDoc) {
        Map<String, Object> stringObjectMap = super.buildPathUrlsRequest(apiConfig, apiMethodDoc, apiDoc);
        stringObjectMap.put("operationId", apiMethodDoc.getName());
        return stringObjectMap;
    }
}
