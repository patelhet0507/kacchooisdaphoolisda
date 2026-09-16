with open('app/src/main/java/com/example/ui/components/GameOverDialog.kt', 'r') as f:
    content = f.read()

# Add import for ConfettiOverlay if needed (in same package)
# Wrap Dialog content or add ConfettiOverlay inside the Dialog box
old_dialog = """    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card("""

new_dialog = """    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize(), particleCount = 80)
            Card("""

# We also need to close the Box at the end of the Dialog
# Let's find where Card ends in GameOverDialog. Let's inspect the end of GameOverDialog.
