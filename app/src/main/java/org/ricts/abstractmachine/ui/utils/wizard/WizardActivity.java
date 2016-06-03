package org.ricts.abstractmachine.ui.utils.wizard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 30/04/2016.
 */
public abstract class WizardActivity extends AppCompatActivity {
    private static final String TAG = "WizardActivity";

    private ViewPager pager;
    private Button previousButton, nextButton;

    protected Bundle dataBundle;
    protected PagerAdapter pagerAdapter;

    protected abstract PagerAdapter createAdapter();
    protected abstract Intent nextActivityIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard);

        /** Setup main UI data **/
        dataBundle = new Bundle();

        pagerAdapter = createAdapter();
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            private int lastPosition = 0;

            @Override
            public void onPageSelected(int position) {
                if(position > lastPosition) {
                    savePageData(lastPosition); // save last page data
                    updatePage(position); // use latest saved data to update current wizard page if necessary
                }
                else if(position < lastPosition)
                    restorePageData(position);

                updateButtons(position);

                lastPosition = position;
            }
        });

        previousButton = (Button) findViewById(R.id.previousButton);
        styleButton(previousButton);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToPage(pager.getCurrentItem() - 1);
            }
        });

        nextButton = (Button) findViewById(R.id.nextButton);
        styleButton(nextButton);
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

        updateButtons(pager.getCurrentItem());
    }

    protected void styleButton(Button button){
        // default implementation does nothing
    }

    private void switchToPage(int nextPageIndex){
        pager.setCurrentItem( normaliseIndex(nextPageIndex) );
    }

    private void updateButtons(int nextCurrentItem){
        int min = 0;
        int max = pagerAdapter.getCount() - 1;

        nextCurrentItem = normaliseIndex(nextCurrentItem);
        previousButton.setEnabled(nextCurrentItem > min);

        nextButton.setText(getString(
                (nextCurrentItem < max)? R.string.wizardactivity_next_button_text :
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

    private void updatePage(int currentPageIndex){
        ((WizardFragment) pagerAdapter.instantiateItem(pager, currentPageIndex))
                .updatePage(dataBundle);
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
