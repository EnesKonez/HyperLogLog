public class Main {
    public static void main(String[] args) {
        System.out.println("=== HYPERLOGLOG (HLL) ALGORİTMA ANALİZİ ===");

        int exactCount = 100000; // 100 bin benzersiz eleman

        // 1. Kova Sayısının (m) Tahmin Hatasına Etkisinin Analizi
        System.out.println("\n1. Kova Sayısının (p ve m) Tahmin Hatasına Etkisi:");
        System.out.printf("%-5s %-10s %-15s %-15s %-15s%n", "p", "Kova (m)", "Tahmin (HLL)", "Gerçek", "Hata Oranı (%)");
        System.out.println("------------------------------------------------------------------");

        int[] pValues = {10, 12, 14}; // Farklı kova senaryoları

        for (int p : pValues) {
            HyperLogLog hll = new HyperLogLog(p);
            for (int i = 0; i < exactCount; i++) {
                hll.add("veri-" + i);
            }

            long estimate = hll.estimate();
            double errorRate = Math.abs(estimate - exactCount) / (double) exactCount * 100;

            System.out.printf("%-5d %-10d %-15d %-15d %-15.2f%%%n", p, (1<<p), estimate, exactCount, errorRate);
        }

        System.out.println("\nSonuç: p değeri arttıkça hata oranı düşmektedir.");

        // 2. Birleştirme (Merge) Özelliğinin Test Edilmesi
        System.out.println("\n2. Birleştirilebilir (Mergeable) Özelliği Testi:");
        HyperLogLog hllA = new HyperLogLog(12);
        HyperLogLog hllB = new HyperLogLog(12);

        for (int i = 1; i <= 50000; i++) {
            hllA.add("merge-test-" + i);
        }
        for (int i = 50001; i <= 100000; i++) {
            hllB.add("merge-test-" + i);
        }

        System.out.println("HLL-A Tahmini (İlk 50 bin): " + hllA.estimate());
        System.out.println("HLL-B Tahmini (Son 50 bin): " + hllB.estimate());

        hllA.merge(hllB); // B'yi A'nın içine birleştir
        System.out.println("Birleştirilmiş HLL (A+B) Tahmini (Beklenen 100.000): " + hllA.estimate());
    }
}