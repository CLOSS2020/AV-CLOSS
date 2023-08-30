/*Clase....: AdminSQLiteOpenHelper
 * Autor.......: PCV MAR 2021
 * Objetivo....: Administrar la estructura de la BdD
 * Notas.......:
 *
 * Parámetros..: -- Ninguno --
 *
 * Modif.......:
 *
 * NOTAS.......: Prestar especial atención en el cambio de la versión de la base de datos tras
 * cada actualización.
 *
 * Retorna.....: Ninguno
 *-------------**/


package com.appcloos.mimaletin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

//2023-07-10: Version 36
//2023-07-17: Version 37
public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    //la version de la app debe cambiarse tras cada actualización siempre y cuando se hayan agregado tablas
    //CREATE TABLE IF NOT EXISTS tabla ( id INTEGER PRIMARY KEY  AUTOINCREMENT,...);
    public AdminSQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, 39);
    }


    //aqui se define la estructura de la base de datos al instalar la app (no cambia, solo se le agrega)
    @Override
    public void onCreate(SQLiteDatabase ke_android) {
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS articulo (codigo TEXT PRIMARY KEY, subgrupo TEXT, grupo TEXT, nombre TEXT, referencia TEXT, marca TEXT, unidad TEXT, existencia REAL, precio1 REAL, precio2 REAL, precio3 REAL, precio4 REAL, precio5 REAL, precio6 REAL, precio7 REAL,  fechamodifi NUMERIC, discont REAL, vta_min REAL NOT NULL DEFAULT 0, vta_max REAL NOT NULL DEFAULT 0, dctotope REAL NOT NULL DEFAULT 0, enpreventa char(1) NOT NULL DEFAULT '0', comprometido REAL NOT NULL DEFAULT 0, vta_minenx INTEGER NOT NULL DEFAULT 0, vta_solofac int NOT NULL DEFAULT 0, vta_solone int NOT NULL DEFAULT 0)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS tabla_aux(tabla TEXT PRIMARY KEY, fchhn_ultmod NUMERIC)");
        ke_android.execSQL("INSERT INTO tabla_aux VALUES\n" +
                "  ('articulo',    '0001-01-01 01:01:01'),\n" +
                "  ('cliempre',    '0001-01-01 01:01:01'),\n" +
                "  ('listvend',    '0001-01-01 01:01:01'),\n" +
                "  ('config2',     '0001-01-01 01:01:01'),\n" +
                "  ('grupos',      '0001-01-01 01:01:01'),\n" +
                "  ('sectores',    '0001-01-01 01:01:01'),\n" +
                "  ('subgrupos',   '0001-01-01 01:01:01'),\n" +
                "  ('subsectores', '0001-01-01 01:01:01'),\n" +
                "  ('usuarios',    '0001-01-01 01:01:01'),\n" +
                "  ('limites',     '0001-01-01 01:01:01'),\n" +
                "  ('ke_wcnf_conf','0001-01-01 01:01:01'),\n" +
                "  ('listbanc',    '0001-01-01T01:01:01'),\n" +
                "  ('ke_doccti',   '0001-01-01 01:01:01')");

        ke_android.execSQL("CREATE TABLE IF NOT EXISTS cliempre(codigo TEXT, nombre TEXT, direccion TEXT, telefonos TEXT, perscont TEXT, vendedor TEXT, contribespecial REAL, status REAL, sector TEXT, subcodigo TEXT, fechamodifi NUMERIC, precio REAL, kne_activa TEXT DEFAULT '0', kne_mtomin REAL DEFAULT 0.000, `noemifac` int NOT NULL DEFAULT '0', `noeminota` int NOT NULL DEFAULT '0')");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS listvend(codigo TEXT, nombre TEXT, telefonos Text, telefono_movil TEXT, status REAL, superves REAL, supervpor TEXT, sector TEXT, subcodigo TEXT, nivgcial REAL, fechamodifi NUMERIC)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS sectores(codigo TEXT PRIMARY KEY,zona TEXT, fechamodifi NUMERIC)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS config2(id_precio1 TEXT ,id_precio2 TEXT, id_precio3 TEXT, id_precio4 TEXT, id_precio5 TEXT, id_precio6 TEXT, id_precio7 TEXT)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS subsectores(codigo TEXT, subcodigo TEXT , subsector TEXT, fechamodifi NUMERIC)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS subgrupos(codigo TEXT , subcodigo TEXT  , nombre TEXT , fechamodifi NUMERIC)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS grupos(codigo TEXT PRIMARY KEY, nombre TEXT, fechamodifi NUMERIC)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS usuarios(nombre TEXT, username TEXT, password TEXT, vendedor TEXT, almacen TEXT, desactivo REAL, fechamodifi NUMERIC, ualterprec REAL, sesionactiva NUMERIC, superves TEXT, ult_sinc NUMERIC DEFAULT '0001-01-01', sinc_primera NUMERIC NOT NULL DEFAULT 0)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_opti(kti_ndoc TEXT, kti_tdoc TEXT, kti_codcli TEXT, kti_nombrecli TEXT, kti_codven TEXT, kti_docsol TEXT, kti_condicion TEXT, kti_tipprec REAL, kti_totneto REAL, kti_status TEXT, kti_nroped TEXT, kti_fchdoc NUMERIC, fechamodifi NUMERIC, kti_negesp TEXT, kti_totnetodcto REAL NOT NULL DEFAULT 0, ke_pedstatus TEXT NOT NULL DEFAULT '00')");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_opmv(kti_tdoc TEXT, kti_ndoc TEXT, kti_tipprec REAL, kmv_codart TEXT, kmv_nombre TEXT, kmv_cant REAL, kmv_artprec REAL, kmv_stot REAL, kmv_dctolin REAL NOT NULL DEFAULT 0, kmv_stotdcto REAL NOT NULL DEFAULT 0)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_carrito(kmv_codart TEXT, kmv_nombre TEXT, kmv_cant REAL,  kmv_artprec REAL, kmv_stot REAL, kmv_dctolin REAL NOT NULL DEFAULT 0, kmv_stotdcto REAL NOT NULL DEFAULT 0)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_correla(kco_numero INTEGER, kco_vendedor TEXT)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_kardex(kde_codart TEXT PRIMARY KEY, kde_cantidad REAL, ke_fecha NUMERIC)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_estadc01(codcoord VARCHAR(8), nomcoord VARCHAR(54), vendedor VARCHAR(8), nombrevend VARCHAR(54), cntpedidos REAL, mtopedidos REAL, cntfacturas REAL, mtofacturas REAL, metavend REAL, prcmeta REAL, cntclientes REAL, clivisit REAL, prcvisitas REAL, lom_montovtas REAL, lom_prcvtas REAL, lom_prcvisit REAL, rlom_montovtas REAL, rlom_prcvtas REAL, rlom_prcvisit REAL, fecha_estad NUMERIC, ppgdol_totneto REAL, devdol_totneto REAL, defdol_totneto REAL, totdolcob REAL)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_cxc(kcx_nrorecibo VARCHAR (17), kcx_codcli VARCHAR(20), kcx_codven TEXT, kcx_ncliente VARCHAR(100), kcx_monto NUMERIC, kcx_fechamodifi NUMERIC, kcx_status TEXT)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_correlacxc(kcc_numero INTEGER, kcc_vendedor TEXT)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_limitart(kli_track TEXT, kli_codven TEXT, kli_codcli TEXT, kli_codart TEXT, kli_cant INTEGER, kli_fechahizo NUMERIC, kli_fechavence NUMERIC, status TEXT)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_version(kve_version VARCHAR(8), kve_activa CHAR(1))");

        //tabla de las cabeceras de los documentos
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_doccti(" +
                "agencia VARCHAR(3) NOT NULL DEFAULT '', " +
                "tipodoc varchar(3) NOT NULL DEFAULT '', " +
                "documento varchar(8) NOT NULL DEFAULT '', " +
                "tipodocv varchar(3) NOT NULL DEFAULT ''," +
                "codcliente varchar(20) NOT NULL DEFAULT ''," +
                "nombrecli varchar(100) NOT NULL DEFAULT ''," +
                "contribesp double(2,0) NOT NULL DEFAULT '0'," +
                "ruta_parme char(1) NOT NULL DEFAULT '0'," +
                "tipoprecio double(2,0) NOT NULL DEFAULT '1'," +
                "emision date NOT NULL DEFAULT '0000-00-00'," +
                "recepcion date NOT NULL DEFAULT '0000-00-00'," +
                "vence date NOT NULL  DEFAULT '0000-00-00'," +
                "diascred double(2,0) NOT NULl DEFAULT '0'," +
                "estatusdoc varchar(1) NOT NULL  DEFAULT ''," +
                "dtotneto double(24,7) NOT NULL DEFAULT '0'," +
                "dtotimpuest double(24,7) NOT NULL DEFAULT '0'," +
                "dtotalfinal double(24,7) NOT NULL DEFAULT '0'," +
                "dtotpagos double(24,7) NOT NULL DEFAULT '0'," +
                "dtotdescuen double(24,7) NOT NULL DEFAULT '0'," +
                "dFlete double(24,7) NOT NULL DEFAULT '0'," +
                "dtotdev double(24,7) NOT NULL DEFAULT '0'," +
                "dvndmtototal double(24,7) NOT NULL DEFAULT '0'," +
                "dretencion double(24,7) NOT NULL DEFAULT '0'," +
                "dretencioniva double(24,7) NOT NULL DEFAULT '0'," +
                "vendedor varchar(8) NOT NULL  DEFAULT ''," +
                "codcoord varchar(8) NOT NULL  DEFAULT ''," +
                "fechamodifi datetime NOT NULL  DEFAULT '0000-00-00 00:00:00'," +
                "aceptadev char(1) NOT NULL  DEFAULT ''," +
                "kti_negesp char(1)NOT NULL DEFAULT '0'," +
                "bsiva double(24,7) NOT NULL DEFAULT '0'," +
                "bsflete double(24,7) NOT NULL DEFAULT '0'," +
                "bsretencioniva double(24,7) NOT NULL DEFAULT '0'," +
                "bsretencion double(24,7) NOT NULL DEFAULT '0'," +
                "tasadoc double(24,7) NOT NULL DEFAULT '0.00'," +
                "mtodcto double(24,7) NOT NULL DEFAULT '0.00'," +
                "fchvencedcto date NOT NULL DEFAULT '0000-00-00'," +
                "tienedcto char(1) NOT NULL DEFAULT '0'," +
                "cbsret double(24,7) NOT NULL DEFAULT '0.00'," +
                "cdret double(24,7) NOT NULL DEFAULT '0.00'," +
                "cbsretiva double(24,7) NOT NULL DEFAULT '0.00'," +
                "cdretiva double(24,7) NOT NULL DEFAULT '0.00'," +
                "cbsrparme double(24,7) NOT NULL DEFAULT '0.00'," +
                "cdrparme double(24,7) NOT NULL DEFAULT '0.00'," +
                "cbsretflete double(24,7) NOT NULL DEFAULT '0.00'," +
                "cdretflete double(24,7) NOT NULL DEFAULT '0.00'," +
                "bsmtoiva double(24,7) NOT NULL DEFAULT '0.00'," +
                "bsmtofte double(24,7) NOT NULL DEFAULT '0.00'," +
                "retmun_mto double(24,7) NOT NULL DEFAULT '0.00')");

        //tabla de lineas
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_doclmv ( " +
                "agencia varchar(3) NOT NULL DEFAULT '' ," +
                "tipodoc varchar(3) NOT NULL DEFAULT '' , " +
                "documento varchar(8) NOT NULL DEFAULT '', " +
                "tipodocv varchar(3) NOT NULL DEFAULT ''," +
                "grupo varchar(6) NOT NULL DEFAULT ''," +
                "subgrupo varchar(6) NOT NULL DEFAULT ''," +
                "origen double(2,0) NOT NULL DEFAULT '0'," +
                "codigo varchar(25) NOT NULL DEFAULT ''," +
                "codhijo varchar(25) NOT NULL DEFAULT ''," +
                "pid varchar(12) NOT NULL DEFAULT ''," +
                "nombre varchar(100) NOT NULL DEFAULT ' '," +
                "cantidad double(24,7) NOT NULL DEFAULT '0.0000000', " +
                "cntdevuelt double(24,7) NOT NULL DEFAULT '0.0000000', " +
                "vndcntdevuelt double(24,7) NOT NULL DEFAULT '0.0000000'," +
                "dvndmtototal double(24,7) NOT NULL DEFAULT '0.0000000', " +
                "dpreciofin double(24,7) NOT NULL DEFAULT '0.0000000'," +
                "dpreciounit double(24,7) NOT NULL DEFAULT '0.0000000'," +
                "dmontoneto double(24,7) NOT NULL DEFAULT '0.0000000'," +
                "dmontototal double(24,7) NOT NULL DEFAULT '0.0000000'," +
                "timpueprc double(24,7) NOT NULL DEFAULT '0.0000000'," +
                "unidevuelt double(24,7) NOT NULL DEFAULT '0.0000000'," +
                "fechadoc date NOT NULL DEFAULT '0000-00-00', " +
                "vendedor varchar(8) NOT NULL DEFAULT ''," +
                "codcoord varchar(8) NOT NULL DEFAULT ''," +
                "fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00')");

        //tabla de lineas temporal de reclamos
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_devlmtmp (" +
                "kdel_referencia TEXT NOT NULL DEFAULT ''," +
                "kdel_documento  varchar(8) NOT NULL DEFAULT ''," +
                "kdel_pid varchar(12) NOT NULL DEFAULT ''," +
                "kdel_codart varchar(25) NOT NULL DEFAULT ''," +
                "kdel_mtolinea double(2,0 ) NOT NULL DEFAULT '0'," +
                "kdel_preciofin double (2,0) NOT NULL DEFAULT '0', " +
                "kdel_cantdev double(2,0 ) NOT NULL DEFAULT '0'," +
                "kdel_cantped double(2,0 ) NOT NULL DEFAULT '0'," +
                "kdel_nombre VARCHAR(100) NOT NULL DEFAULT '')");

        //tabla de correlativos de devolución
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_correladev(kdev_numero INTEGER, kdev_vendedor TEXT)");

        //tablas definitivas de devolucion
        //cabecera
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_rclcti (" +
                "krti_ndoc TEXT," +
                "krti_status char(2)," +
                "krti_agefac varchar(3)," +
                "krti_tipfac varchar(3)," +
                "krti_docfac varchar(8)," +
                "krti_tipprec double(2,0)," +
                "krti_totneto double (2,0)," +
                "krti_totnetodef double(2,0)," +
                "krti_fchdoc datetime," +
                "kdv_codclasif char(2)," +
                "krti_substatus char(2)," +
                "krti_agenc varchar(3)," +
                "krti_tipnc varchar(3)," +
                "krti_docnc varchar(8)," +
                "krti_agedev varchar(3)," +
                "krti_tipdev varchar(3)," +
                "krti_docdev varchar(8)," +
                "krti_notas longtext(240)," +
                "krti_codvend varchar(3)," +
                "krti_codcoor varchar(3)," +
                "krti_codcli varchar(10)," +
                "krti_nombrecli varchar(100)," +
                "fechamodifi datetime," +
                "kdv_codclasidef char(2))" +
                "");

        //lineas
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_rcllmv (" +
                "krti_ndoc TEXT," +
                "krmv_tipprec double(2,0)," +
                "krmv_codart TEXT," +
                "krmv_nombre TEXT," +
                "krmv_cant double(2,0)," +
                "krmv_artprec double(2,0)," +
                "krmv_stot double(2,0)," +
                "krmv_cantdef double(2,0)," +
                "krmv_stotdef double(2,0)," +
                "krmv_pid TEXT," +
                "fechamodifi datetime" +
                ")");
        ke_android.execSQL("INSERT INTO tabla_aux VALUES ('ke_rclcti',     '0001-01-01 01:01:01')");

        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_imgrcl (krti_ndoc TEXT, kircl_rutafoto TEXT)");//tabla sin uso actual


        //tabla para las clasificaciones de los reclamos
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_tiporecl(kdv_codclasif CHAR, kdv_nomclaweb TEXT, kdv_nomclasif TEXT, kdv_hlpclasif LONGTEXT, fechamodifi NUMERIC)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_enlace(kee_codigo TEXT, kee_nombre TEXT, kee_url TEXT, kee_status TEXT, kee_sucursal TEXT)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_modulos(ked_codigo TEXT, kmo_codigo TEXT, kmo_status TEXT, kee_sucursal TEXT)");


        //tabla cabecera de cobranzas
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_precobranza(  " +
                "cxcndoc varchar(16) NOT NULL DEFAULT ''," +
                " tiporecibo char(1) NOT NULL DEFAULT 'W'," +
                " codvend varchar(8) NOT NULL," +
                " nro_recibo char(8) NOT NULL DEFAULT '',\n" +
                " kecxc_id char(12) NOT NULL DEFAULT '' ,\n" +
                " tasadia double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " fchrecibo date NOT NULL DEFAULT '0000-00-00' ,\n" +
                " clicontesp char(1) NOT NULL DEFAULT '',\n" +
                " bsneto double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bsiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bsretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bsflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bstotal double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " dolneto double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " doliva double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                " dolretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " dolflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " doltotal double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " moneda char(1) NOT NULL DEFAULT '0',\n" +
                " docdifcamb char(1) NOT NULL DEFAULT '0',\n" +
                " ddc_age varchar(3) NOT NULL DEFAULT '',\n" +
                " ddc_tipo varchar(3) NOT NULL DEFAULT '',\n" +
                " ddc_montobs double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " ddc_doc varchar(8) NOT NULL DEFAULT '',\n" +
                " dctoaplic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " netocob double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " concepto varchar(200) NOT NULL DEFAULT '',\n" +
                " efectivo double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                " bcoecod varchar(3) NOT NULL DEFAULT '',\n" +
                " bcocod varchar(3) NOT NULL DEFAULT '' ,\n" +
                " bconombre varchar(59) NOT NULL DEFAULT '',\n" +
                " fchr_dep datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " bcomonto double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bcoref varchar(20) NOT NULL DEFAULT '',\n" +
                " pidvalid varchar(12) NOT NULL DEFAULT '',\n" +
                " edorec char(1) NOT NULL DEFAULT '0',\n" +
                " edocomiv char(1) NOT NULL DEFAULT '0',\n" +
                " prccomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " mtocomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " fchr_pcomv datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " codcoord varchar(8) NOT NULL DEFAULT '',\n" +
                " edocomic char(1) NOT NULL DEFAULT '0',\n" +
                " prccomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " mtocomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " fchr_pcomc datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " fchhr datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " fchvigen date NOT NULL DEFAULT '0000-00-00',\n" +
                " bsretflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " diasvigen double(5,0) NOT NULL DEFAULT '0',\n" +
                " retmun_sbi double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " retmun_sbs double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " comiaut char(1) NOT NULL DEFAULT '0',\n" +
                " comiautpor varchar(30) NOT NULL DEFAULT ' ',\n" +
                " comiautfch datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " reci_age varchar(3) NOT NULL DEFAULT ' ',\n" +
                " reci_doc char(8) NOT NULL DEFAULT '',\n" +
                " status char(2) NOT NULL DEFAULT '0',\n" +
                " fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " cxcndoc_aux varchar(16) NOT NULL DEFAULT '',\n" +
                "  PRIMARY KEY (cxcndoc))");


        //tabla de detalles de cobranza
