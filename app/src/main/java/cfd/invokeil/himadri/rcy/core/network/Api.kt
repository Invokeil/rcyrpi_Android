package cfd.invokeil.himadri.rcy.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface — mirrors the website's ~21 server API routes.
 * All GETs tolerate failure via nullable/defaulted response fields.
 */
interface Api {

    // ── auth ───────────────────────────────────────────────────────────
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginBody): AuthUserResponse

    @POST("api/auth/signup")
    suspend fun signup(@Body body: SignupBody): AuthUserResponse

    @POST("api/auth/logout")
    suspend fun logout(): OkResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @POST("api/auth/forgot")
    suspend fun forgot(@Body body: ForgotBody): OkResponse

    @POST("api/auth/reset")
    suspend fun reset(@Body body: ResetBody): OkResponse

    // ── public content ─────────────────────────────────────────────────
    @GET("api/notices")
    suspend fun notices(@Query("category") category: String? = null): NoticesResponse

    @GET("api/items")
    suspend fun items(@Query("kind") kind: String? = null): ItemsResponse

    @GET("api/leaderboard")
    suspend fun leaderboard(): LeaderboardResponse

    @GET("api/public-stats")
    suspend fun publicStats(): StatsResponse

    @GET("api/emergency/feed")
    suspend fun emergencyFeed(): FeedResponse

    @GET("api/requests")
    suspend fun openRequests(): RequestsResponse

    @GET("api/schedule")
    suspend fun schedule(): ScheduleResponse

    @GET("api/contacts")
    suspend fun contacts(): ContactsResponse

    @GET("api/templates")
    suspend fun templates(): TemplatesResponse

    @GET("api/content")
    suspend fun content(): ContentResponse

    @GET("api/verify/{code}")
    suspend fun verify(@Path("code") code: String): VerifyResult

    // ── member (session required) ──────────────────────────────────────
    @GET("api/profile")
    suspend fun profile(): ProfileResponse

    // website exposes profile edit as PATCH (strict zod schema) — not POST
    @PATCH("api/profile")
    suspend fun updateProfile(@Body body: ProfileUpdateBody): ProfileResponse

    @POST("api/profile/password")
    suspend fun changePassword(@Body body: ChangePasswordBody): OkResponse

    @GET("api/payments")
    suspend fun payments(): PaymentsResponse

    @POST("api/payments/checkout")
    suspend fun checkout(@Body body: CheckoutBody): CheckoutResponse

    // member scope requires ?mine=1 (without it the route is admin-only)
    @GET("api/certificates")
    suspend fun certificates(@Query("mine") mine: String? = null): CertificatesResponse

    @GET("api/notifications")
    suspend fun notifications(): NotificationsResponse

    @POST("api/notifications")
    suspend fun markNotifications(@Body body: MarkNotifsBody): OkResponse

    @POST("api/events/{id}/rsvp")
    suspend fun rsvp(@Path("id") eventId: String, @Body body: RsvpBody): RsvpResponse

    @POST("api/requests")
    suspend fun submitEmergencyRequest(@Body body: EmergencyRequestBody): EmergencyRequestResponse

    companion object {
        const val RSVP_GOING = "going"
        const val RSVP_CANCELLED = "cancelled"
    }
}

@Serializable
data class EmergencyRequestResponse(
    val request: EmergencyRequest? = null,
    val error: String? = null,
)
