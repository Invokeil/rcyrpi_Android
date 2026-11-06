package cfd.invokeil.himadri.rcy.screens.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/**
 * Terms & Conditions — faithful port of website terms-page.tsx:
 * 8 bilingual beginner-friendly sections with red numbered badges.
 */
@Composable
fun TermsScreen(vm: AppViewModel, nav: NavHostController) {
    val lang = LocalLang.current

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = null)
            }
        }

        // ── heading ───────────────────────────────────────────────────
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(48.dp).background(BrandRed.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcon.of("shield"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                tl("foot_terms"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = BrandRed,
                textAlign = TextAlign.Center,
            )
            Text(
                if (lang.isBn) "সহজ ভাষায় আমাদের নিয়মকানুন — একবার পড়ে নিন"
                else "Our rules in simple language — please read once",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Box(Modifier.size(width = 64.dp, height = 4.dp).background(BrandGradient, RoundedCornerShape(2.dp)))
        }

        // ── sections ──────────────────────────────────────────────────
        TERMS_SECTIONS.forEachIndexed { i, s ->
            BrandCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp)) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .background(BrandGradient, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${i + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (lang.isBn) s.bnTitle else s.enTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (lang.isBn) s.bnBody else s.enBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // ── footer tagline ────────────────────────────────────────────
        Text(
            tl("foot_tagline"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
        )
    }
}

private data class TermSection(val enTitle: String, val enBody: String, val bnTitle: String, val bnBody: String)

/** Copy from website src/components/pages/terms-page.tsx (bilingual, sections 1–8). */
private val TERMS_SECTIONS = listOf(
    TermSection(
        "Who we are",
        "We are the Youth Red Crescent Team (RCY) of Rangpur Govt. Polytechnic Institute — a youth unit of the Bangladesh Red Crescent Society (BDRCS). We are trained volunteers: we collect blood, respond to disasters and organize humanitarian events. By using this website you agree to the simple rules below.",
        "আমরা কে?",
        "আমরা রংপুর সরকারি পলিটেকনিক ইনস্টিটিউটের যুব রেড ক্রিসেন্ট দল (সংক্ষেপে RCY) — বাংলাদেশ রেড ক্রিসেন্ট সোসাইটির (বিডিআরসিএস) একটি যুব ইউনিট। আমরা প্রশিক্ষিত স্বেচ্ছাসেবী: রক্ত সংগ্রহ করি, দুর্যোগে সাড়া দিই এবং মানবিক ইভেন্ট আয়োজন করি। এই ওয়েবসাইট ব্যবহার করলে আপনি নিচের সহজ নিয়মগুলো মানছেন।",
    ),
    TermSection(
        "Membership rules",
        "Members must be respectful to everyone, keep their profile information true and up to date, and stay active in unit activities. Admins may suspend fraudulent or fake accounts. When an account is suspended, all of its posts (such as emergency requests) automatically disappear from the public website, and the account can no longer log in until it is reactivated.",
        "সদস্যপদের নিয়ম",
        "সদস্যদের সবার প্রতি শ্রদ্ধাশীল থাকতে হবে, প্রোফাইলের তথ্য সঠিক ও হালনাগাদ রাখতে হবে এবং ইউনিটের কার্যক্রমে সক্রিয় থাকতে হবে। প্রতারণামূলক বা ভুয়া একাউন্ট অ্যাডমিন সাসপেন্ড করতে পারেন। কোনো একাউন্ট সাসপেন্ড হলে তার সব পোস্ট (যেমন ইমার্জেন্সি রিকোয়েস্ট) পাবলিক ওয়েবসাইট থেকে স্বয়ংক্রিয়ভাবে সরে যায়, এবং পুনরায় চালু না হওয়া পর্যন্ত সেই একাউন্টে লগইন করা যায় না।",
    ),
    TermSection(
        "Emergency request usage",
        "The emergency request form is only for real, urgent needs — blood, accident, fire, flood, medical help or a missing person. Sending false or prank requests wastes volunteer time and can put lives in danger. False requests = account suspension. Please be honest; a real person is waiting for help.",
        "ইমার্জেন্সি রিকোয়েস্ট ব্যবহার",
        "ইমার্জেন্সি রিকোয়েস্ট ফর্ম শুধু সত্যিকারের জরুরি প্রয়োজনে ব্যবহার করুন — রক্ত, দুর্ঘটনা, আগুন, বন্যা, চিকিৎসা সাহায্য বা নিখোঁজ ব্যক্তি। মিথ্যা বা মজা করে রিকোয়েস্ট পাঠালে স্বেচ্ছাসেবীদের সময় নষ্ট হয় এবং জীবন বিপদে পড়তে পারে। মিথ্যা রিকোয়েস্ট = একাউন্ট সাসপেনশন। সততার সাথে ব্যবহার করুন — কেউ একজন সত্যিই সাহায্যের অপেক্ষায় থাকে।",
    ),
    TermSection(
        "Payments & refunds",
        "All payments are made online through our secure payment gateway. Every event or fund that has a fee gets a shareable Payment Link. After you pay, the record is verified automatically and appears in your payments list. Admins may also record offline payments, and each record shows which admin recorded it. Always keep your transaction ID as proof. Event fees are non-refundable unless the event is cancelled or postponed by the admin, and donations, once given, are non-refundable — they go straight to humanitarian activities.",
        "পেমেন্ট ও রিফান্ড",
        "সব পেমেন্ট করা হয় আমাদের নিরাপদ পেমেন্ট গেটওয়ের মাধ্যমে, অনলাইনে। যে ইভেন্ট বা ফান্ডের ফি আছে, প্রতিটির জন্য একটি শেয়ারযোগ্য পেমেন্ট লিংক দেওয়া হয়। পেমেন্ট করার পর রেকর্ডটি স্বয়ংক্রিয়ভাবে যাচাই হয়ে আপনার পেমেন্ট লিস্টে দেখা যায়। অ্যাডমিন চাইলে অফলাইন পেমেন্টও রেকর্ড করতে পারেন, এবং রেকর্ডে দেখা যায় কোন অ্যাডমিন সেটি যোগ করেছেন। প্রমাণ হিসেবে সবসময় ট্রানজেকশন আইডি সংরক্ষণ করুন। অ্যাডমিন ইভেন্ট বাতিল বা স্থগিত না করলে ইভেন্ট ফি ফেরতযোগ্য নয়, আর ডোনেশন একবার দেওয়ার পর ফেরত দেওয়া হয় না — তা সরাসরি মানবিক কাজে ব্যয় হয়।",
    ),
    TermSection(
        "Emergency popups",
        "When a critical emergency happens, the site shows an urgent popup alert on every visitor's screen. The cancel button stays locked for 3 seconds so alerts are not missed by accident. Please read the alert carefully — it may be a call for blood or rescue nearby.",
        "ইমার্জেন্সি পপআপ",
        "গুরুতর ইমার্জেন্সি হলে সাইট প্রতিটি ভিজিটরের স্ক্রিনে জরুরি পপআপ সতর্কতা দেখায়। ভুলে মিস না হওয়ার জন্য ক্যান্সেল বাটন ৩ সেকেন্ড লক থাকে। সতর্কতাটি মনোযোগ দিয়ে পড়ুন — এটি আপনার কাছের কারো রক্ত বা উদ্ধারের ডাক হতে পারে।",
    ),
    TermSection(
        "Data usage",
        "Your name, phone, blood group and address are used only for unit activities: emergency response, blood matching and event management. We never sell or share your personal data with any third party. For account security, use the \u201CForgot password\u201D option on the login page to reset your password securely.",
        "তথ্যের ব্যবহার",
        "আপনার নাম, ফোন, রক্তের গ্রুপ ও ঠিকানা শুধু ইউনিটের কাজে ব্যবহৃত হয়: ইমার্জেন্সি রেসপন্স, রক্ত ম্যাচিং ও ইভেন্ট ব্যবস্থাপনা। আমরা কখনো আপনার ব্যক্তিগত তথ্য তৃতীয় পক্ষের কাছে বিক্রি বা শেয়ার করি না। একাউন্টের নিরাপত্তার জন্য লগইন পেজের \u201Cফরগট পাসওয়ার্ড\u201D অপশন ব্যবহার করে নিরাপদে পাসওয়ার্ড রিসেট করুন।",
    ),
    TermSection(
        "Content rights",
        "Notices, event details and materials on this site belong to the Youth Red Crescent Team (RCY) of Rangpur Govt. Polytechnic Institute. Please take permission before copying or reusing any content for other purposes.",
        "কনটেন্টের অধিকার",
        "এই সাইটের নোটিশ, ইভেন্টের তথ্য ও ম্যাটেরিয়াল রংপুর সরকারি পলিটেকনিক ইনস্টিটিউটের যুব রেড ক্রিসেন্ট দলের (RCY) সম্পদ। অন্য কোনো কাজে কনটেন্ট কপি বা পুনঃব্যবহারের আগে অনুমতি নিন।",
    ),
    TermSection(
        "Contact",
        "Questions about these terms? Visit our unit room at Rangpur Govt. Polytechnic Institute campus, Rangpur-5400, or email us at admin@example.org. We are always happy to explain anything.",
        "যোগাযোগ",
        "এই শর্তাবলী নিয়ে প্রশ্ন আছে? আমাদের ইউনিট রুমে আসুন — রংপুর সরকারি পলিটেকনিক ইনস্টিটিউট, রংপুর-৫৪০০ — অথবা ইমেইল করুন admin@example.org ঠিকানায়। আমরা সবকিছু বুঝিয়ে বলতে সবসময় আনন্দিত।",
    ),
)
