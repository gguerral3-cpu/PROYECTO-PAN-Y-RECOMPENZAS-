SET SERVEROUTPUT ON
SET DEFINE OFF

PROMPT ============================================================
PROMPT PRUEBA 1: Cliente no puede eliminarse si tiene compras
PROMPT ============================================================
BEGIN
    DELETE FROM CLIENTE WHERE DPI = '12345678-1';
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('FALLO: el cliente con compras fue eliminado.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR ESPERADO -> ' || SQLERRM);
END;
/

PROMPT ============================================================
PROMPT PRUEBA 2: Cliente no puede eliminarse si tiene movimientos
PROMPT ============================================================
BEGIN
    DELETE FROM CLIENTE WHERE DPI = '12345678-2';
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('FALLO: el cliente con movimientos fue eliminado.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR ESPERADO -> ' || SQLERRM);
END;
/

PROMPT ============================================================
PROMPT PRUEBA 3: Venta sin existencias suficientes (debe fallar)
PROMPT ============================================================
DECLARE
    V_PED NUMBER;
BEGIN
    INSERT INTO PEDIDO (ID_PEDIDO, ID_CLIENTE, SUBTOTAL, TOTAL, PUNTOS_GENERADOS, ESTADO)
    VALUES (SEQ_PEDIDO.NEXTVAL, 1, 50.00, 50.00, 4, 'PENDIENTE')
    RETURNING ID_PEDIDO INTO V_PED;

    INSERT INTO DETALLE_PEDIDO (ID_DETALLE, ID_PEDIDO, TIPO_ITEM, ID_PRODUCTO, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL, PUNTOS)
    VALUES (SEQ_DETALLE_PEDIDO.NEXTVAL, V_PED, 'PRODUCTO', 1, 9999, 25.00, 9999 * 25.00, 9999 * 2);

    UPDATE PEDIDO SET ESTADO = 'PAGADO' WHERE ID_PEDIDO = V_PED;
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('FALLO: se vendio sin existencias suficientes.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR ESPERADO -> ' || SQLERRM);
END;
/

PROMPT ============================================================
PROMPT PRUEBA 4: Canje con puntos insuficientes (debe fallar)
PROMPT ============================================================
DECLARE
    V_CLIENTE NUMBER;
BEGIN
    SELECT ID_CLIENTE INTO V_CLIENTE FROM CLIENTE WHERE DPI = '12345678-4';
    PK_PUNTOS.DESCONTAR(V_CLIENTE, 9999, 'PRUEBA');
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('FALLO: se desconto saldo por debajo de cero.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR ESPERADO -> ' || SQLERRM);
END;
/

PROMPT ============================================================
PROMPT PRUEBA 5: Pedido PAGADO no puede anularse
PROMPT ============================================================
DECLARE
    V_PED NUMBER;
BEGIN
    SELECT MIN(ID_PEDIDO) INTO V_PED FROM PEDIDO WHERE ESTADO = 'PAGADO';
    IF V_PED IS NULL THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('SIN DATOS: no hay pedidos PAGADO para probar');
        RETURN;
    END IF;
    UPDATE PEDIDO SET ESTADO = 'ANULADO' WHERE ID_PEDIDO = V_PED;
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('FALLO: se anulo un pedido ya pagado.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR ESPERADO -> ' || SQLERRM);
END;
/

PROMPT ============================================================
PROMPT PRUEBA 6: Transicion invalida de estado
PROMPT ============================================================
DECLARE
    V_PED NUMBER;
BEGIN
    INSERT INTO PEDIDO (ID_PEDIDO, ID_CLIENTE, SUBTOTAL, TOTAL, PUNTOS_GENERADOS, ESTADO)
    VALUES (SEQ_PEDIDO.NEXTVAL, 1, 0, 0, 0, 'PENDIENTE')
    RETURNING ID_PEDIDO INTO V_PED;

    UPDATE PEDIDO SET ESTADO = 'ENTREGADO' WHERE ID_PEDIDO = V_PED;
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('FALLO: se permitio PENDIENTE directo a ENTREGADO.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR ESPERADO -> ' || SQLERRM);
END;
/

PROMPT ============================================================
PROMPT PRUEBA 7: Precio o existencia negativos (debe fallar)
PROMPT ============================================================
BEGIN
    UPDATE PRODUCTO SET PRECIO_UNITARIO = -5 WHERE ID_PRODUCTO = 1;
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('FALLO: se acepto un precio negativo.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR ESPERADO -> ' || SQLERRM);
END;
/

PROMPT ============================================================
PROMPT PRUEBA 8: Verificacion de integridad de datos finales
PROMPT ============================================================
COLUMN NOMBRE FORMAT A32
SELECT C.DPI, C.NOMBRE, C.SALDO_PUNTOS, C.ESTADO
  FROM CLIENTE C ORDER BY C.ID_CLIENTE;

COLUMN NOMBRE FORMAT A32
SELECT P.CODIGO, P.NOMBRE, P.EXISTENCIA
  FROM PRODUCTO P ORDER BY P.ID_PRODUCTO;

SELECT PE.ID_PEDIDO, C.NOMBRE, PE.TOTAL, PE.PUNTOS_GENERADOS, PE.ESTADO
  FROM PEDIDO PE JOIN CLIENTE C ON C.ID_CLIENTE = PE.ID_CLIENTE
 ORDER BY PE.ID_PEDIDO;

PROMPT ============================================================
PROMPT PRUEBA 9: Los tres estados de cliente son validos
PROMPT ============================================================
DECLARE
    PROCEDURE PROBAR_ESTADO(P_ESTADO VARCHAR2) IS
    BEGIN
        UPDATE CLIENTE SET ESTADO = P_ESTADO WHERE DPI = '12345678-4';
        DBMS_OUTPUT.PUT_LINE('OK  -> ' || P_ESTADO || ' aceptado por el catalogo');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('FALLO -> ' || P_ESTADO || ' rechazado : ' || SQLERRM);
    END;
BEGIN
    PROBAR_ESTADO('ACTIVO');
    PROBAR_ESTADO('INACTIVO');
    PROBAR_ESTADO('SUSPENDIDO');
    UPDATE CLIENTE SET ESTADO = 'ACTIVO' WHERE DPI = '12345678-4';
    COMMIT;
END;
/

PROMPT ============================================================
PROMPT PRUEBA 10: Un cliente no puede quedar en un estado invalido
PROMPT ============================================================
DECLARE
    V_ESTADO VARCHAR2(20);
BEGIN
    UPDATE CLIENTE SET ESTADO = 'BORRADO' WHERE DPI = '12345678-4';
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('FALLO: se acepto un estado fuera del catalogo.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR ESPERADO -> ' || SQLERRM);
END;
/