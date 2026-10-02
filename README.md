# Transfer Service (sample project)

A small, banking-flavoured backend skeleton: Java 17 / Spring Boot 3 REST API
for accounts and idempotent money transfers, with PostgreSQL, Docker,
Kubernetes manifests, CI and a Terraform starter.

## Run locally
    mvn spring-boot:run                 # in-memory H2
    docker compose up --build           # app + PostgreSQL on :8080

## Try it
    curl -X POST localhost:8080/api/v1/accounts -H 'Content-Type: application/json' \
      -d '{"owner":"alice","currency":"EUR","initialBalance":100.00}'
    curl -X POST localhost:8080/api/v1/accounts -H 'Content-Type: application/json' \
      -d '{"owner":"bob","currency":"EUR","initialBalance":0.00}'
    curl -X POST localhost:8080/api/v1/transfers -H 'Content-Type: application/json' \
    []()  -H 'Idempotency-Key: demo-123' \
      -d '{"fromAccountId":"<alice-id>","toAccountId":"<bob-id>","amount":25.00,"currency":"EUR"}'
    # Repeat the same call: you get the original result (200) and no second debit.

## Test
    mvn verify

## Kubernetes
    kubectl apply -f k8s/configmap.yaml -f k8s/secret.example.yaml \
                  -f k8s/deployment.yaml -f k8s/service.yaml -f k8s/hpa.yaml
Set the image in `k8s/deployment.yaml` first (ECR URL from `terraform output`).

## Docs
- docs/architecture.md - overview, flow and quality attributes
- docs/adr/ - architecture decision records (idempotency, module boundaries)
