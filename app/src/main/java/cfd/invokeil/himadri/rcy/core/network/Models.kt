package cfd.invokeil.himadri.rcy.core.network

import cfd.invokeil.himadri.rcy.core.i18n.LText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ────────────────────────────────────────────────────────────────────────
// API models — mirror website src/lib/types.ts (Supabase table shapes).
// All fields nullable/defaulted: the server may add columns at any time.
// ────────────────────────────────────────────────────────────────────────

@Serializable
data class Address(
    val district: String = "",
    val upazila: String = "",
    val thana: String = "",
    val ward: String = "",
    val para: String = "",
)

@Serializable
data class SessionUser(
    val id: String = "",
    val username: String = "",
    val full_name: String = "",
    val role: String = "member",
    val avatar_url: String? = null,
    val blood_group: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: Address? = null,
    val member_no: String? = null,
    val team: String? = null,
    val sub_team: String? = null,
    val upazila_unit: String? = null,
    val status: String? = "active",
    val phone_verified: Boolean? = null,
    val created_at: String? = null,
) {
    val isAdmin: Boolean get() = role == "admin"
    val displayName: String get() = full_name.ifBlank { username }
}

@Serializable
data class Impersonator(
    val id: String = "",
    val full_name: String = "",
    val username: String = "",
)

@Serializable
data class MeResponse(
    val user: SessionUser? = null,
    val impersonator: Impersonator? = null,
)

@Serializable
data class AuthUserResponse(
    val user: SessionUser? = null,
    val error: String? = null,
)

@Serializable
data class OkResponse(val ok: Boolean = false, val error: String? = null)

@Serializable
data class Notice(
    val id: String = "",
    val title: LText = LText.EMPTY,
    val body: LText = LText.EMPTY,
    val category: String = "general",
    val severity: String = "low",
    val is_pinned: Boolean = false,
    val show_popup: Boolean = false,
    val created_by: String? = null,
    val created_at: String = "",
    val expires_at: String? = null,
)

@Serializable
data class NoticesResponse(val notices: List<Notice> = emptyList(), val error: String? = null)

@Serializable
data class Item(
    val id: String = "",
    val kind: String = "event",
    val title: LText = LText.EMPTY,
    val description: LText = LText.EMPTY,
    val icon: String = "calendar",
    val amount: Double = 0.0,
    val payment_required: Boolean = false,
    val deadline: String? = null,
    val event_date: String? = null,
    val location: String? = null,
    val map_link: String? = null,
    val status: String = "active",
    val postpone_note: LText? = null,
    val volunteer_hours: Int? = null,
    val rsvp_enabled: Boolean? = null,
    val rsvp_label: LText? = null,
    val created_by: String? = null,
    val created_at: String = "",
) {
    val isActive: Boolean get() = status == "active"
    val isEvent: Boolean get() = kind == "event"
    val isFund: Boolean get() = kind == "donation" || kind == "gift"
}

@Serializable
data class ItemsResponse(val items: List<Item> = emptyList(), val error: String? = null)

@Serializable
data class ItemResponse(val item: Item? = null, val error: String? = null)

@Serializable
data class EmergencyRequest(
    val id: String = "",
    val kind: String = "blood",
    val urgency: String = "immediate",
    val patient_name: String? = null,
    val phone: String = "",
    val alt_phone: String? = null,
    val hospital: String? = null,
    val blood_group: String? = null,
    val patient_type: String? = null,
    val location: String = "",
    val detail_location: String? = null,
    val needed_at: String? = null,
    val note: String? = null,
    val units: Int? = null,
    val status: String = "open",
    val created_by: String? = null,
    val created_at: String = "",
)

@Serializable
data class RequestsResponse(val requests: List<EmergencyRequest> = emptyList(), val error: String? = null)

@Serializable
data class FeedAlert(
    val id: String = "",
    val severity: String = "high",
    val source: String = "admin",
    val title: LText = LText.EMPTY,
    val body: LText = LText.EMPTY,
    val link: String? = null,
    val created_at: String = "",
    val phone: String? = null,
    val hospital: String? = null,
    val location: String? = null,
)

@Serializable
data class FeedResponse(val alerts: List<FeedAlert> = emptyList(), val error: String? = null)

