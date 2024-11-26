#!groovy

//declaring a function
def decidePipeline(Map configMap) {
    type = configMap.get("type")
    switch(type) {
        case "nodejsEKS":
            nodeJSEKS(configMap)
            break
        case "nodejsVM":
            nodejsVM(configMap)
            break
        default:
            error "type is not matched" 
            break
    }
}