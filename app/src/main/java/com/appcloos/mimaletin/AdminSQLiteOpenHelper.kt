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
package com.appcloos.mimaletin

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.appcloos.mimaletin.ObjetoUtils.Companion.valorReal
import com.appcloos.mimaletin.dialogChangeAccount.model.keDataconex
import com.appcloos.mimaletin.model.cliente.ClientesKt
import com.appcloos.mimaletin.model.pedidos.keOpmv
import com.appcloos.mimaletin.model.pedidos.keOpti
import com.appcloos.mimaletin.moduloCXC.viewmodel.EdoGeneralCxc
import com.appcloos.mimaletin.moduloCXC.viewmodel.PlanificadorCxc
import com.appcloos.mimaletin.sincronizar.dataClass.articulos.ArticulosResponse
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


//2023-07-10: Version 36
//2023-07-17: Version 37
class AdminSQLiteOpenHelper  //la version de la app debe cambiarse tras cada actualización siempre y cuando se hayan agregado tablas
//CREATE TABLE IF NOT EXISTS tabla ( id INTEGER PRIMARY KEY  AUTOINCREMENT,...);
    (val context: Context?, val name: String?, val factory: CursorFactory?) :
    SQLiteOpenHelper(context, name, factory, 55) {//<-- 46 para pruebas / 55

    //private lateinit var dataBase: SQLiteDatabase
    //aqui se define la estructura de la base de datos al instalar la app (no cambia, solo se le agrega)
    override fun onCreate(keAndroid: SQLiteDatabase) {
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS articulo (codigo TEXT PRIMARY KEY, subgrupo TEXT, grupo TEXT, nombre TEXT, referencia TEXT, marca TEXT, unidad TEXT, existencia REAL, precio1 REAL, precio2 REAL, precio3 REAL, precio4 REAL, precio5 REAL, precio6 REAL, precio7 REAL,  fechamodifi NUMERIC, discont REAL, vta_min REAL NOT NULL DEFAULT 0, vta_max REAL NOT NULL DEFAULT 0, dctotope REAL NOT NULL DEFAULT 0, enpreventa char(1) NOT NULL DEFAULT '0', comprometido REAL NOT NULL DEFAULT 0, vta_minenx INTEGER NOT NULL DEFAULT 0, vta_solofac int NOT NULL DEFAULT 0, vta_solone int NOT NULL DEFAULT 0)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS tabla_aux(tabla TEXT PRIMARY KEY, fchhn_ultmod NUMERIC)")
        keAndroid.execSQL(
            """INSERT INTO tabla_aux VALUES
  ('articulo',    '0001-01-01 01:01:01'),
  ('cliempre',    '0001-01-01 01:01:01'),
  ('listvend',    '0001-01-01 01:01:01'),
  ('config2',     '0001-01-01 01:01:01'),
  ('grupos',      '0001-01-01 01:01:01'),
  ('sectores',    '0001-01-01 01:01:01'),
  ('subgrupos',   '0001-01-01 01:01:01'),
  ('subsectores', '0001-01-01 01:01:01'),
  ('usuarios',    '0001-01-01 01:01:01'),
  ('limites',     '0001-01-01 01:01:01'),
  ('ke_wcnf_conf','0001-01-01 01:01:01'),
  ('listbanc',    '0001-01-01T01:01:01'),
  ('ke_tabdctos', '0001-01-01T01:01:01'),
  ('ke_tabdctosbcos', '0001-01-01T01:01:01'),
  ('ke_opti', '0001-01-01T01:01:01'),
  ('ke_doccti',   '0001-01-01 01:01:01')"""
        )
        keAndroid.execSQL(
            "CREATE TABLE IF NOT EXISTS cliempre(codigo TEXT, nombre TEXT, direccion TEXT, telefonos TEXT, perscont TEXT, vendedor TEXT, contribespecial REAL, status REAL, sector TEXT, subcodigo TEXT, fechamodifi NUMERIC, precio REAL, kne_activa TEXT DEFAULT '0', kne_mtomin REAL DEFAULT 0.000, `noemifac` int NOT NULL DEFAULT '0', `noeminota` int NOT NULL DEFAULT '0', fchultvta TEXT NOT NULL DEFAULT '0000-00-00', mtoultvta double(20,6) NOT NULL DEFAULT '0.000000', prcdpagdia double(20,6) NOT NULL DEFAULT '0.000000', promdiasp double(20,6) NOT NULL DEFAULT '0.000000', riesgocrd double(20,6) NOT NULL DEFAULT '0.000000', cantdocs double(20,6) NOT NULL DEFAULT '0.000000', totmtodocs double(20,6) NOT NULL DEFAULT '0.000000', prommtodoc double(20,6) NOT NULL DEFAULT '0.000000', diasultvta double(20,6) NOT NULL DEFAULT '0.000000', promdiasvta double(20,6) NOT NULL DEFAULT '0.000000', limcred double(20,6) NOT NULL DEFAULT '0.000000', fchcrea TEXT NOT NULL DEFAULT '0000-00-00', email TEXT NOT NULL DEFAULT '')"
        )
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS listvend(codigo TEXT, nombre TEXT, telefonos Text, telefono_movil TEXT, status REAL, superves REAL, supervpor TEXT, sector TEXT, subcodigo TEXT, nivgcial REAL, fechamodifi NUMERIC)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS sectores(codigo TEXT PRIMARY KEY,zona TEXT, fechamodifi NUMERIC)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS config2(id_precio1 TEXT ,id_precio2 TEXT, id_precio3 TEXT, id_precio4 TEXT, id_precio5 TEXT, id_precio6 TEXT, id_precio7 TEXT)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS subsectores(codigo TEXT, subcodigo TEXT , subsector TEXT, fechamodifi NUMERIC)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS subgrupos(codigo TEXT , subcodigo TEXT  , nombre TEXT , fechamodifi NUMERIC)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS grupos(codigo TEXT PRIMARY KEY, nombre TEXT, fechamodifi NUMERIC)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS usuarios(nombre TEXT, username TEXT, password TEXT, vendedor TEXT, almacen TEXT, desactivo REAL, fechamodifi NUMERIC, ualterprec REAL, sesionactiva NUMERIC, superves TEXT, ult_sinc NUMERIC DEFAULT '0001-01-01', sinc_primera NUMERIC NOT NULL DEFAULT 0/*,empresa TEXT NOT NULL DEFAULT '', PRIMARY KEY(empresa)*/);")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_opti(kti_ndoc TEXT, kti_tdoc TEXT, kti_codcli TEXT, kti_nombrecli TEXT, kti_codven TEXT, kti_docsol TEXT, kti_condicion TEXT, kti_tipprec REAL, kti_totneto REAL, kti_status TEXT, kti_nroped TEXT, kti_fchdoc NUMERIC, fechamodifi NUMERIC, kti_negesp TEXT, kti_totnetodcto REAL NOT NULL DEFAULT 0, ke_pedstatus TEXT NOT NULL DEFAULT '00')")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_opmv(kti_tdoc TEXT, kti_ndoc TEXT, kti_tipprec REAL, kmv_codart TEXT, kmv_nombre TEXT, kmv_cant REAL, kmv_artprec REAL, kmv_stot REAL, kmv_dctolin REAL NOT NULL DEFAULT 0, kmv_stotdcto REAL NOT NULL DEFAULT 0)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_carrito(kmv_codart TEXT, kmv_nombre TEXT, kmv_cant REAL,  kmv_artprec REAL, kmv_stot REAL, kmv_dctolin REAL NOT NULL DEFAULT 0, kmv_stotdcto REAL NOT NULL DEFAULT 0)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_correla(kco_numero INTEGER, kco_vendedor TEXT)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_kardex(kde_codart TEXT PRIMARY KEY, kde_cantidad REAL, ke_fecha NUMERIC)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_estadc01(codcoord VARCHAR(8), nomcoord VARCHAR(54), vendedor VARCHAR(8), nombrevend VARCHAR(54), cntpedidos REAL, mtopedidos REAL, cntfacturas REAL, mtofacturas REAL, metavend REAL, prcmeta REAL, cntclientes REAL, clivisit REAL, prcvisitas REAL, lom_montovtas REAL, lom_prcvtas REAL, lom_prcvisit REAL, rlom_montovtas REAL, rlom_prcvtas REAL, rlom_prcvisit REAL, fecha_estad NUMERIC, ppgdol_totneto REAL, devdol_totneto REAL, defdol_totneto REAL, totdolcob REAL)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_cxc(kcx_nrorecibo VARCHAR (17), kcx_codcli VARCHAR(20), kcx_codven TEXT, kcx_ncliente VARCHAR(100), kcx_monto NUMERIC, kcx_fechamodifi NUMERIC, kcx_status TEXT)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_correlacxc(kcc_numero INTEGER, kcc_vendedor TEXT)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_limitart(kli_track TEXT, kli_codven TEXT, kli_codcli TEXT, kli_codart TEXT, kli_cant INTEGER, kli_fechahizo NUMERIC, kli_fechavence NUMERIC, status TEXT)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_version(kve_version VARCHAR(8), kve_activa CHAR(1))")

        //tabla de las cabeceras de los documentos
        keAndroid.execSQL(
            "CREATE TABLE IF NOT EXISTS ke_doccti(" + "agencia VARCHAR(3) NOT NULL DEFAULT '', " + "tipodoc varchar(3) NOT NULL DEFAULT '', " + "documento varchar(8) NOT NULL DEFAULT '', " + "tipodocv varchar(3) NOT NULL DEFAULT ''," + "codcliente varchar(20) NOT NULL DEFAULT ''," + "nombrecli varchar(100) NOT NULL DEFAULT ''," + "contribesp double(2,0) NOT NULL DEFAULT '0'," + "ruta_parme char(1) NOT NULL DEFAULT '0'," + "tipoprecio double(2,0) NOT NULL DEFAULT '1'," + "emision date NOT NULL DEFAULT '0000-00-00'," + "recepcion date NOT NULL DEFAULT '0000-00-00'," + "vence date NOT NULL  DEFAULT '0000-00-00'," + "diascred double(2,0) NOT NULl DEFAULT '0'," + "estatusdoc varchar(1) NOT NULL  DEFAULT ''," + "dtotneto double(24,7) NOT NULL DEFAULT '0'," + "dtotimpuest double(24,7) NOT NULL DEFAULT '0'," + "dtotalfinal double(24,7) NOT NULL DEFAULT '0'," + "dtotpagos double(24,7) NOT NULL DEFAULT '0'," + "dtotdescuen double(24,7) NOT NULL DEFAULT '0'," + "dFlete double(24,7) NOT NULL DEFAULT '0'," + "dtotdev double(24,7) NOT NULL DEFAULT '0'," + "dvndmtototal double(24,7) NOT NULL DEFAULT '0'," + "dretencion double(24,7) NOT NULL DEFAULT '0'," + "dretencioniva double(24,7) NOT NULL DEFAULT '0'," + "vendedor varchar(8) NOT NULL  DEFAULT ''," + "codcoord varchar(8) NOT NULL  DEFAULT ''," + "fechamodifi datetime NOT NULL  DEFAULT '0000-00-00 00:00:00'," + "aceptadev char(1) NOT NULL  DEFAULT ''," + "kti_negesp char(1)NOT NULL DEFAULT '0'," + "bsiva double(24,7) NOT NULL DEFAULT '0'," + "bsflete double(24,7) NOT NULL DEFAULT '0'," + "bsretencioniva double(24,7) NOT NULL DEFAULT '0'," + "bsretencion double(24,7) NOT NULL DEFAULT '0'," + "tasadoc double(24,7) NOT NULL DEFAULT '0.00'," + "mtodcto double(24,7) NOT NULL DEFAULT '0.00'," + "fchvencedcto date NOT NULL DEFAULT '0000-00-00'," + "tienedcto char(1) NOT NULL DEFAULT '0'," + "cbsret double(24,7) NOT NULL DEFAULT '0.00'," + "cdret double(24,7) NOT NULL DEFAULT '0.00'," + "cbsretiva double(24,7) NOT NULL DEFAULT '0.00'," + "cdretiva double(24,7) NOT NULL DEFAULT '0.00'," + "cbsrparme double(24,7) NOT NULL DEFAULT '0.00'," + "cdrparme double(24,7) NOT NULL DEFAULT '0.00'," + "cbsretflete double(24,7) NOT NULL DEFAULT '0.00'," + "cdretflete double(24,7) NOT NULL DEFAULT '0.00'," + "bsmtoiva double(24,7) NOT NULL DEFAULT '0.00'," + "bsmtofte double(24,7) NOT NULL DEFAULT '0.00'," + "retmun_mto double(24,7) NOT NULL DEFAULT '0.00')"
        )

        //tabla de lineas
        keAndroid.execSQL(
            "CREATE TABLE IF NOT EXISTS ke_doclmv ( " + "agencia varchar(3) NOT NULL DEFAULT '' ," + "tipodoc varchar(3) NOT NULL DEFAULT '' , " + "documento varchar(8) NOT NULL DEFAULT '', " + "tipodocv varchar(3) NOT NULL DEFAULT ''," + "grupo varchar(6) NOT NULL DEFAULT ''," + "subgrupo varchar(6) NOT NULL DEFAULT ''," + "origen double(2,0) NOT NULL DEFAULT '0'," + "codigo varchar(25) NOT NULL DEFAULT ''," + "codhijo varchar(25) NOT NULL DEFAULT ''," + "pid varchar(12) NOT NULL DEFAULT ''," + "nombre varchar(100) NOT NULL DEFAULT ' '," + "cantidad double(24,7) NOT NULL DEFAULT '0.0000000', " + "cntdevuelt double(24,7) NOT NULL DEFAULT '0.0000000', " + "vndcntdevuelt double(24,7) NOT NULL DEFAULT '0.0000000'," + "dvndmtototal double(24,7) NOT NULL DEFAULT '0.0000000', " + "dpreciofin double(24,7) NOT NULL DEFAULT '0.0000000'," + "dpreciounit double(24,7) NOT NULL DEFAULT '0.0000000'," + "dmontoneto double(24,7) NOT NULL DEFAULT '0.0000000'," + "dmontototal double(24,7) NOT NULL DEFAULT '0.0000000'," + "timpueprc double(24,7) NOT NULL DEFAULT '0.0000000'," + "unidevuelt double(24,7) NOT NULL DEFAULT '0.0000000'," + "fechadoc date NOT NULL DEFAULT '0000-00-00', " + "vendedor varchar(8) NOT NULL DEFAULT ''," + "codcoord varchar(8) NOT NULL DEFAULT ''," + "fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00')"
        )

        //tabla de lineas temporal de reclamos
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_devlmtmp (" + "kdel_referencia TEXT NOT NULL DEFAULT ''," + "kdel_documento  varchar(8) NOT NULL DEFAULT ''," + "kdel_pid varchar(12) NOT NULL DEFAULT ''," + "kdel_codart varchar(25) NOT NULL DEFAULT ''," + "kdel_mtolinea double(2,0 ) NOT NULL DEFAULT '0'," + "kdel_preciofin double (2,0) NOT NULL DEFAULT '0', " + "kdel_cantdev double(2,0 ) NOT NULL DEFAULT '0'," + "kdel_cantped double(2,0 ) NOT NULL DEFAULT '0'," + "kdel_nombre VARCHAR(100) NOT NULL DEFAULT '')")

        //tabla de correlativos de devolución
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_correladev(kdev_numero INTEGER, kdev_vendedor TEXT)")

        //tablas definitivas de devolucion
        //cabecera
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_rclcti (" + "krti_ndoc TEXT," + "krti_status char(2)," + "krti_agefac varchar(3)," + "krti_tipfac varchar(3)," + "krti_docfac varchar(8)," + "krti_tipprec double(2,0)," + "krti_totneto double (2,0)," + "krti_totnetodef double(2,0)," + "krti_fchdoc datetime," + "kdv_codclasif char(2)," + "krti_substatus char(2)," + "krti_agenc varchar(3)," + "krti_tipnc varchar(3)," + "krti_docnc varchar(8)," + "krti_agedev varchar(3)," + "krti_tipdev varchar(3)," + "krti_docdev varchar(8)," + "krti_notas longtext(240)," + "krti_codvend varchar(3)," + "krti_codcoor varchar(3)," + "krti_codcli varchar(10)," + "krti_nombrecli varchar(100)," + "fechamodifi datetime," + "kdv_codclasidef char(2))" + "")

        //lineas
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_rcllmv (" + "krti_ndoc TEXT," + "krmv_tipprec double(2,0)," + "krmv_codart TEXT," + "krmv_nombre TEXT," + "krmv_cant double(2,0)," + "krmv_artprec double(2,0)," + "krmv_stot double(2,0)," + "krmv_cantdef double(2,0)," + "krmv_stotdef double(2,0)," + "krmv_pid TEXT," + "fechamodifi datetime" + ")")
        keAndroid.execSQL("INSERT INTO tabla_aux VALUES ('ke_rclcti',     '0001-01-01 01:01:01')")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_imgrcl (krti_ndoc TEXT, kircl_rutafoto TEXT)") //tabla sin uso actual


        //tabla para las clasificaciones de los reclamos
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_tiporecl(kdv_codclasif CHAR, kdv_nomclaweb TEXT, kdv_nomclasif TEXT, kdv_hlpclasif LONGTEXT, fechamodifi NUMERIC)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_enlace(kee_codigo TEXT, kee_nombre TEXT, kee_url TEXT, kee_status TEXT, kee_sucursal TEXT)")
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_modulos(ked_codigo TEXT, kmo_codigo TEXT, kmo_status TEXT, kee_sucursal TEXT)")


        //tabla cabecera de cobranzas
        keAndroid.execSQL(
            "CREATE TABLE IF NOT EXISTS ke_precobranza(  " + "cxcndoc varchar(16) NOT NULL DEFAULT ''," + " tiporecibo char(1) NOT NULL DEFAULT 'W'," + " codvend varchar(8) NOT NULL," + " nro_recibo char(8) NOT NULL DEFAULT '',\n" + " kecxc_id char(12) NOT NULL DEFAULT '' ,\n" + " tasadia double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " fchrecibo date NOT NULL DEFAULT '0000-00-00' ,\n" + " clicontesp char(1) NOT NULL DEFAULT '',\n" + " bsneto double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " bsiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " bsretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " bsflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " bstotal double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " dolneto double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " doliva double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" + " dolretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " dolflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " doltotal double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " moneda char(1) NOT NULL DEFAULT '0',\n" + " docdifcamb char(1) NOT NULL DEFAULT '0',\n" + " ddc_age varchar(3) NOT NULL DEFAULT '',\n" + " ddc_tipo varchar(3) NOT NULL DEFAULT '',\n" + " ddc_montobs double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " ddc_doc varchar(8) NOT NULL DEFAULT '',\n" + " dctoaplic double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " netocob double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " concepto varchar(200) NOT NULL DEFAULT '',\n" + " efectivo double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" + " bcoecod varchar(3) NOT NULL DEFAULT '',\n" + " bcocod varchar(3) NOT NULL DEFAULT '' ,\n" + " bconombre varchar(59) NOT NULL DEFAULT '',\n" + " fchr_dep datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" + " bcomonto double(20,7) NOT NULL DEFAULT '0.0000000',\n" + " bcoref varchar(20) NOT NULL DEFAULT '',\n" + " pidvalid varchar(12) NOT NULL DEFAULT '',\n" + " edorec char(1) NOT NULL DEFAULT '0',\n" + " edocomiv char(1) NOT NULL DEFAULT '0',\n" + " prccomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " mtocomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " fchr_pcomv datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" + " codcoord varchar(8) NOT NULL DEFAULT '',\n" + " edocomic char(1) NOT NULL DEFAULT '0',\n" + " prccomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " mtocomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " fchr_pcomc datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" + " fchhr datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" + " fchvigen date NOT NULL DEFAULT '0000-00-00',\n" + " bsretflete double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " diasvigen double(5,0) NOT NULL DEFAULT '0',\n" + " retmun_sbi double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " retmun_sbs double(24,7) NOT NULL DEFAULT '0.0000000',\n" + " comiaut char(1) NOT NULL DEFAULT '0',\n" + " comiautpor varchar(30) NOT NULL DEFAULT ' ',\n" + " comiautfch datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" + " reci_age varchar(3) NOT NULL DEFAULT ' ',\n" + " reci_doc char(8) NOT NULL DEFAULT '',\n" + " status char(2) NOT NULL DEFAULT '0',\n" + " fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" + " cxcndoc_aux varchar(16) NOT NULL DEFAULT '', tipo_pago NUMERIC NOT NULL DEFAULT 0, complemento TEXT NOT NULL DEFAULT '', \n" + "  PRIMARY KEY (cxcndoc))"
        )


        //tabla de detalles de cobranza