@Serializable
data class Stats(
    val active_members: Int = 0,
    val events_completed: Int = 0,
    val blood_bags: Int = 0,
    val volunteer_hours: Int = 0,
)

@Serializable
data class StatsResponse(val stats: Stats = Stats(), val error: String? = null)

@Serializable
data class LeaderBadge(val key: String = "", val icon: String = "")

@Serializable
data class LeaderRow(
    val member_id: String = "",
    val full_name: String = "",
    val username: String = "",
    val avatar_url: String? = null,
    val blood_group: String? = null,
    val member_no: String? = null,
    val team: String? = null,
    val blood_count: Int = 0,
    val streak: Int = 0,
    val hours: Int = 0,
    val badges: List<LeaderBadge> = emptyList(),
)

@Serializable
data class LeaderboardResponse(
    val rows: List<LeaderRow> = emptyList(),
    val rules: BadgeRules? = null,
    val error: String? = null,
)

@Serializable
data class BadgeRules(
    @SerialName("first_drop") val firstDrop: Int = 1,
    @SerialName("rising_donor") val risingDonor: Int = 3,
    @SerialName("life_saver") val lifeSaver: Int = 10,
    @SerialName("streak_days") val streakDays: Int = 180,
    @SerialName("streak_count") val streakCount: Int = 2,
)

@Serializable
data class DonationLog(
    val id: String = "",
    val member_id: String = "",
    val donated_on: String = "",
    val place: String? = null,
    val units: Int = 1,
    val note: String? = null,
    val verified: Boolean = false,
    val created_at: String = "",
)

@Serializable
data class Eligibility(
    val eligible: Boolean = true,
    @SerialName("lastDate") val last_date: String? = null,
    @SerialName("nextDate") val next_date: String? = null,
    @SerialName("daysLeft") val days_left: Int = 0,
    val count: Int = 0,
    @SerialName("gap_days") val gap_days: Int = 120,
)

@Serializable
data class Certificate(
    val id: String = "",
    val serial: String = "",
    val member_id: String = "",
    val template: String = "appreciation",
    val title: String = "",
    val fields: Map<String, String> = emptyMap(),
    val issued_by: String? = null,
    val auto: Boolean? = null,
    val emailed_at: String? = null,
    val created_at: String = "",
)

@Serializable
data class ProfileResponse(
    val profile: SessionUser? = null,
    val donations: List<DonationLog> = emptyList(),
    val eligibility: Eligibility? = null,
    val certificates: List<Certificate> = emptyList(),
    val error: String? = null,
)

@Serializable
data class CertificatesResponse(val certificates: List<Certificate> = emptyList(), val error: String? = null)

@Serializable
data class PaymentRecord(
    val id: String = "",
    val tran_id: String = "",
    val member_id: String = "",
    val member_name: String = "",
    val item_id: String = "",
    val item_title: LText = LText.EMPTY,
    val amount: Double = 0.0,
    val method: String = "gateway",
    val status: String = "pending",
    val gateway_ref: String? = null,
    val gateway_trxid: String? = null,
    val gateway_method: String? = null,
    val sender_number: String? = null,
    val note: String? = null,
    val source: String? = "gateway",
    val admin_username: String? = null,
    val admin_name: String? = null,
    val created_at: String = "",
    val verified_at: String? = null,
    val verified_by: String? = null,
)

@Serializable
data class PaymentsResponse(val payments: List<PaymentRecord> = emptyList(), val error: String? = null)

/** POST /api/payments/checkout response — fields vary by gateway config */
@Serializable
data class CheckoutResponse(
    val tran_id: String? = null,
    val item_id: String? = null,
    val amount: Double? = null,
    // PipraPay success payload
    val payment_url: String? = null,
    val url: String? = null,
    // mock checkout mode
    val mock: Boolean? = null,
    val error: String? = null,
) {
    val resolvedUrl: String? get() = payment_url ?: url
}

@Serializable
data class AppNotification(
    val id: String = "",
    val kind: String = "info",
    val title: String = "",
    val body: String? = null,
    val link: String? = null,
    val created_at: String = "",
    val read: Boolean = false,
)

@Serializable
data class NotificationsResponse(
    val unread: Int = 0,
    val items: List<AppNotification> = emptyList(),
    val error: String? = null,
)

