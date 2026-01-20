# 📘 UdhaarBook: AI-Native Financial Ledger
**A Cloud-Synced Android Ecosystem for Seamless Credit & Inventory Management.**

UdhaarBook is a high-performance, offline-first Android application built for modern business owners. It bridges the gap between traditional bookkeeping and intelligent automation, featuring real-time cloud synchronization, AI-driven financial insights, and automated inventory scanning.

---

## 🛠 Strategic Tech Stack
* **Language:** Kotlin
* **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture
* **Local Database:** Room Persistence Library (Offline-First)
* **Cloud Backend:** Firebase (Auth, Firestore)
* **AI/ML:** Integrated LLM for financial queries + Computer Vision (OCR/Object Detection) for product scanning
* **Reporting:** Apache POI for dynamic Excel generation

---

## 🚀 Key Technical Features

### ☁️ Cloud & Data Integrity
* **Real-time Bidirectional Sync:** Engineered a robust synchronization engine that maintains data parity between the **Room Local DB** and **Firebase Firestore**, ensuring 100% availability during network outages.
* **Firebase Authentication:** Secure user onboarding with isolated data environments for multi-user privacy.
* **Cross-Platform Persistence:** Real-time updates across multiple devices; any change on one device reflects instantly across the cloud ecosystem.

### 🤖 AI & Intelligent Automation
* **AI Financial Chatbot:** A "Human-in-the-Loop" assistant integrated with the local database to provide natural language insights (e.g., *"Calculate my total pending collections for January"*).
* **AI Product Scanning:** Integrated Computer Vision to scan products on-the-go, automatically fetching and populating product details into the inventory.

### 📊 Financial Governance & Reporting
* **Dynamic Excel Generation:** On-the-go creation of formatted .xlsx spreadsheets for professional auditing and customer sharing.
* **Advanced Data Sorting:** A granular transaction engine allowing users to filter and sort records by date, payment status, or account type.
* **CRUD Lifecycle Management:** Full-scale profile and transaction management with recursive deletes/updates across both local and cloud layers.

---

## 📐 Architecture Design
The application follows **Clean Architecture** principles to ensure scalability:
1.  **UI Layer:** Jetpack Compose / XML for a responsive, modern interface.
2.  **Domain Layer:** Business logic for interest calculation and credit balancing.
3.  **Data Layer:** The "Source of Truth" manager that handles the logic between Local Room DB and Remote Firebase.

---

## 🏗 Installation & Setup
1. Clone the repository: `git clone https://github.com/yourusername/UdhaarBook.git`
2. Add your `google-services.json` to the `/app` directory.
3. Obtain an API Key for the AI integration and add it to your `local.properties`.
4. Build and run on Android Studio.

---


## 🤝 Contributing
Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".

1. **Fork** the Project
2. Create your **Feature Branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit** your Changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the Branch (`git push origin feature/AmazingFeature`)
5. Open a **Pull Request**

---

## 📄 License
This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contact
**Junaid Mir** *Full Stack Software Developer* [LinkedIn](https://www.linkedin.com/in/junaid-mir-68b87b1b9/) | [Email](junaidmeer055@gmail.com)