//TABLA DE LINEAS DE CXC
        keAndroid.execSQL(
            "CREATE TABLE IF NOT EXISTS ke_precobradocs(" + "cxcndoc varchar(16) NOT NULL DEFAULT ''," + "  agencia varchar(3) NOT NULL DEFAULT '' ,\n" + "  tipodoc varchar(3) NOT NULL DEFAULT '',\n" + "  documento varchar(8) NOT NULL DEFAULT '',\n" + "  bscobro double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  prccobro double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  prcdsctopp double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  nroret varchar(8) NOT NULL DEFAULT '',\n" + "  fchemiret date NOT NULL DEFAULT '0000-00-00' ,\n" + "  bsretiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  refret varchar(20) NOT NULL DEFAULT '',\n" + "  nroretfte varchar(8) NOT NULL DEFAULT '',\n" + "  fchemirfte date NOT NULL DEFAULT '0000-00-00',\n" + "  bsmtofte double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  bsretfte double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  refretfte varchar(20) NOT NULL DEFAULT '',\n" + "  pidvalid varchar(12) NOT NULL DEFAULT '',\n" + "  bsmtoiva double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  retmun_bi double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  retmun_cod varchar(5) NOT NULL DEFAULT ' ',\n" + "  retmun_nro varchar(8) NOT NULL DEFAULT ' ',\n" + "  retmun_mto double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  retmun_fch date NOT NULL DEFAULT '0000-00-00',\n" + "  retmun_ref varchar(20) NOT NULL DEFAULT ' ',\n" + "  diascalc double(4,0) NOT NULL DEFAULT '0',\n" + "  prccomiv double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  prccomic double(24,7) NOT NULL DEFAULT '0.0000000',\n" + "  cxcndoc_aux varchar(16) NOT NULL DEFAULT '', \n" + "  tnetodbs double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" + "  tnetoddol double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" + "  fchrecibod date NOT NULL DEFAULT '0000-00-00' ,\n" + "  kecxc_idd char(12) NOT NULL DEFAULT '' ,\n" + "  tasadiad double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" + "  afavor double(24,7) NOT NULL DEFAULT '0.0000000' ,\n" + "  reten varchar(1) NOT NULL DEFAULT '0', codcliente TEXT NOT NULL DEFAULT '', nombrecli TEXT NOT NULL DEFAULT '', tasadoc double(24,7) NOT NULL DEFAULT 0.0, cbsretiva double(24,7) NOT NULL DEFAULT 0.0, cbsretflete double(24,7) NOT NULL DEFAULT 0.0)"
        )

        // creacion de tabla para las tasas de cambio en bss
        keAndroid.execSQL(
            """CREATE TABLE IF NOT EXISTS kecxc_tasas (
  `kecxc_id` varchar(12) NOT NULL DEFAULT '',
  `kecxc_fecha` date NOT NULL DEFAULT '0000-00-00',
  `kecxc_tasa` double(24,7) NOT NULL DEFAULT '0.0000000',
  `kecxc_usuario` varchar(30) NOT NULL DEFAULT '',
  `kecxc_ip` varchar(20) NOT NULL DEFAULT '',
  `kecxc_fchyhora` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `fechamodifi` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
   kecxc_tasaib double(24,7) NOT NULL DEFAULT '0.0000000')"""
        )
        keAndroid.execSQL(
            """CREATE TABLE IF NOT EXISTS `listbanc` (
  codbanco     varchar(3)  NOT NULL DEFAULT '' UNIQUE,
  nombanco      varchar(59) not null DEFAULT '',
  cuentanac     double(2,0) NOT NULL DEFAULT '0',
  inactiva      double(1,0) NOT NULL DEFAULT '0',
  fechamodifi   datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (codbanco))"""
        )
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_corprec(kcor_numero INTEGER, kcor_vendedor TEXT)")
        keAndroid.execSQL(
            """CREATE TABLE IF NOT EXISTS ke_precobdcto (
  agencia varchar(3) NOT NULL DEFAULT '' ,
  tipodoc varchar(3) NOT NULL DEFAULT '' ,
  documento varchar(8) NOT NULL DEFAULT '' ,
  codcliente varchar(20) NOT NULL DEFAULT '' ,
  edodcto char(1) NOT NULL DEFAULT '0',
  prcdctoaplic double(24,7) NOT NULL DEFAULT '0.0000000',
  montodctodol double(24,7) NOT NULL DEFAULT '0.0000000',
  montodctobs double(24,7) NOT NULL DEFAULT '0.0000000',
  tasadoc double(24,7) NOT NULL DEFAULT '0.0000000',
  fchvigen date NOT NULL DEFAULT '0000-00-00',
  docgen_age varchar(3) NOT NULL DEFAULT '',
  docgen_tipo varchar(3) NOT NULL DEFAULT '',
  docgen_nro varchar(8) NOT NULL DEFAULT '' ,
  recgen_age varchar(3) NOT NULL DEFAULT '' ,
  recgen_tipo varchar(3) NOT NULL DEFAULT '' ,
  recgen_nro varchar(8) NOT NULL DEFAULT '' ,
  fechamodifi datetime NOT NULL DEFAULT '1000-01-01 00:00:00')"""
        )
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_mtopendcli(" + " codcliente TEXT NOT NULL DEFAULT '', " + " id_recibo TEXT NOT NULL DEFAULT '', " + " moneda TEXT NOT NULL DEFAULT '1', " + " montocli REAL NOT NULL DEFAULT '0.0000000', " + " estado TEXT NOT NULL DEFAULT '0', " + " id_reciboap TEXT NOT NULL DEFAULT '', " + " agencia TEXT NOT NULL DEFAULT '', " + " tipodoc TEXT NOT NULL DEFAULT '', " + " documento TEXT NOT NULL DEFAULT '', " + " fchregdif TEXT NOT NULL DEFAULT '0000-00-00 00:00:00', " + " usuario TEXT NOT NULL DEFAULT '', " + " edoweb TEXT NOT NULL DEFAULT '0', " + " fechamodifi TEXT NOT NULL DEFAULT '0000-00-00 00:00:00' " + ")")
        keAndroid.execSQL(
            """CREATE TABLE IF NOT EXISTS `ke_retimg` (
  `cxcndoc` char(16) NOT NULL DEFAULT '' ,
  `ruta` varchar(100) NOT NULL DEFAULT '',
  `ret_nomimg` varchar(30) NOT NULL DEFAULT '' ,
  `status` char(1) NOT NULL DEFAULT '0'
)"""
        )
        keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_referencias(bcoref TEXT, bcocod TEXT, tiporef TEXT);")

        //2023-06-15 Tabla de configuración
        keAndroid.execSQL(
            """CREATE TABLE IF NOT EXISTS `ke_wcnf_conf` (
  `cnfg_idconfig` char(30) NOT NULL DEFAULT '',
  `cnfg_clase` char(1) NOT NULL DEFAULT '' ,
  `cnfg_tipo` char(1) NOT NULL ,
  `cnfg_valnum` double(24,7) NOT NULL DEFAULT '0.0000000' ,
  `cnfg_valsino` double(1,0) NOT NULL DEFAULT '0' ,
  `cnfg_valtxt` text ,
  `cnfg_lentxt` double(3,0) NOT NULL DEFAULT '0' ,
  `cnfg_valfch` date NOT NULL DEFAULT '0000-00-00' ,
  `cnfg_activa` double(1,0) NOT NULL DEFAULT '0' ,
  `cnfg_etiq` text ,
  `cnfg_ttip` text ,
  `fechamodifi` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' ,
  `username` varchar(30) NOT NULL DEFAULT '')"""
        )
        keAndroid.execSQL(
            """CREATE TABLE IF NOT EXISTS `img_carousel` (
  `nombre` varchar(250) NOT NULL DEFAULT '',
  `enlace` varchar(250) NOT NULL DEFAULT '' ,
  `fechamodifi` datetime NOT NULL DEFAULT '0001-01-01 01:01:01', ancho NUMERIC NOT NULL DEFAULT 0, alto NUMERIC NOT NULL DEFAULT 0)"""
        )
        keAndroid.execSQL(
            """CREATE TABLE IF NOT EXISTS `ke_tabdctos` (
  `dcob_id` varchar(12) NOT NULL DEFAULT '',
  `dcob_prc` double(24,7) NOT NULL DEFAULT '0.0000000',
  `dcob_activo` char(1) NOT NULL DEFAULT '1',
  `dcob_maxdvenc` double(20,7) NOT NULL DEFAULT '0.0000000',
  `dcob_valefac` char(1) NOT NULL DEFAULT '1',
  `dcob_valene` char(1) NOT NULL DEFAULT '1',
  `dcob_fchini` date NOT NULL DEFAULT '2000-01-01',
  `dcob_fchfin` date NOT NULL DEFAULT '2100-12-31',
  `fechamodifi` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
  `dcob_valesiempre` char(1) NOT NULL DEFAULT '0',
  `dcob_valemon` char(1) NOT NULL DEFAULT '0'
);"""
        )
        keAndroid.execSQL(
            """CREATE TABLE IF NOT EXISTS `ke_tabdctosbcos` (
  `dcob_id` varchar(12) NOT NULL DEFAULT '',
  `bco_codigo` varchar(3) NOT NULL DEFAULT '',
  `fechamodifi` datetime NOT NULL DEFAULT '1000-01-01 00:00:00'
);"""
        )
    }

    //aqui van las instrucciones de la BdD tras cada nueva actualización (cambiar siempre que se cree una nueva versión)
    override fun onUpgrade(keAndroid: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
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



        / *ke_android.execSQL("CREATE TABLE ke_precobradocs(" +
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
      / *  ke_android.execSQL("CREATE TABLE kecxc_tasas (\n" +
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
                "  fechamodifi   datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +*//* "  PRIMARY KEY (codbanco))");*/

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
                keAndroid.execSQL("ALTER TABLE ke_precobradocs ADD afavor double(24,7) NOT NULL DEFAULT '0.0000000';")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (oldVersion < 30) {
            //ke_android.execSQL("CREATE TABLE ke_imgret (cxcndoc TEXT, rutafoto TEXT, nombre TEXT)");
            keAndroid.execSQL("CREATE TABLE IF NOT EXISTS ke_referencias(bcoref TEXT, bcocod TEXT, tiporef TEXT);")
        }
        if (oldVersion <= 35) {
            keAndroid.execSQL(
                """CREATE TABLE IF NOT EXISTS ke_wcnf_conf (
  cnfg_idconfig char(30) NOT NULL DEFAULT '',
  cnfg_clase char(1) NOT NULL DEFAULT '' ,
  cnfg_tipo char(1) NOT NULL ,
  cnfg_valnum double(24,7) NOT NULL DEFAULT '0.0000000' ,
  cnfg_valsino double(1,0) NOT NULL DEFAULT '0' ,
  cnfg_valtxt text ,
  cnfg_lentxt double(3,0) NOT NULL DEFAULT '0' ,
  cnfg_valfch date NOT NULL DEFAULT '0000-00-00' ,
  cnfg_activa double(1,0) NOT NULL DEFAULT '0' ,
  cnfg_etiq text ,
  cnfg_ttip text ,
  fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00' ,
  username varchar(30) NOT NULL DEFAULT '')"""
            )
            keAndroid.execSQL(
                """CREATE TABLE IF NOT EXISTS ke_retimg (
  cxcndoc char(16) NOT NULL DEFAULT '' ,
  ruta varchar(100) NOT NULL DEFAULT '',
  ret_nomimg varchar(30) NOT NULL DEFAULT '' ,
  status char(1) NOT NULL DEFAULT '0'
)"""
            )
            try {
                keAndroid.execSQL("ALTER TABLE ke_precobradocs ADD reten varchar(1) NOT NULL DEFAULT '0';")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (oldVersion <= 36) {
            try {
                keAndroid.execSQL("INSERT INTO tabla_aux VALUES ('listbanc', '0001-01-01T01:01:01')")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD `noemifac` int NOT NULL DEFAULT '0';")
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD `noeminota` int NOT NULL DEFAULT '0';")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `articulo` ADD `vta_solofac` int NOT NULL DEFAULT '0';")
                keAndroid.execSQL("ALTER TABLE `articulo` ADD `vta_solone` int NOT NULL DEFAULT '0';")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (oldVersion <= 37) {
            try {
                keAndroid.execSQL("ALTER TABLE `usuarios` ADD sinc_primera NUMERIC NOT NULL DEFAULT 0;")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (oldVersion <= 38) {
            keAndroid.execSQL(
                """CREATE TABLE IF NOT EXISTS `img_carousel` (
  `nombre` varchar(250) NOT NULL DEFAULT '',
  `enlace` varchar(250) NOT NULL DEFAULT '' ,
  `fechamodifi` datetime NOT NULL DEFAULT '0001-01-01 01:01:01')"""
            )
            keAndroid.execSQL(
                """CREATE TABLE IF NOT EXISTS `ke_tabdctos` (
  `dcob_id` varchar(12) NOT NULL DEFAULT '',
  `dcob_prc` double(24,7) NOT NULL DEFAULT '0.0000000',
  `dcob_activo` char(1) NOT NULL DEFAULT '1',
  `dcob_maxdvenc` double(20,7) NOT NULL DEFAULT '0.0000000',
  `dcob_valefac` char(1) NOT NULL DEFAULT '1',
  `dcob_valene` char(1) NOT NULL DEFAULT '1',
  `dcob_fchini` date NOT NULL DEFAULT '2000-01-01',
  `dcob_fchfin` date NOT NULL DEFAULT '2100-12-31',
  `fechamodifi` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
  `dcob_valesiempre` char(1) NOT NULL DEFAULT '0',
  `dcob_valemon` char(1) NOT NULL DEFAULT '0'
);"""
            )
            keAndroid.execSQL(
                """CREATE TABLE IF NOT EXISTS `ke_tabdctosbcos` (
  `dcob_id` varchar(12) NOT NULL DEFAULT '',
  `bco_codigo` varchar(3) NOT NULL DEFAULT '',
  `fechamodifi` datetime NOT NULL DEFAULT '1000-01-01 00:00:00'
);"""
            )
            keAndroid.execSQL("INSERT INTO tabla_aux VALUES ('ke_tabdctos', '0001-01-01 01:01:01')")
            keAndroid.execSQL("INSERT INTO tabla_aux VALUES ('ke_tabdctosbcos', '0001-01-01 01:01:01')")
        }
        if (oldVersion <= 39) {
            //tipo_pago 0 = Completo; 1 = Abono;
            try {
                keAndroid.execSQL("ALTER TABLE `ke_precobranza` ADD tipo_pago NUMERIC NOT NULL DEFAULT 0;")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            //Anotacion para su recibo complementario
            try {
                keAndroid.execSQL("ALTER TABLE `ke_precobranza` ADD complemento TEXT NOT NULL DEFAULT '';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        if (oldVersion <= 41) {
            //tipo_pago 0 = Completo; 1 = Abono;
            try {
                keAndroid.execSQL("ALTER TABLE `img_carousel` ADD ancho NUMERIC NOT NULL DEFAULT 0;")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            //Anotacion para su recibo complementario
            try {
                keAndroid.execSQL("ALTER TABLE `img_carousel` ADD alto NUMERIC NOT NULL DEFAULT 0;")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        if (oldVersion <= 42) {
            //AGREGANDO CODIGO DEL CLIENTE A LA COBRANZA
            try {
                keAndroid.execSQL("ALTER TABLE `ke_precobradocs` ADD codcliente TEXT NOT NULL DEFAULT '';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            //AGREGANDO NOMBRE DEL CLIENTE A LA COBRANZA
            try {
                keAndroid.execSQL("ALTER TABLE `ke_precobradocs` ADD nombrecli TEXT NOT NULL DEFAULT '';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            //AGREGANDO TASA DEL CLIENTE A LA COBRANZA
            try {
                keAndroid.execSQL("ALTER TABLE `ke_precobradocs` ADD tasadoc double(24,7) NOT NULL DEFAULT 0.0;")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        if (oldVersion <= 43) {
            //NO LOS QUIERO PONER EN KE_PRECOBRADOCS, DEBERIA TENER UNA TABLA DE AUXILIO
            // PERO POR FALTA DE TIEMPO VA AQUI
            //2023-10-19 AGREGANDO LA RETENCION DE IVA QUE DEBIO EL CLIENTE
            try {
                keAndroid.execSQL("ALTER TABLE `ke_precobradocs` ADD cbsretiva double(24,7) NOT NULL DEFAULT 0.0;")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            //2023-10-19 AGREGANDO LA RETENCION DE FLETE QUE DEBIO EL CLIENTE
            try {
                keAndroid.execSQL("ALTER TABLE `ke_precobradocs` ADD cbsretflete double(24,7) NOT NULL DEFAULT 0.0;")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        if (oldVersion <= 44) {
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD fchultvta TEXT NOT NULL DEFAULT '0000-00-00';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD mtoultvta double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD prcdpagdia double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD promdiasp double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD riesgocrd double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD cantdocs double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD totmtodocs double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD prommtodoc double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD diasultvta double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD promdiasvta double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD limcred double(20,6) NOT NULL DEFAULT '0.000000';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD fchcrea TEXT NOT NULL DEFAULT '0000-00-00';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        if (oldVersion <= 45) {
            try {
                keAndroid.execSQL("ALTER TABLE `cliempre` ADD email TEXT NOT NULL DEFAULT '';")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        if (oldVersion < 47) {
            try {
                keAndroid.beginTransaction()
                keAndroid.execSQL("ALTER TABLE usuarios ADD empresa TEXT NOT NULL DEFAULT '${Constantes.CLO}';")
                keAndroid.execSQL("ALTER TABLE usuarios RENAME TO old_usuarios;")
                keAndroid.execSQL(
                    "CREATE TABLE IF NOT EXISTS usuarios(" +
                            "nombre TEXT DEFAULT ''," +
                            "username TEXT DEFAULT ''," +
                            "password TEXT DEFAULT ''," +
                            "vendedor TEXT DEFAULT ''," +
                            "almacen TEXT DEFAULT ''," +
                            "desactivo REAL DEFAULT ''," +
                            "fechamodifi NUMERIC DEFAULT ''," +
                            "ualterprec REAL DEFAULT ''," +
                            "sesionactiva NUMERIC DEFAULT 0," +
                            "superves TEXT DEFAULT ''," +
                            "ult_sinc NUMERIC DEFAULT '0001-01-01'," +
                            "sinc_primera NUMERIC NOT NULL DEFAULT 0," +
                            "empresa TEXT NOT NULL DEFAULT '081196'," +
                            "PRIMARY KEY(empresa));"
                )
                keAndroid.execSQL(
                    "INSERT INTO usuarios \n" +
                            "SELECT * FROM old_usuarios;"
                )
                keAndroid.execSQL("DROP TABLE old_usuarios;")
                keAndroid.setTransactionSuccessful()

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                keAndroid.endTransaction()
            }
        }

        if (oldVersion < 48) {
            try {
                keAndroid.beginTransaction()

                alterarTablas(keAndroid, "listvend")
                alterarTablas(keAndroid, "articulo")
                alterarTablas(keAndroid, "tabla_aux")
                alterarTablas(keAndroid, "cliempre")
                alterarTablas(keAndroid, "sectores")
                alterarTablas(keAndroid, "config2")
                alterarTablas(keAndroid, "subsectores")
                alterarTablas(keAndroid, "subgrupos")
                alterarTablas(keAndroid, "grupos")
                alterarTablas(keAndroid, "ke_opti")
                alterarTablas(keAndroid, "ke_opmv")
                alterarTablas(
                    keAndroid,
                    "ke_carrito"
                )// <-- No deberia ser importante, es una tabla aux interna del tlf
                alterarTablas(keAndroid, "ke_correla")
                alterarTablas(keAndroid, "ke_kardex")
                alterarTablas(keAndroid, "ke_estadc01")
                alterarTablas(keAndroid, "ke_cxc")// <-- Creo que es el modulo viejo CXC
                alterarTablas(keAndroid, "ke_correlacxc")
                alterarTablas(keAndroid, "ke_limitart")
                alterarTablas(keAndroid, "ke_version")
                alterarTablas(keAndroid, "ke_doccti")
                alterarTablas(keAndroid, "ke_doclmv")
                alterarTablas(keAndroid, "ke_devlmtmp")
                alterarTablas(keAndroid, "ke_correladev")
                alterarTablas(keAndroid, "ke_rclcti")
                alterarTablas(keAndroid, "ke_rcllmv")
                alterarTablas(keAndroid, "ke_imgrcl")
                alterarTablas(keAndroid, "ke_tiporecl")
                alterarTablas(keAndroid, "ke_modulos")
                alterarTablas(keAndroid, "ke_precobranza")
                alterarTablas(keAndroid, "ke_precobradocs")
                alterarTablas(keAndroid, "kecxc_tasas")
                alterarTablas(keAndroid, "listbanc")
                alterarTablas(keAndroid, "ke_precobdcto")
                alterarTablas(keAndroid, "ke_mtopendcli")
                alterarTablas(keAndroid, "ke_referencias")
                alterarTablas(keAndroid, "ke_wcnf_conf")
                alterarTablas(keAndroid, "img_carousel")
                alterarTablas(keAndroid, "ke_tabdctos")
                alterarTablas(keAndroid, "ke_tabdctosbcos")

                keAndroid.setTransactionSuccessful()

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                keAndroid.endTransaction()
            }

        }

        if (oldVersion < 49) {
            try {
                keAndroid.execSQL("INSERT INTO tabla_aux VALUES ('ke_opti', '0001-01-01 01:01:01', '081196')")
            } catch (e: SQLException) {
                e.printStackTrace()
            }

        }

        if (oldVersion < 50) {
            try {
                keAndroid.beginTransaction()
                //keAndroid.execSQL("ALTER TABLE tabla_aux ADD empresa TEXT NOT NULL DEFAULT '081196';")
                keAndroid.execSQL("ALTER TABLE tabla_aux RENAME TO old_tabla_aux;")
                keAndroid.execSQL(
                    "CREATE TABLE IF NOT EXISTS tabla_aux(" +
                            "tabla TEXT NOT NULL DEFAULT ''," +
                            "fchhn_ultmod TEXT NOT NULL DEFAULT ''," +
                            "empresa TEXT NOT NULL DEFAULT '${Constantes.CLO}'," +
                            "PRIMARY KEY(tabla, empresa));"
                )
                keAndroid.execSQL(
                    "INSERT INTO tabla_aux \n" +
                            "SELECT * FROM old_tabla_aux;"
                )
                keAndroid.execSQL("DROP TABLE old_tabla_aux;")
                keAndroid.setTransactionSuccessful()

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                keAndroid.endTransaction()
            }
        }

        if (oldVersion < 51) {
            try {
                keAndroid.beginTransaction()
                //keAndroid.execSQL("ALTER TABLE tabla_aux ADD empresa TEXT NOT NULL DEFAULT '081196';")
                keAndroid.execSQL("ALTER TABLE grupos RENAME TO old_grupos;")
                keAndroid.execSQL(
                    "CREATE TABLE IF NOT EXISTS grupos(" +
                            "codigo TEXT NOT NULL DEFAULT ''," +
                            "nombre TEXT NOT NULL DEFAULT ''," +
                            "fechamodifi TEXT NOT NULL DEFAULT ''," +
                            "empresa TEXT NOT NULL DEFAULT '${Constantes.CLO}'," +
                            "PRIMARY KEY(codigo, empresa));"
                )
                keAndroid.execSQL(
                    "INSERT INTO grupos \n" +
                            "SELECT * FROM old_grupos;"
                )
                keAndroid.execSQL("DROP TABLE old_grupos;")
                keAndroid.setTransactionSuccessful()

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                keAndroid.endTransaction()
            }
        }

        if (oldVersion < 52) {
            try {
                keAndroid.beginTransaction()
                //keAndroid.execSQL("ALTER TABLE tabla_aux ADD empresa TEXT NOT NULL DEFAULT '081196';")
                keAndroid.execSQL("ALTER TABLE sectores RENAME TO old_sectores;")
                keAndroid.execSQL(
                    "CREATE TABLE IF NOT EXISTS sectores(" +
                            "codigo TEXT NOT NULL DEFAULT ''," +
                            "zona TEXT NOT NULL DEFAULT ''," +
                            "fechamodifi TEXT NOT NULL DEFAULT ''," +
                            "empresa TEXT NOT NULL DEFAULT '${Constantes.CLO}'," +
                            "PRIMARY KEY(codigo, empresa));"
                )
                keAndroid.execSQL(
                    "INSERT INTO sectores \n" +
                            "SELECT * FROM old_sectores;"
                )
                keAndroid.execSQL("DROP TABLE old_sectores;")
                keAndroid.setTransactionSuccessful()

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                keAndroid.endTransaction()
            }
        }

        if (oldVersion < 53) {
            try {
                keAndroid.beginTransaction()
                //keAndroid.execSQL("ALTER TABLE tabla_aux ADD empresa TEXT NOT NULL DEFAULT '081196';")
                keAndroid.execSQL("ALTER TABLE articulo RENAME TO old_articulo;")
                keAndroid.execSQL(
                    "CREATE TABLE IF NOT EXISTS articulo(" +
                            "codigo varchar(25) NOT NULL DEFAULT '',\n" +
                            "subgrupo varchar(6) NOT NULL DEFAULT '',\n" +
                            "grupo varchar(6) NOT NULL DEFAULT '',\n" +
                            "nombre char(150) NOT NULL DEFAULT '',\n" +
                            "referencia varchar(20) NOT NULL DEFAULT '',\n" +
                            "marca varchar(20) NOT NULL DEFAULT '',\n" +
                            "unidad varchar(15) NOT NULL DEFAULT '',\n" +
                            "existencia double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "precio1 double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "precio2 double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "precio3 double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "precio4 double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "precio5 double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "precio6 double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "precio7 double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                            "discont double(4,0) NOT NULL DEFAULT '0',\n" +
                            "vta_min double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "vta_max double(20,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "dctotope double(4,2) NOT NULL DEFAULT '0.00',\n" +
                            "enpreventa char(1) NOT NULL DEFAULT '0',\n" +
                            "comprometido double(24,7) NOT NULL DEFAULT '0.0000000',\n" +
                            "vta_minenx double(15,3) NOT NULL DEFAULT '0.000',\n" +
                            "vta_solofac int NOT NULL DEFAULT '0',\n" +
                            "vta_solone int NOT NULL DEFAULT '0'," +
                            "empresa TEXT NOT NULL DEFAULT '${Constantes.CLO}'," +
                            "PRIMARY KEY(codigo, empresa));"
                )
                keAndroid.execSQL(
                    "INSERT INTO articulo \n" +
                            "SELECT * FROM old_articulo;"
                )
                keAndroid.execSQL("DROP TABLE old_articulo;")
                keAndroid.setTransactionSuccessful()

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                keAndroid.endTransaction()
            }
        }

        if (oldVersion < 54) {
            try {
                keAndroid.beginTransaction()
                //keAndroid.execSQL("ALTER TABLE tabla_aux ADD empresa TEXT NOT NULL DEFAULT '081196';")
                keAndroid.execSQL("ALTER TABLE ke_kardex RENAME TO old_ke_kardex;")
                keAndroid.execSQL(
                    "CREATE TABLE IF NOT EXISTS ke_kardex(" +
                            "kde_codart TEXT NOT NULL DEFAULT ''," +
                            "kde_cantidad TEXT NOT NULL DEFAULT ''," +
                            "ke_fecha TEXT NOT NULL DEFAULT ''," +
                            "empresa TEXT NOT NULL DEFAULT '${Constantes.CLO}'," +
                            "PRIMARY KEY(kde_codart, empresa));"
                )
                keAndroid.execSQL(
                    "INSERT INTO ke_kardex \n" +
                            "SELECT * FROM old_ke_kardex;"
                )
                keAndroid.execSQL("DROP TABLE old_ke_kardex;")
                keAndroid.setTransactionSuccessful()

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                keAndroid.endTransaction()
            }
        }

        if (oldVersion < 55) {
            try {
                keAndroid.beginTransaction()
                //keAndroid.execSQL("ALTER TABLE tabla_aux ADD empresa TEXT NOT NULL DEFAULT '081196';")
                keAndroid.execSQL("ALTER TABLE listbanc RENAME TO old_listbanc;")
                keAndroid.execSQL(
                    "CREATE TABLE IF NOT EXISTS `listbanc`(" +
                            "codbanco varchar(3) NOT NULL DEFAULT '',\n" +
                            "nombanco varchar(59) NOT NULL DEFAULT '',\n" +
                            "cuentanac double(2,0) NOT NULL DEFAULT '0',\n" +
                            "inactiva double(1,0) NOT NULL DEFAULT '0',\n" +
                            "fechamodifi datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                            "empresa TEXT NOT NULL DEFAULT '${Constantes.CLO}',\n" +
                            "PRIMARY KEY(codbanco, empresa));"

                )
                keAndroid.execSQL(
                    "INSERT INTO listbanc \n" +
                            "SELECT * FROM old_listbanc;"
                )
                keAndroid.execSQL("DROP TABLE old_listbanc;")
                keAndroid.setTransactionSuccessful()

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                keAndroid.endTransaction()
            }
        }

        println()


// la nueva oldversion <= 34, pero arriba pon 35
        //keAndroid.close()
    }

    private fun alterarTablas(keAndroid: SQLiteDatabase, table: String) {
        keAndroid.execSQL("ALTER TABLE $table ADD empresa TEXT NOT NULL DEFAULT '${Constantes.CLO}';")
    }

    private fun cerarDB(db: SQLiteDatabase) {
        //db.close();
    }

    private fun reOpen() {
        close()
        val conn = AdminSQLiteOpenHelper(context, name, factory)
        //dataBase = conn.writableDatabase // ya eso lo intentaste supongo
    }

    fun upReciboCobroStatus(idRecibo: String, codEmpresa: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("edorec", 3)
        cv.put("fechamodifi", fechaHoy(true))
        try {
            db.beginTransaction()
            db.update("ke_precobranza", cv, "cxcndoc = ? AND empresa = ?", arrayOf(idRecibo, codEmpresa))
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        cerarDB(db)
    }

    private fun getConfigTipo(config: String?): String {
        var campoAux = ""
        when (config) {
            "N" -> campoAux = "cnfg_valnum"
            "C" -> campoAux = "cnfg_valtxt"
            "0" -> campoAux = "cnfg_valsino"
            "D" -> campoAux = "cnfg_valfch"
        }
        return campoAux
    }

    fun getConfigNum(config: String, codEmpresa: String): Double {
        val db = this.writableDatabase
        val num: Double
        val cursor = db.rawQuery(
            "SELECT ${getConfigTipo("N")} FROM ke_wcnf_conf " +
                    "WHERE cnfg_idconfig = '$config' AND empresa = '$codEmpresa';",
            null
        )
        num = if (cursor.moveToFirst()) {
            cursor.getDouble(0)
        } else {
            0.0
        }
        cursor.close()
        cerarDB(db)
        return num
    }

    fun getConfigBool(config: String, codEmpresa: String): Boolean {
        val db = this.writableDatabase
        var flag = false
        val cursor = db.rawQuery(
            "SELECT ${getConfigTipo("0")} FROM ke_wcnf_conf " +
                    "WHERE cnfg_idconfig = '$config' AND empresa = '$codEmpresa';",
            null
        )
        //System.out.println("SELECT " + getConfigTipo("0") + " FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + config + "';");
        if (cursor.moveToFirst()) {
            flag = cursor.getInt(0) == 1
        }
        cursor.close()
        cerarDB(db)
        return flag
    }

    fun getConfigBoolUsuario(config: String, user: String, codEmpresa: String): Boolean {
        val db = this.writableDatabase
        var flag = false
        val cursor = db.rawQuery(
            "SELECT ${getConfigTipo("0")} FROM ke_wcnf_conf WHERE cnfg_idconfig = '${config}' AND username = '${user}' AND cnfg_activa = '1.0' AND empresa = '$codEmpresa';",
            null
        )
        //System.out.println("SELECT " + getConfigTipo("0") + " FROM ke_wcnf_conf WHERE cnfg_idconfig = '" + config + "';");
        if (cursor.moveToFirst()) {
            flag = cursor.getInt(0) == 1
        }
        cursor.close()
        cerarDB(db)
        return flag
    }

    fun getConfigString(config: String, codEmpresa: String): String {

        val db = this.writableDatabase
        val texto: String
        val cursor = db.rawQuery(
            "SELECT ${getConfigTipo("C")} FROM ke_wcnf_conf " +
                    "WHERE cnfg_idconfig = '$config' AND cnfg_activa = '1.0' AND empresa = '$codEmpresa';",
            null
        )
        texto = if (cursor.moveToFirst()) {
            cursor.getString(0)
        } else {
            ""
        }
        cursor.close()
        cerarDB(db)
        return texto
    }

    private fun fechaHoy(wTime: Boolean): String {
        val dateFormat: DateFormat = if (wTime) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        }
        return dateFormat.format(Calendar.getInstance().time)
    }

    //2023-06-19 FUncion que me devuelve un true si el cliente tiene documentos vencidos
    fun getDeudaCliente(cliente: String): Boolean {
        val db = this.writableDatabase
        var flag = false
        val cursor = db.rawQuery(
            "SELECT COUNT(documento) FROM ke_doccti WHERE codcliente = '$cliente' AND vence < '" + fechaHoy(
                false
            ) + "';", null
        )
        //System.out.println("SELECT COUNT(documento) FROM ke_doccti WHERE codcliente = '" + cliente + "' AND vence < '"+ FechaHoy(false) +"';");
        if (cursor.moveToFirst()) {
            flag = cursor.getInt(0) > 0
        }
        cursor.close()
        cerarDB(db)
        return flag
    }

    //2023-06-19 Funcion que me devuelve la cantidad de documentos vencidos de un cliente
    fun getDeudaClienteNum(cliente: String): Int {
        val db = this.writableDatabase
        var num = 0
        val cursor = db.rawQuery(
            "SELECT COUNT(documento) FROM ke_doccti WHERE codcliente = '$cliente' AND vence < '" + fechaHoy(
                false
            ) + "';", null
        )
        //System.out.println("SELECT COUNT(documento) FROM ke_doccti WHERE codcliente = '" + cliente + "' AND vence < '"+ FechaHoy(false) +"';");
        if (cursor.moveToFirst()) {
            num = cursor.getInt(0)
        }
        cursor.close()
        cerarDB(db)
        return num
    }

    fun getEfectivoDoc(correlativo: String): Double {
        val db = this.writableDatabase
        var retorno = 0.0
        val cursor =
            db.rawQuery("SELECT efectivo FROM ke_precobranza WHERE cxcndoc = '$correlativo';", null)
        if (cursor.moveToFirst()) {
            retorno = cursor.getDouble(0)
        }
        cursor.close()
        cerarDB(db)
        return retorno
    }

    fun getDeudaClienteTotal(
        codCliente: String, monedaSigno: String, tasa: Double, totalCobrado: Double
    ): Double {
        val db = this.writableDatabase
        var numero = 0.0
        val cursor = db.rawQuery(
            "SELECT SUM(dtotalfinal - dtotpagos) FROM ke_doccti WHERE codcliente = '$codCliente';",
            null
        )
        if (cursor.moveToFirst()) {
            numero = cursor.getDouble(0)
            return if (monedaSigno == "$") {
                if (numero - totalCobrado < 0) {
                    0.0
                } else {
                    numero - totalCobrado
                }
            } else {
                if (numero - totalCobrado / tasa < 0) {
                    0.0
                } else {
                    numero - totalCobrado / tasa
                }
            }
        }
        cursor.close()
        cerarDB(db)
        return numero
    }

    /*fun getCampoInt(tabla: String, campo: String, campoWhere: String, respuestaWhere: String): Int {
        var retorno = 0
        val db = this.writableDatabase
        db.rawQuery("SELECT $campo FROM $tabla WHERE $campoWhere = '$respuestaWhere';", null)
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    retorno = cursor.getInt(0)
                }
            }
        cerarDB(db)
        return retorno
    }*/

    fun getCampoIntCamposVarios(tabla: String, campo: String, campoWhere: List<String>, respuestaWhere: List<String>): Int {
        var retorno = 0
        val db = this.writableDatabase
        val sql = "SELECT $campo FROM $tabla WHERE "
        var where = ""
        for (i in campoWhere.indices) {
            where += campoWhere[i] + " = '" + respuestaWhere[i] + "'"
            if (i + 1 != campoWhere.size) {
                where += " AND "
            }
        }
        val query = sql + where
        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getInt(0)
            }
        }
        cerarDB(db)
        return retorno
    }

    /*fun getCampoDouble(
        tabla: String, campo: String, campoWhere: String, respuestaWhere: String
    ): Double {
        var retorno = 0.0
        val db = this.writableDatabase
        println("SELECT $campo FROM $tabla WHERE $campoWhere = '$respuestaWhere';")
        db.rawQuery("SELECT $campo FROM $tabla WHERE $campoWhere = '$respuestaWhere';", null)
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    retorno = cursor.getDouble(0)
                }
            }
        cerarDB(db)
        return retorno
    }*/

    fun getCampoDoubleCamposVarios(
        tabla: String,
        campo: String,
        campoWhere: List<String>,
        respuestaWhere: List<String>
    ): Double {
        var retorno = 0.0
        val db = this.writableDatabase
        val sql = "SELECT $campo FROM $tabla WHERE "
        var where = ""
        for (i in campoWhere.indices) {
            where += campoWhere[i] + " = '" + respuestaWhere[i] + "'"
            if (i + 1 != campoWhere.size) {
                where += " AND "
            }
        }
        val query = sql + where
        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getDouble(0)
            }
        }
        cerarDB(db)
        return retorno
    }

    /*fun getCampoString(
        tabla: String, campo: String, campoWhere: String, respuestaWhere: String
    ): String {
        var retorno = ""
        val db = this.writableDatabase
        //reOpen()
        println("SELECT $campo FROM $tabla WHERE $campoWhere = '$respuestaWhere';")
        db.rawQuery("SELECT $campo FROM $tabla WHERE $campoWhere = '$respuestaWhere';", null)
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    retorno = cursor.getString(0)//Look at This
                }
            }
        //cerarDB(db)
        return retorno
    }*/

    fun getCampoStringCamposVarios(
        tabla: String,
        campo: String,
        campoWhere: List<String>,
        respuestaWhere: List<String>
    ): String {
        var retorno = ""
        val db = this.writableDatabase
        val sql = "SELECT $campo FROM $tabla WHERE "
        var where = ""
        for (i in campoWhere.indices) {
            where += campoWhere[i] + " = '" + respuestaWhere[i] + "'"
            if (i + 1 != campoWhere.size) {
                where += " AND "
            }
        }
        val query = sql + where
        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getString(0)
            }
        }
        cerarDB(db)
        return retorno
    }

    fun deleteAll(tabla: String?) {
        val db = this.writableDatabase
        try {
            db.delete(tabla, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cerarDB(db)
    }

    fun validarExistencia(tabla: String, campo: String, campoWhere: String): Boolean {
        var retorno = false
        val db = this.writableDatabase
        db.rawQuery("SELECT count($campo) FROM $tabla WHERE $campo = '$campoWhere';", null)
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    retorno = cursor.getInt(0) > 0
                }
            }
        cerarDB(db)
        return retorno
    }

    fun validarExistenciaCamposVarios(
        tabla: String, campo: ArrayList<String>, campoWhere: ArrayList<String>
    ): Boolean {
        var retorno = false
        val db = this.writableDatabase
        val sql = "SELECT count(*) FROM $tabla WHERE "
        var where = ""
        for (i in campo.indices) {
            where += campo[i] + " = '" + campoWhere[i] + "'"
            if (i + 1 != campo.size) {
                where += " AND "
            }
        }
        val query = sql + where
        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getInt(0) > 0
            }
        }
        cerarDB(db)
        return retorno
    }

    fun getCampoDoubleV(
        tabla: String, campo: String, campoWhere: List<String>, respuestaWhere: List<String>
    ): Double {
        var retorno = 0.0
        val db = this.writableDatabase
        val sql = "SELECT $campo FROM $tabla WHERE "
        var where = ""
        for (i in campoWhere.indices) {
            where += campoWhere[i] + " = '" + respuestaWhere[i] + "'"
            if (i + 1 != campoWhere.size) {
                where += " AND "
            }
        }
        db.rawQuery(sql + where, null).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getDouble(0)
            }
        }
        cerarDB(db)
        return retorno
    }

    fun insertJSON(table: String?, cv: ContentValues?) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            db.insert(table, null, cv)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        cerarDB(db)
    }

    fun updateJSON(table: String?, cv: ContentValues?, campo: String, id: String) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            db.update(table, cv, "$campo = ?", arrayOf(id))
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        cerarDB(db)
    }

    fun updateJSONCamposVarios(
        table: String?, cv: ContentValues?, whereClause: String?, whereArgs: Array<String?>?
    ) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            db.update(table, cv, whereClause, whereArgs)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        cerarDB(db)
    }

    fun deleteJSONCamposVarios(
        table: String?, whereClause: String?, whereArgs: Array<String?>?
    ) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            db.delete(table, whereClause, whereArgs)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        cerarDB(db)
    }

    //Actualizacion de tabla auxiliar
    fun updateTablaAux(table: String, codEmpresa: String) {
        val db = this.writableDatabase
        try {
            val cv = ContentValues()
            cv.put("tabla", table)
            cv.put("fchhn_ultmod", fechaHoy(true))
            cv.put("empresa", codEmpresa)

            db.beginTransaction()

            //Se coloca un if para saber si se actualiza el registro o se crea
            if (this.validarExistenciaCamposVarios(
                    "tabla_aux",
                    ArrayList(mutableListOf("tabla", "empresa")),
                    arrayListOf(table, codEmpresa)
                )
            ) {
                db.update("tabla_aux", cv, "tabla = ? AND empresa = ?", arrayOf(table, codEmpresa))
            } else {
                db.execSQL("INSERT INTO tabla_aux VALUES ('$table', '${fechaHoy(true)}', '$codEmpresa')") //<-- Colocandolo asi funciona, con las demas formas no

                //db.insert(table, null, cv)

                //db.setTransactionSuccessful()
                //this.insertJSON("tabla_aux", cv)
            }

            db.setTransactionSuccessful()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        cerarDB(db)
    }

    //Funcion que sirve para verificar si el vendedor sincronizo alguna vez en la vida de la app
    //Se eligio articulos para la funcion por ser una tabla que se llena sin importar la situacion
    fun sincronizoPriVez(vendedor: String, codEmpresa: String): Boolean {
        var retorno = false
        val db = this.writableDatabase
        db.rawQuery("select sinc_primera from usuarios WHERE vendedor = '$vendedor' AND empresa = '$codEmpresa';", null)
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    retorno = cursor.getInt(0) == 1
                }
            }
        cerarDB(db)
        return retorno
    }

    fun imgCarousel(codEmpresa: String): List<CarouselItem>
         {
            val lista: MutableList<CarouselItem> = ArrayList()
            val db = this.writableDatabase
            db.rawQuery("SELECT * FROM img_carousel WHERE empresa = '$codEmpresa';", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val nombre = cursor.getString(0)
                    val enlace = cursor.getString(1)
                    lista.add(CarouselItem(enlace))
                }
            }
            cerarDB(db)
            return lista
        }

    fun validarExistenciaDescuento(banco: String): Boolean {
        var retorno = false
        val db = this.writableDatabase
        db.rawQuery(
            "SELECT count(*) FROM ke_tabdctosbcos INNER JOIN ke_tabdctos ON ke_tabdctosbcos.dcob_id = ke_tabdctos.dcob_id WHERE ke_tabdctosbcos.bco_codigo = '$banco' AND ke_tabdctos.dcob_activo = '1';",
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getInt(0) > 0
            }
        }
        cerarDB(db)
        return retorno
    }

    fun getDescuento(banco: String, tipoDoc: String): Double {
        var retorno = 0.0
        val db = this.writableDatabase
        db.rawQuery(
            "SELECT dcob_prc FROM ke_tabdctosbcos INNER JOIN ke_tabdctos ON ke_tabdctosbcos.dcob_id = ke_tabdctos.dcob_id WHERE ke_tabdctosbcos.bco_codigo = '$banco' AND ke_tabdctos.dcob_activo = '1'" + getTipoDocDescuento(
                tipoDoc
            ) + ";", null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getDouble(0)
            }
        }
        cerarDB(db)
        return retorno
    }

    fun getDescuentoEfectivo(moneda: String, tipoDoc: String): Double {
        var retorno = 0.0
        val db = this.writableDatabase
        val dcobValemon: String = if (moneda == "USD") {
            "('3', '5')"
        } else {
            "('3', '4')"
        }
        db.rawQuery(
            "SELECT dcob_prc FROM ke_tabdctos WHERE dcob_valemon IN $dcobValemon AND dcob_activo = '1';" + getTipoDocDescuento(
                tipoDoc
            ) + ";", null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getDouble(0)
            }
        }
        cerarDB(db)
        return retorno
    }

    private fun getTipoDocDescuento(tipoDoc: String): String {
        return when (tipoDoc) {
            "FAC" -> " AND dcob_valefac = '1'"
            "N/E" -> " AND dcob_valene = '1'"
            else -> ""
        }
    }

    fun ValidarDescuento(): Boolean {
        var retorno = false
        val db = this.writableDatabase
        db.rawQuery("SELECT count(*) FROM ke_tabdctosbcos;", null).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno = cursor.getInt(0) > 0
            }
        }
        cerarDB(db)
        return retorno
    }

    fun articulosPromo(codEmpresa: String): ArrayList<Catalogo> {
        val retorno = ArrayList<Catalogo>()
        val db = this.writableDatabase
        db.rawQuery(
            "SELECT * FROM articulo " +
                    "WHERE dctotope > 0 AND (existencia - comprometido) > 0 AND empresa = '$codEmpresa';",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val catalogo = Catalogo()
                catalogo.setCodigo(cursor.getString(0))
                catalogo.setNombre(cursor.getString(3))
                catalogo.setExistencia(cursor.getInt(7) - cursor.getInt(21))
                catalogo.setPrecio1(valorReal(cursor.getDouble(8)))
                catalogo.setDctotope(cursor.getDouble(19))
                catalogo.setMultiplo(cursor.getInt(22))
                catalogo.setVta_min(cursor.getDouble(17))
                catalogo.setVta_max(cursor.getDouble(18))
                retorno.add(catalogo)
            }
        }
        cerarDB(db)
            return retorno
        }

    fun getSizeImage(dimension: String, position: Int): Int {
        var retorno = 0
        val db = this.writableDatabase
        db.rawQuery("SELECT $dimension FROM img_carousel LIMIT 1 OFFSET $position;", null)
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    retorno = cursor.getInt(0)
                }
            }
        cerarDB(db)
        return retorno
    }

    fun getPlanificadorDocs(codUsuario: String, text: String?): ArrayList<PlanificadorCxc> {
        val retorno = ArrayList<PlanificadorCxc>()
        val db = this.writableDatabase
        var sql =
            "SELECT ke_doccti.codcliente, ke_doccti.nombrecli, ke_doccti.estatusdoc, ke_doccti.documento, ke_doccti.vence, ke_doccti.diascred, ke_doccti.kti_negesp, (ke_doccti.dtotalfinal - (ke_doccti.dtotpagos + ke_doccti.dtotdev)), ke_doccti.recepcion, ke_doccti.dtotdev, ke_opti.ke_pedstatus FROM ke_doccti LEFT JOIN ke_opti ON ke_opti.kti_nroped = ke_doccti.documento WHERE ke_doccti.vendedor = '$codUsuario' AND (ke_doccti.estatusdoc = '0' OR ke_doccti.estatusdoc= '1') AND (ke_doccti.dtotalfinal - (ke_doccti.dtotpagos + ke_doccti.dtotdev)) > 0.00"
        if (!text.isNullOrEmpty()) {
            sql += " AND (nombrecli LIKE '%$text%' OR documento LIKE '%$text%')"
        }
        sql += " ORDER BY ke_doccti.vence asc, (ke_doccti.dtotalfinal - (ke_doccti.dtotpagos + ke_doccti.dtotdev)) desc"
        db.rawQuery(sql, null).use { cursor ->
            while (cursor.moveToNext()) {
                val codcliente = cursor.getString(0)
                val nombrecli = cursor.getString(1)
                val estatusdoc = cursor.getString(2)
                val documento = cursor.getString(3)
                val vence = cursor.getString(4)
                val diascred = cursor.getInt(5)
                val ktiNegesp = cursor.getString(6)
                val dtotalfinal = cursor.getDouble(7)
                val recepcion = cursor.getString(8)
                val flag = cursor.getDouble(9)
                val reclamo = cursor.getDouble(9) > 0
                val statusPed = if (cursor.getString(10) != null) cursor.getString(10) else ""
                val planificadorCxc = PlanificadorCxc(
                    documento,
                    nombrecli,
                    codcliente,
                    dtotalfinal,
                    vence,
                    estatusdoc,
                    statusPed,
                    recepcion,
                    diascred,
                    reclamo
                )
                retorno.add(planificadorCxc)
            }
        }
        return retorno
    }

    fun getEdoGenCuenta(codUsuario: String, text: String?): ArrayList<EdoGeneralCxc> {
        val retorno = ArrayList<EdoGeneralCxc>()
        val db = this.writableDatabase
        var sql =
            "SELECT DISTINCT  codcliente, nombrecli, SUM(dtotalfinal - (dtotpagos + dtotdev)) as montototal, MIN(vence), ROUND(JULIANDAY('now') - JULIANDAY(MIN(vence))) as fechaentregaantigua FROM ke_doccti WHERE estatusdoc < '2' AND vendedor = '$codUsuario'"
        if (!text.isNullOrEmpty()) {
            sql += " AND (nombrecli LIKE '%$$text%' OR codcliente LIKE '%$text%')"
        }
        sql += " GROUP BY codcliente ORDER BY montototal desc"
        db.rawQuery(sql, null).use { cursor ->
            while (cursor.moveToNext()) {
                val codcliente = cursor.getString(0)
                val nombreCliente = cursor.getString(1)
                val montoTotal = valorReal(cursor.getDouble(2))
                val fechaVence = cursor.getString(4)
                val limite = 0.00
                val saldo = 0.00
                val edoGeneralCxc = EdoGeneralCxc(
                    codcliente, nombreCliente, montoTotal, fechaVence, limite, saldo
                )
                retorno.add(edoGeneralCxc)
            }
        }
        return retorno
    }

    fun guardarArticulos(articulosResponse: ArticulosResponse) {
        val db = this.writableDatabase

        articulosResponse.articulo?.let { list ->
            list.forEach { item ->
                item?.let {
                    db.execSQL(
                        """INSERT INTO `articulo`
                        (`codigo`,
                         `grupo`,
                         `subgrupo`,
                         `nombre`,
                         `referencia`,
                         `marca`,
                         `unidad`,
                         `existencia`,
                         `precio1`,
                         `precio2`,
                         `precio3`,
                         `precio4`,
                         `precio5`,
                         `precio6`,
                         `precio7`,
                         `fechamodifi`,
                         `discont`,
                         `vta_max`,
                         `vta_min`,
                         `dctotope`,
                         `enpreventa`,
                         `comprometido`,
                         `vta_minenx`,
                         `vta_solofac`,
                         `vta_solone`) 
                        VALUES 
                        ('${item.codigo}',
                        '${item.grupo}',
                        '${item.subgrupo}',
                        '${item.nombre}',
                        '${item.referencia}',
                        '${item.marca}',
                        '${item.unidad}',
                        '${item.existencia}',
                        '${item.precio1}',
                        '${item.precio2}',
                        '${item.precio3}',
                        '${item.precio4}',
                        '${item.precio5}',
                        '${item.precio6}',
                        '${item.precio7}',
                        '${item.fechamodifi}',
                        '${item.discont}',
                        '${item.vtaMax}',
                        '${item.vtaMin}',
                        '${item.dctotope}',
                        '${item.enpreventa}',
                        '${item.comprometido}',
                        '${item.vtaMinenx}',
                        '${item.vtaSolofac}',
                        '${item.vtaSolone}')""".trimMargin()
                    )
                }
            }
        }


    }

    fun getCabeceraPedido(codigoPedido: String, codEmpresa: String): keOpti {
        val retorno = keOpti()
        val db = this.writableDatabase
        db.rawQuery(
            "SELECT * FROM ke_opti WHERE kti_ndoc = '$codigoPedido' AND empresa = '$codEmpresa';",
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                retorno.kti_ndoc = cursor.getString(0)
                retorno.kti_tdoc = cursor.getString(1)
                retorno.kti_codcli = cursor.getString(2)
                retorno.kti_nombrecli = cursor.getString(3)
                retorno.kti_codven = cursor.getString(4)
                retorno.kti_docsol = cursor.getString(5)
                retorno.kti_condicion = cursor.getString(6)
                retorno.kti_tipprec = cursor.getDouble(7)
                retorno.kti_totneto = cursor.getDouble(8)
                retorno.kti_status = cursor.getString(9)
                retorno.kti_nroped = cursor.getString(10)
                retorno.kti_fchdoc = cursor.getString(11)
                retorno.fechamodifi = cursor.getString(12)
                retorno.kti_negesp = cursor.getString(13)
                retorno.kti_totnetodcto = cursor.getDouble(14)
                retorno.ke_pedstatus = cursor.getString(15)

            }
        }
        cerarDB(db)
        return retorno
    }

    fun getLineasPedido(codigoPedido: String, codEmpresa: String): ArrayList<keOpmv> {
        val retorno = arrayListOf<keOpmv>()
        val db = this.writableDatabase
        db.rawQuery("SELECT * FROM ke_opmv WHERE kti_ndoc = '$codigoPedido' AND empresa = '$codEmpresa';", null).use { cursor ->
            while (cursor.moveToNext()) {
                val item = keOpmv()
                item.kti_tdoc = cursor.getString(0)
                item.kti_ndoc = cursor.getString(1)
                item.kti_tipprec = cursor.getDouble(2)
                item.kmv_codart = cursor.getString(3)
                item.kmv_nombre = cursor.getString(4)
                item.kmv_cant = cursor.getDouble(5)
                item.kmv_artprec = cursor.getDouble(6)
                item.kmv_stot = cursor.getDouble(7)
                item.kmv_dctolin = cursor.getDouble(8)
                item.kmv_stotdcto = cursor.getDouble(9)

                retorno.add(item)
            }
        }
        cerarDB(db)
        return retorno
    }

    fun getCliente(codigoCliente: String, codEmpresa: String): ClientesKt {
        val item = ClientesKt()
        val db = this.writableDatabase
        db.rawQuery(
            "SELECT * FROM cliempre WHERE codigo = '$codigoCliente' AND empresa = '$codEmpresa';",
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {


                item.codigo = cursor.getString(0);
                item.nombre = cursor.getString(1);
                item.direccion = cursor.getString(2);
                item.telefonos = cursor.getString(3);
                item.perscont = cursor.getString(4);
                item.vendedor = cursor.getString(5);
                item.contribespecial = cursor.getDouble(6);
                item.status = cursor.getDouble(7);
                item.sector = cursor.getString(8);
                item.subcodigo = cursor.getString(9);
                item.fechamodifi = cursor.getString(10);
                item.precio = cursor.getDouble(11);

                item.kne_activa = cursor.getString(12);
                item.kne_mtomin = cursor.getDouble(13);
                item.noemifac = cursor.getInt(14);
                item.noeminota = cursor.getInt(15);
                item.fchultvta = cursor.getString(16);
                item.mtoultvta = cursor.getDouble(17);
                item.prcdpagdia = cursor.getDouble(18);
                item.promdiasp = cursor.getDouble(19);
                item.riesgocrd = cursor.getDouble(20);
                item.cantdocs = cursor.getDouble(21);
                item.totmtodocs = cursor.getDouble(22);
                item.prommtodoc = cursor.getDouble(23);
                item.diasultvta = cursor.getDouble(24);
                item.promdiasvta = cursor.getDouble(25);
                item.limcred = cursor.getDouble(26);
                item.fchcrea = cursor.getString(27);

                item.email = cursor.getString(28);


            }
        }
        cerarDB(db)
        return item
    }

    fun getClientes(text: String?, codEmpresa: String): ArrayList<ClientesKt> {
        val retorno = ArrayList<ClientesKt>()
        val db = this.writableDatabase

        var sql = "SELECT * FROM cliempre WHERE empresa = '$codEmpresa'"

        if (!text.isNullOrEmpty()) {
            sql += " AND (nombre LIKE '%$text%' OR codigo LIKE'%$text%');"
        }

        db.rawQuery(sql, null).use { cursor ->
            while (cursor.moveToNext()) {
                val item = ClientesKt()

                item.codigo = cursor.getString(0);
                item.nombre = cursor.getString(1);
                item.direccion = cursor.getString(2);
                item.telefonos = cursor.getString(3);
                item.perscont = cursor.getString(4);
                item.vendedor = cursor.getString(5);
                item.contribespecial = cursor.getDouble(6);
                item.status = cursor.getDouble(7);
                item.sector = cursor.getString(8);
                item.subcodigo = cursor.getString(9);
                item.fechamodifi = cursor.getString(10);
                item.precio = cursor.getDouble(11);

                item.kne_activa = cursor.getString(12);
                item.kne_mtomin = cursor.getDouble(13);
                item.noemifac = cursor.getInt(14);
                item.noeminota = cursor.getInt(15);
                item.fchultvta = cursor.getString(16);
                item.mtoultvta = cursor.getDouble(17);
                item.prcdpagdia = cursor.getDouble(18);
                item.promdiasp = cursor.getDouble(19);
                item.riesgocrd = cursor.getDouble(20);
                item.cantdocs = cursor.getDouble(21);
                item.totmtodocs = cursor.getDouble(22);
                item.prommtodoc = cursor.getDouble(23);
                item.diasultvta = cursor.getDouble(24);
                item.promdiasvta = cursor.getDouble(25);
                item.limcred = cursor.getDouble(26);
                item.fchcrea = cursor.getString(27);

                item.email = cursor.getString(28);

                retorno.add(item)

            }
        }
        cerarDB(db)
        return retorno
    }

    fun saveImg(
        listaImagenes: List<Uri>,
        numCXC: String,
        activity: AppCompatActivity = AppCompatActivity()
    ) {

        val db = this.writableDatabase
        try {
            db.beginTransaction()

            for (i in listaImagenes.indices) {
                val cv = ContentValues()

                val img = activity.contentResolver.openInputStream(listaImagenes[i])
                var bitmap = BitmapFactory.decodeStream(img)
                bitmap = bitmap.redimensionarImagen(1000f, 1000f)
                val cadena: String = bitmap.convertirUriToBase64()!!

                cv.put("cxcndoc", numCXC)
                cv.put("ruta", cadena)
                cv.put("ret_nomimg", numCXC + "_" + i)

                bitmap.recycle()

                db.insert("ke_retimg", null, cv)
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        cerarDB(db)

    }

    fun getEmpresas(agencia: String): ArrayList<keDataconex> {
        val retorno = ArrayList<keDataconex>()
        val db = this.writableDatabase
        val sql = "SELECT * FROM ke_enlace"

        db.rawQuery(sql, null).use { cursor ->
            while (cursor.moveToNext()) {
                val kedCodigo = cursor.getString(0)
                val kedNombre = cursor.getString(1)
                val kedStatus = cursor.getString(2)
                val kedEnlace = cursor.getString(3)
                val kedAgen = cursor.getString(4)
                val selected = kedCodigo == agencia

                val keDataconex = keDataconex(
                    kedCodigo,
                    kedNombre,
                    kedStatus,
                    kedEnlace,
                    kedAgen,
                    selected
                )

                retorno.add(keDataconex)
            }
        }
        return retorno
    }

    fun getFecha(tabla: String, codEmpresa: String): String {
        val keAndroid = this.writableDatabase
        val fechaUltmod =
            keAndroid.rawQuery(
                "SELECT fchhn_ultmod FROM tabla_aux WHERE tabla = '$tabla' AND empresa = '$codEmpresa';",
                null
            )
        var fecha = "0001-01-01 01:01:01"
        if (fechaUltmod.moveToFirst()) {
            fecha = fechaUltmod.getString(0)
        }
        fechaUltmod.close()
        return fecha
    }


}