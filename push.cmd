@echo off
REM Usage: push.cmd "commit message"

if "%~1"=="" (
  echo Usage: push.cmd "commit message"
  exit /b 1
)

set MSG=%~1

echo === Adding all changes ===
git add .

echo === Committing: %MSG% ===
git commit -m "%MSG%"

echo === Pushing to origin/dev ===
git push origin dev

echo === Switching to main ===
git checkout main

echo === Pulling latest main ===
git pull origin main

echo === Merging dev into main ===
git merge dev -m "Merge dev: %MSG%"

if %ERRORLEVEL% NEQ 0 (
  echo !!! Merge conflict detected. Resolve manually, then:
  echo     git add . ^& git commit ^& git push origin main
  exit /b 1
)

echo === Pushing main ===
git push origin main

echo === Switching back to dev ===
git checkout dev

echo === Done! ===
