namespace php Dirpc.SDK.${strutil.replace(info.name,"Controller","")}
namespace go  Dirpc.SDK.${strutil.replace(info.name,"Controller","")}
namespace java  Dirpc.SDK.${strutil.replace(info.name,"Controller","")}

const string serverName = ${"dirpc." + strutil.toLowerCase(info.projectName + "." + strutil.replace(info.name,"Controller",""))}
const string groupId = ${"com.didichuxing.dirpc." + strutil.toLowerCase(info.projectName)}
const string artifactId = ${strutil.toLowerCase(strutil.replace(info.name,"Controller",""))}
const string version = "1.0.0"

<%
  // struct
  for(entry in components['schemas']){
      var key = entry.key;
      println("struct " + key + " {");
      var properties = entry.value;
      var idx = 1;
      for(property in properties['properties']) {
        var fieldName = property.key;
        var fieldInfo = property.value;
        println("   /**" + fieldInfo.description + "**/");
        var fieldType = solveThriftFieldType(fieldInfo);
//        if (!propertyLP.last) {
//            println("   " + idx + ": optional " + fieldType + " " + fieldName + ",");
//        } else {
//            println("   " + idx + ": optional " + fieldType + " " + fieldName);
//        }
        println("   " + idx + ": optional " + fieldType + " " + fieldName);
        idx++;
      }
      println("}");
  }
%>

service ${strutil.replace (info.name,"Controller","Service")} {
<%
  // struct
  for(entry in paths){
      var path = entry.key;
      var apiInfo = entry.value;
      for(httpMethodInfo in apiInfo){
        var httpMethod = httpMethodInfo.key;
        var methodInfo = httpMethodInfo.value;
        println();
        println("   /**" + methodInfo.description + "**/");
        var fieldTypeRes = solveThriftFieldType(methodInfo["responses"]["200"]["content"]["*/*"]["schema"]);
        println("   " + fieldTypeRes + " " + methodInfo.operationId + "(");
        var contentTypeFlag = "form";
        switch(httpMethod){
            case "get":
                for(parameter in methodInfo.parameters){
                    println("   /**" + parameter.description + "**/");
                    var requiredFlag = "optional";
                    if (parameter.required) {
                        requiredFlag = "required";
                    }
                    if (!parameterLP.last) {
                       println("     " + parameterLP.index + ": " + solveThriftFieldType(parameter.schema) + " " + parameter.name + ",");
                    } else {
                       println("     " + parameterLP.index + ": " + solveThriftFieldType(parameter.schema) + " " + parameter.name);
                    }
                }
                break;
            default:
                for(contentInfo in methodInfo.requestBody.content){
                    var contentType = contentInfo.key;
                    if (strutil.contain(contentType, "json")) {
                        contentTypeFlag = "json";
                    }
                    var contentData = contentInfo.value;
                    var reqType = solveThriftFieldType(contentData.schema);
                    println("     " + "1: " + reqType + " " + reqType);
                }
        }
      println("     )");
      println("     (");
      println("      timeoutMsec=\"500\"");
      println("      connectTimeoutMsec=\"500\"");
      println("      path=\"" + path + "\"");
      println("      httpMethod=\"" + httpMethod + "\"");
      println("      contentType=\"" + contentTypeFlag + "\"");
      println("      )");
      break;
      }
  }
%>
} (
     version="1.0.0"
     servName="${servers[0].url}"
     servType="http"
     /** 超时配置 */
     timeoutMsec="500"
     connectTimeoutMsec="500"
 )
