package com.example.rainsafe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;

/**
 * SunBrightnessGraphView
 *
 * Custom view that draws a premium smooth line graph representing
 * the sun's brightness index (lux) throughout the day (06:00 - 18:00).
 *
 * Features:
 *   - Smooth cubic curve (Bézier interpolation).
 *   - Beautiful gradient area fill under the curve.
 *   - Responsive sizing using DP/SP conversion.
 *   - Real-time indicator dot matching the current time and light sensor value.
 */
public class SunBrightnessGraphView extends View {

    // Paints
    private Paint gridPaint;
    private Paint textPaint;
    private Paint linePaint;
    private Paint fillPaint;
    private Paint pointPaint;
    private Paint glowPaint;
    private Paint badgeBgPaint;
    private Paint badgeTextPaint;

    // Constants & Colors
    private static final int COLOR_LINE = Color.parseColor("#2196F3"); // Premium Blue
    private static final int COLOR_LINE_END = Color.parseColor("#00BCD4"); // Cyan
    private static final int COLOR_GRID = Color.parseColor("#ECEFF1"); // Light Grey
    private static final int COLOR_TEXT = Color.parseColor("#9E9E9E"); // Muted Grey
    
    // Y-Axis limits
    private static final float MAX_LUX = 1200f; // Max scale for sun brightness
    private static final float MIN_LUX = 0f;

    // X-Axis labels
    private static final String[] X_LABELS = {"06:00", "09:00", "12:00", "15:00", "18:00"};
    private static final float[] X_HOURS = {6f, 9f, 12f, 15f, 18f};

    // Graph Data
    // Hourly baseline sunlight values (indices correspond to 06:00, 09:00, 12:00, 15:00, 18:00)
    private int[] hourlyValues = {100, 500, 950, 600, 120};
    
    // Realtime values
    private int currentLux = 0;
    private float currentHour = 12f; // Current decimal hour (e.g. 12.5 for 12:30)

    public SunBrightnessGraphView(Context context) {
        super(context);
        init();
    }

    public SunBrightnessGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SunBrightnessGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Grid Paint
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(COLOR_GRID);
        gridPaint.setStrokeWidth(dpToPx(1f));
        gridPaint.setStyle(Paint.Style.STROKE);

