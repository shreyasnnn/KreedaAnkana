package com.shreyas.kreedaankana.features.notifications.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class AppNotification(
    var id: String = "",
    var receiverId: String = "",
    var title: String = "",
    var message: String = "",
    var type: String = "info",
    var relatedId: String = "",

    // 🔹 Ensures Firestore maps this perfectly
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,

    var timestamp: Timestamp? = null
)