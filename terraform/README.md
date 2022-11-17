# terraform ec2 cloudwatch

## Pré requisitos

* terraform
* aws cli configurada

## Como usar?

1. Ajuste variables.tf de acordo com o seu ambiente

2. Para criar o ambiente:

```bash
terraform init && terraform apply --auto-approve
```

*Pode levar até 5min para a instância se tornar operacional*

3. Acesse a instância e execute:

```bash
sudo su - 
bash -c 'cd /opt/lojavirtual && make start'
```

4. Para destruir o ambiente:

```bash
terraform destroy --auto-approve
```



