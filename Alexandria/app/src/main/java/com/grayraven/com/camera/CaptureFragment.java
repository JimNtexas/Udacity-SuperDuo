package com.grayraven.com.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.List;

import it.jaschke.alexandria.R;

import static com.grayraven.com.camera.Utilities.showAlertDialog;

public class CaptureFragment extends android.support.v4.app.Fragment
        implements SurfaceHolder.Callback {

    public static final String ISBN_STRING = "shared_pref_isbn";
    private static final String TAG = "CaptureFragment";


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    Camera mCamera;
    Context mContext;
    List<Camera.Size> mSupportedPreviewSizes;
    SurfaceHolder mHolder;
    SurfaceView mSurfaceView;
    int mCameraId = 0;
    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;


    public static CaptureFragment newInstance() {
        return new CaptureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        saveIsbn("");
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_view);

        Button captureButton = (Button)view.findViewById(R.id.capture_image);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaptureImage();

            }
        });
    }

    private void CaptureImage() {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        setCamera(mCamera);
    }

    @Override
    public void onPause() {
        stopPreviewAndFreeCamera();
        Log.d(TAG, "capture fragement onPause");
        super.onPause();
    }

    public void setCamera(Camera camera) {

        stopPreviewAndFreeCamera();
        saveIsbn("");
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if(!safeCameraOpen()) {
            return;
        }
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                Log.e(TAG, "could not set preview display");
                return;
            }

            SetupCallbacks();
            setCameraDisplayOrientation(getActivity(), mCameraId, mCamera);
            mCamera.startPreview();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;

        }
    }

    private void SetupCallbacks() {
        rawCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Log", "onPictureTaken - raw");
            }
        };

        /** Handles data for jpeg picture */
        shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };

        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {

                Bitmap bitmap = BitmapFactory.decodeByteArray(data,0, data.length);
                String isbn = (detectBarcode(bitmap));
                boolean result = !isbn.isEmpty();
                if(result) {
                   saveIsbn(isbn);
                   Toast.makeText(getActivity(), R.string.image_success, Toast.LENGTH_SHORT).show();
                    Activity activity = getActivity();
                    Log.d(TAG, activity.getLocalClassName());
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                } else {
                    saveIsbn("");
                    Toast.makeText(getActivity(), R.string.image_failure, Toast.LENGTH_LONG).show();
                    stopPreviewAndFreeCamera();
                    setCamera(mCamera);
                }
            }
        };
    }

    private String detectBarcode(Bitmap bitmap) {

        String isbn = "";
        if(bitmap == null) {
            Log.d(TAG, "barcode bitmap was null");
            return isbn;
        }

        BarcodeDetector detector =
                new BarcodeDetector.Builder(getActivity())
                        .setBarcodeFormats(Barcode.ISBN | Barcode.EAN_13)
                        .build();
        if(!detector.isOperational()){
            Log.e(TAG,"Could not set up the detector!");
            return isbn;
        }

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        if(barcodes.size() < 1) {
            Log.d(TAG, "NO BARCODE FOUND");
            return isbn;
        }

        for(int i=0; i < barcodes.size(); i++) {
            int key = barcodes.keyAt(i);
            Barcode code = barcodes.get(key);
            Log.d(TAG, "barcode found:");
            Log.d(TAG, "display: " +code.displayValue);
            Log.d(TAG, "raw    : " + code.rawValue);
            Log.d(TAG, "format : " + code.valueFormat);
            isbn = code.displayValue;
        }

        return isbn;
    }

    private void saveIsbn(String isbn) {
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getLocalClassName(), Context.MODE_PRIVATE);
        Log.d(TAG, "Saving isbn: " + isbn);
        prefs.edit().putString(ISBN_STRING, isbn).commit();
    }

    private boolean safeCameraOpen() {
        boolean result = false;

        try {
            stopPreviewAndFreeCamera();

            //get index of front camera
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int count = Camera.getNumberOfCameras();
            for(int i=0; i < count; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCamera = Camera.open(i);
                    mCameraId = i;
                    result = true;
                    break;
                }
            }
            result = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
            result = false;
        }

        if(!result) {
            showAlertDialog(R.string.no_camera_error, getActivity());
        }

        return result;
    }



    public void startCameraPreview() {
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "sufaceCreated");
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "sufaceChanged");
        Log.d(TAG, "surface size (WxH):" + width + " - " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "sufaceDestroyed");

    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
