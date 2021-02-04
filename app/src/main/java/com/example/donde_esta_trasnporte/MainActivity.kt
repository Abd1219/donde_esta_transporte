package com.example.donde_esta_trasnporte

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
//getMapAsync(this) necesita que nuestra activity implemente la función onMapReady()
class MainActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,GoogleMap.OnMyLocationClickListener {
////Ahora implementaremos el cierre de sesión haciendo clic en el botón "SALIR". Esto nos dirige a la pantalla de inicio de sesión nuevamente
    // declare the GoogleSignInClient
    lateinit var mGoogleSignInClient: GoogleSignInClient
    // val auth is initialized by lazy
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private lateinit var map: GoogleMap
    companion object{
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)
        // que será la encargada de inicializar el fragment que hemos creado

// pasar el mismo ID de cliente del servidor que se utilizó al implementar la función de inicio de sesión anteriormente.
        //boton salir
        logout.setOnClickListener {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent= Intent(this, LoginScreen::class.java)
                startActivity(intent)
                finish()
            }
        }
        createMapFragment()
    }



    //llama al mapa para crearlo
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //crear marcador
        createMarker()
        enableLocation()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        //map.setOnMyLocationClickListener { this }

    }
    //carga el mapa en el fragmento
    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    //funcion crea marcador
    private fun createMarker() {
        //longitud y latitud del marcador
        val favoritePlace = LatLng(19.521109, -96.922288)
        map.addMarker(MarkerOptions().position(favoritePlace).title("Pueba de lugar!"))
       //animación para que el mapa haga zoom donde creamos el marker.
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, 18f),
            4000,
            null
        )
    }
    //CHECAR PERMISOS si esta dado o no
    private fun isLocationPermissionGranted() =
        ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    //Activar la localizacion
    private fun enableLocation(){
        if (!::map.isInitialized) return//si el mapa no esta inicializado salir
        if(isLocationPermissionGranted()){//si los permisos de localizacion estan activado
            map.isMyLocationEnabled = true//activa la localizacion en tiempo real
        }else{
            requestLocationPermission()// si no pide los permisos

        }
    }
    //pedir al usuario que active permisos
    private fun requestLocationPermission(){
        //si ya pidio permisos y los rechazo
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this,"Activa los permisos",Toast.LENGTH_SHORT).show()
        }else{
            //si no, pedimos permisos
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }
    //comprobar permiso aceptado
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            //si pedimos los permisos y los acepta
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true// activamos la localizacion
            }else{
                Toast.makeText(this,"Activa los permisos en ajustes o no podras usar la app",Toast.LENGTH_SHORT).show()

            }
            else -> {}
        }
    }
    //cuando vuelve de otra app comprueba nuevamente los permisos
    override fun onResumeFragments() {
        super.onResumeFragments()
        if(!::map.isInitialized)return
        if (!isLocationPermissionGranted()){
            map.isMyLocationEnabled=false
            Toast.makeText(this,"Activa los permisos en ajustes o no podras usar la app",Toast.LENGTH_SHORT).show()

        }
    }
    //cuando el ususario de clic al boton de localizacion
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this,"boton pulsado",Toast.LENGTH_SHORT).show()
        return false
    }
    //cuando el usuario da click
    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this,"Estas en ${p0.latitude},${p0.longitude}",Toast.LENGTH_SHORT).show()
        //Toast.makeText(this,"has echo click",Toast.LENGTH_SHORT).show()

    }
}