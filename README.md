# Büyük Veri Analitiğinde Olasılıksal Veri Yapıları: HyperLogLog (HLL) Tasarımı

Bu proje, "Cardinality Estimation" (Küme Büyüklüğü Tahmini) problemini hafıza dostu ve yüksek performanslı bir şekilde çözen **HyperLogLog (HLL)** veri yapısının dış kütüphane kullanılmadan, sıfırdan Java ile gerçeklenmesini içermektedir.

Büyük veri setlerinde benzersiz (unique) eleman sayısını bulmak, geleneksel yöntemlerle (örneğin `HashSet`) çok yüksek RAM tüketimine yol açar. Bu proje, olasılıksal bir yaklaşım sergileyerek $O(1)$ zaman karmaşıklığı ve çok düşük, sabit bir bellek kullanımı ile yüksek doğrulukta tahminler yapmayı amaçlamaktadır.

## 🚀 Proje Özellikleri

- **Sıfırdan İmplementasyon:** Hiçbir dış bağımlılık (Google Guava vb.) kullanılmamış, tüm çekirdek yapı saf (vanilla) Java ile yazılmıştır.
- **Güçlü Hash Fonksiyonu:** Standart `hashCode()` yerine, homojen dağılım sağlamak ve ardışık verilerdeki çakışmaları (collision) önlemek için **FNV-1a 64-bit** algoritması ve **Murmur3 Avalanche (Çığ Etkisi) Mixer** mekanizması entegre edilmiştir.
- **Kovalama (Bucketing) ve Register Yapısı:** Veriler, belirlenen hassasiyet (precision - $p$) değerine göre $m = 2^p$ adet kovaya dağıtılmış ve ardışık sıfır sayıları (rank) bit manipülasyonu ile hesaplanmıştır.
- **Harmonik Ortalama ve Düzeltmeler:** Aykırı değerlerin (outliers) etkisini kırmak için Harmonik Ortalama kullanılmış; küçük veri setlerindeki sapmaları önlemek için ise **Linear Counting** düzeltme faktörü koda dahil edilmiştir.
- **Dağıtık Sistem Uyumluluğu (Mergeable):** İki farklı HLL veri yapısının, hiçbir veri kaybı yaşanmadan `merge` edilebilmesi özelliği başarıyla sağlanmıştır.

## 🛠️ Geliştirme Metodolojisi (Agentic Workflow)

Bu proje geliştirilirken geleneksel kodlama yerine **Agentic Kodlama** süreci benimsenmiştir:
1. Geliştirme ortamı olarak **IntelliJ IDEA**, dil modeli olarak **Gemini** kullanılmıştır.
2. Tasarım süreci modüler adımlara (İskelet -> Bucketing -> Hashing -> Hata Düzeltmeleri) bölünmüştür.
3. Test aşamasında tespit edilen *Avalanche Effect (Çığ Etkisi)* eksikliği, LLM'e doğrudan kütüphane ekletmek yerine problemin matematiksel çözümünün (Murmur3 bit karıştırıcı) modele yazdırılmasıyla iteratif olarak çözülmüştür.

## 📊 Teorik Analiz ve Sonuçlar

Algoritmanın hata payı $\approx 1.04 / \sqrt{m}$ formülü ile teorik olarak belirlenmiştir. Proje içindeki `main` metodu ile yapılan 100.000 benzersiz elemanlık ampirik analiz sonuçları, teorik beklentilerle birebir uyuşmaktadır:

| Hassasiyet (p) | Kova Sayısı (m) | Tahmin Edilen | Gerçek Değer | Hata Oranı (%) |
| :---: | :---: | :---: | :---: | :---: |
| 10 | 1024 | 102143 | 100000 | % 2.14 |
| 12 | 4096 | 100432 | 100000 | % 0.43 |
| 14 | 16384 | 99871 | 100000 | % 0.13 |

*Tablodan da görüleceği üzere, $p$ değeri (ve dolayısıyla kova sayısı) arttıkça hata oranı istikrarlı bir şekilde düşmektedir.*

**Merge (Birleştirme) Analizi:**
- İlk 50.000 elemanlık set (HLL-A)
- İkinci 50.000 elemanlık set (HLL-B)
- `hllA.merge(hllB)` işlemi sonucunda elde edilen tahmin kümesi, beklenen **100.000** değerine başarıyla ($\pm \%1$ hata payıyla) ulaşmıştır.

## 💻 Kurulum ve Kullanım

Projeyi kendi bilgisayarınızda çalıştırmak için:

1. Repoyu klonlayın:
   ```bash
   git clone [https://github.com/](https://github.com/)[GitHub Kullanıcı Adın]/hyperloglog-java.git
   Proje dizinine gidin:
2. Proje dizinine gidin:
Bash

cd hyperloglog-javaKodu derleyin:
3. Kodu derleyin:
Bash

javac HyperLogLog.java
4. Analiz ve test simülasyonunu çalıştırın:
Bash

java HyperLogLog

