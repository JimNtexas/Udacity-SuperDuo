package com.grayraven.com.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import it.jaschke.alexandria.R;

public class CaptureFragment extends android.support.v4.app.Fragment
        implements SurfaceHolder.Callback {

    public static final String CAPTURED_IMAGE_FILENAME = "captured_image";
    private static final String TAG = "CaptureFragment";

    public interface ImageCaptured {
        void imageCaptured(String filename);
    }

    ImageCaptured imageCapturedCb;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            imageCapturedCb = (ImageCaptured) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ImageCaptured");
        }
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

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        safeCameraOpen();
        if (mCamera != null) {
            // List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
            // mSupportedPreviewSizes = localSizes;

            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                Log.e(TAG, "could not set preview display");
                return;
            }

            SetupCallbacks();
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
                boolean result = false;
                File pictureFile = getOutputMediaFile();
                if (pictureFile == null) {
                    Toast.makeText(getActivity(), "Image retrieval failed.", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Log.i(TAG, "Imaged saved to:" + pictureFile.getAbsolutePath());
                    result = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, "File exception saving captured image: " + e.getLocalizedMessage());
                    result = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "File exception saving captured image: " + e.getLocalizedMessage());
                    result = false;
                }

                if(result) {
                   Toast.makeText(getActivity(), "Image retrieval success.", Toast.LENGTH_SHORT).show();
                    imageCapturedCb.imageCaptured(pictureFile.getAbsolutePath());
                } else {
                    Toast.makeText(getActivity(), "Image retrieval success.", Toast.LENGTH_LONG).show();
                }



            }
        };
    }


    private boolean safeCameraOpen() {
        boolean qOpened = false;

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
                    break;
                }
            }
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
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
        setCameraDisplayOrientation(getActivity(), mCameraId, mCamera);
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


    private File getOutputMediaFile(){

        String path = getActivity().getCacheDir().getAbsolutePath() + File.separator + CAPTURED_IMAGE_FILENAME;
        File mediaFile = new File(path);
        if(mediaFile.exists()) {
            mediaFile.delete();
            mediaFile = new File(path);
        }
        return mediaFile;
    }
}
