SET DEFINE OFF
SET FEEDBACK ON
SET ECHO OFF
WHENEVER SQLERROR CONTINUE

PROMPT [1/13] 00_reset.sql
@00_reset.sql

PROMPT [2/13] 01_secuencias.sql
@01_secuencias.sql

PROMPT [3/13] 02_tablas.sql
@02_tablas.sql

PROMPT [4/13] 03_claves_foraneas.sql
@03_claves_foraneas.sql

PROMPT [5/13] 04_restricciones.sql
@04_restricciones.sql

PROMPT [6/13] 05_indices.sql
@05_indices.sql

PROMPT [7/13] 06_pkg_inventario.sql
@06_pkg_inventario.sql

PROMPT [8/13] 07_pkg_puntos.sql
@07_pkg_puntos.sql

PROMPT [9/13] 08_triggers.sql
@08_triggers.sql

PROMPT [10/13] 09_datos_prueba.sql
@09_datos_prueba.sql

PROMPT [11/13] 10_vistas.sql
@10_vistas.sql

PROMPT [12/13] 11_reportes.sql
@11_reportes.sql

PROMPT [13/13] 12_pruebas_validacion.sql
@12_pruebas_validacion.sql

PROMPT ============================================================
PROMPT INSTALACION COMPLETADA
PROMPT ============================================================
