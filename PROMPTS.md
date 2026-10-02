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

## M2 · Que recuerde

**Prompt textual:**

```
# M2 · Que recuerde

Quiero que los datos de la app no se pierdan al cerrarla.

Usá localStorage y explicame:

1. Dónde queda guardada la información exactamente.
2. Qué pasa si el usuario borra el caché o cambia de dispositivo.
3. Cómo hago para exportar los datos a un archivo, por si quiero respaldarlos.

Dame el código de guardar, leer y borrar, y un dato de ejemplo ya cargado para probar.

Guardá cada receta junto con sus porciones base.

No elimines ni cambies las funciones que ya funcionan. La nueva implementación debe integrarse con la aplicación existente.
```

**Commit:** M2: Que recuerde - Persistencia permanente de recetas con porciones base y exportación a JSON

## M3 · Que se entienda

**Prompt textual:**

```
# M3 · Que se entienda

Antes: abrí la app en tu celular y anotá los tres estorbos más grandes.

Después:

Ajustá la interfaz de la app con estos requisitos, sin cambiar la lógica:

1. Se usa bien desde 320 px de ancho, con una sola mano y sin hacer zoom.
2. Contraste suficiente para leerse al sol; texto nunca menor a 16 px.
3. Todos los campos con etiqueta visible, no solo con texto de ejemplo dentro.
4. Un solo botón principal por pantalla; los demás, secundarios.
5. Estado vacío: qué se muestra cuando todavía no hay ningún dato, con una frase que invite a la primera acción. Por ejemplo: "Todavía no hay recetas. Escribí la primera receta de tu abuela".
6. Mensajes de éxito y de error visibles, en español, sin palabras técnicas.

Dame los cambios y decime cuál de los seis puntos NO pudiste cumplir y por qué.

No cambies la lógica ni elimines las funciones existentes.
```

**Commit:** M3: Que se entienda - Accesibilidad desde 320px, alto contraste al sol, tipografía >=16px y etiquetas visibles

## M4 · Que no se rompa

**Prompt textual:**

```
# M4 · Que no se rompa

Actuá como tester de software, no como programador.

Dame diez formas concretas de romper esta app desde la interfaz, por ejemplo:

* Campos vacíos.
* Texto donde debería ir un número.
* Números negativos.
* Fechas imposibles.
* Textos de 500 caracteres.
* Doble clic en el botón Guardar.
* Pérdida de conexión a mitad de una acción.

Para cada una decime:

1. Qué pasaría hoy.
2. Qué debería pasar.
3. El código mínimo que lo evita.

No cambies el diseño ni agregues funciones nuevas.
```

**Commit:** M4: Que no se rompa - 10 pruebas destructivas de QA y validaciones defensivas




