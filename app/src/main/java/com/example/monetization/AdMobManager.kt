package com.example.monetization

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.localization.LocalAppStrings
import com.example.ui.theme.LocalVpnTokens
import com.example.ui.theme.RoyalViolet
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AdMobTestIds {
    const val BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val NATIVE_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    const val INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
}

object SponsoredAdLinks {
    const val PARTNER_OFFER_ONE = "https://asiafilm.org/4/d84436540e313f056f42e823e8b65e3f"
    const val PARTNER_OFFER_TWO = "https://auctionr.org/4/aea6cf04f568aca1362e2dd6941cde58"
}

class AdMobManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var lastInterstitialShownMs: Long = 0L

    fun initializeAsync() {
        if (isInitialized) return
        scope.launch {
            runCatching {
                MobileAds.initialize(context) {
                    isInitialized = true
                    CoroutineScope(Dispatchers.Main).launch {
                        preloadInterstitial()
                        preloadRewarded()
                    }
                }
            }
        }
    }

    fun preloadInterstitial() {
        val request = AdRequest.Builder().build()
        runCatching {
            InterstitialAd.load(
                context,
                AdMobTestIds.INTERSTITIAL_UNIT_ID,
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        }
    }

    fun preloadRewarded() {
        val request = AdRequest.Builder().build()
        runCatching {
            RewardedAd.load(
                context,
                AdMobTestIds.REWARDED_UNIT_ID,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                    }
                }
            )
        }
    }

    /**
     * Carefully placed interstitial:
     * - Never shown to Premium / Ad-Free pass users
     * - Never shown while VPN handshake or reconnection is in progress
     * - Enforces a minimum 90-second cooldown between interstitial impressions
     */
    fun maybeShowInterstitial(
        activity: Activity?,
        isAdFree: Boolean,
        isVpnHandshakeInProgress: Boolean
    ) {
        if (activity == null || isAdFree || isVpnHandshakeInProgress) return
        val now = System.currentTimeMillis()
        if (now - lastInterstitialShownMs < 90_000L) return

        val ad = interstitialAd
        if (ad == null) {
            preloadInterstitial()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preloadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                preloadInterstitial()
            }
        }
        lastInterstitialShownMs = now
        ad.show(activity)
    }

    fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        if (activity == null) {
            onUnavailable()
            return
        }
        val ad = rewardedAd
        if (ad == null) {
            preloadRewarded()
            onUnavailable()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                preloadRewarded()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                preloadRewarded()
            }
        }

        ad.show(activity) {
            onRewardEarned()
        }
    }
}

@Composable
fun AdMobBannerBar(
    isAdFree: Boolean,
    modifier: Modifier = Modifier
) {
    if (isAdFree) return

    val context = LocalContext.current
    var adLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobTestIds.BANNER_UNIT_ID
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            adLoaded = true
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            adLoaded = false
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { adView ->
                adView.destroy()
            }
        )
    }
}

@Composable
fun AdMobNativeCard(
    isAdFree: Boolean,
    modifier: Modifier = Modifier
) {
    if (isAdFree) return

    val context = LocalContext.current
    val tokens = LocalVpnTokens.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        val loader = AdLoader.Builder(context, AdMobTestIds.NATIVE_UNIT_ID)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // Fail silently without blocking VPN UI
                }
            })
            .build()

        runCatching { loader.loadAd(AdRequest.Builder().build()) }

        onDispose {
            nativeAd?.destroy()
        }
    }

    nativeAd?.let { currentAd ->
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = tokens.cardSurface,
            border = BorderStroke(1.dp, tokens.cardBorder)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                factory = { ctx ->
                    val adView = LayoutInflater.from(ctx)
                        .inflate(R.layout.layout_native_ad, null) as NativeAdView
                    bindNativeAd(currentAd, adView)
                    adView
                },
                update = { adView ->
                    bindNativeAd(currentAd, adView)
                }
            )
        }
    }
}

@Composable
fun SponsoredBannerSection(
    isAdFree: Boolean,
    modifier: Modifier = Modifier
) {
    if (isAdFree) return

    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current

    val offers = remember(strings) {
        listOf(
            Triple(strings.sponsoredOfferOne, SponsoredAdLinks.PARTNER_OFFER_ONE, "sponsored_ad_link_1"),
            Triple(strings.sponsoredOfferTwo, SponsoredAdLinks.PARTNER_OFFER_TWO, "sponsored_ad_link_2")
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = tokens.cardSurface,
        border = BorderStroke(1.dp, tokens.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RoyalViolet.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, RoyalViolet.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = strings.adBadgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalViolet,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = strings.sponsoredBannerTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                offers.forEach { (title, url, tag) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                runCatching { context.startActivity(intent) }
                            }
                            .testTag(tag),
                        shape = RoundedCornerShape(12.dp),
                        color = tokens.cardSurfaceElevated,
                        border = BorderStroke(1.dp, tokens.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = title,
                                tint = tokens.cyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun bindNativeAd(nativeAd: NativeAd, adView: NativeAdView) {
    val headline = adView.findViewById<TextView>(R.id.ad_headline)
    val advertiser = adView.findViewById<TextView>(R.id.ad_advertiser)
    val body = adView.findViewById<TextView>(R.id.ad_body)
    val cta = adView.findViewById<Button>(R.id.ad_call_to_action)
    val icon = adView.findViewById<ImageView>(R.id.ad_app_icon)

    adView.headlineView = headline
    adView.advertiserView = advertiser
    adView.bodyView = body
    adView.callToActionView = cta
    adView.iconView = icon

    headline.text = nativeAd.headline.orEmpty()
    advertiser.text = nativeAd.advertiser.orEmpty()
    advertiser.visibility = if (nativeAd.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE

    if (nativeAd.body.isNullOrBlank()) {
        body.visibility = View.GONE
    } else {
        body.visibility = View.VISIBLE
        body.text = nativeAd.body
    }

    if (nativeAd.callToAction.isNullOrBlank()) {
        cta.visibility = View.GONE
    } else {
        cta.visibility = View.VISIBLE
        cta.text = nativeAd.callToAction
    }

    val drawable = nativeAd.icon?.drawable
    if (drawable == null) {
        icon.visibility = View.GONE
    } else {
        icon.visibility = View.VISIBLE
        icon.setImageDrawable(drawable)
    }

    adView.setNativeAd(nativeAd)
}
