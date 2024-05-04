<%
  // struct
  for(entry in components['schemas']){
      var key = entry.key;
      println("struct " + key + "{");
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

service ${info.title} {
<%
  // struct
  for(entry in paths){
      var path = entry.key;
      var apiInfo = entry.value;
      for(httpMethodInfo in apiInfo){
        var httpMethod = httpMethodInfo.key;
        var methodInfo = httpMethodInfo.value;
        println("   /**" + methodInfo.description + "**/");
        var fieldTypeRes = solveThriftFieldType(methodInfo["responses"]["200"]["content"]["*/*"]);
        println("   " + fieldTypeRes + " " + methodInfo.operationId);
        switch(httpMethod){
            case "get":
                print("(");
                for(parameter in methodInfo.parameters){
                    print(parameterLP.index + ": " + solveThriftFieldType(parameter.schema) + " " + parameter.name + ",");
                }
                print(")");
                break;
            default:
                print("(");
                for(contentInfo in methodInfo.requestBody.content){
                    var contentType = contentInfo.key;
                    var contentData = contentInfo.value;
                    var reqType = solveThriftFieldType(contentData.schema);
                    print("1: " + reqType + " " + reqType);
                }
                print(")");
                break;
        }
      print(";");
      }
  }
println("");
%>
}
