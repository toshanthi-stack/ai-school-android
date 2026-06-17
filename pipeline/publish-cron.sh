#!/bin/bash
# Weekly AI School feed refresh.
#
# Regenerates the catalog (crawl + Claude rewrite + macOS `say` TTS) and pushes
# it to the ai-school-feed repo, which GitHub Pages serves. The apps pick up new
# courses on next launch, with no app release.
#
# Run by launchd (see ~/Library/LaunchAgents/com.lillytech.aischool.feed.plist).
# Secrets/config live OUTSIDE the repo in ~/.config/ai-school/env so nothing
# secret is ever committed. That file must export ANTHROPIC_API_KEY.

set -euo pipefail

# launchd starts with a minimal PATH; make git, gh, python3 and make reachable.
export PATH="/usr/local/bin:/usr/bin:/bin:/usr/local/opt/python@3.14/bin:$PATH"

CONFIG="$HOME/.config/ai-school/env"
if [ -f "$CONFIG" ]; then . "$CONFIG"; fi

LOG="$HOME/Library/Logs/ai-school-feed.log"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# How much to (re)crawl each run. 0 = everything. Override in the config file.
: "${MAX_PATHS:=8}"
: "${MAX_TOPICS:=10}"

{
  echo "===== $(date '+%Y-%m-%d %H:%M:%S') refresh start (MAX_PATHS=$MAX_PATHS MAX_TOPICS=$MAX_TOPICS) ====="
  if [ -z "${ANTHROPIC_API_KEY:-}" ]; then
    echo "ERROR: ANTHROPIC_API_KEY not set. Add it to $CONFIG and try again."
    exit 1
  fi
  cd "$HERE"
  make publish MAX_PATHS="$MAX_PATHS" MAX_TOPICS="$MAX_TOPICS"
  echo "===== $(date '+%Y-%m-%d %H:%M:%S') refresh done ====="
} >> "$LOG" 2>&1
