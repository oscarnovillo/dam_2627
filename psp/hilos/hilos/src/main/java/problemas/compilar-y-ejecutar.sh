#!/bin/bash
# Compila TODO (raiz + paquete redesocial) y ejecuta la clase indicada.
# Uso: ./compilar-y-ejecutar.sh NombreClase [args...]
#   ./compilar-y-ejecutar.sh Problema1_ContadorVisitas
#   ./compilar-y-ejecutar.sh redesocial.RedSocial 30
set -e
if [ -z "$1" ]; then
  echo "Uso: $0 <NombreDeLaClase> [argumentos]"
  echo "Ejemplos:"
  echo "  $0 Problema1_ContadorVisitas"
  echo "  $0 redesocial.RedSocial 30"
  exit 1
fi
CLASE=$1
shift
mkdir -p bin
javac --release 25 -d bin *.java redesocial/*.java
java -cp bin "$CLASE" "$@"
