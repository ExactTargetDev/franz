load 'deploy' if respond_to?(:namespace) # cap2 differentiator
Dir['vendor/gems/*/recipes/*.rb','vendor/plugins/*/recipes/*.rb'].each { |plugin| load(plugin) }

require 'rubygems'
require 'chef'
require 'et-deployment-core'

#######################################
##### Configuration section start #####
#######################################
default_run_options[:pty] = true
ssh_options[:user] = ssh_options_user
ssh_options[:keys] = ssh_options_keys

Chef::Config[:chef_server_url] = chef_server_url
Chef::Config[:node_name] = node_name
Chef::Config[:client_key] = client_key
Chef::Config[:client_name] = client_name
Chef::Config[:validation_key] = validation_key
Chef::Config[:validation_client_name] = validation_client_name
#######################################
##### Configuration section end   #####
#######################################

role(:franz) do
  find_nodes("franz")
end

namespace(:deploy) do

  task :franz, :roles => :franz do
    set :app_name, "franz"
    config = set_env_for_scala_app_deployment(Chef::Config[:chef_server_url], app_name)
    set :user, config["user"]

    top.upload("./#{app_name}/dist/#{app_name}-#{config['version']}.zip", "/home/#{user}/#{app_name}-#{config['version']}.zip")

    transaction do
      unzip_and_copy_scala_app(app_name, config['deploy_to'], config['release'], config['version'], user)
      do_symlink(app_name, config['deploy_to'], config['current_release'], config['release'])
      create_scala_upstart_script(config['deploy_to'], app_name, java_opts, app_opts, jar_version)
    end

    restart_scala_app(Chef::Config[:chef_server_url], app_name)
  end

end

namespace(:rollback) do

  task :franz, :roles => :franz do
    set :app_name, "franz"
    config = set_env_for_scala_app_rollback(Chef::Config[:chef_server_url], app_name)
    set :user, config["user"]

    transaction do
      rollback_symlink(app_name, config['deploy_to'], config['previous_release'])
      clean_as_root(app_name, config['deploy_to'], config['current_release'])
    end

    restart_scala_app(Chef::Config[:chef_server_url], app_name)
  end

end
