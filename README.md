# Fullstack E-Commerce Project

Spring Boot ve React ile geliştirilmiş, ürün listeleme, sepet, sipariş ve Iyzico ödeme akışını uçtan uca kapsayan bir e-ticaret uygulaması.

## İçindekiler

- [Özellikler](#özellikler)
- [Teknoloji Stack'i](#teknoloji-stacki)
- [Proje Yapısı](#proje-yapısı)
- [Mimari Yaklaşım](#mimari-yaklaşım)
- [Lokal Kurulum](#lokal-kurulum)
- [Ortam Değişkenleri](#ortam-değişkenleri)
- [API Dokümantasyonu](#api-dokümantasyonu)
- [Testler](#testler)
- [Deployment](#deployment)
- [Faydalı Komutlar](#faydalı-komutlar)

## Özellikler

Backend tarafında:

- Ürün listeleme, ürün detayı, kategori ve kategori ağacı
- Pagination destekli ürün API'si
- JWT tabanlı authentication ve authorization
- Refresh token yönetimi için Redis
- Sepete ürün ekleme, çıkarma ve miktar güncelleme
- Sipariş oluşturma ve sipariş geçmişi
- Sipariş satırlarında ürün adı, görseli ve fiyat snapshot'ı
- Iyzico checkout form entegrasyonu
- Ödeme denemelerini ayrı takip eden `PaymentAttempt` modeli
- Ödeme callback'i için idempotent işleme
- Stok rezervasyonu, commit ve release akışı
- Outbox tabanlı event dispatch
- Admin ürün ve sipariş ekranları için API'ler
- PostgreSQL + Flyway migration yönetimi
- Swagger/OpenAPI dokümantasyonu
- Actuator health endpoint'leri
- Unit, integration ve architecture testleri

Frontend tarafında:

- React + TypeScript + Vite
- Chakra UI ile responsive arayüz
- Ürün listeleme ve detay sayfaları
- Kategori, fiyat ve stok odaklı ürün deneyimi
- Sepet sayfası
- Checkout ve ödeme sonucu ekranı
- Auth akışı
- Access token + refresh cookie ile oturum yenileme
- React Query ile server state yönetimi
- Zustand ile auth state yönetimi
- Kullanıcı dostu hata mesajları + loading ve error state'leri

## Teknoloji Stack'i

Backend:

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway
- MapStruct
- Lombok
- Iyzico Java SDK
- Springdoc OpenAPI
- Testcontainers
- ArchUnit
- Jib

Frontend:

- React 18
- TypeScript
- Vite
- Chakra UI
- TanStack React Query
- Axios
- Zustand
- React Router
- React Hot Toast

DevOps:

- Docker
- Docker Compose
- Jib ile container image build
- GitHub Actions
- Amazon ECR
- Elastic Beanstalk
- S3 + CloudFront
- RDS PostgreSQL
- Slack deploy bildirimi

## Proje Yapısı

```text
.
├── backend
│   ├── src/main/java/com/furkan/ecommerce
│   │   ├── auth
│   │   ├── cart
│   │   ├── common
│   │   ├── infrastructure
│   │   ├── location
│   │   ├── order
│   │   ├── payment
│   │   └── product
│   ├── src/main/resources/db/migration
│   ├── src/test/java
│   ├── docker-compose.yml
│   └── pom.xml
├── frontend
│   ├── src
│   │   ├── api
│   │   ├── app
│   │   ├── business-components
│   │   ├── containers
│   │   ├── services
│   │   ├── store
│   │   ├── types
│   │   └── views
│   └── package.json
├── deployment
│   └── Dockerrun.aws.json.template
├── docs
└── .github/workflows
```

Backend modülleri kendi içinde `api`, `internal`, `application`, `domain`, `persistence` ve `web` ayrımıyla ilerliyor. Amaç, modüllerin birbirinin internal detaylarına doğrudan bağımlı olmaması.

Örneğin cart modülü ürün bilgisini doğrudan `ProductRepository` üzerinden okumuyor. Bunun yerine product modülünün dışarı açtığı `ProductReadApi` üzerinden okuyor. Bu küçük ayrım, proje büyüdüğünde bağımlılıkların kontrolden çıkmasını engelliyor.

## Mimari Yaklaşım

Bu projede klasik controller-service-repository yapısının üzerine domain davranışları da ayırılmaya çalışıldı. İş kuralları mümkün olduğunca domain modelinin içinde duruyor.

Örnekler:

- `Cart`, ürün ekleme/güncelleme ve sepet temizleme davranışını kendi içinde yönetir.
- `Order`, `PENDING`, `PAID`, `PAYMENT_FAILED`, `EXPIRED`, `REQUIRES_REVIEW` gibi state geçişlerini kontrol eder.
- `Payment`, başarılı olmuş bir ödemenin tekrar failed'a dönmesi gibi geçersiz geçişleri engeller.
- `Product`, stok rezervasyonu, reserved stock commit ve release işlemlerini kendi davranışı olarak taşır.

Modüller arası yan etkiler doğrudan servis çağrılarıyla zincirlenmek yerine event'lerle yönetiliyor. Bu yüzden sipariş oluşturma, sepet temizleme, stok rezervasyonu ve ödeme sonrası sipariş güncelleme gibi işlemler daha izole duruyor.

## Lokal Kurulum

Gereksinimler:

- Java 21
- Maven
- Node.js 20+
- Docker
- Docker Compose

### 1. PostgreSQL ve Redis'i başlat

```bash
docker compose -f backend/docker-compose.yml up -d postgres redis
```

PostgreSQL varsayılan bilgileri:

```text
host: localhost
port: 5432
database: ecommerce
username: postgres
password: postgres
```

Redis:

```text
host: localhost
port: 6379
```

### 2. Backend'i çalıştır

```bash
mvn -f backend/pom.xml spring-boot:run
```

Backend varsayılan olarak `http://localhost:8080` üzerinde çalışır.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

### 3. Frontend'i çalıştır

```bash
npm ci --prefix frontend
npm --prefix frontend run dev
```

Frontend varsayılan olarak Vite ile `http://localhost:5173` üzerinde açılır.

Frontend API base URL ayarı:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Ortam Değişkenleri

Backend için sık kullanılan değişkenler:

| Değişken | Açıklama | Varsayılan |
| --- | --- | --- |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/ecommerce` |
| `DB_USERNAME` | Veritabanı kullanıcı adı | `postgres` |
| `DB_PASSWORD` | Veritabanı şifresi | `postgres` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis şifresi | boş |
| `REDIS_KEY_PREFIX` | Ortam bazlı Redis prefix | boş |
| `JWT_SECRET` | JWT imzalama secret'ı | local demo değeri |
| `REFRESH_COOKIE_SECURE` | Refresh cookie secure flag | `true` |
| `PII_ENCRYPTION_KEY` | PII encryption key | local demo değeri |
| `PAYMENT_CALLBACK_URL` | Iyzico callback endpoint'i | example URL |
| `PAYMENT_FRONTEND_RESULT_URL` | Callback sonrası frontend yönlendirmesi | `http://localhost:5173/payment/result` |
| `IYZICO_API_KEY` | Iyzico API key | boş |
| `IYZICO_SECRET_KEY` | Iyzico secret key | boş |
| `IYZICO_BASE_URL` | Iyzico base URL | `https://sandbox-api.iyzipay.com` |

Production veya staging ortamında özellikle `JWT_SECRET`, `PII_ENCRYPTION_KEY`, `IYZICO_API_KEY`, `IYZICO_SECRET_KEY` ve database bilgileri gerçek değerlerle verilmelidir.

Frontend:

| Değişken | Açıklama |
| --- | --- |
| `VITE_API_BASE_URL` | Backend API base URL |

## API Dokümantasyonu

Backend çalışırken Swagger UI üzerinden endpoint'ler incelenebilir:

```text
http://localhost:8080/swagger-ui/index.html
```

Ana endpoint grupları:

| Alan | Endpoint |
| --- | --- |
| Auth | `/api/v1/auth/*` |
| Products | `/api/v1/products` |
| Categories | `/api/v1/categories` |
| Cart | `/api/v1/cart` |
| Orders | `/api/v1/orders` |
| Payments | `/api/v1/payments` |
| Locations | `/api/v1/locations` |
| Admin Products | `/api/v1/admin/products` |
| Admin Orders | `/api/v1/admin/orders` |

Public endpoint'ler:

- Ürün listeleme ve detay
- Kategori endpoint'leri
- Lokasyon endpoint'leri
- Login/register/refresh/logout
- Iyzico callback

Kullanıcıya özel endpoint'ler JWT ister:

- Sepet
- Sipariş
- Ödeme başlatma
- Ödeme durumu
- Payment profile

Admin endpoint'leri `ADMIN` rolü ister.

## Testler

Backend testlerini çalıştırmak için:

```bash
mvn -f backend/pom.xml test
```

Test kapsamındaki önemli alanlar:

- Auth integration testleri
- Cart domain ve command service testleri
- Order state machine testleri
- Payment state machine ve command service testleri
- Outbox ve soft delete integration testleri
- Redis refresh token store testleri
- PII encryption converter testleri
- ArchUnit ile modül bağımlılığı kontrolü

Frontend build kontrolü:

```bash
npm --prefix frontend run build
```

## Deployment

Backend image'i Jib ile build edilip Amazon ECR'a gönderiliyor. Elastic Beanstalk Docker platformu bu image'i çalıştırıyor.

Frontend ise build edildikten sonra S3 bucket'a sync ediliyor ve CloudFront invalidation çalıştırılıyor.

GitHub Actions workflow'u genel olarak şu sırayı izliyor:

1. Backend testleri
2. Frontend dependency install
3. Frontend build
4. AWS credentials configure
5. ECR login
6. Backend image build/push
7. Elastic Beanstalk application version oluşturma
8. Elastic Beanstalk environment update
9. Frontend'i S3'e deploy
10. CloudFront invalidation
11. Slack deploy bildirimi

Deployment dosyaları:

- `.github/workflows/deploy.yml`
- `deployment/Dockerrun.aws.json.template`
- `backend/Dockerfile`
- `backend/Dockerfile.local`
- `backend/docker-compose.yml`

## Faydalı Komutlar

Backend compile:

```bash
mvn -f backend/pom.xml -DskipTests compile
```

Backend test:

```bash
mvn -f backend/pom.xml test
```

Frontend dependency kurulumu:

```bash
npm ci --prefix frontend
```

Frontend dev server:

```bash
npm --prefix frontend run dev
```

Frontend build:

```bash
npm --prefix frontend run build
```

PostgreSQL ve Redis başlatma:

```bash
docker compose -f backend/docker-compose.yml up -d postgres redis
```

Servisleri durdurma:

```bash
docker compose -f backend/docker-compose.yml down
```

Volume'ları da silerek temiz başlangıç:

```bash
docker compose -f backend/docker-compose.yml down -v
```

## Notlar

- Flyway migration'ları `backend/src/main/resources/db/migration` altında tutuluyor.
- Demo ürün seed'leri migration olarak ekleniyor.
- Backend `ddl-auto=validate` ile çalışıyor; şema Hibernate tarafından otomatik değiştirilmez.
- Refresh token cookie olarak taşınır, access token frontend state içinde tutulur.
- Payment callback endpoint'i public olmak zorunda, çünkü Iyzico backend'e kullanıcı JWT'siyle gelmez.
- Local Iyzico testi için callback URL'inin dış dünyadan erişilebilir olması gerekir. Bu yüzden geliştirme sırasında ngrok benzeri bir tunnel gerekebilir.

