#!/usr/bin/env bash
set -euo pipefail

secret_pattern='sk_(live|test)_[[:alnum:]]{16,}|PAYMONGO_SECRET_KEY[[:space:]]*=|-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----|gh[pousr]_[[:alnum:]]{36,}|github_pat_[[:alnum:]_]{60,}|AKIA[0-9A-Z]{16}|xox[baprs]-[[:alnum:]-]{10,}'

if git ls-files -- \
    local.properties \
    keystore.properties \
    '*.jks' \
    '*.keystore' \
    ':(glob)**/google-services.json' | rg -q .; then
  echo "A local Firebase, signing, or Android configuration file is tracked." >&2
  exit 1
fi

if git grep -I -q -E "$secret_pattern" -- .; then
  echo "Credential-like content found in tracked files." >&2
  exit 1
fi

if [[ -f local.properties ]] && rg -q -e "$secret_pattern" local.properties; then
  echo "Secret-like payment credential found in local.properties." >&2
  exit 1
fi

while IFS= read -r commit; do
  if git grep -I -q -E "$secret_pattern" "$commit" --; then
    echo "Credential-like content found in Git history at commit $commit." >&2
    exit 1
  fi

  if git ls-tree -r --name-only "$commit" | rg -q \
      '(^|/)(local\.properties|keystore\.properties|google-services\.json)$|\.(jks|keystore)$'; then
    echo "Sensitive configuration or signing material found in Git history at commit $commit." >&2
    exit 1
  fi
done < <(git rev-list --all)

while IFS= read -r -d '' apk; do
  if unzip -p "$apk" | strings | rg -q -e "$secret_pattern|api\.paymongo\.com"; then
    echo "Credential-like content or PayMongo endpoint found in packaged APK: $apk" >&2
    exit 1
  fi
done < <(find app/build/outputs/apk -type f -name '*.apk' -print0 2>/dev/null || true)

echo "Security credential scan passed."
