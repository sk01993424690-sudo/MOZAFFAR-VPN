package com.example.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.data.VpnPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubscriptionPlanUi(
    val productId: String,
    val formattedPrice: String?,
    val productDetails: ProductDetails?
)

data class BillingUiState(
    val isBillingReady: Boolean = false,
    val monthlyPlan: SubscriptionPlanUi = SubscriptionPlanUi(BillingManager.SKU_MONTHLY, null, null),
    val yearlyPlan: SubscriptionPlanUi = SubscriptionPlanUi(BillingManager.SKU_YEARLY, null, null),
    val statusMessage: String? = null
)

class BillingManager(
    context: Context,
    private val prefsRepo: VpnPreferencesRepository
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _billingState = MutableStateFlow(BillingUiState())
    val billingState: StateFlow<BillingUiState> = _billingState.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    init {
        startConnection()
    }

    fun startConnection() {
        if (billingClient.isReady) {
            queryProductsAndPurchases()
            return
        }
        runCatching {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        _billingState.value = _billingState.value.copy(
                            isBillingReady = true,
                            statusMessage = null
                        )
                        queryProductsAndPurchases()
                    } else {
                        _billingState.value = _billingState.value.copy(
                            isBillingReady = false,
                            statusMessage = result.debugMessage.takeIf { it.isNotBlank() }
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _billingState.value = _billingState.value.copy(isBillingReady = false)
                }
            })
        }
    }

    fun queryProductsAndPurchases() {
        if (!billingClient.isReady) {
            startConnection()
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { result, detailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val monthly = detailsList.firstOrNull { it.productId == SKU_MONTHLY }
                val yearly = detailsList.firstOrNull { it.productId == SKU_YEARLY }
                _billingState.value = _billingState.value.copy(
                    monthlyPlan = SubscriptionPlanUi(
                        productId = SKU_MONTHLY,
                        formattedPrice = monthly?.subscriptionOfferDetails?.firstOrNull()
                            ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice,
                        productDetails = monthly
                    ),
                    yearlyPlan = SubscriptionPlanUi(
                        productId = SKU_YEARLY,
                        formattedPrice = yearly?.subscriptionOfferDetails?.firstOrNull()
                            ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice,
                        productDetails = yearly
                    )
                )
            }
        }

        val purchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(purchasesParams) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    fun launchSubscriptionFlow(activity: Activity?, productId: String): Boolean {
        if (activity == null || !billingClient.isReady) return false
        val details = when (productId) {
            SKU_MONTHLY -> _billingState.value.monthlyPlan.productDetails
            SKU_YEARLY -> _billingState.value.yearlyPlan.productDetails
            else -> null
        } ?: return false

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return false
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            _billingState.value = _billingState.value.copy(
                statusMessage = result.debugMessage.takeIf { it.isNotBlank() }
            )
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        var hasActiveSub = false
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasActiveSub = true
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams) { }
                }
            }
        }
        scope.launch {
            prefsRepo.setPremiumSubscribed(hasActiveSub)
        }
    }

    companion object {
        const val SKU_MONTHLY = "mozaffar_vpn_pro_monthly"
        const val SKU_YEARLY = "mozaffar_vpn_pro_yearly"
    }
}
