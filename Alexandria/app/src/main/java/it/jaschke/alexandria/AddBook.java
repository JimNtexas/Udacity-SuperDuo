package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.grayraven.com.camera.CaptureFragment;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;

import static com.grayraven.com.camera.Utilities.internetAvailable;
import static com.grayraven.com.camera.Utilities.showAlertDialog;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ALEX_INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);
        final Button btnScan = (Button) rootView.findViewById(R.id.scan_button);
        final Button btnSearch = (Button) rootView.findViewById(R.id.search_button);
        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            //note: I found the original implementation of afterTextChanged to cause user confusion.
            // I now require the user to click the search button to launch the book service
            @Override
            public void afterTextChanged(Editable s) {
                String ean =s.toString();
                enableButton(btnScan, ean.length() == 0);
                enableButton(btnSearch, ean.length() > 0);
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaptureFragment fragment = CaptureFragment.newInstance();
                clearIsbnPref();
                clearFields();
                hideSoftKeyboard(getActivity());
                //show the capture screen
                android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, fragment, fragment.getClass().getSimpleName());
                ft.addToBackStack(null);
                ft.commit();
            }

        });

        rootView.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = ((EditText) rootView.findViewById(R.id.ean)).getText();
                String ean = editable.toString();
                if(!(ean.length() == 10 || ean.length() == 13)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.isbn_size_error), Toast.LENGTH_SHORT).show();
                    return;
                }
                hideSoftKeyboard(getActivity());  //NOTE:  The softkeyboard was often in the way of the user, even when it wasn't needed
                startBookSearch(ean);
            }
        });

        // Next button
        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Next button clicked");
                getLoaderManager().destroyLoader(LOADER_ID); //Bug fix:  without this call the next search will return stale data
                ean.setText("");
                clearFields(); // Bug fix: The original implementation did not clear the book details when a new search screen was loaded via the next button
                clearIsbnPref();
            }
        });

        /*Todo: I've fixed a crashing bug in the service that could happen here is the user hits the garbage can before saving a book, but I need to update the gui so that
        it better handles the case of the user discovering a book and then hitting the garbage can or cancel links at the bottom of the display.*/
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    private void startBookSearch(String isbn) {
        Log.d(TAG, "startBookSearch:");
        Log.d(TAG, isbn);

        if(!internetAvailable(getActivity())) {
            showAlertDialog(R.string.no_internet_error, getActivity());
            return;
        }

        //catch isbn10 numbers
        if(isbn.length()==10 && !isbn.startsWith("978")){
            isbn = "978" + isbn;
        }
        if(isbn.length() != 13){
            Log.e(TAG, "Incorrect length passed to startBookSearch:" + isbn.length());  //this should be impossible
            return;
        }
        //Once we have an ISBN, start a book intent
        clearFields();
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, isbn);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
        AddBook.this.restartLoader();
    }

    private void enableButton(Button button, boolean enabled) {
        if(button == null) {
            Log.d(TAG, "attempt to enable null button");
            return;
        }
        button.setClickable(enabled);
        if (enabled) {
            button.getBackground().setColorFilter(null);
        } else {
            button.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String isbn = getIsbnFromPrefs();
        if(!isbn.isEmpty()) {
            ean.setText(isbn);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    private String getIsbnFromPrefs() {
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getLocalClassName(), Context.MODE_PRIVATE);
        String isbn = prefs.getString(CaptureFragment.ISBN_STRING, "");
        return isbn;
    }

    private void clearIsbnPref() {
        Log.i(TAG, "clearIsbnPref");
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getLocalClassName(), Context.MODE_PRIVATE);
        prefs.edit().putString(CaptureFragment.ISBN_STRING, "").commit();
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if(authors == null) { //bug fix:  prevent an exception in the split statement
            authors = new String();
        }
        String[] authorsArr = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
        clearIsbnPref();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    public static void hideSoftKeyboard(Activity activity) {
        if(activity == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
