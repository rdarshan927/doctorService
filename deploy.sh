#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
#  deploy.sh  –  Build and deploy doctor-service to Google Cloud Run
#  Usage: ./deploy.sh <GCP_PROJECT_ID> <USER_SERVICE_URL> [REGION]
# ─────────────────────────────────────────────────────────────────
set -euo pipefail

PROJECT_ID="${1:?Usage: ./deploy.sh <GCP_PROJECT_ID> <USER_SERVICE_URL> [REGION]}"
USER_SERVICE_URL="${2:?Provide the deployed URL of user-service, e.g. https://user-service-xxx-uc.a.run.app}"
REGION="${3:-us-central1}"
IMAGE="us-central1-docker.pkg.dev/${PROJECT_ID}/doctor-service/app"
TAG="$(git rev-parse --short HEAD 2>/dev/null || echo 'latest')"

echo "────────────────────────────────────────────"
echo "  Service          : doctor-service"
echo "  Project          : ${PROJECT_ID}"
echo "  Region           : ${REGION}"
echo "  Tag              : ${TAG}"
echo "  User-Service URL : ${USER_SERVICE_URL}"
echo "────────────────────────────────────────────"

echo "[1/4] Configuring Docker auth for Artifact Registry..."
gcloud auth configure-docker "us-central1-docker.pkg.dev" --quiet

echo "[2/4] Ensuring Artifact Registry repository exists..."
gcloud artifacts repositories create doctor-service \
  --repository-format=docker \
  --location="${REGION}" \
  --project="${PROJECT_ID}" 2>/dev/null || echo "  (repository already exists)"

echo "[3/4] Building and pushing Docker image..."
docker build -t "${IMAGE}:${TAG}" -t "${IMAGE}:latest" .
docker push --all-tags "${IMAGE}"

echo "[4/4] Deploying to Cloud Run..."
gcloud run deploy doctor-service \
  --image "${IMAGE}:${TAG}" \
  --region "${REGION}" \
  --platform managed \
  --allow-unauthenticated \
  --port 8081 \
  --memory 512Mi \
  --cpu 1 \
  --set-env-vars "\
DB_URL=jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?prepareThreshold=0&sslmode=require,\
DB_USERNAME=postgres.bdgicssvyfumdnddxzbc,\
SUPABASE_URL=https://bdgicssvyfumdnddxzbc.supabase.co,\
USER_SERVICE_URL=${USER_SERVICE_URL}" \
  --update-secrets "DB_PASSWORD=user-service-db-password:latest" \
  --project "${PROJECT_ID}"

URL=$(gcloud run services describe doctor-service \
  --region="${REGION}" --project="${PROJECT_ID}" --format='value(status.url)')

echo ""
echo "════════════════════════════════════════════"
echo "  ✅ doctor-service deployed!"
echo "  URL: ${URL}"
echo ""
echo "  Quick tests:"
echo "    curl ${URL}/actuator/health"
echo "    curl -X POST ${URL}/api/doctors -H 'Authorization: Bearer <token>' -H 'Content-Type: application/json' -d '{...}'"
echo "    curl ${URL}/api/doctors/<id>/slots?date=2026-03-10 -H 'Authorization: Bearer <token>'"
echo "════════════════════════════════════════════"
