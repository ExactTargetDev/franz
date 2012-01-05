set :environment, "nvps"

set :java_opts, {}
set :app_opts, "-f ./config/test.scala"
set :jar_version, "1.0"

depend :local, :gem, "et-deployment-core", ">=1.0"
