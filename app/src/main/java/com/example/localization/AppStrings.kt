package com.example.localization

import androidx.compose.runtime.Immutable

@Immutable
data class AppStrings(
    val appName: String,
    val navHome: String,
    val navServers: String,
    val navSettings: String,
    val statusDisconnected: String,
    val statusConnecting: String,
    val statusConnected: String,
    val statusReconnecting: String,
    val statusError: String,
    val tapToConnect: String,
    val tapToDisconnect: String,
    val connectingLabel: String,
    val reconnectingLabel: String,
    val encryptionBadge: String,
    val currentIpTitle: String,
    val ipUnavailable: String,
    val ipChecking: String,
    val refreshIp: String,
    val selectedLocationTitle: String,
    val switchServer: String,
    val durationTitle: String,
    val downloadSpeedTitle: String,
    val uploadSpeedTitle: String,
    val dataUsageTitle: String,
    val sessionUsageLabel: String,
    val totalUsageLabel: String,
    val securityProtectionTitle: String,
    val dnsLeakProtection: String,
    val autoReconnect: String,
    val killSwitchLabel: String,
    val activeLabel: String,
    val enabledLabel: String,
    val disabledLabel: String,
    val systemManagedLabel: String,
    val configureKillSwitch: String,
    val configSourceApi: String,
    val configSourceImported: String,
    val configSourceNone: String,
    val serversScreenTitle: String,
    val serversScreenSubtitle: String,
    val refreshPing: String,
    val measuringPing: String,
    val serverOnline: String,
    val serverOffline: String,
    val serverNeedsConfig: String,
    val importConfigButton: String,
    val pasteConfigButton: String,
    val clearConfigButton: String,
    val publicKeyCardTitle: String,
    val publicKeyCardDesc: String,
    val copyPublicKey: String,
    val copiedToast: String,
    val pasteConfigDialogTitle: String,
    val pasteConfigPlaceholder: String,
    val saveConfig: String,
    val saveAndConnect: String,
    val quickSetupButton: String,
    val setupGuideBanner: String,
    val yourPublicKeyLabel: String,
    val cancel: String,
    val invalidConfigError: String,
    val configSavedMessage: String,
    val settingsTitle: String,
    val sectionAppearanceLanguage: String,
    val languageLabel: String,
    val themeModeLabel: String,
    val themeSystem: String,
    val themeDark: String,
    val themeLight: String,
    val sectionVpnSecurity: String,
    val dnsProviderLabel: String,
    val killSwitchCardDesc: String,
    val openAndroidVpnSettings: String,
    val regenerateKeysLabel: String,
    val regenerateKeysDesc: String,
    val keysRegeneratedMessage: String,
    val sectionSupportLegal: String,
    val contactSupport: String,
    val shareApp: String,
    val rateApp: String,
    val aboutApp: String,
    val privacyPolicy: String,
    val termsConditions: String,
    val premiumCardTitle: String,
    val premiumCardSubtitle: String,
    val premiumActiveBadge: String,
    val upgradeToPro: String,
    val rewardedAdTitle: String,
    val rewardedAdDesc: String,
    val watchAdButton: String,
    val adFreePassActive: String,
    val sponsoredBannerTitle: String,
    val sponsoredOfferOne: String,
    val sponsoredOfferTwo: String,
    val adBadgeLabel: String,
    val premiumScreenTitle: String,
    val premiumHeroTitle: String,
    val premiumHeroSubtitle: String,
    val benefitNoAds: String,
    val benefitPriorityRouting: String,
    val benefitUnlimitedSwitching: String,
    val benefitSupportDev: String,
    val monthlyPlanTitle: String,
    val monthlyPlanFallbackPrice: String,
    val yearlyPlanTitle: String,
    val yearlyPlanFallbackPrice: String,
    val subscribeButton: String,
    val restorePurchasesButton: String,
    val billingStatusUnavailable: String,
    val vpnPermissionDenied: String,
    val errNoServerConfig: String,
    val errProvisioningFailed: String,
    val errTunnelStartFailed: String,
    val errNetworkOffline: String,
    val shareAppMessage: String,
    val supportEmailSubject: String,
    val aboutBody: String,
    val privacyPolicyBody: String,
    val termsBody: String,
    val countryNames: Map<String, String>,
    val cityNames: Map<String, String>
)

