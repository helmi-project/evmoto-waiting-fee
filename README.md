# EVmoto Waiting Fee & Cancellation Engine

Implementasi sederhana Spring Boot 3.x untuk aturan waiting fee dan cancellation fee.

## Menjalankan

```bash
mvn spring-boot:run
```

Test:

```bash
mvn clean test
```

## Endpoint

`POST /v1/orders/{orderId}/fee-preview`

Request mengikuti spesifikasi assessment: `arrivedAt`, `endedAt`, `endReason`, `pickupPoint`, dan `driverPings`.

```bash
{
  "arrivedAt": "2026-08-10T09:00:00+07:00",
  "endedAt": "2026-08-10T09:21:40+07:00",
  "endReason": "CANCELLED_BY_CUSTOMER",
  "pickupPoint": {
    "lat": -6.21462,
    "lng": 106.84513
  },
  "driverPings": [
    {
      "at": "2026-08-10T09:00:00+07:00",
      "lat": -6.21462,
      "lng": 106.84513
    },
    {
      "at": "2026-08-10T09:08:00+07:00",
      "lat": -6.21980,
      "lng": 106.85110
    },
    {
      "at": "2026-08-10T09:14:00+07:00",
      "lat": -6.21470,
      "lng": 106.84520
    }
  ]
}
```

## Rule

- Timer mulai dari `arrivedAt`.
- 5 menit pertama gratis.
- Setelah itu Rp500 per menit yang sudah dimulai, dibulatkan ke atas.
- Waiting fee maksimal Rp15.000.
- Trip started: total = waiting fee.
- Customer cancel setelah free waiting: waiting fee + Rp5.000, maksimal Rp20.000.
- Customer cancel sebelum/tepat free waiting: Rp0.
- Driver cancel: Rp0.
- Timer pause jika posisi driver >100 meter dari pickup.
- Input waktu ISO-8601 dengan offset; elapsed time dihitung dari instant timestamp.

## Asumsi sederhana

- `driverPings` kosong berarti tidak ada pause.
- Ping pertama yang >100m mulai pause pada waktu ping tersebut.
- Posisi terakhir dianggap tetap sampai ping berikutnya.
- Ping di luar rentang arrivedAt-endedAt diabaikan.
- `endedAt < arrivedAt` menghasilkan HTTP 400.
- Tepat 100m tidak pause; pause hanya jika >100m.

Kode sengaja tidak menambahkan DB, service eksternal, atau library untuk logika fee/geo.
