#!/bin/bash

# genpakid.sh - Generate the SHA-256 Fingerprint for ADC registration
# Uses the production keystore on Jabari/Akili

KEYSTORE_PATH="$HOME/keys/drilltutor/drilltutor-release.p12"
ALIAS="drilltutor_alpha"

if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "ERROR: Keystore not found at $KEYSTORE_PATH"
    exit 1
fi

echo "--- DrillTutor Production Identity (SHA-256) ---"
keytool -list -v \
  -keystore "$KEYSTORE_PATH" \
  -alias "$ALIAS" \
  -storetype PKCS12 | grep "SHA256:" | sed 's/SHA256: //g'

echo "-----------------------------------------------"
echo "Copy the hex string above for your ADC Hobbyist registration."
