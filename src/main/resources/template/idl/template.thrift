namespace cpp ${info.packageName}
namespace go  ${info.packageName}
namespace java  ${info.packageName}

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
        println("   " + idx + ": optional " + fieldType + " " + fieldName + ",");
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
        switch(httpMethod){
            case "get":
                for(parameter in methodInfo.parameters){
                    println("   /**" + parameter.description + "**/");
                    var requiredFlag = "optional";
                    if (parameter.required) {
                        requiredFlag = "required";
                    }
                    println("     " + parameterLP.index + ": " + requiredFlag + " " + solveThriftFieldType(parameter.schema) + " " + parameter.name + ",");
                }
                break;
            default:
                for(contentInfo in methodInfo.requestBody.content){
                    var contentType = contentInfo.key;
                    var contentData = contentInfo.value;
                    var reqType = solveThriftFieldType(contentData.schema);
                    println("     " + "1: required " + reqType + " " + reqType);
                }
                break;
        }
      println("     );");
      }
  }
%>
}
