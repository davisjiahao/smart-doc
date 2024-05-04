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
package com.power.doc.function;

import com.power.doc.utils.DocUtil;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Context;
import org.beetl.core.Function;
import org.beetl.ext.fn.StringUtil;

import java.util.Map;

/**
 * beetl template function
 * @author yu 2021/7/24.
 */
public class SolveThriftFieldTypeFun implements Function {

    @Override
    public String call(Object[] paras, Context ctx) {
        Map<String, Object> typeInfo = (Map<String, Object>) paras[0];
        return solveType(typeInfo);
    }

    private String solveType(Map<String, Object> typeInfo) {
        if (typeInfo == null) {
            return "object";
        }
        String fieldType = (String) typeInfo.get("type");
        if (typeInfo.get("type") != null) {
            switch((String) typeInfo.get("type")) {
                case "boolean":
                    fieldType = "bool";
                    break;
                case "array":
                    fieldType = "list<" + solveType((Map<String, Object>) typeInfo.get("items")) + ">";
                    break;
                case "map":
                    if (typeInfo.get("items") == null) {
                        fieldType = "list<string,string>";
                    } else {
                        fieldType = "list<string," + solveType((Map<String, Object>) typeInfo.get("items")) + ">";
                    }
                    break;
                case "number":
                    fieldType = StringUtils.replace((String) typeInfo.get("format"),"int","i");;
                    break;
                case "integer":
                    fieldType = StringUtils.replace((String) typeInfo.get("format"),"int","i");;
                    break;
                case "object":
                    if (typeInfo.get("$ref") != null) {
                        fieldType = StringUtils.replace((String) typeInfo.get("$ref"),"#/components/schemas/","");
                    }
                    break;

            }
        } else if (typeInfo.get("$ref") != null) {
            fieldType = StringUtils.replace((String) typeInfo.get("$ref"),"#/components/schemas/","");
        } else {
            return "object";
        }
        return fieldType;
    }
}
