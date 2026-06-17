# AI School content pipeline.
# Chains: crawl the site -> lessons.json -> adapt + TTS -> out/syllabus.json + audio
#
# Override any tunable on the command line, e.g.:
#   make crawl MAX_PATHS=5 MAX_TOPICS=8
#   make build AUDIO_URL_BASE=https://www.lillytechsystems.com/ai-school/audio
#   make feed  MAX_PATHS=3 MAX_TOPICS=4

VENV := .venv
PY   := $(VENV)/bin/python
PIP  := $(VENV)/bin/pip

# Tunables
MAX_PATHS      ?= 0
MAX_TOPICS     ?= 0
MAX_LESSONS    ?= 0
MODEL          ?= claude-opus-4-8
INCLUDE_CORE   ?=
AUDIO_URL_BASE ?=
LESSONS        ?= lessons.json
OUT            ?= out/syllabus.json
AUDIO_DIR      ?= out/audio
URL            ?=

# Where the published audio is served from (the ai-school-feed GitHub Pages).
# `make publish` stamps audioUrls with this unless AUDIO_URL_BASE is overridden.
FEED_AUDIO_BASE ?= https://toshanthi-stack.github.io/ai-school-feed/audio
PUBLISH_AUDIO_BASE := $(if $(strip $(AUDIO_URL_BASE)),$(AUDIO_URL_BASE),$(FEED_AUDIO_BASE))

CRAWL_FLAGS := --out $(LESSONS)
ifneq ($(MAX_PATHS),0)
CRAWL_FLAGS += --max-paths $(MAX_PATHS)
endif
ifneq ($(MAX_TOPICS),0)
CRAWL_FLAGS += --max-topics $(MAX_TOPICS)
endif
ifneq ($(MAX_LESSONS),0)
CRAWL_FLAGS += --max-lessons $(MAX_LESSONS)
endif
ifneq ($(strip $(INCLUDE_CORE)),)
CRAWL_FLAGS += --include-core
endif

BUILD_FLAGS := --config $(LESSONS) --audio-dir $(AUDIO_DIR) --out $(OUT)
ifneq ($(strip $(AUDIO_URL_BASE)),)
BUILD_FLAGS += --audio-url-base $(AUDIO_URL_BASE)
endif

.DEFAULT_GOAL := help
.PHONY: help install crawl build feed publish adapt check check-anthropic clean distclean

help:
	@echo "AI School content pipeline"
	@echo ""
	@echo "  make install         create the venv and install dependencies"
	@echo "  make crawl           crawl the site into $(LESSONS)"
	@echo "                         tunables: MAX_PATHS= MAX_TOPICS= INCLUDE_CORE=1"
	@echo "  make build           adapt + synthesize $(LESSONS) -> $(OUT) + audio"
	@echo "                         tunables: AUDIO_URL_BASE=...  MODEL=claude-haiku-4-5"
	@echo "  make feed            crawl then build (the full chain)"
	@echo "  make publish         feed + push to the ai-school-feed repo (apps update live)"
	@echo "                         tunables: MAX_PATHS= MAX_TOPICS=  (0 = everything)"
	@echo "  make adapt URL=...   adapt one lesson URL and print the result"
	@echo "  make check           show selected TTS backend and which keys are set"
	@echo "  make clean           remove generated output ($(OUT), $(AUDIO_DIR))"
	@echo "  make distclean       clean + remove the venv"
	@echo ""
	@echo "Keys: ANTHROPIC_API_KEY (required) + ELEVENLABS_API_KEY or OPENAI_API_KEY"
	@echo "(optional; without a TTS key the local 'say' backend is used)."

$(VENV):
	python3 -m venv $(VENV)
	$(PIP) install --quiet --upgrade pip
	$(PIP) install --quiet -r requirements.txt

install:
	@test -d $(VENV) || python3 -m venv $(VENV)
	$(PIP) install --quiet --upgrade pip
	$(PIP) install --quiet -r requirements.txt
	@echo "dependencies installed in $(VENV)"

crawl: $(VENV)
	$(PY) crawl_catalog.py $(CRAWL_FLAGS)

build: $(VENV) check-anthropic
	ADAPT_MODEL=$(MODEL) $(PY) build_feed.py $(BUILD_FLAGS)

feed: crawl build

# One command: generate the feed with hosted audio URLs, then push it to the
# ai-school-feed repo. GitHub Pages serves it; the apps pick up new courses on
# next launch with no app release. Override the target clone with FEED_REPO=...
publish: $(VENV) check-anthropic
	$(MAKE) feed AUDIO_URL_BASE=$(PUBLISH_AUDIO_BASE) \
	  MAX_PATHS=$(MAX_PATHS) MAX_TOPICS=$(MAX_TOPICS) MAX_LESSONS=$(MAX_LESSONS) \
	  MODEL=$(MODEL) INCLUDE_CORE=$(INCLUDE_CORE)
	$(PY) publish_to_feed.py

adapt: $(VENV) check-anthropic
	@test -n "$(URL)" || { echo "usage: make adapt URL=https://..."; exit 2; }
	$(PY) adapt_lesson.py "$(URL)"

check:
	@if [ -n "$$ANTHROPIC_API_KEY" ]; then echo "ANTHROPIC_API_KEY: set"; \
	 else echo "ANTHROPIC_API_KEY: MISSING (required for the rewrite step)"; fi
	@if [ -n "$$ELEVENLABS_API_KEY" ]; then echo "TTS: elevenlabs (ELEVENLABS_API_KEY set)"; \
	 elif [ -n "$$OPENAI_API_KEY" ]; then echo "TTS: openai (OPENAI_API_KEY set)"; \
	 else echo "TTS: say (local prototype; no production key set)"; fi

check-anthropic:
	@test -n "$$ANTHROPIC_API_KEY" || \
	 { echo "ERROR: ANTHROPIC_API_KEY not set (needed for the rewrite step)"; exit 1; }

clean:
	rm -rf $(OUT) $(AUDIO_DIR)
	@rmdir out 2>/dev/null || true

distclean: clean
	rm -rf $(VENV) __pycache__
