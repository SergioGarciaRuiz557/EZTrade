#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
IMAGE="${PLANTUML_DOCKER_IMAGE:-plantuml/plantuml:latest}"
JAR="${PLANTUML_JAR:-${TMPDIR:-/tmp}/plantuml.jar}"

USE_DOCKER=false
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  USE_DOCKER=true
fi

if [[ "${USE_DOCKER}" != "true" && ! -f "${JAR}" ]]; then
  echo "Docker or a local PlantUML jar is required to render the diagrams." >&2
  exit 1
fi

echo "Using PlantUML Docker image: ${IMAGE}"
echo "Diagram root: ${DOCS_ROOT}"
if [[ "${USE_DOCKER}" == "true" ]]; then
  echo "Renderer: Docker"
else
  echo "Renderer: local PlantUML jar (${JAR})"
fi

rendered_count=0

while IFS= read -r -d '' puml_file; do
  plantuml_dir="$(dirname "${puml_file}")"
  category_dir="$(dirname "${plantuml_dir}")"
  rendered_dir="${category_dir}/rendered"
  mkdir -p "${rendered_dir}"

  rel_file="${puml_file#${DOCS_ROOT}/}"
  rel_rendered="${rendered_dir#${DOCS_ROOT}/}"

  echo "Rendering ${rel_file}"
  if [[ "${USE_DOCKER}" == "true" ]]; then
    docker run --rm \
      -v "${DOCS_ROOT}:/workspace" \
      "${IMAGE}" \
      -tpng \
      -o "/workspace/${rel_rendered}" \
      "/workspace/${rel_file}"

    docker run --rm \
      -v "${DOCS_ROOT}:/workspace" \
      "${IMAGE}" \
      -tsvg \
      -o "/workspace/${rel_rendered}" \
      "/workspace/${rel_file}"
  else
    java -jar "${JAR}" -tpng -o "../rendered" "${puml_file}"
    java -jar "${JAR}" -tsvg -o "../rendered" "${puml_file}"
  fi

  rendered_count=$((rendered_count + 1))
done < <(find "${DOCS_ROOT}" -type f -path "*/plantuml/*.puml" -print0 | sort -z)

echo "Rendered ${rendered_count} PlantUML diagram(s) to PNG and SVG."
