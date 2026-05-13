package com.shreyas.kreedaankana.features.booking.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()

    // 🔹 Fetch all grounds once (Optional, usually use listenGrounds)
    suspend fun getGrounds(): List<Ground> {
        return try {
            db.collection("grounds").get().await().documents.mapNotNull {
                it.toObject(Ground::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🔹 Create a new ground (ensure ownerId is passed in the Ground object)
    suspend fun createGround(ground: Ground) {
        db.collection("grounds").add(ground).await()
    }

    // 🔹 Update existing ground details or closedDates (Holidays)
    suspend fun updateGround(ground: Ground) {
        db.collection("grounds").document(ground.id).set(ground).await()
    }

    // 🔹 Standard booking
    suspend fun createBooking(booking: Booking) {
        db.collection("bookings").add(booking).await()
    }

    // 🔹 Match-to-Booking conversion
    suspend fun createBookingFromMatch(
        groundId: String,
        groundName: String,
        date: String,
        timeSlot: String,
        userId: String,
        sportType: String
    ) {
        val booking = hashMapOf(
            "groundId" to groundId,
            "groundName" to groundName,
            "date" to date,
            "timeSlot" to timeSlot,
            "userId" to userId,
            "sportType" to sportType,
            "status" to "confirmed"
        )
        db.collection("bookings").add(booking).await()
    }

    // 🔹 Real-time listener for all grounds
    fun listenGrounds(onResult: (List<Ground>) -> Unit) {
        db.collection("grounds").addSnapshotListener { snapshot, _ ->
            val grounds = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Ground::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            onResult(grounds)
        }
    }

    // 🔹 Real-time listener for specific user bookings
    fun listenBookings(userId: String, onResult: (List<Booking>) -> Unit) {
        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onResult(bookings)
            }
    }

    // 🔹 Real-time listener for ground reviews
    fun listenToReviews(groundId: String, onResult: (List<Review>) -> Unit) {
        db.collection("grounds").document(groundId).collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onResult(reviews)
            }
    }

    // 🔹 Post a new review
    suspend fun postReview(groundId: String, review: Review) {
        db.collection("grounds").document(groundId).collection("reviews")
            .add(review.copy(timestamp = Timestamp.now())).await()
    }

    // 🔹 Delete a booking
    suspend fun deleteBooking(bookingId: String) {
        db.collection("bookings").document(bookingId).delete().await()
    }

    suspend fun deleteGround(groundId: String) {
        db.collection("grounds").document(groundId).delete().await()
    }
}