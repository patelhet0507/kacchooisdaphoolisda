with open('app/src/main/java/com/example/ui/components/GameOverDialog.kt', 'r') as f:
    content = f.read()

old_code = """    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card("""

new_code = """    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize(), particleCount = 80)
            Card("""

# Find where the Card ends (just before the closing braces of Dialog)
# The file ends with:
#                }
#            }
#        }
#    }
# }

old_end = """                }
            }
        }
    }
}"""

new_end = """                }
            }
        }
    }
}"""

if old_code in content:
    content = content.replace(old_code, new_code)
    # Add closing brace for Box right before the last closing brace of Dialog
    # Let's target the card closing
    old_card_end = """            ) {
                Column("""
    # Actually, let's replace from Dialog down to the end carefully.
    pass

with open('app/src/main/java/com/example/ui/components/GameOverDialog.kt', 'r') as f:
    lines = f.readlines()

# Let's inspect lines around Dialog
for idx, line in enumerate(lines):
    if "Dialog(" in line:
        print(f"Found Dialog at line {idx+1}")
        # Insert Box
        lines[idx] = "    Dialog(\n        onDismissRequest = { /* Modal */ },\n        properties = DialogProperties(usePlatformDefaultWidth = false)\n    ) {\n        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {\n            ConfettiOverlay(modifier = Modifier.fillMaxSize(), particleCount = 90)\n"
        break

# And before the final closing brace of Dialog, close the Box
# Let's find the second to last line or insert before last brace
for idx in range(len(lines) - 1, -1, -1):
    if "}" in lines[idx]:
        # replace the closing brace of Dialog with closing Box + closing Dialog
        lines[idx] = "        }\n    }\n}\n"
        break

with open('app/src/main/java/com/example/ui/components/GameOverDialog.kt', 'w') as f:
    f.writelines(lines)

print("Successfully updated GameOverDialog.kt")