@Serializable
data class VerifyResult(
    val verified: Boolean = false,
    val full_name: String? = null,
    val username: String? = null,
    val blood_group: String? = null,
    val member_no: String? = null,
    val team: String? = null,
    val status: String? = null,
    val member_since: String? = null,
    val error: String? = null,
)

@Serializable
data class Contact(
    val id: String = "",
    val kind: String = "phone",
    val label: LText = LText.EMPTY,
    val value: String = "",
    val icon: String = "phone",
    val active: Boolean = true,
    val sort: Int = 0,
    val created_at: String = "",
)

@Serializable
data class ContactsResponse(val contacts: List<Contact> = emptyList(), val error: String? = null)

@Serializable
data class ScheduleRow(
    val id: String = "",
    val day: String = "sat",
    val time_text: String = "",
    val activity: LText = LText.EMPTY,
    val place: LText = LText.EMPTY,
    val icon: String = "calendar",
    val sort: Int = 0,
    val active: Boolean = true,
    val created_at: String = "",
)

@Serializable
data class ScheduleResponse(val schedule: List<ScheduleRow> = emptyList(), val error: String? = null)

@Serializable
data class TplField(
    val key: String = "",
    val label: LText = LText.EMPTY,
    val type: String = "text",
    val options: List<String> = emptyList(),
    val required: Boolean = false,
    val placeholder: LText? = null,
)

@Serializable
data class Tpl(
    val id: String = "",
    val group: String = "emergency",
    val icon: String = "siren",
    val title: LText = LText.EMPTY,
    val severity: String = "medium",
    val fields: List<TplField> = emptyList(),
    val defaults: TplDefaults = TplDefaults(),
)

@Serializable
data class TplDefaults(val title: LText = LText.EMPTY, val body: LText = LText.EMPTY)

@Serializable
data class TemplatesResponse(val templates: List<Tpl> = emptyList(), val error: String? = null)

@Serializable
data class HomeSectionCfg(
    val key: String = "hero",
    val visible: Boolean = true,
    val title: LText? = null,
    val subtitle: LText? = null,
)

@Serializable
data class ContentResponse(val sections: List<HomeSectionCfg> = emptyList(), val error: String? = null)

@Serializable
data class RsvpResponse(val ok: Boolean = false, val mine: String? = null, val error: String? = null)

// ── request bodies ─────────────────────────────────────────────────────

@Serializable
data class LoginBody(val id: String, val password: String, val role: String = "member")

@Serializable
data class SignupBody(
    val username: String,
    val full_name: String,
    val email: String,
    val phone: String,
    val alt_phone: String? = null,
    val password: String,
    val blood_group: String? = null,
    val address: Address,
    val avatar_url: String? = null,
    // website zod schema: z.literal(true) — explicit consent is mandatory
    val agree_terms: Boolean = false,
)

@Serializable
data class ForgotBody(
    // website reads { email, lang } — the login-id field name is NOT accepted
    val email: String,
    val lang: String? = null,
)

@Serializable
data class ResetBody(val token: String, val password: String)

@Serializable
data class RsvpBody(val going: Boolean)

@Serializable
data class EmergencyRequestBody(
    val kind: String,
    val urgency: String? = null,
    val patient_name: String? = null,
    val phone: String,
    val alt_phone: String? = null,
    val hospital: String? = null,
    val blood_group: String? = null,
    val patient_type: String? = null,
    val location: String,
    val detail_location: String? = null,
    val needed_at: String? = null,
    val note: String? = null,
)

@Serializable
data class CheckoutBody(val item_id: String)

/** POST /api/notifications — website expects { notice_ids?, app_ids? } (raw ids, no prefix) */
@Serializable
data class MarkNotifsBody(
    val notice_ids: List<String>? = null,
    val app_ids: List<String>? = null,
)

@Serializable
data class ChangePasswordBody(
    // website zod schema: { current_password, new_password }
    @SerialName("current_password") val current: String,
    @SerialName("new_password") val password: String,
)

@Serializable
data class ProfileUpdateBody(
    val full_name: String? = null,
    val phone: String? = null,
    val alt_phone: String? = null,
    val email: String? = null,
    val blood_group: String? = null,
    val address: Address? = null,
    val avatar_url: String? = null,
)
