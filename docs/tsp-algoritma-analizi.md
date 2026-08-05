### 17. Gün: Rota Optimizasyon Algoritmalarının Kuramsal Analizi (TSP)

**1. Problem Tanımı ve Kapsam**
Tank Görev Simülatörü projesinde, üsten yola çıkan bir tankın harita üzerindeki $N$ adet hedefi en az yakıt tüketimi (en kısa mesafe) ile ziyaret edip başlangıç noktasına dönmesi gerekmektedir. Bu senaryo, bilgisayar bilimlerinde "Gezgin Satıcı Problemi" (Traveling Salesperson Problem - TSP) olarak bilinir. TSP, doğası gereği "NP-Hard" sınıfına ait bir problemdir ve veri seti büyüdükçe çözüm süresi üstel (eksponansiyel) olarak artar. Sistem performansını ve simülasyon akıcılığını korumak amacıyla projede tek bir algoritma yerine, hedef sayısına bağlı olarak dinamik geçiş yapan **Hibrit Algoritma Mimarisi** tasarlanmıştır.

**2. Kusursuz Permütasyon (Brute-Force) Analizi**
Sistemdeki hedef sayısı $N \le 10$ olduğunda devreye giren bu yaklaşım, hedeflerin ziyaret edilebileceği tüm olası rotaları hesaplar ve matematiksel olarak en kısa olanı seçer.

*   **Çalışma Mantığı:** Hedeflerin tüm permütasyonları çıkarılır, her rotanın toplam mesafesi hesaplanır ve minimum değere sahip rota uygulanır.
*   **Asimptotik Karmaşıklık (Big O Notation):** $\mathcal{O}(N!)$
*   **Teorik Değerlendirme:** Faktöriyel zaman karmaşıklığı, veri setinin küçük olduğu durumlarda kusursuz sonuç verir. $10$ hedef için maksimum $10! = 3.628.800$ ihtimal hesaplanır. Modern bir işlemci (CPU) bu işlem hacmini birkaç milisaniye içinde tamamlayabilir. Bu nedenle $10$ hedefe kadar olan görevlerde kesin doğruluk (%100 optimizasyon) tercih edilmiştir.

**3. En Yakın Komşu (Nearest Neighbor) Sezgisel Analizi**
Hedef sayısı sınır değeri aştığında ($N > 10$), $\mathcal{O}(N!)$ karmaşıklığı sistemin kilitlenmesine (UI Thread donması) yol açacaktır (Örn: $15! \approx 1.3 \times 10^{12}$ işlem). Bu darboğazı önlemek için sistem otomatik olarak "En Yakın Komşu" (Nearest Neighbor) sezgisel (heuristic) algoritmasına geçer.

*   **Çalışma Mantığı:** "Açgözlü" (Greedy) bir yaklaşımdır. Tank bulunduğu koordinattan tüm diğer ziyaret edilmemiş hedeflere olan mesafeyi ölçer ve her adımda kendisine en yakın olan hedefe yönelir. Tüm hedefler bitene kadar bu yerel optimizasyon döngüsü devam eder.
*   **Asimptotik Karmaşıklık (Big O Notation):** $\mathcal{O}(N^2)$
*   **Teorik Değerlendirme:** Her adımda $N$ adet hedef taranır ve bu işlem $N$ kez tekrarlanır. $100$ hedefli devasa bir simülasyon senaryosunda bile yalnızca $100^2 = 10.000$ işlem yapılır. Kusursuz rotayı garanti etmese de, kabul edilebilir doğrulukta bir rotayı mikro saniyeler içinde hesaplayarak sistemin "Real-Time" (Gerçek Zamanlı) çalışma prensibini korur.

**4. Mimari Sonuç ve Sistem Dengesi**
Uygulanan bu ikili (hibrit) mimari, yazılım mühendisliğindeki "Performans vs. Doğruluk" (Trade-off) problemini çözmektedir. Alt sınırda hesaplama gücü kullanılarak maksimum yakıt tasarrufu sağlanırken, üst sınırda asimptotik karmaşıklık $\mathcal{O}(N^2)$ seviyesine çekilerek simülasyonun çökmesi engellenmiş ve sürdürülebilir bir sistem kararlılığı elde edilmiştir.

**5. Performans, Gecikme (Latency) ve Taktiksel Yönelim Değerlendirmesi**
Hibrit algoritma tasarımında alınan kararlar, savunma sanayii standartlarındaki "Maliyet/Kazanç" (Trade-off) prensipleri ve taktiksel gereksinimler doğrultusunda şekillendirilmiştir:

*   **Planlama vs. Reaksiyon (Soft vs. Hard Real-Time):** Askeri yazılımlarda zaman kısıtlamaları sistemin niteliğine göre değişir. Reaktif sistemlerde (örn. aktif koruma) milisaniyelik gecikmeler kritikken, tasarlanan bu yazılım bir "Görev Planlama Sistemi" (Soft Real-Time) olarak çalışmaktadır. $\mathcal{O}(N!)$ algoritmasının $N \le 10$ sınırında yarattığı maksimum 35-40 milisaniyelik hesaplama süresi, operasyonel sahada insan algı sınırının çok altındadır. Bu cüzi işlemci yükü karşılığında elde edilen %100 kusursuz rota; sahada kilometrelerce yakıt tasarrufu ve minimum risk olarak geri dönmektedir. Bu ödünleşim (trade-off) mimari açıdan tamamen kârlıdır.
*   **Simetrik TSP ve Taktiksel İlk Vuruş (First Strike Time):** Öklid geometrisinde hedefler arası gidiş ve dönüş mesafeleri eşittir (Simetrik TSP). Algoritma, saat yönü veya saat yönünün tersi gibi toplam maliyeti birebir aynı olan eşdeğer rotalar bulduğunda, rastgele bir seçim yapmak yerine "Taktiksel İlk Vuruş" (First Strike Time) prensibini kullanacak bir eşitlik bozucu (tie-breaker) mantığına ihtiyaç duyar. Toplam mesafesi eşit rotalar arasında, **üss'ten ilk hedefe olan mesafesi en kısa olan yön** tercih edilmelidir. Bu taktiksel hamle, sahadaki zırhlı birimin operasyona en hızlı şekilde başlamasını sağlayarak düşmanın yer değiştirme veya önlem alma riskini minimize eder.