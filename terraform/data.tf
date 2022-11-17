data "aws_ami" "amazon-linux-2" {
  most_recent = true


  filter {
    name   = "name"
    values = ["amzn-ami-hvm-*-x86_64-gp2"]
  }

  owners = ["137112412989"] # Amazon

}