package bushfireAlert.com.example.myapp.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import bushfireAlert.com.example.myapp.R;

public class GaugeView extends View {

    private Paint arcPaint;
    private Paint needlePaint;
    private Paint textPaint;
    private float progress = 0; // 0 to 100
    private RectF arcRect;

    private static final int[] COLORS = {
            Color.parseColor("#27AE60"), // LOW - Green
            Color.parseColor("#F1C40F"), // MOD - Yellow
            Color.parseColor("#E67E22"), // HIGH - Orange
            Color.parseColor("#D35400"), // EXT - Dark Orange
            Color.parseColor("#C0392B")  // CAT - Red
    };

    public GaugeView(Context context) {
        super(context);
        init();
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(40f);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);

        // Resolve text_main color for the needle and pivot
        int mainTextColor = ContextCompat.getColor(getContext(), R.color.text_main);
        int secondaryTextColor = ContextCompat.getColor(getContext(), R.color.text_secondary);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(mainTextColor);
        needlePaint.setStrokeWidth(8f);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(secondaryTextColor);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        arcRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width / 2, height) - 50;

        arcRect.set(width / 2 - radius, height - radius, width / 2 + radius, height + radius);

        // Draw background segments
        float startAngle = 180f;
        float sweepAngle = 180f / COLORS.length;

        for (int i = 0; i < COLORS.length; i++) {
            arcPaint.setColor(COLORS[i]);
            canvas.drawArc(arcRect, startAngle + (i * sweepAngle), sweepAngle, false, arcPaint);
        }

        // Draw Needle
        canvas.save();
        float needleAngle = 180f + (progress / 100f * 180f);
        canvas.rotate(needleAngle, width / 2, height);
        canvas.drawLine(width / 2, height, width / 2 + radius - 20, height, needlePaint);
        canvas.restore();

        // Draw center pivot
        needlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(width / 2, height, 15f, needlePaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.min(100, Math.max(0, progress));
        invalidate();
    }
}
