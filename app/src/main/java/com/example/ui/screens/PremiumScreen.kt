package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.localization.LocalAppStrings
import com.example.monetization.BillingManager
import com.example.ui.VpnDashboardUiState
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldSecure
import com.example.ui.theme.LocalVpnTokens
import com.example.ui.theme.RoyalViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    uiState: VpnDashboardUiState,
    onBack: () -> Unit,
    onSubscribe: (Activity?, String) -> Boolean,
    onRestorePurchases: () -> Unit,
    onWatchRewardedAd: (Activity?, () -> Unit) -> Unit
) {
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedSku by remember { mutableStateOf(BillingManager.SKU_YEARLY) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(strings.premiumScreenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("premium_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_vpn_shield_banner_1795618520495),
                    contentDescription = strings.premiumHeroTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                Text(
                    text = strings.premiumHeroTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.premiumHeroSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    val benefits = listOf(
                        strings.benefitNoAds,
                        strings.benefitPriorityRouting,
                        strings.benefitUnlimitedSwitching,
                        strings.benefitSupportDev
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        benefits.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSecure,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = item, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                // Yearly Plan Card
                PlanOptionCard(
                    title = strings.yearlyPlanTitle,
                    price = uiState.billingState.yearlyPlan.formattedPrice
                        ?: strings.yearlyPlanFallbackPrice,
                    selected = selectedSku == BillingManager.SKU_YEARLY,
                    onClick = { selectedSku = BillingManager.SKU_YEARLY },
                    testTag = "plan_yearly_card"
                )

                // Monthly Plan Card
                PlanOptionCard(
                    title = strings.monthlyPlanTitle,
                    price = uiState.billingState.monthlyPlan.formattedPrice
                        ?: strings.monthlyPlanFallbackPrice,
                    selected = selectedSku == BillingManager.SKU_MONTHLY,
                    onClick = { selectedSku = BillingManager.SKU_MONTHLY },
                    testTag = "plan_monthly_card"
                )

                Button(
                    onClick = {
                        val launched = onSubscribe(activity, selectedSku)
                        if (!launched) {
                            Toast.makeText(
                                context,
                                strings.billingStatusUnavailable,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalViolet),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("subscribe_pro_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.preferences.isPremiumSubscribed)
                            strings.premiumActiveBadge else strings.subscribeButton,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (!uiState.preferences.isPremiumSubscribed) {
                    OutlinedButton(
                        onClick = {
                            onWatchRewardedAd(activity) {
                                Toast.makeText(
                                    context,
                                    "Rewarded ad is still loading. Try again shortly.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("premium_rewarded_ad_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${strings.watchAdButton} • ${strings.rewardedAdTitle}")
                    }
                }

                TextButton(
                    onClick = onRestorePurchases,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("restore_purchases_btn")
                ) {
                    Text(strings.restorePurchasesButton)
                }

                uiState.billingState.statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.reconnectingColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanOptionCard(
    title: String,
    price: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val tokens = LocalVpnTokens.current
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        onClick = onClick,
        borderColor = if (selected) RoyalViolet else tokens.cardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = price,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) RoyalViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = title,
                tint = if (selected) RoyalViolet else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}
