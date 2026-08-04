# Tank Görev Simülatörü

## Sistem Mimarisi (Use Case Diagram)
![Tank Simülasyonu Use Case Diyagramı](images/use-case-diagram.png)

## Sistem Mimarisi (UML Sınıf Diyagramı - V2)

Projenin teknik altyapısı; sürdürülebilirlik, modülerlik ve savunma sanayii standartlarına uygun olarak tasarlanmıştır. Sistem; loglama mekanizması, durum yönetimi ve merkezi görev yöneticisi ile desteklenmektedir.

![Sistem Mimarisi V2](images/Tank_Simulation_Class_Diagram_V2.png)

## Kullanılan Teknolojiler ve Altyapı
* **Loglama:** Sürdürülebilirlik ve modern standartlar gereği `SLF4J` (Arayüz) ve `Logback` (Motor) kullanılacaktır.

### 4. Hafta - Görev Başlatma ve Algoritma Akışı (Sequence Diagram)
Kullanıcının simülasyonu başlatmasıyla birlikte; UI katmanından Business Logic katmanına giden veri akışı, hibrit TSP algoritmasının tetiklenmesi ve testlerin çalışma sırası aşağıdaki UML Ardışık Düzen (Sequence) diyagramında modellenmiştir:

![Sequence Diagram](images/sequence-diagram-start-mission.drawio.png)