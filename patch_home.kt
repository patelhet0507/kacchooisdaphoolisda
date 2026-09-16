@Composable
private fun ActionCard(
    title: String,
    icon: String,
    desc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isPrimary) 120.dp else 100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPrimary) DarkSurfaceElevated else DarkSurface.copy(alpha=0.9f)),
        border = BorderStroke(
            if (isPrimary) 1.5.dp else 1.dp,
            if (isPrimary) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = if (isPrimary) 32.sp else 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isPrimary) GoldPrimary else TextLight,
                fontSize = if (isPrimary) 16.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (isPrimary) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
