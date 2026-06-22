#!/bin/bash

# Usage: ./push.sh "commit message"

if [ -z "$1" ]; then
  echo "Usage: ./push.sh \"commit message\""
  exit 1
fi

MSG="$1"

echo "=== Adding all changes ==="
git add .

echo "=== Committing: $MSG ==="
git commit -m "$MSG"

echo "=== Pushing to origin/dev ==="
git push origin dev

