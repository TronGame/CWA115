package cwa115.trongame.Utils;

// Source code from http://stackoverflow.com/questions/541966/lazy-load-of-images-in-listview

/*
 Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class DrawableManager {
    private final static int DEFAULT_CACHE_SIZE = 15;

    public static DrawableCache cache;

    private DrawableManager(){ }

    public static void InitializeCache(int cacheSize){
        cache = new DrawableCache(cacheSize);
    }
    public static void InitializeCache(){
        InitializeCache(DEFAULT_CACHE_SIZE);
    }

    public final static class DrawableCache {
        private final Map<String, Drawable> drawableMap;
        private int cacheSize;

        public DrawableCache(int cacheSize) {
            this.cacheSize = cacheSize;
            drawableMap = new HashMap<>();
        }

        public Drawable fetchDrawable(String urlString) {
            if (drawableMap.containsKey(urlString)) {
                return drawableMap.get(urlString);
            }
            if (drawableMap.size() >= cacheSize) {
                Log.e("DRAWABLE_MANAGER", "Cache limit reached. Raise cache limit or release some drawables from the cache.");
                return null;
            }

            try {
                InputStream is = (InputStream) new URL(urlString).getContent();
                Drawable drawable = Drawable.createFromStream(is, "src");

                if (drawable != null) {
                    drawableMap.put(urlString, drawable);
                } else {
                    Log.e("DRAWABLE_MANAGER", "could not get thumbnail");
                }

                return drawable;
            } catch (IOException e) {
                Log.e("DRAWABLE_MANAGER", "fetchDrawable failed");
                return null;
            } catch (OutOfMemoryError e) {
                Log.e("DRAWABLE_MANAGER", "Too much drawables cached");
                drawableMap.get(drawableMap.keySet().iterator().next());// Remove first element from cache
                return null;
            }
        }

        public void fetchDrawableAsync(final String urlString, final ImageView imageView) {
            if (drawableMap.containsKey(urlString)) {
                imageView.setImageDrawable(drawableMap.get(urlString));
            } else {
                new AsyncTask<String, Void, Drawable>() {
                    @Override
                    protected Drawable doInBackground(String... params) {
                        return fetchDrawable(params[0]);
                    }

                    @Override
                    protected void onPostExecute(Drawable drawable) {
                        if (drawable != null)
                            imageView.setImageDrawable(drawable);
                    }
                }.execute(urlString);
            }
        }

        public void releaseDrawable(String urlString) {
            if (drawableMap.containsKey(urlString))
                drawableMap.remove(urlString);
        }
    }
}
