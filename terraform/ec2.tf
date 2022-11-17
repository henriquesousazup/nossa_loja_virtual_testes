resource "aws_network_interface" "amazonlinux" {
  subnet_id   = var.subnet_id

  tags = {
    Name = "primary_network_interface"
  }
}

resource "aws_instance" "amazonlinux" {
  ami           = data.aws_ami.amazon-linux-2.id
  instance_type = "t3.micro"
	
	user_data            = local.userdata
	iam_instance_profile = aws_iam_instance_profile.amazonlinux.name
	key_name = var.keyname

  network_interface {

    network_interface_id = aws_network_interface.amazonlinux.id
    device_index         = 0
  }

  tags = {
    Name = "loja-virtual-cloud-watch-henrique zcoders"
  }
}




