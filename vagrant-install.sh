#!/bin/bash

# install docker
sudo yum install -y yum-utils device-mapper-persistent-data lvm2
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sudo yum makecache fast
sudo yum -y install docker-ce
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<- 'EOF'
{
    "registry-mirrors": ["https://z7fdio80.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
sudo usermod -aG docker vagrant


# install docker-compose
sudo curl -L "http://get.daocloud.io/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# ignore key check
sudo sed  -i '/StrictHostKeyChecking ask/c\\StrictHostKeyChecking no' /etc/ssh/ssh_config

# update default login directory
tee -a /home/vagrant/.bashrc <<-'EOF'
cd /home/vagrant/redis-java
EOF