#!/bin/bash

# Make sure we have all the latest updates when we launch this instance

echo "Update and install few things"
yum update -y
yum upgrade -y
yum install git docker -y

wget https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) 
mv docker-compose-$(uname -s)-$(uname -m) /usr/local/bin/docker-compose
chmod -v +x /usr/local/bin/docker-compose
service docker start

echo "Cloning git repo"
git clone https://github.com/henriquesousazup/nossa_loja_virtual_testes /opt/lojavirtual
mkdir /opt/lojavirtual/logs

echo "Configuring cloudwatch"
# Configure Cloudwatch agent
wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
rpm -U ./amazon-cloudwatch-agent.rpm

# Use cloudwatch config from SSM
/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
-a fetch-config \
-m ec2 \
-c ssm:${ssm_cloudwatch_config} -s

echo "Start compose"

bash -c 'cd /opt/bancodigital && git checkout monitoria-e-tracing-tc3-tl1 && make start'

echo "Done initialization"