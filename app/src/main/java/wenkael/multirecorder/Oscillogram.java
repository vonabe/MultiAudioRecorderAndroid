package wenkael.multirecorder;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by wenkael™ on 15.10.2015.
 */
public class Oscillogram extends Drawable {

    private Paint paint;
    private float screenWidth, screnHeight;
    private float[] draw_array;
    private ImageView view;

    public Oscillogram(ImageView context, float width, float height) {
        view = context;
        paint = new Paint();
        screenWidth = width;
        screnHeight = height;
    }

    public void setSize(float width, float height) {
        screenWidth = width;screnHeight = height;
    }

    public void mixher(byte[]mask)
    {
        draw_array = new float[(int)(screenWidth)];
        for(int i=0;i<draw_array.length;i++)
        {
            draw_array[i] = (float)mask[i];
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if(screenWidth<1||screnHeight<1)return;

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2);

        float array[] = new float[(int)screenWidth];

        int count = 0;
        for(int i = 0; i<(int)screenWidth/4;i++)
        {
            array[count] = i*4;                               // startX
            count++;
            array[count] = screnHeight/2;                     // startY
            count++;
            array[count] = i*4;                               // endX
            count++;
            array[count] = random(screnHeight,0);             // endY
            count++;
        }
        canvas.drawLines(array, paint);
    }

    private int random(float max, float min)
    {
    return (int)(Math.random()*(max-min)+min);
    }


    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}