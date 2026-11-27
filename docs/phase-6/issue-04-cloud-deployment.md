# Phase 6.4: Cloud Deployment Runbook & Operational Guide

## Overview
This runbook provides step-by-step instructions for deploying the complete FlopKart platform to production:
1. **Frontend:** React 18 SPA hosted globally on **Vercel** (with automated CI/CD and CDN edge routing).
2. **Backend:** Multi-container microservices orchestrated via **Docker Compose** on an **Ubuntu 24.04 Linux VPS** behind **Caddy** (Auto-HTTPS / Let's Encrypt).

---

## Part 1: Deploying the Frontend to Vercel

### Step 1: Connect GitHub Repository
1. Log in to [Vercel](https://vercel.com) and click **"Add New Project"**.
2. Select the `FlopKart` GitHub repository.

### Step 2: Configure Project Settings
* **Framework Preset:** `Vite`
* **Root Directory:** Edit and set to `frontend` (click "Continue").
* **Build Command:** `npm run build`
* **Output Directory:** `dist`
* **Install Command:** `npm ci`

### Step 3: Configure Environment Variables
Add the following environment variable in the Vercel project settings:

| Variable | Value | Description |
| :--- | :--- | :--- |
| `VITE_API_GATEWAY_URL` | `https://api.yourdomain.com/api/v1` | Public API Gateway endpoint via Caddy HTTPS |

### Step 4: Deploy & Verify Single-Page Routing
* Click **Deploy**. Vercel will install dependencies and compile the bundle.
* `frontend/vercel.json` provides rewrite rules (`/(.*) -> /index.html`) so refreshing deep links (e.g. `/manager`, `/cart`, `/orders/ORD-123`) loads the React application without 404 errors.

---

## Part 2: Provisioning & Deploying the Backend on Linux VPS

### Step 1: Provision Server & Security Groups
1. Provision an Ubuntu 24.04 LTS instance (e.g. AWS EC2, DigitalOcean Droplet, or Hetzner VPS with $\ge$ 4 GB RAM).
2. Configure DNS `A` record pointing `api.yourdomain.com` to the VPS public IP address.
3. Open firewall ports:
   ```bash
   sudo ufw allow 22/tcp
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   sudo ufw enable
   ```

### Step 2: Install Docker Engine & Compose Plugin
```bash
# Add Docker's official GPG key
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Add Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker and plugins
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER
```

### Step 3: Authenticate with GitHub Container Registry (GHCR)
Generate a GitHub Personal Access Token (PAT) with `read:packages` permission:
```bash
echo "<YOUR_GITHUB_TOKEN>" | docker login ghcr.io -u <YOUR_GITHUB_USERNAME> --password-stdin
```

### Step 4: Clone Deployment Manifests & Configure Environment
```bash
mkdir -p /opt/flopkart && cd /opt/flopkart
git clone https://github.com/NachikethaKG/FlopKart.git .

# Prepare production environment variables
cp .env.prod.example .env
nano .env
```
Ensure `.env` contains:
```properties
GITHUB_REPOSITORY_OWNER=nachikethakg
IMAGE_TAG=latest
DOMAIN_NAME=api.yourdomain.com
ACME_EMAIL=admin@yourdomain.com
ALLOWED_ORIGINS=https://your-frontend.vercel.app
JWT_SECRET=YourSuperSecretKeyWithAtLeast256BitsLengthForHmac512Signatures!
POSTGRES_PASSWORD=YourStrongDatabasePassword123!
```

### Step 5: Launch the Production Stack
```bash
# Pull latest images from GHCR
docker compose -f docker-compose.prod.yml pull

# Start all containers in background
docker compose -f docker-compose.prod.yml up -d

# Verify all services are healthy and running
docker compose -f docker-compose.prod.yml ps
```

### Step 6: Verify SSL & Reverse Proxy
Visit `https://api.yourdomain.com/actuator/health` in your browser. Caddy will automatically provision a valid Let's Encrypt TLS certificate on the first incoming request and proxy to `api-gateway:8080`.

---

## Part 3: Live Verification Checklist

- [x] Storefront loads on Vercel (`https://your-frontend.vercel.app`).
- [x] Customer registration and login succeed against the live backend.
- [x] Products display with real-time stock and prices fetched from PostgreSQL.
- [x] Items can be added to Redis shopping cart.
- [x] Manager login (`manager@flopkart.com`) displays Manager Portal badge.
- [x] Manager stock increment/decrement updates catalog immediately.
- [x] Order checkout triggers Kafka event Saga and updates status to `CONFIRMED`.
