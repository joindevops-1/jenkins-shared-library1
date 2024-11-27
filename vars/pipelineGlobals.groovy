static final String region(){
    return "us-east-1"
}
static final String nexusURL(){
    return "nexus.daws78s.online:8081"
}
static final String dev_account_id(){
    return "315069654700"
}

static final String prod_account_id(){
    return "315069654700"
}

static String getAccountId(String environment) {
    switch (environment.toLowerCase()) {
        case 'dev':
            return "315069654700"
        case 'prod':
            return "315069654700"
        default:
            throw new IllegalArgumentException("Invalid environment: ${environment}. Expected 'dev' or 'prod'.")
    }
}