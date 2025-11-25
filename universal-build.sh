#!/usr/bin/env sh
# Universal build entrypoint for template repos.
# Benefits:
# - Single command (`./universal-build.sh`) for all stacks; only prerequisite is Nix.
# - Toolchains come from Nix; language-native build tools stay idiomatic (e.g., Gradle, Poetry, pnpm).
# - Deterministic: flake/lock pin tool versions; wrappers/lockfiles pin language deps; same env on macOS/Linux.
# - CI/dev parity: runs the same `nix develop` environment everywhere, then dispatches to the repo’s build command.
# - Minimal friction: guides on missing Nix/flake support, resolves the repo path so it works from any cwd, requires explicit Gradle task args (passes them through), and supports a flake update flag.

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
NIX_FLAGS="--extra-experimental-features nix-command --extra-experimental-features flakes"

usage() {
  echo "Usage:" >&2
  echo "  $0 --update-flake            # update nix flake lockfile" >&2
  echo "  $0 <gradle-task> [tasks...]  # run gradle tasks inside nix dev shell" >&2
  exit 1
}

require_nix() {
  if ! command -v nix >/dev/null 2>&1; then
    echo "Error: Nix is required. Install from https://nix.dev/install-nix" >&2
    exit 1
  fi
}

print_gradle_cmd() {
  printf "Starting Java Gradle build with: ./gradlew"
  for arg in "$@"; do
    printf " %s" "$arg"
  done
  printf "\n"
}

update_flake() {
  echo "Updating nix flake lock in ${SCRIPT_DIR}"
  nix ${NIX_FLAGS} flake update "${SCRIPT_DIR}"
  printf "\033[32mBUILD SUCCEEDED\033[0m\n"
}

gradle_build() {
  print_gradle_cmd "$@"
  exec nix ${NIX_FLAGS} develop "${SCRIPT_DIR}" --command sh -c 'cd "$1" && shift && ./gradlew "$@" && printf "\033[32mBUILD SUCCEEDED\033[0m\n"' _ "${SCRIPT_DIR}" "$@"
}

main() {
  require_nix

  if [ "$#" -eq 0 ]; then
    usage
  fi

  # Guard against misplaced --update-flake so it never reaches Gradle.
  if printf '%s\n' "$@" | grep -q -- '^--update-flake$'; then
    if [ "$#" -ne 1 ]; then
      echo "Error: --update-flake must be the only argument." >&2
      exit 1
    fi
  fi

  case "$1" in
    --update-flake)
      update_flake
      ;;
    *)
      gradle_build "$@"
      ;;
  esac
}

main "$@"
