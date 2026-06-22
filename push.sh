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

echo "=== Switching to main ==="
git checkout main

echo "=== Pulling latest main ==="
git pull origin main

echo "=== Merging dev into main ==="
git merge dev -m "Merge dev: $MSG"

if [ $? -ne 0 ]; then
  echo "!!! Merge conflict detected. Resolve manually, then:"
  echo "    git add . && git commit && git push origin main"
  exit 1
fi

echo "=== Pushing main ==="
git push origin main

echo "=== Switching back to dev ==="
git checkout dev

echo "=== Done! ==="
