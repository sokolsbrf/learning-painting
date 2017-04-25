package ru.dimasokol.learning.painting;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends Activity {

    private PaintingView mPaintingView;
    private ImageButton mRectangleButton;
    private ImageButton mBrushButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPaintingView = (PaintingView) findViewById(R.id.painting);

        mBrushButton = (ImageButton) findViewById(R.id.palette_brush);
        mBrushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaintingView.setPaintingToolType(PaintingView.PaintingTool.BRUSH);
            }
        });

        mRectangleButton = (ImageButton) findViewById(R.id.palette_rectangle);
        mRectangleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaintingView.setPaintingToolType(PaintingView.PaintingTool.RECTANGLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_clear:
                mPaintingView.clear();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
