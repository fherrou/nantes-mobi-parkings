package fr.ippon.android.opendata.android.map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.nfc.Tag;
import android.util.Log;
import fr.ippon.android.opendata.android.MainApplication;
import fr.ippon.android.opendata.android.content.SegmentColorTableDescription;
import fr.ippon.android.opendata.android.content.SegmentDao;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.ippon.android.opendata.android.content.convert.ColorId;
import fr.ippon.android.opendata.data.traffic.Segment;
import roboguice.util.RoboAsyncTask;

import javax.inject.Inject;

import static fr.ippon.android.opendata.android.content.convert.ColorId.*;

/**
 * User: nicolasguillot
 * Date: 06/05/12
 * Time: 19:29
 */
public class LoadSegmentsOverlayTask extends RoboAsyncTask<List<SegmentsOverlay>> {
    private static final String TAG = LoadSegmentsOverlayTask.class.getName();

    @Inject
    private SegmentDao dao;

    @Inject
    ContentResolver contentResolver;

    CustomMapView mapView;
    SegmentItemizedOvlerlay overlays;

    protected LoadSegmentsOverlayTask(Context context, CustomMapView mapView, SegmentItemizedOvlerlay overlays) {
        super(context, overlays.getExecutor());
        this.mapView = mapView;
        this.overlays = overlays;
    }

    @Override
    protected void onInterrupted(Exception e) {
    }

    public List<SegmentsOverlay> call() throws Exception {
        try {
            // Attente de 200 millisecondes
            // ... on ne sait jamais on pourrait se faire annuler
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
        try {
            if (MainApplication.isDefaultPrefShowTraffic()) {
                List<Segment> segments = loadSegments();
                for (Segment seg : segments) {
                    for (SegmentsOverlay ov : overlays.getOverlay()) {
                        if (ov.getSegmentId().equals(seg.getName())) {
                            ov.setColor(getColorFromColorIndice(seg.getColorId()));
                            break;
                        }
                    }
                }
                this.mapView.addAllOverlay(overlays.getOverlay());
            } else {
                this.mapView.getOverlays().removeAll(overlays.getOverlay());
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        }
        return overlays.getOverlay();
    }


    protected List<Segment> loadSegments() {
        List<Segment> segments = new ArrayList<Segment>();
        try {
        Cursor cursor = contentResolver.query(
                SegmentColorTableDescription.CONTENT_URI,
                SegmentColorTableDescription.PROJECTION_ALL, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Segment segment = dao.loadFromCursor(cursor);
                segments.add(segment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return segments;
    }
}