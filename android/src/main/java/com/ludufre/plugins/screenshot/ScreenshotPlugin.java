package com.ludufre.plugins.screenshot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Base64;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.ByteArrayOutputStream;

@CapacitorPlugin(name = "Screenshot")
public class ScreenshotPlugin extends Plugin {
    @PluginMethod
    public void take(PluginCall call) {
        getBridge().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView webView = getBridge().getWebView();
                String elementId = call.getString("id", "ion-app");
                String func = "(function() { " +
                        "var element = document.getElementById('" + elementId + "'); " +
                        "var multiply = window.devicePixelRatio;" +
                        "if (element) {" +
                        "var box = element.getBoundingClientRect();" +
                        "return box.x + ',' + box.y + ',' + box.width + ',' + box.height + ',' + multiply;" +
                        "} else {" +
                        "return '0,0,0,0,' + multiply;" +
                        "}" +
                        "})();";
                webView.evaluateJavascript(
                        func,
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                // value will be in the format "top,left,width,height,multiply"
                                String[] dimensions = value.replace("\"", "").split(",");
                                int x = (int) Double.parseDouble(dimensions[0]);
                                int y = (int) Double.parseDouble(dimensions[1]);
                                int width = (int) Double.parseDouble(dimensions[2]);
                                int height = (int) Double.parseDouble(dimensions[3]);
                                double m = Double.parseDouble(dimensions[4]);

                                if (width == 0 || height == 0) {
                                    call.reject("No area to take screenshot.");
                                } else {
                                    JSObject ret = new JSObject();
                                    ret.put("base64", takeScreenshot(x, y, width, height, m));
                                    call.resolve(ret);
                                }
                            }
                        }
                );
            }

            /**
             * Takes a screenshot of a specified area of the WebView.
             *
             * @param x        The x-coordinate of the top-left corner of the area to capture.
             * @param y        The y-coordinate of the top-left corner of the area to capture.
             * @param width    The width of the area to capture.
             * @param height   The height of the area to capture.
             * @param multiply The scaling factor to apply to the coordinates and dimensions.
             * @return A Base64 encoded string representing the captured screenshot.
             */
            String takeScreenshot(int x, int y, int width, int height, double multiply) {
                int scaledX = (int) (x * multiply);
                int scaledY = (int) (y * multiply);
                int scaledWidth = (int) (width * multiply);
                int scaledHeight = (int) (height * multiply);

                View view = getBridge().getWebView();

                // Create a bitmap of the desired area
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);

                Bitmap img = null;
                // Crop the bitmap to the specified area
                img = Bitmap.createBitmap(bitmap, scaledX, scaledY, scaledWidth, scaledHeight);

                // Recycle the original bitmap to free memory
                if (bitmap != img) {
                    bitmap.recycle();
                }

                // Compress the bitmap and encode it to Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.PNG, 70, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                // Recycle the cropped bitmap to free memory
                img.recycle();

                return Base64.encodeToString(byteArray, Base64.NO_WRAP);
            }
        });
    }
}