        // Text Paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(COLOR_TEXT);
        textPaint.setTextSize(spToPx(10f));
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Line Paint
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dpToPx(3f));
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        // Fill Paint
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        // Active Point Paint
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(COLOR_LINE);
        pointPaint.setStyle(Paint.Style.FILL);

        // Glow Paint
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(Color.parseColor("#802196F3")); // Translucent Blue
        glowPaint.setStyle(Paint.Style.FILL);

        // Badge Background Paint
        badgeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgeBgPaint.setColor(Color.parseColor("#E3F2FD"));
        badgeBgPaint.setStyle(Paint.Style.FILL);

        // Badge Text Paint
        badgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgeTextPaint.setColor(Color.parseColor("#1565C0"));
        badgeTextPaint.setTextSize(spToPx(9f));
        badgeTextPaint.setTextAlign(Paint.Align.CENTER);
        badgeTextPaint.setFakeBoldText(true);
    }

    /**
     * Set the entire hourly data array along with the real-time reading.
     * 
     * @param values Array of 5 integers representing light intensity at 06:00, 09:00, 12:00, 15:00, 18:00
     * @param currentLux Current real-time light sensor value (lux)
     */
    public void setGraphData(int[] values, int currentLux) {
        if (values != null && values.length == 5) {
            this.hourlyValues = values.clone();
        }
        this.currentLux = currentLux;
        
        // Compute current decimal hour
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        this.currentHour = hour + (minute / 60f);

        // Clamp decimal hour for the graph display range (06:00 to 18:00)
        if (this.currentHour < 6f) this.currentHour = 6f;
        if (this.currentHour > 18f) this.currentHour = 18f;

        // Dynamically update the baseline slot closest to the current hour
        updateClosestBaselineSlot(currentLux);

        invalidate(); // Request redraw
    }

    private void updateClosestBaselineSlot(int lux) {
        float minDiff = Float.MAX_VALUE;
        int closestIndex = 2; // Default to 12:00

        for (int i = 0; i < X_HOURS.length; i++) {
            float diff = Math.abs(currentHour - X_HOURS[i]);
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }
        hourlyValues[closestIndex] = lux;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Padding/Margins
        float paddingLeft = dpToPx(55f); // Space for Y-Axis labels
        float paddingRight = dpToPx(20f);
        float paddingTop = dpToPx(25f); // Space for current value badge
        float paddingBottom = dpToPx(20f); // Space for X-Axis labels

        float graphWidth = width - paddingLeft - paddingRight;
        float graphHeight = height - paddingTop - paddingBottom;

        if (graphWidth <= 0 || graphHeight <= 0) return;

        // ─── DRAW GRID & Y-LABELS ────────────────────────────────────────────
        // 3 grid lines (Top: Terang, Middle: Sedang, Bottom: Gelap)
        float[] yTicks = {0f, 0.5f, 1f};
        String[] yLabels = {"Gelap (0 lx)", "Sedang (500 lx)", "Terang (1000 lx)"};
        
        for (int i = 0; i < yTicks.length; i++) {
            float y = paddingTop + graphHeight * (1f - yTicks[i]);
            // Draw grid line
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
            
            // Draw Y label
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(yLabels[i], paddingLeft - dpToPx(8f), y + dpToPx(3.5f), textPaint);
        }

        // ─── DRAW X-LABELS ───────────────────────────────────────────────────
        float xStep = graphWidth / (X_LABELS.length - 1);
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < X_LABELS.length; i++) {
            float x = paddingLeft + i * xStep;
            canvas.drawText(X_LABELS[i], x, height - dpToPx(4f), textPaint);
        }

        // ─── CALCULATE COORDINATES ───────────────────────────────────────────
        float[] xCoords = new float[X_HOURS.length];
        float[] yCoords = new float[X_HOURS.length];

        for (int i = 0; i < X_HOURS.length; i++) {
            xCoords[i] = paddingLeft + i * xStep;
            
            // Normalize value to y-axis range
            float val = hourlyValues[i];
            if (val < MIN_LUX) val = MIN_LUX;
            if (val > MAX_LUX) val = MAX_LUX;
            
            float ratio = (val - MIN_LUX) / (MAX_LUX - MIN_LUX);
            yCoords[i] = paddingTop + graphHeight * (1f - ratio);
        }

        // ─── DRAW CURVE & SHADING ────────────────────────────────────────────
        Path linePath = new Path();
        Path fillPath = new Path();

        linePath.moveTo(xCoords[0], yCoords[0]);
        fillPath.moveTo(xCoords[0], paddingTop + graphHeight); // Bottom left corner of fill
        fillPath.lineTo(xCoords[0], yCoords[0]);

        // Draw smooth cubic spline
        for (int i = 0; i < X_HOURS.length - 1; i++) {
            float x1 = xCoords[i];
            float y1 = yCoords[i];
            float x2 = xCoords[i + 1];
            float y2 = yCoords[i + 1];

            // Control points: half-way horizontally, flat slope
            float cx1 = x1 + (x2 - x1) / 2f;
            float cy1 = y1;
            float cx2 = x1 + (x2 - x1) / 2f;
            float cy2 = y2;

            linePath.cubicTo(cx1, cy1, cx2, cy2, x2, y2);
            fillPath.cubicTo(cx1, cy1, cx2, cy2, x2, y2);
        }

        fillPath.lineTo(xCoords[X_HOURS.length - 1], paddingTop + graphHeight); // Bottom right of fill
        fillPath.close();

        // 1. Draw the gradient fill
        Shader fillShader = new LinearGradient(
                0, paddingTop,
                0, paddingTop + graphHeight,
                Color.parseColor("#352196F3"), // Translucent blue
                Color.parseColor("#002196F3"), // Completely transparent
                Shader.TileMode.CLAMP
        );
        fillPaint.setShader(fillShader);
        canvas.drawPath(fillPath, fillPaint);

        // 2. Draw the curve outline
        Shader lineShader = new LinearGradient(
                paddingLeft, 0,
                width - paddingRight, 0,
                COLOR_LINE,
                COLOR_LINE_END,
                Shader.TileMode.CLAMP
        );
        linePaint.setShader(lineShader);
        canvas.drawPath(linePath, linePaint);

        // ─── DRAW ACTIVE POINT MARKER ────────────────────────────────────────
        // Map the current Hour to X coordinate
        float relativeHour = currentHour - 6f; // Hours past 06:00
        if (relativeHour < 0f) relativeHour = 0f;
        float totalGraphHours = 12f; // From 06:00 to 18:00 is 12 hours
        float activeX = paddingLeft + (relativeHour / totalGraphHours) * graphWidth;

        // Map current lux reading to Y coordinate
        float activeLux = currentLux;
        if (activeLux < MIN_LUX) activeLux = MIN_LUX;
        if (activeLux > MAX_LUX) activeLux = MAX_LUX;
        float activeRatio = (activeLux - MIN_LUX) / (MAX_LUX - MIN_LUX);
        float activeY = paddingTop + graphHeight * (1f - activeRatio);

        // Draw glow and dot
        canvas.drawCircle(activeX, activeY, dpToPx(10f), glowPaint); // Outer glow
        pointPaint.setColor(Color.WHITE);
        canvas.drawCircle(activeX, activeY, dpToPx(6f), pointPaint); // White border
        pointPaint.setColor(COLOR_LINE);
        canvas.drawCircle(activeX, activeY, dpToPx(4f), pointPaint); // Blue center

        // ─── DRAW CURRENT VALUE BADGE ────────────────────────────────────────
        String badgeText = currentLux + " lx";
        float badgeWidth = badgeTextPaint.measureText(badgeText) + dpToPx(12f);
        float badgeHeight = dpToPx(16f);
        
        float badgeX = activeX;
        float badgeY = activeY - dpToPx(14f);

        // Keep badge bounds inside graph width
        if (badgeX - badgeWidth / 2f < paddingLeft) {
            badgeX = paddingLeft + badgeWidth / 2f;
        } else if (badgeX + badgeWidth / 2f > width - paddingRight) {
            badgeX = width - paddingRight - badgeWidth / 2f;
        }

        // Draw rounded rectangle badge
        float rx = dpToPx(6f);
        canvas.drawRoundRect(
                badgeX - badgeWidth / 2f, badgeY - badgeHeight / 2f,
                badgeX + badgeWidth / 2f, badgeY + badgeHeight / 2f,
                rx, rx, badgeBgPaint
        );

        // Draw badge text
        canvas.drawText(badgeText, badgeX, badgeY + dpToPx(3.5f), badgeTextPaint);
    }

    // Helpers to convert DP / SP to Pixel
    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                getResources().getDisplayMetrics()
        );
    }
}
