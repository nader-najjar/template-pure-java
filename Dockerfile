FROM ubuntu:22.04@sha256:104ae83764a5119017b8e8d6218fa0832b09df65aae7d5a6de29a85d813da2fb

# Within reason, the versions of these dependencies do not matter, including Nix as it is declarative based on the lockfile and flake.
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y \
    ca-certificates curl xz-utils bash coreutils nix && \
    rm -rf /var/lib/apt/lists/*

ENV NIX_CONFIG="experimental-features = nix-command flakes"

COPY flake.nix flake.lock /custom-software/buildtime-workspace/

# We snapshot the runtime dev shell environment once at image build time, then reuse it at runtime without invoking Nix again.
# This is for security reasons and for minimizing runtime complexity.
RUN mkdir -p /custom-software/runtime-workspace && \
    nix print-dev-env /custom-software/buildtime-workspace/#runtime > /custom-software/runtime-workspace/nix-env.sh

# Copy the Gradle application distribution produced by installDist.
# This contains the launch script under bin/ and all runtime jars under lib/.
COPY build/install/template-pure-java /custom-software/runtime-workspace/application

# Runtime wrapper:
#     - Applies the flake-defined runtime dev shell environment recorded at image build time
#     - Delegates to the Gradle-generated launch script
RUN printf '%s\n' \
  '#!/usr/bin/env bash' \
  'set -euo pipefail' \
  'set -a' \
  '. /custom-software/runtime-workspace/nix-env.sh' \
  'set +a' \
  'exec /custom-software/runtime-workspace/application/bin/template-pure-java "$@"' \
  > /custom-software/runtime-workspace/run-application && \
  chmod +x /custom-software/runtime-workspace/run-application

ENTRYPOINT ["/custom-software/runtime-workspace/run-application"]
CMD []
