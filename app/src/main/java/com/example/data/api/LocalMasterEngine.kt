package com.example.data.api

import com.example.data.model.GameState
import kotlin.random.Random

object LocalMasterEngine {
    fun generateResponse(prompt: String, state: GameState): String {
        val p = prompt.lowercase()
        val peri = state.fairyName
        val master = state.masterName
        val time = state.formattedTime

        return when {
            p.contains("mulai") || p.contains("halo") || p.contains("status") -> {
                val heroesSummary = state.heroes.filter { it.isAlive }.joinToString("\n") { h ->
                    "| ${h.name.padEnd(14)} | ★${h.starGrade} Lv.${h.level.toString().padEnd(2)} | ${h.jobClass.padEnd(8)} | HP:${h.currentHp}/${h.maxHp} | Fat:${h.fatigue} | Strs:${h.stress} | [${h.tag}] |"
                }

                """
Laporan sistem dimensi Mobius diterima, Master $master.

Medan energi Lobby stabil. Seluruh hero yang melintasi gerbang telah dipulihkan luka fisiknya, namun beban stamina dan mental tetap melekat pada jiwa mereka. Saya, $peri, siap menyampaikan komando fisik Anda kepada seluruh penghuni Lobby.

```text
======================= DATABASE LOBBY =======================
Lobby   : ${state.lobbyName}
Waktu   : $time
Dompet  : ${state.gold} Gold | ${state.diamond} Diamond
Material: CM:${state.materials.cm} | UM:${state.materials.um} | RM:${state.materials.rm} | EM:${state.materials.em}
--------------------------------------------------------------
DAFTAR HERO AKTIF:
$heroesSummary
==============================================================
```

Peri ($peri) berdiri di samping monolit Altar, menunggu titah berikutnya dari Master.

---
### 🧭 PILIHAN AKSI
| No | Perintah | Keterangan |
|---|---|---|
| 1 | `Masuk Tower Lantai ${state.towerFloorCurrent}` | Kirim party untuk ekspedisi Menara Mobius |
| 2 | `Cek Altar Gacha` | Panggil hero baru dengan Gold atau Diamond |
| 3 | `Istirahat di Kitchen` | Pulihkan Fatigue (-40) & Stress (-30) selama 8 Jam |
| 4 | `Inspeksi Roster Hero` | Lihat status lengkap, skill, dan formasi |
| 5 | `Scan Radar PvP Hangar` | Cari lawan Master lain di Blood Arena |

Master, ke mana arah komando kita berikutnya?
""".trimIndent()
            }

            p.contains("kitchen") || p.contains("istirahat") || p.contains("makan") -> {
                """
Komando pemulihan fisik dan mental dijalankan. Para hero yang bertugas dan lelah segera diarahkan ke **Iron Bar & Kitchen**.

Aroma daging panggang berlemak dan kuah kaldu rempah hangat memenuhi ruangan batu yang dingin. Beberapa hero yang sebelumnya gemetar ketakutan mulai merilekskan otot bahu mereka. Gelas kayu berdenting di meja panjang.

```text
[LEDGER STATUS PEMULIHAN KITCHEN]
Waktu Bertambah : +8 Jam In-Game
Efek Pemulihan  : Fatigue -40 poin | Stress -30 poin
Kondisi Fisik   : Seluruh party kembali bugar dan siap tempur.
```

[RNG Kondisi Dapur: 88 vs 50 - Suasana kondusif dan hangat]

---
### 🧭 PILIHAN AKSI
| No | Perintah | Keterangan |
|---|---|---|
| 1 | `Masuk Tower Lantai ${state.towerFloorCurrent}` | Berangkat ekspedisi dengan kondisi bugar |
| 2 | `Inspeksi Roster Hero` | Periksa penurunan fatigue para pahlawan |
| 3 | `Cek Altar Gacha` | Lakukan pemanggilan hero tambahan |
| 4 | `Latihan Training Hall` | Kirim hero mengasah skill otonom |

Seluruh pahlawan telah selesai bersantap, Master. Apakah kita akan langsung menuju gerbang Menara?
""".trimIndent()
            }

            p.contains("gacha") || p.contains("summon") || p.contains("altar") -> {
                """
Pilar Altar Pemanggilan Mobius menyala dengan pendar cahaya ungu keemasan yang berdenyut lambat seperti detak jantung purba.

Rantai-rantai eterik berdenting di atas altar batu hitam. Peri $peri mengangkat tablet kristal:
*"Altar siap merobek dimensi untuk menarik jiwa baru. Ingat Master, hero yang dipanggil tidak tahu dunia ini adalah sebuah game. Bagi mereka, panggilan ini adalah nasib mutlak."*

```text
[BIAYA & TINGKAT SUMMON]
1. Gold Summon    : 1.000 Gold  | Rate: ★1(86%), ★2(10%), ★3(3%), ★4(0.9%), ★5(0.1%)
2. Diamond Summon : 10 Diamond  | Rate: ★2(85%), ★3(10%), ★4(4%), ★5(0.9%), ★6(0.1%)
3. Event Summon   : 50 Diamond  | Rate: ★3(90%), ★4(4%), ★5(5%), ★6(0.9%), ★7(0.1%)
Saldo Saat Ini    : ${state.gold} Gold | ${state.diamond} Diamond
```

---
### 🧭 PILIHAN AKSI
| No | Perintah | Keterangan |
|---|---|---|
| 1 | `Summon 1x Gold` | Panggil 1 hero biaya 1.000 Gold |
| 2 | `Summon 10x Gold` | Panggil 10 hero biaya 10.000 Gold |
| 3 | `Summon 1x Diamond` | Panggil 1 hero biaya 10 Diamond |
| 4 | `Summon 10x Diamond` | Panggil 10 hero biaya 100 Diamond |
| 5 | `Kembali ke Lobby` | Kembali ke aula utama |

Berapa banyak jiwa yang hendak Master panggil ke altar hari ini?
""".trimIndent()
            }

            p.contains("tower") || p.contains("menara") || p.contains("lantai") -> {
                val partyDesc = if (state.activePartyHeroes.isNotEmpty()) {
                    state.activePartyHeroes.joinToString(", ") { "${it.name} (★${it.starGrade})" }
                } else {
                    state.heroes.take(5).joinToString(", ") { "${it.name} (★${it.starGrade})" }
                }

                """
Pintu gerbang batu berukir simbol spiral Mobius berderit membuka perlahan. Dari celah gerbang terhembus udara berbau belerang, darah mengering, dan hawa dingin kematian.

Lantai ${state.towerFloorCurrent} menanti di kegelapan. Para hero yang ditugaskan dalam formasi ($partyDesc) merapatkan barisan, memeriksa bilah pedang dan konsentrasi sihir mereka. Begitu melintasi ambang pintu ini, intervensi taktis Master terkunci—hanya 'Miracle' yang dapat mengubah jalannya takdir!

```text
[BERKAS QUEST SHEET - LANTAI ${state.towerFloorCurrent}]
Zona          : Distrik Kehancuran Koridor Ke-1
Tipe Objektif : Annihilation (Bantai Seluruh Monster)
Kondisi Medan : Gelap, visibilitas rendah, jebakan duri berkarat
Estimasi Waktu: +4 Jam s/d +6 Jam In-Game
```

[RNG Blind Entry Check: 72 vs 40 - Gerbang stabil]

---
### 🧭 PILIHAN AKSI
| No | Perintah | Keterangan |
|---|---|---|
| 1 | `Luncurkan Ekspedisi Lantai ${state.towerFloorCurrent}` | Terobos ke dalam pertempuran otomatis |
| 2 | `Ubah Susunan Party` | Atur posisi Frontline, Midline, Backline |
| 3 | `Siapkan Miracle Heal & Shield` | Pastikan saldo Diamond cukup untuk intervensi |
| 4 | `Batalkan & Pulang ke Lobby` | Hindari risiko korban jiwa yang sia-sia |

Apakah party sudah siap melangkah ke dalam pembantaian, Master?
""".trimIndent()
            }

            else -> {
                """
Laporan diterima oleh Peri $peri.

Sistem memproses perintah: *"$prompt"*. Semua parameter fisik dan kestabilan mental hero di Lobby tetap berada di bawah pengawasan ketat. Jika Master memaksakan aksi yang melebihi ambang batas kelelahan (Fatigue >= 60), hero memiliki hak otonom untuk menolak atau memberontak demi bertahan hidup.

```text
[STATUS KONTROL MASTER]
Master Name   : $master
Dimensi       : ${state.lobbyName}
Waktu         : $time
Status Moral  : ${state.lobbyMorale}%
Tekanan Sosial: ${state.socialPressure}%
```

[RNG Respon Sistem: 84 vs 30 - Eksekusi berjalan lancar]

---
### 🧭 PILIHAN AKSI
| No | Perintah | Keterangan |
|---|---|---|
| 1 | `Masuk Tower Lantai ${state.towerFloorCurrent}` | Terjunkan tim tempur ke Menara Mobius |
| 2 | `Buka Altar Gacha` | Rekrut pejuang baru dari pilar dimensi |
| 3 | `Pergi ke Iron Bar & Kitchen` | Santap makanan bergizi untuk pemulihan |
| 4 | `Cek Fasilitas Blacksmith` | Tempa zirah dan senjata mematikan |
| 5 | `Inspeksi Roster Pahlawan` | Buka lembar data detail STR/VIT/AGI/INT |

Apa titah Anda selanjutnya, Master $master?
""".trimIndent()
            }
        }
    }
}
