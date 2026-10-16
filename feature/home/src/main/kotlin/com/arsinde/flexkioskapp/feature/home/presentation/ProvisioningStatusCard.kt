package com.arsinde.flexkioskapp.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.arsinde.flexkioskapp.designsystem.AppSpacing
import com.arsinde.flexkioskapp.feature.home.R

/**
 * Reports whether the device is under device management.
 *
 * Stateless and previewable: it renders the [status] it is handed and holds no logic of its own. The
 * wording lives in resources, and the unavailable variant explains the state without describing the
 * failure, because no failure detail is carried into the presentation state.
 *
 * Nothing is rendered while [status] is null, which is the state before the first read completes.
 */
@Composable
fun ProvisioningStatusCard(
    status: ProvisioningStatus?,
    modifier: Modifier = Modifier,
) {
    if (status == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                ProvisioningStatus.Unavailable -> MaterialTheme.colorScheme.errorContainer
                ProvisioningStatus.DeviceOwner,
                ProvisioningStatus.NotProvisioned,
                -> MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall),
        ) {
            Text(
                text = stringResource(R.string.feature_home_provisioning_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(status.textRes),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
            )
        }
    }
}

private val ProvisioningStatus.textRes: Int
    get() = when (this) {
        ProvisioningStatus.DeviceOwner -> R.string.feature_home_provisioning_device_owner
        ProvisioningStatus.NotProvisioned -> R.string.feature_home_provisioning_not_provisioned
        ProvisioningStatus.Unavailable -> R.string.feature_home_provisioning_unavailable
    }
