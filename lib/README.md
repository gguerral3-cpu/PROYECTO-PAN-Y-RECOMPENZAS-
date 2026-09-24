# Driver JDBC de Oracle

Coloque aqui el archivo `ojdbc11.jar` (Java 11+), que no se versiona en Git.

Descarguelo desde la pagina oficial de Oracle Database JDBC Drivers, o desde Maven Central:

```
https://central.sonatype.com/artifact/com.oracle.database.jdbc/ojdbc11
```

Versiones usadas: 21.x, 23.x o 23.4.0.24.05.

Despues de copiarlo, compile y ejecute:

```
.\compilar.ps1
.\ejecutar.ps1
```

Si prefiere no moverlo a esta carpeta, indique la ruta en cada ejecucion:

```
.\compilar.ps1 -RutaDriver "C:\ruta\ojdbc11.jar"
.\ejecutar.ps1 -RutaDriver "C:\ruta\ojdbc11.jar"
```

Si usa Maven, no hace falta el jar: `mvn clean package` lo descarga de forma automatica.
