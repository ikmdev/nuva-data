#!/usr/bin/env bash
#
# Runs the NUVA data pipeline stage by stage, in order:
#   origin -> starterdata -> pipeline -> owl-transform -> reasoner -> integration -> export
# (the order of the <modules> in pom.xml, which is the order used by rxnorm-data).
#
# A stage is skipped when its module is not listed in pom.xml, or when --skip <stage> is given.
# The script stops at the first failing stage and exits with that stage's exit code.
#
# On a fresh clone this script FAILS at the origin stage (source files missing). See README.md,
# "Expected failures on a fresh clone".
#
# Usage:
#   ./run-data-pipeline.sh [--skip <stage>]... [--dry-run] [--local-origin] [--deploy] [--input-directory <dir>]
#
#   --skip <stage>           skip a stage (repeatable)
#   --dry-run                print the commands without running them
#   --local-origin           run the origin stage with profile generateDataLocal (installs into ~/.m2)
#   --deploy                 deploy the origin artifact and publish the export module; requires a repository,
#                            see ALT_DEPLOYMENT_REPOSITORY below
#   --input-directory <dir>  where the licensed source files are (default: ~/Downloads)
#
# Environment:
#   ALT_DEPLOYMENT_REPOSITORY  <id>::<url> passed as -DaltDeploymentRepository when --deploy is used.
#                              Default is a placeholder; no real URL is stored in this repository.

set -u

STAGES=(origin starterdata pipeline owl-transform reasoner integration export)
ALT_DEPLOYMENT_REPOSITORY="${ALT_DEPLOYMENT_REPOSITORY:-REPLACE_ME}"

SKIP=()
DRY_RUN=0
LOCAL_ORIGIN=0
DEPLOY=0
INPUT_DIRECTORY=""

usage() { sed -n '2,/^set -u/p' "$0" | sed '$d' | sed 's/^# \{0,1\}//'; }

while [ $# -gt 0 ]; do
  case "$1" in
    --skip)
      [ $# -ge 2 ] || { echo "--skip needs a stage name" >&2; exit 2; }
      SKIP+=("$2"); shift 2 ;;
    --dry-run) DRY_RUN=1; shift ;;
    --local-origin) LOCAL_ORIGIN=1; shift ;;
    --deploy) DEPLOY=1; shift ;;
    --input-directory)
      [ $# -ge 2 ] || { echo "--input-directory needs a directory" >&2; exit 2; }
      INPUT_DIRECTORY="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage >&2; exit 2 ;;
  esac
done

for s in "${SKIP[@]+"${SKIP[@]}"}"; do
  known=0
  for t in "${STAGES[@]}"; do [ "$s" = "$t" ] && known=1; done
  [ "$known" = 1 ] || { echo "Unknown stage to skip: $s (known: ${STAGES[*]})" >&2; exit 2; }
done

if [ "$DEPLOY" = 1 ] && [ "$ALT_DEPLOYMENT_REPOSITORY" = "REPLACE_ME" ]; then
  echo "--deploy needs ALT_DEPLOYMENT_REPOSITORY=<id>::<url>; it is still the placeholder REPLACE_ME." >&2
  exit 2
fi

cd "$(dirname "$0")" || exit 1

is_skipped() {
  for s in "${SKIP[@]+"${SKIP[@]}"}"; do [ "$s" = "$1" ] && return 0; done
  return 1
}

is_configured() {
  grep -Eq "^[[:space:]]*<module>nuva-$1</module>" pom.xml
}

run() {
  echo "+ $*"
  [ "$DRY_RUN" = 1 ] && return 0
  "$@"
}

COMMON=(-Dmaven.build.cache.enabled=false)
[ -n "$INPUT_DIRECTORY" ] && COMMON+=("-Dinput.directory=$INPUT_DIRECTORY")

for stage in "${STAGES[@]}"; do
  if is_skipped "$stage"; then echo "=== Stage: $stage (skipped by --skip) ==="; continue; fi
  if ! is_configured "$stage"; then echo "=== Stage: $stage (skipped: module nuva-$stage not in pom.xml) ==="; continue; fi

  echo "=== Stage: $stage ==="
  profiles=""
  goal="install"
  extra=()
  case "$stage" in
    origin)
      [ "$LOCAL_ORIGIN" = 1 ] && profiles="generateDataLocal"
      if [ "$DEPLOY" = 1 ]; then goal="deploy"; extra+=("-DaltDeploymentRepository=$ALT_DEPLOYMENT_REPOSITORY"); fi ;;
    export)
      if [ "$DEPLOY" = 1 ]; then
        goal="deploy"; profiles="deployExport"
        extra+=("-DaltDeploymentRepository=$ALT_DEPLOYMENT_REPOSITORY")
      fi ;;
  esac

  pargs=()
  [ -n "$profiles" ] && pargs=("-P$profiles")

  # --also-make builds the plugin module the stage depends on, so a stage can run on its own.
  run ./mvnw --projects "nuva-$stage" --also-make clean "$goal" ${pargs[@]+"${pargs[@]}"} "${COMMON[@]}" ${extra[@]+"${extra[@]}"}
  rc=$?
  if [ "$rc" -ne 0 ]; then
    echo "Stage '$stage' failed with exit code $rc. Stopping." >&2
    exit "$rc"
  fi
done

echo "=== All requested stages finished ==="
