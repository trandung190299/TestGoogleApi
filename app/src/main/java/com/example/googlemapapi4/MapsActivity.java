package com.example.googlemapapi4;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;


// đang bị lỗi tự cập nhật vị trí liên tục , sửa bên main
/*implements GoogleMap.OnMapClickListener sự kiện click trên map
ren ra hàm onMapClick(@NonNull LatLng latLng)*/

/* implements GoogleMap.OnMarkerDragListener creart 3 menthort
chức năng di chuyển marker trên bản đồ
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener
{
    public static final String TAG = "MainActivity";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private Geocoder geocoder;  // dùng geocoder để mã hóa vị trí . từ tọa độ ra địa chỉ và ngc lại
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Marker userLocationMarker;
    Circle userLocationAccuracyCircle;

    private int ACCESS_LOCATION_REQUEST_CODE = 10001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // check lỗi chưa có quyền
    public void checkSelfPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(this);  // sự kiện click trên map
        mMap.setOnMarkerDragListener(this);// ghim phần map chỗ có marker click

        // thỏa mãn quyền thì chạy hàm
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
            zoomUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We can show user a dialog why this permission is necessary
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            }
        }

        /*
        add mặc định 1 vị trí
        LatLng latLng = new LatLng(27.1751, 78.0421);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Taj Mahal").snippet("Wonder of the world!");
        mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.animateCamera(cameraUpdate);
         */

        try {

            // mã hóa 1 tọa độ thành địa chỉ cụ thể
            List<Address> addresses = geocoder.getFromLocationName("london", 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                LatLng london = new LatLng(address.getLatitude(), address.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(london)
                        .title(address.getLocality()); // lấy tên thành phố
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(london, 16));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // sự kiện click trên bản đồ và add vào điểm đó 1 marker, hiển thị thông tin về điểm
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
       //  latLng=mMap.getCameraPosition().target;
       // fixMarker();
        Log.d(TAG, "onMapLongClick: " + latLng.toString());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(streetAddress)
                        .draggable(true)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // hiển thị nút vị trí trên map click chuyển về vị trí hiện tại
    private void enableUserLocation() {
        checkSelfPermission();
        mMap.setMyLocationEnabled(true);
       // fixMarker();
    }

    // hiển thị khu vực xung quanh vị trí hiện tại , add 1 marker vào vị trí hiện tại và show data location
    private void zoomUserLocation()
    {
        checkSelfPermission();
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));  //khoảng hiện thị

                // add 1 marker
                //mMap.addMarker(new MarkerOptions().position(latLng));
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String streetAddress = address.getAddressLine(0);
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(streetAddress)
                                .draggable(true)
                        );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        Log.d(TAG, "onMarkerDragStart: ");

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
        Log.d(TAG, "onMarkerDrag: ");

    }

    // cập nhật lại data local sau khi di chuyển marker
    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        Log.d(TAG, "onMarkerDragEnd: ");
       LatLng latLng=marker.getPosition();
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                marker.setTitle(streetAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                enableUserLocation();
                zoomUserLocation();
            }
            else
            {
                // chưa đc cấp quyền
                //Chúng ta có thể hiển thị hộp thoại cho biết quyền không được cấp ...
            }
        }
    }


    // fix 1 marker cố định trên map
    public void fixMarker()
    {
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove()
            {
               //LatLng location= mMap.getProjection().getVisibleRegion().latLngBounds.getCenter(); //chỏ hiển thị giữa bản đồ
                LatLng location=mMap.getCameraPosition().target;  // gán cứng vị trí marker trên map
                MarkerOptions marker=new MarkerOptions().position(location).title("");
                mMap.clear();
                mMap.addMarker(marker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20));
            }
        });
    }


}