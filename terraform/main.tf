terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

provider "aws" {
  region = var.region
}

variable "region" {
  type    = string
  default = "ap-south-1"
}

variable "environment" {
  type    = string
  default = "dev"
}

# Container registry for the service image. Extend with ECS/EKS, RDS and
# networking modules per environment (see docs/architecture.md).
resource "aws_ecr_repository" "transfer_service" {
  name                 = "transfer-service-${var.environment}"
  image_tag_mutability = "IMMUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

output "ecr_repository_url" {
  value = aws_ecr_repository.transfer_service.repository_url
}