val EnglishStrings = AppStrings(
    appName = "MOZAFFAR VPN",
    navHome = "Dashboard",
    navServers = "Servers",
    navSettings = "Settings",
    statusDisconnected = "Disconnected",
    statusConnecting = "Establishing Tunnel…",
    statusConnected = "Protected • Connected",
    statusReconnecting = "Reconnecting Tunnel…",
    statusError = "Connection Failed",
    tapToConnect = "CONNECT",
    tapToDisconnect = "DISCONNECT",
    connectingLabel = "CONNECTING",
    reconnectingLabel = "RECONNECTING",
    encryptionBadge = "WireGuard • ChaCha20-Poly1305",
    currentIpTitle = "Current Public IP",
    ipUnavailable = "Unavailable",
    ipChecking = "Checking…",
    refreshIp = "Refresh IP",
    selectedLocationTitle = "Selected Server",
    switchServer = "Change",
    durationTitle = "Duration",
    downloadSpeedTitle = "Download",
    uploadSpeedTitle = "Upload",
    dataUsageTitle = "Tunnel Data Usage",
    sessionUsageLabel = "Session",
    totalUsageLabel = "Total",
    securityProtectionTitle = "Tunnel Security Guard",
    dnsLeakProtection = "DNS Leak Protection",
    autoReconnect = "Auto-Reconnect",
    killSwitchLabel = "System Kill Switch",
    activeLabel = "Active",
    enabledLabel = "Enabled",
    disabledLabel = "Disabled",
    systemManagedLabel = "Lockdown On",
    configureKillSwitch = "OS Settings",
    configSourceApi = "Provisioned API",
    configSourceImported = "WireGuard Profile",
    configSourceNone = "Setup Required",
    serversScreenTitle = "VPN Server Locations",
    serversScreenSubtitle = "Real TCP latency & WireGuard tunnel endpoints",
    refreshPing = "Ping All",
    measuringPing = "Pinging…",
    serverOnline = "Reachable",
    serverOffline = "Unreachable",
    serverNeedsConfig = "Needs Config",
    importConfigButton = "Import .conf",
    pasteConfigButton = "Paste Config",
    clearConfigButton = "Remove",
    publicKeyCardTitle = "Device WireGuard Public Key",
    publicKeyCardDesc = "Generated locally using Curve25519. Authorize this key on your WireGuard server or use the provisioning API.",
    copyPublicKey = "Copy Key",
    copiedToast = "Copied to clipboard",
    pasteConfigDialogTitle = "Import WireGuard Configuration",
    pasteConfigPlaceholder = "Paste your real WireGuard .conf text here ([Interface] and [Peer])…",
    saveConfig = "Save Profile",
    saveAndConnect = "Save & Connect",
    quickSetupButton = "Paste / Import .conf",
    setupGuideBanner = "Paste a WireGuard .conf profile or pick a .conf file to establish a real encrypted tunnel.",
    yourPublicKeyLabel = "Device Public Key",
    cancel = "Cancel",
    invalidConfigError = "Invalid WireGuard configuration. Check [Interface] and [Peer] fields.",
    configSavedMessage = "WireGuard profile saved securely.",
    settingsTitle = "Settings & Privacy",
    sectionAppearanceLanguage = "Language & Appearance",
    languageLabel = "App Language",
    themeModeLabel = "Theme Mode",
    themeSystem = "System",
    themeDark = "Dark",
    themeLight = "Light",
    sectionVpnSecurity = "VPN Security & Protocol",
    dnsProviderLabel = "Protected DNS Resolver",
    killSwitchCardDesc = "Android enforces hardware Kill Switch via Settings > VPN > MOZAFFAR VPN > Block connections without VPN.",
    openAndroidVpnSettings = "Open Android VPN Settings",
    regenerateKeysLabel = "Regenerate Curve25519 KeyPair",
    regenerateKeysDesc = "Rotates your local WireGuard private/public identity keys.",
    keysRegeneratedMessage = "New Curve25519 key pair generated.",
    sectionSupportLegal = "Support & Legal",
    contactSupport = "Contact Support",
    shareApp = "Share Application",
    rateApp = "Rate Application",
    aboutApp = "About MOZAFFAR VPN",
    privacyPolicy = "Privacy Policy",
    termsConditions = "Terms & Conditions",
    premiumCardTitle = "MOZAFFAR VPN Premium",
    premiumCardSubtitle = "Remove all advertisements & unlock priority support",
    premiumActiveBadge = "PRO ACTIVE",
    upgradeToPro = "Go Ad-Free",
    rewardedAdTitle = "Free 2-Hour Ad-Free Pass",
    rewardedAdDesc = "Watch a short optional video ad to disable banners & interstitials for 2 hours.",
    watchAdButton = "Watch Ad",
    adFreePassActive = "Ad-Free Pass Active",
    sponsoredBannerTitle = "Sponsored Partner Offers",
    sponsoredOfferOne = "Featured Partner Offer 1",
    sponsoredOfferTwo = "Featured Partner Offer 2",
    adBadgeLabel = "Ad",
    premiumScreenTitle = "Premium Ad-Free",
    premiumHeroTitle = "Uninterrupted Privacy",
    premiumHeroSubtitle = "Support independent WireGuard infrastructure and enjoy a completely ad-free interface.",
    benefitNoAds = "Zero banner, native, or interstitial ads",
    benefitPriorityRouting = "Priority server provisioning & DNS options",
    benefitUnlimitedSwitching = "Instant unlimited server switching",
    benefitSupportDev = "Directly funds secure server maintenance",
    monthlyPlanTitle = "Monthly Pass",
    monthlyPlanFallbackPrice = "$2.99 / month",
    yearlyPlanTitle = "Annual Pass (Best Value)",
    yearlyPlanFallbackPrice = "$24.99 / year",
    subscribeButton = "Subscribe via Google Play",
    restorePurchasesButton = "Restore Purchases",
    billingStatusUnavailable = "Google Play Billing requires installing via Google Play with active SKU configuration.",
    vpnPermissionDenied = "VPN permission was denied by the user. Authorize Android VPN access to establish an encrypted tunnel.",
    errNoServerConfig = "No live WireGuard profile or provisioning server is configured for this location. Import a .conf file or configure VPN_API_BASE_URL.",
    errProvisioningFailed = "Server authentication or provisioning failed. Verify VPN_API_BASE_URL and VPN_API_TOKEN or import a .conf profile.",
    errTunnelStartFailed = "WireGuard kernel/userspace tunnel failed to establish.",
    errNetworkOffline = "No active internet connection detected.",
    shareAppMessage = "Protect your privacy with MOZAFFAR VPN — genuine WireGuard encryption, DNS leak protection, and clean Android design.",
    supportEmailSubject = "MOZAFFAR VPN Support Request",
    aboutBody = """
MOZAFFAR VPN is a native Android WireGuard client engineered for transparency, verifiable cryptography, and privacy-first operation.

• Protocol: Genuine WireGuard (wireguard-go userspace VpnService)
• Cipher Suite: ChaCha20-Poly1305 authenticated encryption, Curve25519 key exchange, BLAKE2s hashing, SipHash24
• DNS Leak Protection: Full-tunnel IPv4/IPv6 routing with encrypted/trusted DNS pinning
• Auto-Reconnect: Automatic connectivity monitor with network-switch resilience
• Honest Status Reporting: Connection status reflects the true Android VpnService & WireGuard tunnel state—never simulated.

Version: 1.0.0
    """.trimIndent(),
    privacyPolicyBody = """
MOZAFFAR VPN Privacy Policy

1. Strict No-Logs Commitment
MOZAFFAR VPN does not monitor, record, log, or sell your browsing history, DNS queries, destination IP addresses, traffic payloads, or connection timestamps.

2. Local-Only Key & Profile Storage
Your WireGuard Curve25519 private key and any imported .conf profiles are stored strictly on your device using Android EncryptedSharedPreferences. Private keys never leave your device.

3. Network Telemetry & Diagnostics
• Public IP Display: Requests your visible IP address directly from ipwho.is solely to display your current network exit point inside the dashboard.
• Latency Measurement: Performs lightweight TCP handshake timing checks directly from your device to measure server ping.
• Traffic Counters: Download/upload speeds and byte counters are read locally from the on-device WireGuard GoBackend statistics API.

4. Advertising & Subscriptions
Free-tier users may see Google AdMob advertisements (using official test ad units during development). AdMob operates independently and never inspects or intercepts encrypted VPN tunnel packets. Purchasing Premium removes all ads via Google Play Billing.

5. Contact
For privacy inquiries, contact: support@mozaffarvpn.example
    """.trimIndent(),
    termsBody = """
MOZAFFAR VPN Terms & Conditions

1. Acceptance of Terms
By using MOZAFFAR VPN, you agree to comply with applicable local and international laws. You may not use the VPN service for illegal activities, cyberattacks, or abuse.

2. Genuine Tunnel Infrastructure
MOZAFFAR VPN provides an authentic WireGuard VPN client. Connecting to a remote server requires either an authenticated provisioning backend endpoint (configured via VPN_API_BASE_URL) or a valid user-imported WireGuard (.conf) configuration.

3. No Warranty & Service Availability
While automatic reconnection, DNS leak protection, and OS Kill Switch guidance are provided to strengthen privacy, network conditions vary and uninterrupted connectivity cannot be guaranteed on all networks.

4. Subscriptions & Billing
Premium Ad-Free subscriptions are processed securely through Google Play Billing and renew automatically unless canceled in Google Play Store settings.
    """.trimIndent(),
    countryNames = mapOf(
        "US" to "United States",
        "GB" to "United Kingdom",
        "DE" to "Germany",
        "SG" to "Singapore",
        "JP" to "Japan",
        "CA" to "Canada",
        "NL" to "Netherlands",
        "FR" to "France"
    ),
    cityNames = mapOf(
        "US" to "New York",
        "GB" to "London",
        "DE" to "Frankfurt",
        "SG" to "Singapore",
        "JP" to "Tokyo",
        "CA" to "Toronto",
        "NL" to "Amsterdam",
        "FR" to "Paris"
    )
)

