public class HyperLogLog {

    // p: Hassasiyet (Precision) derecesi. Veriyi kaç kovaya böleceğimizi belirleyen üs değeri.
    private final int p;
    // m: Toplam kova (bucket/register) sayısı. m = 2^p formülüyle bulunur.
    private final int m;
    // registers: Her kovanın gördüğü maksimum ardışık sıfır sayısını (rank) tutan byte dizisi.
    private final byte[] registers;
    // alphaMM: HLL algoritmasının matematiksel bias (sapma) düzeltme sabiti.
    private final double alphaMM;

    // --- CONSTRUCTOR (KURUCU METOD) ---
    public HyperLogLog(int p) {
        if (p < 4 || p > 16) {
            throw new IllegalArgumentException("p değeri 4 ile 16 arasında olmalıdır.");
        }
        this.p = p;
        // Performans optimizasyonu: 2^p hesabını Math.pow yerine bitwise shift (sola kaydırma) ile yapıyoruz.
        this.m = 1 << p;
        this.registers = new byte[m];
        this.alphaMM = calculateAlphaMM(p, m);
    }

    // Flajolet'in orijinal HLL makalesindeki standart düzeltme katsayıları
    private double calculateAlphaMM(int p, int m) {
        switch (p) {
            case 4:
                return 0.673 * m * m;
            case 5:
                return 0.697 * m * m;
            case 6:
                return 0.709 * m * m;
            default:
                return (0.7213 / (1.0 + 1.079 / m)) * m * m;
        }
    }

    // --- KÜTÜPHANESİZ GÜÇLÜ HASH FONKSİYONU ---
    // 64-bit FNV-1a tabanı + Murmur3 Avalanche (Çığ Etkisi) Karıştırıcısı
    private long hash(String data) {
        long hash = -3750763034362895579L; // FNV offset basis
        for (int i = 0; i < data.length(); i++) {
            hash ^= data.charAt(i); // XOR işlemi
            hash *= 1099511628211L; // FNV prime
        }

        // AVALANCHE (ÇIĞ ETKİSİ) EKLENTİSİ:
        // Ardışık verilerde (ör: veri-1, veri-2) bitlerin çok benzer çıkmasını engellemek için
        // bitleri sağa kaydırıp tekrar XOR'layarak homojen bir dağılım elde ediyoruz.
        // Bu sayede çakışmaları (collision) minimuma indiriyoruz.
        hash ^= hash >>> 33;
        hash *= -4906477813569752945L; // Murmur3 karıştırıcı sabiti 1
        hash ^= hash >>> 33;
        hash *= -4265267296055464877L; // Murmur3 karıştırıcı sabiti 2
        hash ^= hash >>> 33;

        return hash;
    }

    // --- VERİ EKLEME (ALGORİTMANIN KALBİ) ---
    public void add(String item) {
        long hashVal = hash(item);

        // 1. Kova İndeksini Bul: Hash'in ilk 'p' adet bitini sağa kaydırarak kova numarasını buluyoruz.
        // Unsigned right shift (>>>) kullanarak işaret bitinden (negatif sayılardan) etkilenmiyoruz.
        int index = (int) (hashVal >>> (64 - p));

        // 2. Kalan Bitleri Ayır: Kova indeksini bulduğumuz ilk p biti çöpe atmak için sola kaydırıyoruz.
        long w = hashVal << p;

        // 3. Sıfırları Say: Kalan bit dizilimindeki en soldaki ilk '1' bitine kadar olan sıfır sayısını bulup 1 ekliyoruz.
        int rank = Long.numberOfLeadingZeros(w) + 1;

        // 4. Register Güncelle: Eğer bulduğumuz sıfır sayısı (rank), kovadaki mevcut sayıdan büyükse güncelliyoruz.
        if (rank > registers[index]) {
            registers[index] = (byte) rank;
        }
    }

    // --- TAHMİN (CARDINALITY ESTIMATION) METODU ---
    public long estimate() {
        double sum = 0.0;

        // Harmonik ortalama hesabının payda kısmı: 2^(-register_değeri)
        // Aritmetik ortalama yerine harmonik kullanıyoruz ki aykırı değerler (outliers) tahmini bozmasın.
        for (int i = 0; i < m; i++) {
            sum += Math.pow(2.0, -registers[i]);
        }

        // Ham tahmin (Raw Estimate) hesabı
        double estimate = alphaMM / sum;

        // LINEAR COUNTING (Küçük Veri Düzeltmesi)
        // Eğer tahmin 2.5 * m'den küçükse, boş kovalar hata yaratıyor olabilir.
        if (estimate <= 2.5 * m) {
            int emptyRegisters = 0;
            for (int i = 0; i < m; i++) {
                if (registers[i] == 0) emptyRegisters++;
            }
            // Boş kova varsa Linear Counting matematiksel formülü ile tahmini düzeltiyoruz.
            if (emptyRegisters > 0) {
                estimate = m * Math.log((double) m / emptyRegisters);
            }
        }

        return Math.round(estimate); // Yuvarlayarak tam sayı döndür
    }

    // --- BİRLEŞTİRİLEBİLİRLİK (MERGE) ÖZELLİĞİ ---
    // Dağıtık sistemlerde iki farklı HLL yapısını veri kaybı olmadan birleştirir.
    public void merge(HyperLogLog other) {
        if (this.p != other.p) {
            throw new IllegalArgumentException("Birleştirme için hassasiyet (p) değerleri aynı olmalıdır!");
        }
        // Her iki yapının aynı indeksli kovalarındaki MAKSİMUM değeri alıyoruz.
        for (int i = 0; i < m; i++) {
            this.registers[i] = (byte) Math.max(this.registers[i], other.registers[i]);
        }
    }
}