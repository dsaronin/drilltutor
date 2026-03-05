#!/bin/bash

# stage_release.sh - Collects the signed APK and Fingerprint for distribution.

PROJECT_ROOT=$(pwd)
DIST_DIR="$PROJECT_ROOT/Distribute"
APK_SOURCE="$PROJECT_ROOT/app/build/outputs/apk/release/app-release.apk"
ID_SCRIPT="$PROJECT_ROOT/genpakid.sh"

# 1. Create Distribute folder if it doesn't exist
mkdir -p "$DIST_DIR"

# 2. Check if the Release APK exists
if [ ! -f "$APK_SOURCE" ]; then
    echo "ERROR: Release APK not found. Did you run ./gradlew assembleRelease?"
    exit 1
fi

# 3. Copy APK to Distribute folder with a timestamp/versioning
# Uses your existing versioning logic if possible, or simple date
DATE=$(date +%Y%m%d)
cp "$APK_SOURCE" "$DIST_DIR/DrillTutor_$DATE.apk"

# 4. Generate the Fingerprint file in the Distribute folder
if [ -f "$ID_SCRIPT" ]; then
    echo "Generating Fingerprint for ADC..."
    ./genpakid.sh > "$DIST_DIR/fingerprint.txt"
else
    echo "Warning: genpakid.sh not found. Fingerprint not generated."
fi

echo "-----------------------------------------------"
echo "Success! Distribution files are ready in: $DIST_DIR"
ls -l "$DIST_DIR"