val BengaliStrings = AppStrings(
    appName = "মোজাফফর ভিপিএন",
    navHome = "ড্যাশবোর্ড",
    navServers = "সার্ভার",
    navSettings = "সেটিংস",
    statusDisconnected = "বিচ্ছিন্ন",
    statusConnecting = "টানেল সংযোগ হচ্ছে…",
    statusConnected = "সুরক্ষিত • সংযুক্ত",
    statusReconnecting = "পুনরায় সংযোগ হচ্ছে…",
    statusError = "সংযোগ ব্যর্থ হয়েছে",
    tapToConnect = "সংযুক্ত করুন",
    tapToDisconnect = "বিচ্ছিন্ন করুন",
    connectingLabel = "সংযোগ হচ্ছে",
    reconnectingLabel = "পুনঃসংযোগ",
    encryptionBadge = "ওয়্যারগার্ড • ChaCha20-Poly1305",
    currentIpTitle = "বর্তমান পাবলিক আইপি",
    ipUnavailable = "পাওয়া যায়নি",
    ipChecking = "যাচাই হচ্ছে…",
    refreshIp = "আইপি রিফ্রেশ",
    selectedLocationTitle = "নির্বাচিত সার্ভার",
    switchServer = "পরিবর্তন",
    durationTitle = "সময়কাল",
    downloadSpeedTitle = "ডাউনলোড",
    uploadSpeedTitle = "আপলোড",
    dataUsageTitle = "টানেল ডেটা ব্যবহার",
    sessionUsageLabel = "সেশন",
    totalUsageLabel = "মোট",
    securityProtectionTitle = "টানেল নিরাপত্তা ব্যবস্থা",
    dnsLeakProtection = "ডিএনএস লিক সুরক্ষা",
    autoReconnect = "স্বয়ংক্রিয় পুনঃসংযোগ",
    killSwitchLabel = "সিস্টেম কিল সুইচ",
    activeLabel = "সক্রিয়",
    enabledLabel = "চালু",
    disabledLabel = "বন্ধ",
    systemManagedLabel = "লকডাউন চালু",
    configureKillSwitch = "সিস্টেম সেটিংস",
    configSourceApi = "সার্ভার এপিআই",
    configSourceImported = "ওয়্যারগার্ড প্রোফাইল",
    configSourceNone = "কনফিগ প্রয়োজন",
    serversScreenTitle = "ভিপিএন সার্ভার লোকেশন",
    serversScreenSubtitle = "বাস্তব TCP ল্যাটেন্সি ও ওয়্যারগার্ড এন্ডপয়েন্ট",
    refreshPing = "পিং যাচাই",
    measuringPing = "মাপা হচ্ছে…",
    serverOnline = "সচল",
    serverOffline = "অগম্য",
    serverNeedsConfig = "কনফিগ নেই",
    importConfigButton = ".conf ইমপোর্ট",
    pasteConfigButton = "কনফিগ পেস্ট",
    clearConfigButton = "মুছুন",
    publicKeyCardTitle = "ডিভাইস ওয়্যারগার্ড পাবলিক কী",
    publicKeyCardDesc = "Curve25519 ব্যবহার করে ডিভাইসে তৈরি। আপনার ওয়্যারগার্ড সার্ভারে এই কী অনুমোদন করুন অথবা প্রোভিশনিং এপিআই ব্যবহার করুন।",
    copyPublicKey = "কী কপি করুন",
    copiedToast = "ক্লিপবোর্ডে কপি হয়েছে",
    pasteConfigDialogTitle = "ওয়্যারগার্ড কনফিগারেশন ইমপোর্ট",
    pasteConfigPlaceholder = "এখানে আপনার আসল WireGuard .conf টেক্সট পেস্ট করুন ([Interface] ও [Peer])…",
    saveConfig = "প্রোফাইল সংরক্ষণ",
    saveAndConnect = "সংরক্ষণ ও সংযুক্ত করুন",
    quickSetupButton = ".conf পেস্ট / ইমপোর্ট করুন",
    setupGuideBanner = "বাস্তব এনক্রিপ্টেড টানেল চালু করতে একটি ওয়্যারগার্ড .conf প্রোফাইল পেস্ট করুন অথবা ফাইল বাছাই করুন।",
    yourPublicKeyLabel = "ডিভাইস পাবলিক কী",
    cancel = "বাতিল",
    invalidConfigError = "সঠিক ওয়্যারগার্ড কনফিগারেশন নয়। [Interface] এবং [Peer] তথ্য যাচাই করুন।",
    configSavedMessage = "ওয়্যারগার্ড প্রোফাইল নিরাপদে সংরক্ষিত হয়েছে।",
    settingsTitle = "সেটিংস ও গোপনীয়তা",
    sectionAppearanceLanguage = "ভাষা এবং থিম",
    languageLabel = "অ্যাপের ভাষা",
    themeModeLabel = "থিম মোড",
    themeSystem = "সিস্টেম",
    themeDark = "ডার্ক",
    themeLight = "লাইট",
    sectionVpnSecurity = "ভিপিএন নিরাপত্তা ও প্রোটোকল",
    dnsProviderLabel = "সুরক্ষিত ডিএনএস রিজলভার",
    killSwitchCardDesc = "অ্যান্ড্রয়েড সিস্টেম সেটিংস > VPN > MOZAFFAR VPN > Block connections without VPN থেকে হার্ডওয়্যার কিল সুইচ চালু করুন।",
    openAndroidVpnSettings = "অ্যান্ড্রয়েড ভিপিএন সেটিংস খুলুন",
    regenerateKeysLabel = "Curve25519 কী নতুন করে তৈরি",
    regenerateKeysDesc = "আপনার ডিভাইসের ওয়্যারগার্ড প্রাইভেট ও পাবলিক কী পরিবর্তন করে।",
    keysRegeneratedMessage = "নতুন Curve25519 কী তৈরি হয়েছে।",
    sectionSupportLegal = "সহায়তা ও আইনি তথ্য",
    contactSupport = "সাপোর্টে যোগাযোগ",
    shareApp = "অ্যাপ শেয়ার করুন",
    rateApp = "অ্যাপ রেটিং দিন",
    aboutApp = "মোজাফফর ভিপিএন সম্পর্কে",
    privacyPolicy = "গোপনীয়তা নীতি",
    termsConditions = "শর্তাবলী",
    premiumCardTitle = "মোজাফফর ভিপিএন প্রিমিয়াম",
    premiumCardSubtitle = "সব বিজ্ঞাপন সরান এবং প্রায়োরিটি সাপোর্ট পান",
    premiumActiveBadge = "প্রো সক্রিয়",
    upgradeToPro = "বিজ্ঞাপন মুক্ত হোন",
    rewardedAdTitle = "ফ্রি ২ ঘণ্টার বিজ্ঞাপন-মুক্ত পাস",
    rewardedAdDesc = "২ ঘণ্টার জন্য ব্যানার ও ইন্টারস্টিশিয়াল বিজ্ঞাপন বন্ধ রাখতে একটি ছোট ভিডিও বিজ্ঞাপন দেখুন।",
    watchAdButton = "বিজ্ঞাপন দেখুন",
    adFreePassActive = "বিজ্ঞাপন-মুক্ত পাস চালু আছে",
    sponsoredBannerTitle = "স্পন্সরড পার্টনার বিজ্ঞাপন",
    sponsoredOfferOne = "পার্টনার অফার ১",
    sponsoredOfferTwo = "পার্টনার অফার ২",
    adBadgeLabel = "বিজ্ঞাপন",
    premiumScreenTitle = "প্রিমিয়াম বিজ্ঞাপন-মুক্ত",
    premiumHeroTitle = "নিরবচ্ছিন্ন গোপনীয়তা",
    premiumHeroSubtitle = "স্বাধীন ওয়্যারগার্ড অবকাঠামোকে সমর্থন করুন এবং সম্পূর্ণ বিজ্ঞাপন-মুক্ত অভিজ্ঞতা উপভোগ করুন।",
    benefitNoAds = "কোনো ব্যানার, নেটিভ বা ইন্টারস্টিশিয়াল বিজ্ঞাপন নয়",
    benefitPriorityRouting = "প্রায়োরিটি সার্ভার প্রোভিশনিং ও ডিএনএস সুবিধা",
    benefitUnlimitedSwitching = "সীমাহীন দ্রুত সার্ভার পরিবর্তন",
    benefitSupportDev = "নিরাপদ সার্ভার রক্ষণাবেক্ষণে সরাসরি সহায়তা",
    monthlyPlanTitle = "মাসিক প্যাক",
    monthlyPlanFallbackPrice = "$2.99 / মাস",
    yearlyPlanTitle = "বার্ষিক প্যাক (সেরা সাশ্রয়)",
    yearlyPlanFallbackPrice = "$24.99 / বছর",
    subscribeButton = "Google Play-তে সাবস্ক্রাইব করুন",
    restorePurchasesButton = "পূর্বের ক্রয় পুনরুদ্ধার",
    billingStatusUnavailable = "Google Play Billing ব্যবহার করতে Google Play থেকে ইনস্টল এবং সক্রিয় SKU কনফিগারেশন প্রয়োজন।",
    vpnPermissionDenied = "ব্যবহারকারী ভিপিএন অনুমতি দেননি। এনক্রিপ্টেড টানেল চালু করতে অ্যান্ড্রয়েড ভিপিএন অনুমতি দিন।",
    errNoServerConfig = "এই সার্ভারের জন্য কোনো সক্রিয় ওয়্যারগার্ড প্রোফাইল বা প্রোভিশনিং সার্ভার সেট করা নেই। একটি .conf ফাইল ইমপোর্ট করুন বা VPN_API_BASE_URL কনফিগার করুন।",
    errProvisioningFailed = "সার্ভার অথেনটিকেশন বা প্রোভিশনিং ব্যর্থ হয়েছে। VPN_API_BASE_URL এবং VPN_API_TOKEN যাচাই করুন অথবা .conf ফাইল ইমপোর্ট করুন।",
    errTunnelStartFailed = "ওয়্যারগার্ড টানেল চালু করা যায়নি।",
    errNetworkOffline = "সক্রিয় ইন্টারনেট সংযোগ পাওয়া যায়নি।",
    shareAppMessage = "মোজাফফর ভিপিএন দিয়ে আপনার অনলাইন গোপনীয়তা সুরক্ষিত রাখুন — প্রকৃত ওয়্যারগার্ড এনক্রিপশন ও ডিএনএস লিক সুরক্ষা।",
    supportEmailSubject = "মোজাফফর ভিপিএন সাপোর্ট অনুরোধ",
    aboutBody = """
মোজাফফর ভিপিএন (MOZAFFAR VPN) একটি নেটিভ অ্যান্ড্রয়েড ওয়্যারগার্ড ক্লায়েন্ট, যা স্বচ্ছতা, যাচাইযোগ্য ক্রিপ্টোগ্রাফি এবং ব্যবহারকারীর গোপনীয়তা রক্ষার জন্য নির্মিত।

• প্রোটোকল: প্রকৃত ওয়্যারগার্ড (wireguard-go VpnService)
• এনক্রিপশন: ChaCha20-Poly1305, Curve25519 কী এক্সচেঞ্জ, BLAKE2s হ্যাশিং
• ডিএনএস লিক সুরক্ষা: সম্পূর্ণ IPv4/IPv6 টানেলিং ও সুরক্ষিত ডিএনএস পিনিং
• স্বয়ংক্রিয় পুনঃসংযোগ: নেটওয়ার্ক পরিবর্তনের সাথে সাথে টানেল পুনরুদ্ধার
• সঠিক অবস্থা প্রদর্শন: প্রকৃত Android VpnService এবং WireGuard টানেল সক্রিয় না হওয়া পর্যন্ত কখনোই সংযুক্ত দেখানো হয় না।

সংস্করণ: ১.০.০
    """.trimIndent(),
    privacyPolicyBody = """
মোজাফফর ভিপিএন গোপনীয়তা নীতি

১. কঠোর নো-লগস নীতি
মোজাফফর ভিপিএন আপনার ব্রাউজিং ইতিহাস, ডিএনএস অনুসন্ধান, গন্তব্য আইপি ঠিকানা, ডেটা প্যাকেট বা সংযোগের সময়সীমা কখনোই সংরক্ষণ বা বিক্রি করে না।

২. শুধুমাত্র ডিভাইসে কী সংরক্ষণ
আপনার ওয়্যারগার্ড Curve25519 প্রাইভেট কী এবং ইমপোর্ট করা .conf প্রোফাইল Android EncryptedSharedPreferences ব্যবহার করে শুধুমাত্র আপনার ডিভাইসে সংরক্ষিত থাকে। প্রাইভেট কী কখনোই বাইরে পাঠানো হয় না।

৩. নেটওয়ার্ক ডায়াগনস্টিকস
• পাবলিক আইপি প্রদর্শন: ড্যাশবোর্ডে বর্তমান আইপি দেখাতে ipwho.is থেকে শুধুমাত্র বর্তমান এক্সিট আইপি যাচাই করা হয়।
• ল্যাটেন্সি পরীক্ষা: সার্ভার পিং মাপার জন্য ডিভাইস থেকে সরাসরি হালকা TCP হ্যান্ডশেক সময় পরিমাপ করা হয়।
• ডেটা ব্যবহারের হিসাব: WireGuard GoBackend পরিসংখ্যান API থেকে সরাসরি ডিভাইসে গণনা করা হয়।

৪. বিজ্ঞাপন ও সাবস্ক্রিপশন
ফ্রি সংস্করণে Google AdMob বিজ্ঞাপন প্রদর্শিত হতে পারে (ডেভেলপমেন্টে অফিসিয়াল টেস্ট আইডি ব্যবহৃত হয়)। বিজ্ঞাপন কখনোই ভিপিএন ট্র্যাফিকে হস্তক্ষেপ করে না। প্রিমিয়াম সাবস্ক্রিপশনে সব বিজ্ঞাপন বন্ধ হয়ে যায়।

৫. যোগাযোগ
গোপনীয়তা সংক্রান্ত প্রশ্নের জন্য: support@mozaffarvpn.example
    """.trimIndent(),
    termsBody = """
মোজাফফর ভিপিএন শর্তাবলী

১. শর্তাবলীর স্বীকৃতি
এই অ্যাপ ব্যবহারের মাধ্যমে আপনি প্রযোজ্য আইন মেনে চলতে সম্মত হচ্ছেন। কোনো অবৈধ কার্যক্রম বা সাইবার আক্রমণের জন্য ভিপিএন ব্যবহার করা যাবে না।

২. প্রকৃত টানেল অবকাঠামো
মোজাফফর ভিপিএন একটি প্রকৃত ওয়্যারগার্ড ভিপিএন ক্লায়েন্ট। রিমোট সার্ভারে সংযুক্ত হতে একটি অনুমোদিত প্রোভিশনিং এপিআই অথবা বৈধ ওয়্যারগার্ড (.conf) ফাইল প্রয়োজন।

৩. সেবার প্রাপ্যতা
স্বয়ংক্রিয় পুনঃসংযোগ, ডিএনএস সুরক্ষা এবং কিল সুইচ নির্দেশনা থাকা সত্ত্বেও নেটওয়ার্কের অবস্থার ওপর নির্ভর করে নিরবচ্ছিন্ন সংযোগের নিশ্চয়তা পরিবর্তিত হতে পারে।

৪. সাবস্ক্রিপশন ও বিলিং
প্রিমিয়াম বিজ্ঞাপন-মুক্ত সাবস্ক্রিপশন Google Play Billing এর মাধ্যমে পরিচালিত হয় এবং বাতিল না করা পর্যন্ত স্বয়ংক্রিয়ভাবে নবায়ন হয়।
    """.trimIndent(),
    countryNames = mapOf(
        "US" to "যুক্তরাষ্ট্র",
        "GB" to "যুক্তরাজ্য",
        "DE" to "জার্মানি",
        "SG" to "সিঙ্গাপুর",
        "JP" to "জাপান",
        "CA" to "কানাডা",
        "NL" to "নেদারল্যান্ডস",
        "FR" to "ফ্রান্স"
    ),
    cityNames = mapOf(
        "US" to "নিউ ইয়র্ক",
        "GB" to "লন্ডন",
        "DE" to "ফ্রাঙ্কফুর্ট",
        "SG" to "সিঙ্গাপুর",
        "JP" to "টোকিও",
        "CA" to "টরন্টো",
        "NL" to "আমস্টারডাম",
        "FR" to "প্যারিস"
    )
)
