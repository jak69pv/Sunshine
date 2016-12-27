import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by alberto on 27/12/16.
 */

public class MyView extends View {

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public MyView(Context context, AttributeSet attrs, int DefaultStyle) {
        super(context,attrs,DefaultStyle);
    }

    // PAra medidas
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // Para dibujar la vista
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    // Para accesibilidad
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}
