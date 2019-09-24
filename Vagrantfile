# -*- mode: ruby -*-
# vi: set ft=ruby :


Vagrant.configure("2") do |config|
  config.vm.define "test" do |node|
    node.vm.box = "bento/centos-7.6"
    node.vm.box_version = "201907.24.0"
    node.vm.hostname = "docker"
    node.vm.provider "virtualbox" do |v|
      v.memory = 4096
      v.cpus = 2
    end
    node.vm.provision "shell", path: "vagrant-install.sh", privileged: false
    node.vm.synced_folder "./", "/home/vagrant/redis-java/"
  end
end
