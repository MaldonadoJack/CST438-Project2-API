package com.fittrack.api.workout

import com.fittrack.api.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "workouts")
class Workout(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String = "",

    @Column(name = "performed_at")
    var performedAt: OffsetDateTime? = null,

    @Column(name = "is_completed", nullable = false)
    var isCompleted: Boolean = false,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null
)
