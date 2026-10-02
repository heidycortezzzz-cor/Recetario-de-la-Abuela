# Bitácora de prompts

## P0 · Prompt cero

**Prompt textual:**

```
ROL: Sos un desarrollador senior de aplicaciones web.

CONTEXTO: Estoy construyendo una app llamada RECETARIO DE LA ABUELA para quien cocina en casa.
El problema que resuelve es: las recetas de la familia se pierden cuando nadie las escribe.

TAREA: Generá la primera versión funcional, con estas tres funciones y nada más:
1. Guardar una receta con nombre, ingredientes (con cantidad), pasos y foto.
2. Convertir las porciones de una receta (por ejemplo, de 4 a 10 personas), recalculando las cantidades de cada ingrediente.
3. Buscar recetas por un ingrediente que tenga disponible.

RESTRICCIONES: en español, sin librerías de pago, sin login, sin base de datos en servidor todavía. Que se vea bien en un celular. Código comentado en los puntos donde alguien vaya a equivocarse.

FORMATO DE SALIDA: los archivos completos, cada uno con su nombre, y al final una lista de lo que NO hiciste y por qué.

CRITERIO DE ACEPTACIÓN: abro la app, guardo una receta de 4 porciones, la cambio a 10 y veo las cantidades recalculadas, y al buscar un ingrediente aparece esa receta, sin ningún error en la consola.
```


**Commit:** P0: primera version generada con IA

## m1 · Función

**Prompt textual:**

```
quiero registra esta version como m1: Función. agregar unicamente el texto m1 junto al titulo "Recetario de la Abuela" sin cambiar niguna otra funcion ni el diseño de la aplicacion
```

**Commit:** m1: Función - agregar texto m1 junto al título principal

