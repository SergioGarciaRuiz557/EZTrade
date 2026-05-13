#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
IMAGE="${PLANTUML_DOCKER_IMAGE:-plantuml/plantuml:latest}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Se requiere Docker para renderizar los diagramas." >&2
  exit 1
fi

echo "Usando imagen Docker de PlantUML: ${IMAGE}"
echo "Raiz de diagramas: ${DOCS_ROOT}"

rendered_count=0

while IFS= read -r -d '' puml_file; do
  plantuml_dir="$(dirname "${puml_file}")"
  category_dir="$(dirname "${plantuml_dir}")"
  rendered_dir="${category_dir}/rendered"
  mkdir -p "${rendered_dir}"

  rel_file="${puml_file#${DOCS_ROOT}/}"
  rel_rendered="${rendered_dir#${DOCS_ROOT}/}"

  echo "Renderizando ${rel_file}"
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

  rendered_count=$((rendered_count + 1))
done < <(find "${DOCS_ROOT}" -type f -path "*/plantuml/*.puml" -print0 | sort -z)

echo "Renderizados ${rendered_count} diagrama(s) PlantUML a PNG y SVG."
