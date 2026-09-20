#!/bin/bash
BASE_URL="http://localhost:8080/api/entrenador"

echo "1) POST - Crear Entrenador 1"
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Carlos Rodríguez","especialidad":"Yoga"}' | tee /tmp/entrenador1.json
echo -e "\n"

echo "2) POST - Crear Entrenador 2"
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Martínez","especialidad":"Spinning"}'
echo -e "\n"

echo "3) GET - Listar todos los entrenadores"
curl -s "$BASE_URL"
echo -e "\n"

echo "4) GET - Buscar entrenador por id=1"
curl -s "$BASE_URL/1"
echo -e "\n"

echo "5) PUT - Actualizar entrenador id=1"
curl -s -X PUT "$BASE_URL/1" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Carlos Rodríguez","especialidad":"Crossfit"}'
echo -e "\n"

echo "6) GET - Verificar actualización id=1"
curl -s "$BASE_URL/1"
echo -e "\n"

echo "7) DELETE - Eliminar entrenador id=2"
curl -s -w "HTTP status: %{http_code}\n" -X DELETE "$BASE_URL/2"
echo -e "\n"

echo "8) GET - Listar entrenadores tras eliminar"
curl -s "$BASE_URL"
echo -e "\n"

echo "9) GET - Buscar entrenador inexistente (id=999, debe dar 404)"
curl -s -w "HTTP status: %{http_code}\n" "$BASE_URL/999"