with open('app/src/main/java/com/example/ui/screens/MultiplayerLobbyScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.foundation.BorderStroke" not in content:
    content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.BorderStroke\nimport androidx.compose.foundation.background")
    with open('app/src/main/java/com/example/ui/screens/MultiplayerLobbyScreen.kt', 'w') as f:
        f.write(content)
    print("Added BorderStroke import")
else:
    print("BorderStroke import already exists")
