# TikTok Bluetooth Swiper

Contrôlez TikTok avec les boutons de votre casque Bluetooth ! 🎧

## 📋 Description

Application Android qui permet de contrôler TikTok en utilisant les boutons media de votre casque Bluetooth :
- **Swipe UP** : Bouton Next (ou Volume Down)
- **Swipe DOWN** : Bouton Previous (ou Volume Up)  
- **Play/Pause** : Pause/Play la vidéo *En projet*

L'application utilise un **AccessibilityService** pour dispatcher les gestes et une **MediaSession** pour intercepter les événements Bluetooth.

## ✨ Fonctionnalités

- ✅ Détection automatique de TikTok au foreground
- ✅ Gestion des boutons media du Bluetooth (Next, Previous)
- ✅ Gestion du focus audio
- ✅ Notification persistante (foreground service)
- ✅ Interface de debug pour voir les touches détectées
- ✅ Logs détaillés pour troubleshooting

## 🛠️ Technologies utilisées

- **Kotlin** - Langage principal
- **Jetpack Compose** - Interface utilisateur
- **AccessibilityService** - Pour dispatcher les gestes
- **MediaSession** - Pour intercepter les boutons Bluetooth
- **Android 11+** (API 30+)



À Faire : 

- Ajout de la fonction Play/Pause
