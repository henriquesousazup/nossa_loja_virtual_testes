resource "aws_ssm_parameter" "cw_agent" {
  description = "Cloudwatch agent config to configure custom log"
  name        = "/cloudwatch-agent/config"
  type        = "SecureString"
  overwrite = true
  value       = file("cw_agent_config.json")
}