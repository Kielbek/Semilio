package com.example.semilio.notification;

public enum NotificationType {
    CHAT_MESSAGE,       // Wiadomości
    SYSTEM_ALERT,       // Info od admina
    PRODUCT_LIKE,       // "Użytkownik X polubił Twój przedmiot Y" <--- TO DODAJEMY
    PRICE_DROP,         // "Cena przedmiotu, który obserwujesz, spadła"
    ITEM_SOLD           // "Twój przedmiot został sprzedany"
}