//TABLA DE LINEAS DE CXC
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_precobradocs(" +
                "cxcndoc varchar(16) NOT NULL DEFAULT ''," +
                "  agencia varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  tipodoc varchar(3) NOT NULL DEFAULT '',\n" +
                "  documento varchar(8) NOT NULL DEFAULT '',\n" +
                "  bscobro double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prccobro double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prcdsctopp double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  nroret varchar(8) NOT NULL DEFAULT '',\n" +
                "  fchemiret date NOT NULL DEFAULT '0000-00-00' ,\n" +
                "  bsretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  refret varchar(20) NOT NULL DEFAULT '',\n" +
                "  nroretfte varchar(8) NOT NULL DEFAULT '',\n" +
                "  fchemirfte date NOT NULL DEFAULT '0000-00-00',\n" +
                "  bsmtofte double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  bsretfte double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  refretfte varchar(20) NOT NULL DEFAULT '',\n" +
                "  pidvalid varchar(12) NOT NULL DEFAULT '',\n" +
                "  bsmtoiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_bi double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_cod varchar(5) NOT NULL DEFAULT ' ',\n" +
                "  retmun_nro varchar(8) NOT NULL DEFAULT ' ',\n" +
                "  retmun_mto double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_fch date NOT NULL DEFAULT '0000-00-00',\n" +
                "  retmun_ref varchar(20) NOT NULL DEFAULT ' ',\n" +
                "  diascalc double(4,0) NOT NULL DEFAULT '0',\n" +
                "  prccomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prccomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  cxcndoc_aux varchar(16) NOT NULL DEFAULT '', \n" +
                "  tnetodbs double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                "  tnetoddol double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                "  fchrecibod date NOT NULL DEFAULT '0000-00-00' ,\n" +
                "  kecxc_idd char(12) NOT NULL DEFAULT '' ,\n" +
                "  tasadiad double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                "  afavor double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                "  reten varchar(1) NOT NULL DEFAULT '0')");

        // creacion de tabla para las tasas de cambio en bss
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS kecxc_tasas (\n" +
                "  `kecxc_id` varchar(12) NOT NULL DEFAULT '',\n" +
                "  `kecxc_fecha` date NOT NULL DEFAULT '0000-00-00',\n" +
                "  `kecxc_tasa` double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  `kecxc_usuario` varchar(30) NOT NULL DEFAULT '',\n" +
                "  `kecxc_ip` varchar(20) NOT NULL DEFAULT '',\n" +
                "  `kecxc_fchyhora` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                "  `fechamodifi` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                "   kecxc_tasaib double(24,7) NOT NULL DEFAULT '0.0000000')");

        ke_android.execSQL("CREATE TABLE IF NOT EXISTS `listbanc` (\n" +
                "  codbanco     varchar(3)  NOT NULL DEFAULT '' UNIQUE,\n" +
                "  nombanco      varchar(59) not null DEFAULT '',\n" +
                "  cuentanac     double(2,0) NOT NULL DEFAULT '0',\n" +
                "  inactiva      double(1,0) NOT NULL DEFAULT '0',\n" +
                "  fechamodifi   datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                "  PRIMARY KEY (codbanco))");

        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_corprec(kcor_numero INTEGER, kcor_vendedor TEXT)");
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_precobdcto (\n" +
                "  agencia varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  tipodoc varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  documento varchar(8) NOT NULL DEFAULT '' ,\n" +
                "  codcliente varchar(20) NOT NULL DEFAULT '' ,\n" +
                "  edodcto char(1) NOT NULL DEFAULT '0',\n" +
                "  prcdctoaplic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  montodctodol double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  montodctobs double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  tasadoc double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  fchvigen date NOT NULL DEFAULT '0000-00-00',\n" +
                "  docgen_age varchar(3) NOT NULL DEFAULT '',\n" +
                "  docgen_tipo varchar(3) NOT NULL DEFAULT '',\n" +
                "  docgen_nro varchar(8) NOT NULL DEFAULT '' ,\n" +
                "  recgen_age varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  recgen_tipo varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  recgen_nro varchar(8) NOT NULL DEFAULT '' ,\n" +
                "  fechamodifi datetime NOT NULL DEFAULT '1000-01-01 00:00:00'" +
                ")");

        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_mtopendcli(" +
                " codcliente TEXT NOT NULL DEFAULT '', " +
                " id_recibo TEXT NOT NULL DEFAULT '', " +
                " moneda TEXT NOT NULL DEFAULT '1', " +
                " montocli REAL NOT NULL DEFAULT '0.0000000', " +
                " estado TEXT NOT NULL DEFAULT '0', " +
                " id_reciboap TEXT NOT NULL DEFAULT '', " +
                " agencia TEXT NOT NULL DEFAULT '', " +
                " tipodoc TEXT NOT NULL DEFAULT '', " +
                " documento TEXT NOT NULL DEFAULT '', " +
                " fchregdif TEXT NOT NULL DEFAULT '0000-00-00 00:00:00', " +
                " usuario TEXT NOT NULL DEFAULT '', " +
                " edoweb TEXT NOT NULL DEFAULT '0', " +
                " fechamodifi TEXT NOT NULL DEFAULT '0000-00-00 00:00:00' " +
                ")");


        ke_android.execSQL("CREATE TABLE IF NOT EXISTS `ke_retimg` (\n" +
                "  `cxcndoc` char(16) NOT NULL DEFAULT '' ,\n" +
                "  `ruta` varchar(100) NOT NULL DEFAULT '',\n" +
                "  `ret_nomimg` varchar(30) NOT NULL DEFAULT '' ,\n" +
                "  `status` char(1) NOT NULL DEFAULT '0'\n" +
                ")");


        ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_referencias(bcoref TEXT, bcocod TEXT, tiporef TEXT);");

        //2023-06-15 Tabla de configuración
        ke_android.execSQL("CREATE TABLE IF NOT EXISTS `ke_wcnf_conf` (\n" +
                "  `cnfg_idconfig` char(30) NOT NULL DEFAULT '',\n" +
                "  `cnfg_clase` char(1) NOT NULL DEFAULT '' ,\n" +
                "  `cnfg_tipo` char(1) NOT NULL ,\n" +
                "  `cnfg_valnum` double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                "  `cnfg_valsino` double(1,0) NOT NULL DEFAULT '0' ,\n" +
                "  `cnfg_valtxt` text ,\n" +
                "  `cnfg_lentxt` double(3,0) NOT NULL DEFAULT '0' ,\n" +
                "  `cnfg_valfch` date NOT NULL DEFAULT '0000-00-00' ,\n" +
                "  `cnfg_activa` double(1,0) NOT NULL DEFAULT '0' ,\n" +
                "  `cnfg_etiq` text ,\n" +
                "  `cnfg_ttip` text ,\n" +
                "  `fechamodifi` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' ,\n" +
                "  `username` varchar(30) NOT NULL DEFAULT '')");

        ke_android.execSQL("CREATE TABLE IF NOT EXISTS `img_carousel` (\n" +
                "  `nombre` varchar(250) NOT NULL DEFAULT '',\n" +
                "  `enlace` varchar(250) NOT NULL DEFAULT '' ,\n" +
                "  `fechamodifi` datetime NOT NULL DEFAULT '0001-01-01 01:01:01')");
    }


    //aqui van las instrucciones de la BdD tras cada nueva actualización (cambiar siempre que se cree una nueva versión)
    @Override
    public void onUpgrade(SQLiteDatabase ke_android, int oldVersion, int newVersion) {
        //ke_android.execSQL("CREATE TABLE ke_version(kve_version VARCHAR(8), kve_activa CHAR(1))");


        /*ke_android.execSQL("ALTER TABLE ke_enlace ADD COLUMN kee_sucursal TEXT");
        ke_android.execSQL("ALTER TABLE ke_modulos ADD COLUMN kee_sucursal TEXT");*/

        //tabla cabecera de cobranzas
      /*  ke_android.execSQL("CREATE TABLE ke_precobranza( " +
                "cxcndoc varchar(16) NOT NULL DEFAULT ''," +
                "tiporecibo char(1) NOT NULL DEFAULT 'W'," +
                " codvend varchar(8) NOT NULL," +
                " nro_recibo char(8) NOT NULL DEFAULT '',\n" +
                " kecxc_id char(12) NOT NULL DEFAULT '' ,\n" +
                " tasadia double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " fchrecibo date NOT NULL DEFAULT '0000-00-00' ,\n" +
                " clicontesp char(1) NOT NULL DEFAULT '',\n" +
                " bsneto double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bsiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bsretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bsflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bstotal double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " dolneto double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " doliva double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                " dolretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " dolflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " doltotal double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " moneda char(1) NOT NULL DEFAULT '0',\n" +
                " docdifcamb char(1) NOT NULL DEFAULT '0',\n" +
                " ddc_age varchar(3) NOT NULL DEFAULT '',\n" +
                " ddc_tipo varchar(3) NOT NULL DEFAULT '',\n" +
                " ddc_montobs double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " ddc_doc varchar(8) NOT NULL DEFAULT '',\n" +
                " dctoaplic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " netocob double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " concepto varchar(200) NOT NULL DEFAULT '',\n" +
                " efectivo double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                " bcoecod varchar(3) NOT NULL DEFAULT '',\n" +
                " bcocod varchar(3) NOT NULL DEFAULT '' ,\n" +
                " bconombre varchar(59) NOT NULL DEFAULT '',\n" +
                " fchr_dep datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " bcomonto double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                " bcoref varchar(20) NOT NULL DEFAULT '',\n" +
                " pidvalid varchar(12) NOT NULL DEFAULT '',\n" +
                " edorec char(1) NOT NULL DEFAULT '0',\n" +
                " edocomiv char(1) NOT NULL DEFAULT '0',\n" +
                " prccomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " mtocomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " fchr_pcomv datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " codcoord varchar(8) NOT NULL DEFAULT '',\n" +
                " edocomic char(1) NOT NULL DEFAULT '0',\n" +
                " prccomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " mtocomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " fchr_pcomc datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " fchhr datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " fchvigen date NOT NULL DEFAULT '0000-00-00',\n" +
                " bsretflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " diasvigen double(5,0) NOT NULL DEFAULT '0',\n" +
                " retmun_sbi double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " retmun_sbs double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                " comiaut char(1) NOT NULL DEFAULT '0',\n" +
                " comiautpor varchar(30) NOT NULL DEFAULT ' ',\n" +
                " comiautfch datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                " reci_age varchar(3) NOT NULL DEFAULT ' ',\n" +
                " reci_doc char(8) NOT NULL DEFAULT '',\n" +
                "  PRIMARY KEY (cxcndoc))");



        /*ke_android.execSQL("CREATE TABLE ke_precobradocs(" +
                "  cxcndoc varchar(16) NOT NULL DEFAULT ''," +
                "  agencia varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  tipodoc varchar(3) NOT NULL DEFAULT '',\n" +
                "  documento varchar(8) NOT NULL DEFAULT '',\n" +
                "  bscobro double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prccobro double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prcdsctopp double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  nroret varchar(8) NOT NULL DEFAULT '',\n" +
                "  fchemiret date NOT NULL DEFAULT '0000-00-00' ,\n" +
                "  bsretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  refret varchar(20) NOT NULL DEFAULT '',\n" +
                "  nroretfte varchar(8) NOT NULL DEFAULT '',\n" +
                "  fchemirfte date NOT NULL DEFAULT '0000-00-00',\n" +
                "  bsmtofte double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  bsretfte double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  refretfte varchar(20) NOT NULL DEFAULT '',\n" +
                "  pidvalid varchar(12) NOT NULL DEFAULT '',\n" +
                "  bsmtoiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_bi double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_cod varchar(5) NOT NULL DEFAULT ' ',\n" +
                "  retmun_nro varchar(8) NOT NULL DEFAULT ' ',\n" +
                "  retmun_mto double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_fch date NOT NULL DEFAULT '0000-00-00',\n" +
                "  retmun_ref varchar(20) NOT NULL DEFAULT ' ',\n" +
                "  diascalc double(4,0) NOT NULL DEFAULT '0',\n" +
                "  prccomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prccomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  CONSTRAINT cxcndoc PRIMARY KEY (cxcndoc, agencia, tipodoc, documento))");*/
        // -- Campos nuevos para la tabla de documentos por cliente --
     /*   ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN bsiva double(24,7) NOT NULL DEFAULT '0'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN bsflete double(24,7) NOT NULL DEFAULT '0'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN bsretencioniva double(24,7) NOT NULL DEFAULT '0'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN bsretencion double(24,7) NOT NULL DEFAULT '0'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN tasadoc double(24,7) NOT NULL DEFAULT '0'");
        ke_android.execSQL("ALTER TABLE articulos ADD COLUMN enpreventa char(1) NOT NULL DEFAULT '0'");

        // creacion de tabla para las tasas de cambio en bss
      /*  ke_android.execSQL("CREATE TABLE kecxc_tasas (\n" +
                "  `kecxc_id` varchar(12) NOT NULL DEFAULT '',\n" +
                "  `kecxc_fecha` date NOT NULL DEFAULT '0000-00-00',\n" +
                "  `kecxc_tasa` double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  `kecxc_usuario` varchar(30) NOT NULL DEFAULT '',\n" +
                "  `kecxc_ip` varchar(20) NOT NULL DEFAULT '',\n" +
                "  `kecxc_fchyhora` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                "  `fechamodifi` datetime NOT NULL DEFAULT '0000-00-00 00:00:00')");*/


       /* ke_android.execSQL("CREATE TABLE listbanc (\n" +
                "  codbanco     varchar(3)  NOT NULL DEFAULT '',\n" +
                "  nombanco      varchar(59) not null DEFAULT '',\n" +
                "  cuentanac     double(2,0) NOT NULL DEFAULT '0',\n" +
                "  inactiva      double(1,0) NOT NULL DEFAULT '0',\n" +
                "  fechamodifi   datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +*/
        /* "  PRIMARY KEY (codbanco))");*/

        /*ke_android.execSQL("CREATE TABLE ke_corprec(kcor_numero INTEGER, kcor_vendedor TEXT)");
        ke_android.execSQL("ALTER TABLE kecxc_tasas ADD COLUMN kecxc_tasaib double(24,7) NOT NULL DEFAULT '0.0000000'");*/


     /*   ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN mtodcto double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN fchvencedcto date NOT NULL DEFAULT '0000-00-00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN tienedcto char(1) NOT NULL DEFAULT '0'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN cbsret double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN cdret double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN cbsretiva double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN cdretiva double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN cbsrparme double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN cdrparme double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN bsmtoiva double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN bsmtofte double(24,7) NOT NULL DEFAULT '0.00'");
        ke_android.execSQL("ALTER TABLE ke_doccti ADD COLUMN retmun_mto double(24,7) NOT NULL DEFAULT '0.00'");*/

   /*     //modify column
        ke_android.execSQL("CREATE TABLE `listbanc` (\n" +
                "  codbanco     varchar(3)  NOT NULL DEFAULT '' UNIQUE,\n" +
                "  nombanco      varchar(59) not null DEFAULT '',\n" +
                "  cuentanac     double(2,0) NOT NULL DEFAULT '0',\n" +
                "  inactiva      double(1,0) NOT NULL DEFAULT '0',\n" +
                "  fechamodifi   datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                "  PRIMARY KEY (codbanco))");

        ke_android.execSQL("CREATE TABLE ke_corprec(kcor_numero INTEGER, kcor_vendedor TEXT)");*/

      /* ke_android.execSQL("CREATE TABLE ke_precobradocs(" +
                "cxcndoc varchar(16) NOT NULL DEFAULT ''," +
                "  agencia varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  tipodoc varchar(3) NOT NULL DEFAULT '',\n" +
                "  documento varchar(8) NOT NULL DEFAULT '',\n" +
                "  bscobro double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prccobro double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prcdsctopp double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  nroret varchar(8) NOT NULL DEFAULT '',\n" +
                "  fchemiret date NOT NULL DEFAULT '0000-00-00' ,\n" +
                "  bsretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  refret varchar(20) NOT NULL DEFAULT '',\n" +
                "  nroretfte varchar(8) NOT NULL DEFAULT '',\n" +
                "  fchemirfte date NOT NULL DEFAULT '0000-00-00',\n" +
                "  bsmtofte double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  bsretfte double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  refretfte varchar(20) NOT NULL DEFAULT '',\n" +
                "  pidvalid varchar(12) NOT NULL DEFAULT '',\n" +
                "  bsmtoiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_bi double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_cod varchar(5) NOT NULL DEFAULT ' ',\n" +
                "  retmun_nro varchar(8) NOT NULL DEFAULT ' ',\n" +
                "  retmun_mto double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  retmun_fch date NOT NULL DEFAULT '0000-00-00',\n" +
                "  retmun_ref varchar(20) NOT NULL DEFAULT ' ',\n" +
                "  diascalc double(4,0) NOT NULL DEFAULT '0',\n" +
                "  prccomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  prccomic double(24,7) NOT NULL DEFAULT '0.0000000')");*/

       /*ke_android.execSQL("CREATE TABLE ke_precobdcto (\n" +
                "  agencia varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  tipodoc varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  documento varchar(8) NOT NULL DEFAULT '' ,\n" +
                "  codcliente varchar(20) NOT NULL DEFAULT '' ,\n" +
                "  edodcto char(1) NOT NULL DEFAULT '0',\n" +
                "  prcdctoaplic double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  montodctodol double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  montodctobs double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  tasadoc double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                "  fchvigen date NOT NULL DEFAULT '0000-00-00',\n" +
                "  docgen_age varchar(3) NOT NULL DEFAULT '',\n" +
                "  docgen_tipo varchar(3) NOT NULL DEFAULT '',\n" +
                "  docgen_nro varchar(8) NOT NULL DEFAULT '' ,\n" +
                "  recgen_age varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  recgen_tipo varchar(3) NOT NULL DEFAULT '' ,\n" +
                "  recgen_nro varchar(8) NOT NULL DEFAULT '' ,\n" +
                "  fechamodifi datetime NOT NULL DEFAULT '1000-01-01 00:00:00'" +
                ")");*/

        /*ke_android.execSQL("ALTER TABLE articulo ADD COLUMN comprometido REAL NOT NULL DEFAULT 0");
        ke_android.execSQL("ALTER TABLE ke_estadc01 ADD COLUMN ppgdol_totneto REAL NOT NULL DEFAULT 0");
        ke_android.execSQL("ALTER TABLE ke_estadc01 ADD COLUMN devdol_totneto REAL NOT NULL DEFAULT 0");
        ke_android.execSQL("ALTER TABLE ke_estadc01 ADD COLUMN defdol_totneto REAL NOT NULL DEFAULT 0");*/

       /* ke_android.execSQL("ALTER TABLE articulo\n" +
                "  ADD vta_minenx INTEGER;");*/

        /*ke_android.execSQL("ALTER TABLE ke_estadc01\n" +
                "  ADD totdolcob REAL;");
                */

        /*
        ke_android.execSQL("ALTER TABLE usuarios\n" +
                "  ADD ult_sinc NUMERIC DEFAULT '0001-01-01';");*/

        /*if (oldVersion >= 23) {
            ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_mtopendcli(" +
                    " codcliente TEXT NOT NULL DEFAULT '', " +
                    " id_recibo TEXT NOT NULL DEFAULT '', " +
                    " moneda TEXT NOT NULL DEFAULT '1', " +
                    " montocli REAL NOT NULL DEFAULT '0.0000000', " +
                    " estado TEXT NOT NULL DEFAULT '0', " +
                    " id_reciboap TEXT NOT NULL DEFAULT '', " +
                    " agencia TEXT NOT NULL DEFAULT '', " +
                    " tipodoc TEXT NOT NULL DEFAULT '', " +
                    " documento TEXT NOT NULL DEFAULT '', " +
                    " fchregdif TEXT NOT NULL DEFAULT '0000-00-00 00:00:00', " +
                    " usuario TEXT NOT NULL DEFAULT '', " +
                    " edoweb TEXT NOT NULL DEFAULT '0', " +
                    " fechamodifi TEXT NOT NULL DEFAULT '0000-00-00 00:00:00' " +
                    ")");

            ke_android.execSQL("ALTER TABLE `ke_precobranza` ADD `cxcndocAux` VARCHAR(16) NOT NULL DEFAULT '';");
            ke_android.execSQL("ALTER TABLE `ke_precobradocs` ADD `cxcndocAux` VARCHAR(16) NOT NULL DEFAULT '';");
            monto_aux double(24,7) NOT NULL DEFAULT '0.0000000'
        }*/

        /*if (oldVersion <= 25){
            ke_android.execSQL("ALTER TABLE `ke_precobradocs` ADD `tnetodbs` double(24,7) NOT NULL DEFAULT '0.0000000';");
            ke_android.execSQL("ALTER TABLE `ke_precobradocs` ADD `tnetoddol` double(24,7) NOT NULL DEFAULT '0.0000000';");
            ke_android.execSQL("ALTER TABLE ke_opti ADD ke_pedstatus TEXT NOT NULL DEFAULT '00';");
        }*/

        //" fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
        //                " cxcndoc_aux varchar(16) NOT NULL DEFAULT '',\n" +

        //if (oldVersion <= 27){
        //ke_android.execSQL("ALTER TABLE `ke_precobranza` ADD `fechamodifi` datetime NOT NULL DEFAULT '0000-00-00 00:00:00';");
        //ke_android.execSQL("ALTER TABLE `ke_precobranza` ADD `cxcndoc_aux` varchar(16) NOT NULL DEFAULT '';");
        // }

        /*if (oldVersion <= 28){
            ke_android.execSQL("ALTER TABLE `ke_precobradocs` ADD `fchrecibod` date NOT NULL DEFAULT '0000-00-00';");
            ke_android.execSQL("ALTER TABLE `ke_precobradocs` ADD `kecxc_idd` char(12) NOT NULL DEFAULT '';");
            ke_android.execSQL("ALTER TABLE `ke_precobradocs` ADD `tasadiad` double(24,7) NOT NULL DEFAULT '0.0000000';");

            //"  kecxc_id char(12) NOT NULL DEFAULT '' ,\n" +
            //                "  tasadia double(24,7) NOT NULL DEFAULT '0.0000000' )");
            //ke_android.execSQL("ALTER TABLE `ke_precobranza` ADD `cxcndoc_aux` varchar(16) NOT NULL DEFAULT '';");
        }*/


        if (oldVersion < 29) {
            try {
                ke_android.execSQL("ALTER TABLE ke_precobradocs ADD afavor double(24,7) NOT NULL DEFAULT '0.0000000';");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (oldVersion < 30) {
            //ke_android.execSQL("CREATE TABLE ke_imgret (cxcndoc TEXT, rutafoto TEXT, nombre TEXT)");

            ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_referencias(bcoref TEXT, bcocod TEXT, tiporef TEXT);");
        }

        if (oldVersion <= 35) {
            ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_wcnf_conf (\n" +
                    "  cnfg_idconfig char(30) NOT NULL DEFAULT '',\n" +
                    "  cnfg_clase char(1) NOT NULL DEFAULT '' ,\n" +
                    "  cnfg_tipo char(1) NOT NULL ,\n" +
                    "  cnfg_valnum double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" +
                    "  cnfg_valsino double(1,0) NOT NULL DEFAULT '0' ,\n" +
                    "  cnfg_valtxt text ,\n" +
                    "  cnfg_lentxt double(3,0) NOT NULL DEFAULT '0' ,\n" +
                    "  cnfg_valfch date NOT NULL DEFAULT '0000-00-00' ,\n" +
                    "  cnfg_activa double(1,0) NOT NULL DEFAULT '0' ,\n" +
                    "  cnfg_etiq text ,\n" +
                    "  cnfg_ttip text ,\n" +
                    "  fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00' ,\n" +
                    "  username varchar(30) NOT NULL DEFAULT '')");

            ke_android.execSQL("CREATE TABLE IF NOT EXISTS ke_retimg (\n" +
                    "  cxcndoc char(16) NOT NULL DEFAULT '' ,\n" +
                    "  ruta varchar(100) NOT NULL DEFAULT '',\n" +
                    "  ret_nomimg varchar(30) NOT NULL DEFAULT '' ,\n" +
                    "  status char(1) NOT NULL DEFAULT '0'\n" +
                    ")");

            try {
                ke_android.execSQL("ALTER TABLE ke_precobradocs ADD reten varchar(1) NOT NULL DEFAULT '0';");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion <= 36) {
            try {
                ke_android.execSQL("INSERT INTO tabla_aux VALUES ('listbanc', '0001-01-01T01:01:01')");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                ke_android.execSQL("ALTER TABLE `cliempre` ADD `noemifac` int NOT NULL DEFAULT '0';");
                ke_android.execSQL("ALTER TABLE `cliempre` ADD `noeminota` int NOT NULL DEFAULT '0';");

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                ke_android.execSQL("ALTER TABLE `articulo` ADD `vta_solofac` int NOT NULL DEFAULT '0';");
                ke_android.execSQL("ALTER TABLE `articulo` ADD `vta_solone` int NOT NULL DEFAULT '0';");

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        if (oldVersion <= 37) {
            try {
                ke_android.execSQL("ALTER TABLE `usuarios` ADD sinc_primera NUMERIC NOT NULL DEFAULT 0;");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (oldVersion <= 38) {
            ke_android.execSQL("CREATE TABLE IF NOT EXISTS `img_carousel` (\n" +
                    "  `nombre` varchar(250) NOT NULL DEFAULT '',\n" +
                    "  `enlace` varchar(250) NOT NULL DEFAULT '' ,\n" +
                    "  `fechamodifi` datetime NOT NULL DEFAULT '0001-01-01 01:01:01')");
        }


// la nueva oldversion <= 34, pero arriba pon 35


    }

    private void CerarDB(SQLiteDatabase db) {
        //db.close();
    }

    public void UpReciboCobroStatus(String idRecibo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("edorec", 3);
        cv.put("fechamodifi", FechaHoy(true));

        try {
            db.beginTransaction();

            db.update("ke_precobranza", cv, "cxcndoc = ?", new String[]{idRecibo});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        CerarDB(db);
    }

    public String getConfigTipo(String config) {
        String campoAux = "";

        switch (config) {
            case "N":
                campoAux = "cnfg_valnum";
                break;

            case "C":
                campoAux = "cnfg_valtxt";
                break;

            case "0":
                campoAux = "cnfg_valsino";
                break;

            case "D":
                campoAux = "cnfg_valfch";
                break;
        }


        return campoAux;
    }

    public Double getConfigNum(String config) {
        SQLiteDatabase db = this.getWritableDatabase();
        double num;

        Cursor cursor = db.rawQuery("SELECT " + getConfigTipo("N") + " FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + config + "';", null);

        if (cursor.moveToFirst()) {
            num = cursor.getDouble(0);
        } else {
            num = 0.0;
        }

        cursor.close();
        CerarDB(db);

        return num;

    }

    public Boolean getConfigBool(String config) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean flag = false;

        Cursor cursor = db.rawQuery("SELECT " + getConfigTipo("0") + " FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + config + "';", null);
        //System.out.println("SELECT " + getConfigTipo("0") + " FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + config + "';");

        if (cursor.moveToFirst()) {
            flag = cursor.getInt(0) == 1;
        }

        cursor.close();
        CerarDB(db);

        return flag;
    }

    public Boolean getConfigBoolUsuario(String config, String user) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean flag = false;

        Cursor cursor = db.rawQuery("SELECT " + getConfigTipo("0") + " FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + config + "' AND username = '" + user + "' AND cnfg_activa = '1.0';", null);
        //System.out.println("SELECT " + getConfigTipo("0") + " FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + config + "';");

        if (cursor.moveToFirst()) {
            flag = cursor.getInt(0) == 1;
        }

        cursor.close();
        CerarDB(db);

        return flag;
    }

    public String getConfigString(String config) {
        SQLiteDatabase db = this.getWritableDatabase();
        String texto;

        Cursor cursor = db.rawQuery("SELECT " + getConfigTipo("C") + " FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + config + "' AND cnfg_activa = '1.0';", null);

        if (cursor.moveToFirst()) {
            texto = cursor.getString(0);
        } else {
            texto = "";
        }

        cursor.close();
        CerarDB(db);

        return texto;

    }

    private String FechaHoy(boolean wTime) {
        DateFormat dateFormat;
        if (wTime) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
        return dateFormat.format(Calendar.getInstance().getTime());

    }

    //2023-06-19 FUncion que me devuelve un true si el cliente tiene documentos vencidos
    public boolean getDeudaCliente(@NotNull String cliente) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean flag = false;

        Cursor cursor = db.rawQuery("SELECT COUNT(documento) FROM ke_doccti WHERE codcliente = '" + cliente + "' AND vence < '" + FechaHoy(false) + "';", null);
        //System.out.println("SELECT COUNT(documento) FROM ke_doccti WHERE codcliente = '" + cliente + "' AND vence < '"+ FechaHoy(false) +"';");
        if (cursor.moveToFirst()) {
            flag = cursor.getInt(0) > 0;
        }

        cursor.close();
        CerarDB(db);

        return flag;
    }

    //2023-06-19 Funcion que me devuelve la cantidad de documentos vencidos de un cliente
    public int getDeudaClienteNum(@NotNull String cliente) {
        SQLiteDatabase db = this.getWritableDatabase();
        int num = 0;

        Cursor cursor = db.rawQuery("SELECT COUNT(documento) FROM ke_doccti WHERE codcliente = '" + cliente + "' AND vence < '" + FechaHoy(false) + "';", null);
        //System.out.println("SELECT COUNT(documento) FROM ke_doccti WHERE codcliente = '" + cliente + "' AND vence < '"+ FechaHoy(false) +"';");
        if (cursor.moveToFirst()) {
            num = cursor.getInt(0);
        }

        cursor.close();
        CerarDB(db);

        return num;
    }

    public double getEfectivoDoc(@NotNull String correlativo) {
        SQLiteDatabase db = this.getWritableDatabase();
        double retorno = 0.0;

        Cursor cursor = db.rawQuery("SELECT efectivo FROM ke_precobranza WHERE cxcndoc = '" + correlativo + "';", null);

        if (cursor.moveToFirst()) {
            retorno = cursor.getDouble(0);
        }

        cursor.close();
        CerarDB(db);

        return retorno;
    }

    public double getDeudaClienteTotal(@NotNull String codCliente, @NotNull String monedaSigno, double tasa, double totalCobrado) {
        SQLiteDatabase db = this.getWritableDatabase();
        double numero = 0.0;

        Cursor cursor = db.rawQuery("SELECT SUM(dtotalfinal - dtotpagos) FROM ke_doccti WHERE codcliente = '" + codCliente + "';", null);

        if (cursor.moveToFirst()) {
            numero = cursor.getDouble(0);

            if (monedaSigno.equals("$")) {
                if (numero - totalCobrado < 0) {
                    return 0.0;
                } else {
                    return numero - totalCobrado;
                }
            } else {
                if (numero - (totalCobrado / tasa) < 0) {
                    return 0.0;
                } else {
                    return numero - (totalCobrado / tasa);
                }
            }

        }

        cursor.close();
        CerarDB(db);

        return numero;
    }

    public int getCampoInt(String tabla, String campo, String campoWhere, String respuestaWhere) {
        int retorno = 0;
        SQLiteDatabase db = this.getWritableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT " + campo + " FROM " + tabla + " WHERE " + campoWhere + " = '" + respuestaWhere + "';", null)) {
            if (cursor.moveToFirst()) {
                retorno = cursor.getInt(0);
            }
        }
        CerarDB(db);

        return retorno;
    }

    public void DeleteAll(String tabla) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(tabla, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CerarDB(db);

    }

    public boolean ValidarExistencia(String tabla, String campo, String campoWhere) {
        boolean retorno = false;
        SQLiteDatabase db = this.getWritableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT count(" + campo + ") FROM " + tabla + " WHERE " + campo + " = '" + campoWhere + "';", null)) {
            if (cursor.moveToFirst()) {
                retorno = cursor.getInt(0) > 0;
            }
        }
        CerarDB(db);

        return retorno;
    }

    public void InsertJSON(String table, ContentValues cv) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.insert(table, null, cv);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        CerarDB(db);
    }

    public void UpdateJSON(String table, ContentValues cv, String campo, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.update(table, cv, campo + " = ?", new String[]{id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        CerarDB(db);
    }

    //Actualizacion de tabla auxiliar
    public void UpdateTablaAux(String table) {

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues cv = new ContentValues();

            cv.put("tabla", table);
            cv.put("fchhn_ultmod", FechaHoy(true));

            db.beginTransaction();

            db.update("tabla_aux", cv, "tabla = ?", new String[]{table});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        CerarDB(db);
    }

    //Funcion que sirve para verificar si el vendedor sincronizo alguna vez en la vida de la app
    //Se eligio articulos para la funcion por ser una tabla que se llena sin importar la situacion
    public boolean SincronizoPriVez(String vendedor) {
        boolean retorno = false;
        SQLiteDatabase db = this.getWritableDatabase();

        try (Cursor cursor = db.rawQuery("select sinc_primera from usuarios WHERE vendedor = '"+vendedor+"';", null)) {
            if (cursor.moveToFirst()) {
                retorno = cursor.getInt(0) == 1;
            }
        }
        CerarDB(db);

        return retorno;
    }

    public List<CarouselItem> getImgCarousel() {
        List<CarouselItem> lista = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM img_carousel;", null)) {
            while (cursor.moveToNext()){

                String nombre = cursor.getString(0);
                String enlace = cursor.getString(1);

                lista.add(
                        new CarouselItem(
                                enlace
                        )
                );
            }
        }
        CerarDB(db);

        return lista;
    }
}
