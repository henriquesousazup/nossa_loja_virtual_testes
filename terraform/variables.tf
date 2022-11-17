variable "subnet_id" {
  type    = string
  default = "subnet-08f0b109559319ddf"
}

variable "keyname" {
  type    = string
  default = "zup-sandbox-edu"
}

variable "sg_id" {
  type    = list(any)
  default = ["sg-06180d1bc14807a01"]
}

variable "aws_region" {
  type = string
  default = "us-east-1"
}