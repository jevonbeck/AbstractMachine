package org.ricts.abstractmachine.ui.utils.wizard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 30/04/2016.
 */
public abstract class WizardActivity extends AppCompatActivity {
    private static final String TAG = "WizardActivity";
    public static final String BUTTON_LOCATION_KEY = "buttonOnTop";

    private ViewPager pager;
    private Button previousButton, nextButton;

    protected Bundle dataBundle;
    protected PagerAdapter pagerAdapter;

    protected abstract PagerAdapter createAdapter();
    protected abstract Intent nextActivityIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RelativeLayout mainLayout = new RelativeLayout(this);

        pager = new ViewPager(this);
        pager.setId(R.id.WizardActivity_ViewPager);

        previousButton = new Button(this);
        styleButton(previousButton);
        previousButton.setId(R.id.WizardActivity_PreviousButton);
        previousButton.setText(getString(R.string.wizardactivity_prev_button_text));

        nextButton = new Button(this);
        styleButton(nextButton);
        nextButton.setText(getString(R.string.wizardactivity_next_button_next));

        float scaleFactor = getResources().getDisplayMetrics().density;
        RelativeLayout.LayoutParams lpPager = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, (int) (400 * scaleFactor));
        RelativeLayout.LayoutParams lpPrevButton = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lpNextButton = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //lpNextButton.addRule(RelativeLayout.RIGHT_OF, previousButton.getId());

        Bundle startOptions = getIntent().getExtras();
        //boolean buttonsOnTop = startOptions.getBoolean(BUTTON_LOCATION_KEY, false);
        boolean buttonsOnTop = false;

        if(buttonsOnTop){
            mainLayout.addView(previousButton, lpPrevButton);
            lpNextButton.addRule(RelativeLayout.RIGHT_OF, previousButton.getId());
            mainLayout.addView(nextButton, lpNextButton);

            lpPager.addRule(RelativeLayout.BELOW, previousButton.getId());
            mainLayout.addView(pager, lpPager);
        }
        else{
            mainLayout.addView(pager, lpPager);
            lpPrevButton.addRule(RelativeLayout.BELOW, pager.getId());
            lpNextButton.addRule(RelativeLayout.BELOW, pager.getId());

            mainLayout.addView(previousButton, lpPrevButton);

            lpNextButton.addRule(RelativeLayout.RIGHT_OF, previousButton.getId());
            mainLayout.addView(nextButton, lpNextButton);
        }
        setContentView(mainLayout);

        /** Setup main UI data **/
        dataBundle = new Bundle();

        pagerAdapter = createAdapter();
        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            private int lastPosition = 0;

            @Override
            public void onPageSelected(int position) {
                if(position > lastPosition)
                    savePageData(lastPosition);
                else if(position < lastPosition)
                    restorePageData(position);

                updateButtons(position);

                lastPosition = position;
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToPage(pager.getCurrentItem() - 1);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPageIndex = pager.getCurrentItem();

                // determine whether to go to next activity or move to next page
                int nextPageIndex = currentPageIndex + 1;
                if(nextPageIndex == pagerAdapter.getCount()){ // 'Finish' button selected
                    savePageData(currentPageIndex); // save current page data before moving to next activity

                    Intent intent = nextActivityIntent();
                    intent.putExtras(dataBundle);
                    startActivity(intent);
                    finish();
                }
                else{ // 'Next' button selected
                    switchToPage(nextPageIndex);
                }
            }
        });
    }

    protected void styleButton(Button button){
        // default implementation does nothing
    }

    private void switchToPage(int nextPageIndex){
        int nextIndex = normaliseIndex(nextPageIndex);
        pager.setCurrentItem(nextIndex);
        //updateButtons(nextIndex);
    }

    private void updateButtons(int nextCurrentItem){
        int min = 0;
        int max = pagerAdapter.getCount() - 1;

        nextCurrentItem = normaliseIndex(nextCurrentItem);
        previousButton.setEnabled(nextCurrentItem > min);

        nextButton.setText(getString(
                (nextCurrentItem < max)? R.string.wizardactivity_next_button_next :
                    R.string.wizardactivity_next_button_finish));
    }

    private void restorePageData(int currentPageIndex){
        ((WizardFragment) pagerAdapter.instantiateItem(pager, currentPageIndex))
                .restorePageData(dataBundle);
    }

    private void savePageData(int currentPageIndex){
        ((WizardFragment) pagerAdapter.instantiateItem(pager, currentPageIndex))
                .savePageData(dataBundle);
    }

    private int normaliseIndex(int index){
        int min = 0;
        int max = pagerAdapter.getCount() - 1;

        if(index < min){
            index = min;
        }
        else if(index > max){
            index = max;
        }

        return index;
    }
}
