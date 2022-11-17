locals {
  role_policy_arns = [
    "arn:aws:iam::aws:policy/service-role/AmazonEC2RoleforSSM",
    "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
  ]
}

resource "aws_iam_instance_profile" "amazonlinux" {
  name = "EC2-Profile-cw-Henrique"
  role = aws_iam_role.amazonlinux.name
}

resource "aws_iam_role_policy_attachment" "amazonlinux" {
  count = length(local.role_policy_arns)

  role       = aws_iam_role.amazonlinux.name
  policy_arn = element(local.role_policy_arns, count.index)
}

resource "aws_iam_role_policy" "amazonlinux" {
  name = "EC2-Inline-Policy"
  role = aws_iam_role.amazonlinux.id
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Statement" : [
        {
          "Effect" : "Allow",
          "Action" : [
            "ssm:GetParameter"
          ],
          "Resource" : "*"
        }
      ]
    }
  )
}

resource "aws_iam_role" "amazonlinux" {
  name = "EC2-Role-Henrique"
  path = "/"

  assume_role_policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Statement" : [
        {
          "Action" : "sts:AssumeRole",
          "Principal" : {
            "Service" : "ec2.amazonaws.com"
          },
          "Effect" : "Allow"
        }
      ]
    }
  )
}