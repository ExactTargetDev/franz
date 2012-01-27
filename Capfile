load 'deploy' if respond_to?(:namespace) # cap2 differentiator
Dir['vendor/gems/*/recipes/*.rb','vendor/plugins/*/recipes/*.rb'].each { |plugin| load(plugin) }

require 'rubygems'
require 'chef'

set :app_name, "franz"
set :app_type, "scala"

require 'et-deployment-core2'
